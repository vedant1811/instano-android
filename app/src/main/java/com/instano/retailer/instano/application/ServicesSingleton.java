package com.instano.retailer.instano.application;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.model.LatLng;
import com.instano.retailer.instano.BuildConfig;
import com.instano.retailer.instano.R;
import com.instano.retailer.instano.buyerDashboard.QuotationListActivity;
import com.instano.retailer.instano.utilities.GetAddressTask;
import com.instano.retailer.instano.utilities.PeriodicWorker;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.models.Buyer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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

    public static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;

    private static ServicesSingleton sInstance;

    private final MyApplication mApplication;
    private SharedPreferences mSharedPreferences;
    private boolean mFirstTime;

    /* location variables */
    private LocationClient mLocationClient;
    private Location mLastLocation;
    private Location mUserSelectedLocation;
    private Address mUserAddress;
    private String locationErrorString;

    private InitialDataCallbacks mInitialDataCallbacks;
    private AddressCallbacks mAddressCallbacks;

    private Buyer mBuyer;

    private PeriodicWorker mPeriodicWorker;

    @Nullable
    public Buyer getBuyer() {
        return mBuyer;
    }

    /**
     * tries to sign in if login details are saved
     * @return true if login details are saved
     */
    public boolean signIn(@NonNull SignInCallbacks callbacks) {
        String apiKey = mSharedPreferences.getString(KEY_BUYER_API_KEY, null);

        if (apiKey != null) {
            NetworkRequestsManager.instance().signInRequest(apiKey, callbacks);
            return true;
        }
        else
            return false;
    }

    public boolean firstTime() {
        if (BuildConfig.DEBUG)
            return true;
        else
            return mFirstTime;
    }

    /**
     * called after a signIn request
     * @param buyer null if no buyer with given api key
     * @param apiKey null if no buyer with given api key
     */
    /*package*/ void afterSignIn(@Nullable Buyer buyer, @Nullable String apiKey) {
        mBuyer = buyer;
        Log.d(TAG, "buyer ID: " + mBuyer);

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(KEY_BUYER_API_KEY, apiKey); // clear saved API key if null
        editor.putBoolean(KEY_FIRST_TIME, false); // update first time on first login
        editor.apply();

        if (buyer != null) {
            Toast.makeText(mApplication, String.format("you are %d user to sign in", mBuyer.id), Toast.LENGTH_SHORT).show();

            Tracker appTracker = mApplication.getTracker(MyApplication.TrackerName.APP_TRACKER);
            appTracker.setClientId(String.valueOf(mBuyer.id));
            appTracker.send(new HitBuilders.AppViewBuilder().build());

            DataManager.instance().onNewBuyer();
            mPeriodicWorker.start();
        }
        // TODO: else
    }

    public void runPeriodicTasks() {
        if (mBuyer != null) // i.e. if user is signed in
        {
            NetworkRequestsManager.instance().getQuotesRequest(mBuyer); // also fetches quotations once quotes are fetched
            NetworkRequestsManager.instance().getSellersRequest();
        }
        if (DataManager.instance().getProductCategories() == null)
            NetworkRequestsManager.instance().getProductCategoriesRequest();

    }

    public void createNotification() {
        Log.d(TAG, "new quotations received");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mApplication)
                .setSmallIcon(R.drawable.instano_launcher)
                .setContentTitle("New Quotations")
                .setContentText("Click to view your new quotations");

        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                mApplication,
                0,
                new Intent(mApplication, QuotationListActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        builder.setContentIntent(resultPendingIntent);

        NotificationManager manager = (NotificationManager) mApplication.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(001, builder.build());

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
        mSharedPreferences = mApplication.getSharedPreferences(
                "com.instano.SHARED_PREFERENCES_FILE", Context.MODE_PRIVATE);
        mFirstTime = mSharedPreferences.getBoolean(KEY_FIRST_TIME, false);
        mUserAddress = null;
        mLastLocation = null;
        mBuyer = null;

        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        mLocationClient = new LocationClient(mApplication, this, this);
        mLocationClient.connect();
        checkPlayServices(); // not performing checkUserAccount
        // see http://www.androiddesignpatterns.com/2013/01/google-play-services-setup.html

        mPeriodicWorker = new PeriodicWorker(this);
//        signIn();
    }

    public boolean checkPlayServices() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mApplication);
        if (status != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(status) && mInitialDataCallbacks != null) {
                mInitialDataCallbacks.showErrorDialog(status);
            } else {
                locationErrorString = "This device is not supported.";
            }
            return false;
        }
        return true;
    }

    public String getLocationErrorString() {
        return locationErrorString;
    }

    public Address getUserAddress() {
        return mUserAddress;
    }

    @Nullable
    public Location getUserLocation() {
        if (mUserSelectedLocation == null)
            return mLastLocation;
        else
            return mUserSelectedLocation;
    }

    public void userSelectsLocation(LatLng location, Address address) {
        mUserSelectedLocation = new Location("Services Singleton Generated");
        mUserSelectedLocation.setLatitude(location.latitude);
        mUserSelectedLocation.setLongitude(location.longitude);

        mUserAddress = address;

        if (mAddressCallbacks != null)
            mAddressCallbacks.addressUpdated(address, true);
    }

    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        mLastLocation = mLocationClient.getLastLocation();
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
                        mUserAddress = address;
                        if (mAddressCallbacks != null)
                            mAddressCallbacks.addressUpdated(address, false);
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
                e.printStackTrace();
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

    public interface SignInCallbacks {
        public void signedIn(boolean success);
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
        public void addressUpdated(@Nullable Address address, boolean userSelected);
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
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            date = simpleDateFormat.parse(sDate);
        } catch (ParseException e) {
            return 0;
        }
        return date.getTime();
    }

}
