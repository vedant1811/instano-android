package com.instano.retailer.instano.application;

import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.util.TypedValue;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.model.LatLng;
import com.instano.retailer.instano.application.network.NetworkRequestsManager;
import com.instano.retailer.instano.utilities.GetAddressTask;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.models.Buyer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import rx.Observable;

/**
 *
 * Created by vedant on 3/9/14.
 */
public class ServicesSingleton implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private final static String TAG = "ServicesSingleton";

    private final static String KEY_BUYER_API_KEY = "com.instano.retailer.instano.application.ServicesSingleton.buyer_api_key";
    private final static String KEY_FIRST_TIME = "com.instano.retailer.instano.application.ServicesSingleton.first_time";
    private final static String KEY_WHATSAPP_ID = "com.instano.retailer.instano.application.ServicesSingleton.whatsapp_id";

    public static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;

    private static ServicesSingleton sInstance;

    private final MyApplication mApplication;
    private SharedPreferences mSharedPreferences;
    private boolean mFirstTime;
    private ObjectMapper mDefaultObjectMapper;

    /* location variables */
    private LocationClient mLocationClient;
    private Location mLastLocation;
    private Location mUserSelectedLocation;
    private String mUserAddress;
    private String locationErrorString;

    private InitialDataCallbacks mInitialDataCallbacks;
    private AddressCallbacks mAddressCallbacks;

    private Buyer mBuyer;

    @Nullable
    public Buyer getBuyer() {
        return mBuyer;
    }

    /**
     * tries to sign in if login details are saved
     * @return true if login details are saved
     */
    public Observable<Buyer> signIn() {
        String apiKey = mSharedPreferences.getString(KEY_BUYER_API_KEY, null);

        Log.v(TAG, "api key: " + String.valueOf(apiKey));

        if (apiKey != null) {
            Observable<Buyer> buyerObservable = NetworkRequestsManager.instance().signIn(apiKey);
            buyerObservable.subscribe(
                    this::saveBuyer,
                    throwable -> removeFirstTime());
            return buyerObservable;
        }
        else
            return null; // TODO: improve
    }

    private void removeFirstTime() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(KEY_FIRST_TIME, false); // update first time on first login
        editor.apply();
    }

    private void saveBuyer(Buyer buyer) {
        mBuyer = buyer;
        Log.d(TAG, "buyer ID: " + mBuyer);

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(KEY_FIRST_TIME, false); // update first time on first login
        Log.d(TAG, "saving buyer api key: " + buyer.getApi_key());
        Tracker appTracker = mApplication.getTracker(MyApplication.TrackerName.APP_TRACKER);
        appTracker.setClientId(String.valueOf(buyer.getId()));
        appTracker.send(new HitBuilders.AppViewBuilder().build());
        editor.putString(KEY_BUYER_API_KEY, buyer.getApi_key());
        editor.putBoolean(KEY_FIRST_TIME, false); // update first time on first login
        editor.apply();
    }

    public Observable<Buyer> register(@NonNull Buyer buyer) {
        Observable<Buyer> buyerObservable = NetworkRequestsManager.instance().registerBuyer(buyer);
        buyerObservable.subscribe(
                this::saveBuyer,
                throwable -> removeFirstTime());
        return buyerObservable;
    }

    public boolean isFirstTime() {
//        if (BuildConfig.DEBUG)
//            return true;
//        else
            return mFirstTime;
    }

    /*package*/ static void init(MyApplication application) {
        sInstance = new ServicesSingleton(application);
    }

    public static ServicesSingleton instance() {
        if (sInstance == null)
            throw new IllegalStateException("ServicesSingleton.Init() never called");
        return sInstance;
    }

    private ServicesSingleton(MyApplication application) {
        mApplication = application;
        mSharedPreferences = mApplication.getSharedPreferences();
        mFirstTime = mSharedPreferences.getBoolean(KEY_FIRST_TIME, true);
        mUserAddress = null;
        mLastLocation = null;
        mBuyer = null;

        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        mLocationClient = new LocationClient(mApplication, this, this);
        mLocationClient.connect();
        // see http://www.androiddesignpatterns.com/2013/01/google-play-services-setup.html
    }

    public String getLocationErrorString() {
        return locationErrorString;
    }

    public String getUserAddress() {
        return mUserAddress;
    }

    @Nullable
    public Location getUserLocation() {
        if (mUserSelectedLocation == null)
            return mLastLocation;
        else
            return mUserSelectedLocation;
    }

    public void userSelectsLocation(@NonNull LatLng location, @Nullable String address) {
        Log.d(Log.ADDRESS_UPDATED, String.format("userSelectsLocation.address: %s, location: %s", address, location));
        mUserSelectedLocation = new Location("Services Singleton Generated");
        mUserSelectedLocation.setLatitude(location.latitude);
        mUserSelectedLocation.setLongitude(location.longitude);

        mUserAddress = address;

        if (mAddressCallbacks != null)
            mAddressCallbacks.addressUpdated(mUserAddress, true);
    }

    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        mLastLocation = mLocationClient.getLastLocation();
        Log.d(Log.ADDRESS_UPDATED, String.format("onConnected.address: %s, location: %s", null, mLastLocation));
        if (mAddressCallbacks != null)
            mAddressCallbacks.addressUpdated(null, false);
        if (mLastLocation != null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Geocoder.isPresent()) {
                // Show the activity indicator
                if (mAddressCallbacks != null)
                    mAddressCallbacks.searchingForAddress();
                /*
                 * Reverse geocoding is long-running and synchronous.
                 * Run it on a background thread.
                 * Pass the current location to the background task.
                 * When the task finishes,
                 * callback displays the address.
                 */
                (new GetAddressTask(mApplication, new GetAddressTask.AddressCallback() {
                    @Override
                    public void addressFetched(@Nullable Address address) {
                        Log.d("Address", address != null ? address.toString() : "not found");
                        if (mUserAddress != null)
                            return; // user has already set a location. No need for this address.
                        mUserAddress = readableAddress(address);
                        if (mAddressCallbacks != null)
                            mAddressCallbacks.addressUpdated(mUserAddress, false);
                    }
                })).execute(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            }
    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        // TODO: do something
    }

    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mInitialDataCallbacks == null)
            return;
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                mInitialDataCallbacks.resolvableConnectionResultError(connectionResult);
                /*
                 * Thrown if Google Play services canceled the original PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                Log.fatalError(e);
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the user with the error.
             */
            mInitialDataCallbacks.showErrorDialog(connectionResult.getErrorCode());
        }
    }

    public void registerCallback (InitialDataCallbacks initialDataCallbacks) {
        mInitialDataCallbacks = initialDataCallbacks;
    }

    public void registerCallback (AddressCallbacks addressCallbacks) {
        mAddressCallbacks = addressCallbacks;
    }

    public String getInstanoWhatsappId() {
        // TODO: set based on fetched data online
        return mSharedPreferences.getString(KEY_WHATSAPP_ID, "919916782444");
    }

    public interface InitialDataCallbacks {
        /*
         * Implementation activities need to listen for result of the errorDialog in Activity.onActivityResult
         */
        public void showErrorDialog(int errorCode);
        public void resolvableConnectionResultError(ConnectionResult connectionResult) throws IntentSender.SendIntentException;
    }

    public interface AddressCallbacks {
        public void searchingForAddress();

        /**
         *
         * @param address if null, location has been updated and we have lat long
         * @param userSelected
         */
        public void addressUpdated(@Nullable String address, boolean userSelected);
    }

    // TODO: fix bug of showing a future time
    /**
     *
     * @return Human readable time elapsed. Eg: "42 minutes ago"
     */
    public String getPrettyTimeElapsed(long updatedAt) {
        String dateTimeString = (String) DateUtils.getRelativeDateTimeString(mApplication, updatedAt,
                DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);
        return dateTimeString.split(",")[0];
    }

    public static long dateFromString(String sDate) {
        Date date = null;
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            date = simpleDateFormat.parse(sDate);
        } catch (ParseException e) {
            Log.fatalError(e);
            return 0;
        }
        return date.getTime();
    }

    @Nullable
    public final static String readableAddress(@Nullable Address address) {
        String text;
        if (address == null)
            text = null;
        else
            text = address.getMaxAddressLineIndex() > 0 ?
                address.getAddressLine(0) : address.getLocality();

        return text;
    }

    public int dpToPixels(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, mApplication.getResources().getDisplayMetrics());
    }

    public ObjectMapper getDefaultObjectMapper() {
        if (mDefaultObjectMapper == null) {
            mDefaultObjectMapper = new ObjectMapper();
            mDefaultObjectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
            mDefaultObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
        return mDefaultObjectMapper;
    }
}
