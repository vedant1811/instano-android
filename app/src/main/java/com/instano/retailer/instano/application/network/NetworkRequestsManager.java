package com.instano.retailer.instano.application.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.instano.retailer.instano.BuildConfig;
import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.MyApplication;
import com.instano.retailer.instano.application.ServicesSingleton;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.models.Buyer;
import com.instano.retailer.instano.utilities.models.Deal;
import com.instano.retailer.instano.utilities.models.Device;
import com.instano.retailer.instano.utilities.models.ProductCategories;
import com.instano.retailer.instano.utilities.models.Quotation;
import com.instano.retailer.instano.utilities.models.Quote;
import com.instano.retailer.instano.utilities.models.SignIn;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import retrofit.RestAdapter;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import rx.Observable;

/**
 * Created by vedant on 18/12/14.
 */
public class NetworkRequestsManager {

    private static final String TAG = "NetworkRequestsManager";
    private static final String API_VERSION = "v1/";

    private final static String API_ERROR_ALREADY_TAKEN = "has already been taken";
    private final static String API_ERROR_IS_BLANK = "can't be blank";
    public static final String SESSION_ID = "session_id";

    private static final String PROPERTY_GCM_ID = "GCM_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String SENDER_ID = "187047464172";

    private static NetworkRequestsManager sInstance;

    private final MyApplication mApplication;
    private final RegisteredBuyersApiService mRegisteredBuyersApiService;
    private final UnregisteredBuyersApiService mUnregisteredBuyersApiService;

    /**
     * stores the different observables based on class name.
     * Do NOT access this directly, use the accessor {@link #getObservable(Class)}
     */
    private final HashMap<String, Observable> mObservableHashMap;

    public Observable<Buyer> signIn(String apiKey) {
        SignIn signIn = new SignIn();
        signIn.setApi_key(apiKey);
        Observable<Buyer> buyerObservable = mRegisteredBuyersApiService.signIn(signIn);
        buyerObservable.subscribe(this::newBuyer);
        return buyerObservable;
    }

    public Observable<Quote> sendQuote(Quote quote) {
        return mRegisteredBuyersApiService.sendQuote(quote);
    }

    /**
     * always adds a header ("Session-Id", getSessionId())
     */
    public interface RegisteredBuyersApiService {
        @POST("/buyers")
        Observable<Buyer> register(@Body Buyer buyer);

        @POST("/buyers/sign_in")
        Observable<Buyer> signIn(@Body SignIn signIn);

        @POST("/buyers/exists")
        Observable<Boolean> exists(@Body String phone);

        @POST("/buyers/quotes")
        Observable<Quote> sendQuote(@Body Quote quote);

        @GET("/buyers/quotes")
        Observable<List<Quote>> getQuotes();

        @GET("/buyers/quotations")
        Observable<List<Quotation>> getQuotations();

        @GET("/buyers/deals")
        Observable<List<Deal>> getDeals();

        @GET("/product_categories")
        Observable<ProductCategories> getProductCategories();
    }

    public interface UnregisteredBuyersApiService {
        @POST("/devices")
        Observable<Device> registerDevice(@Body Device device);
    }

    public Observable<Buyer> registerBuyer(Buyer buyer) {
        Observable<Buyer> buyerObservable = mergeObservable(Buyer.class,
                        mRegisteredBuyersApiService.register(buyer)
                );
        buyerObservable.subscribe(this::newBuyer);
        return buyerObservable;
    }

    private NetworkRequestsManager(MyApplication application) {
        this.mApplication = application;

        RestAdapter.LogLevel logLevel = BuildConfig.DEBUG ? RestAdapter.LogLevel.HEADERS_AND_ARGS : RestAdapter.LogLevel.NONE;

        String endpoint = application.getResources().getString(R.string.server_url) + API_VERSION; // append api version
        RestAdapter registeredRestAdapter = new RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setRequestInterceptor(request -> request.addHeader("Session-Id", getSessionId()))
                .setErrorHandler(new ResponseErrorHandler())
                .setLogLevel(logLevel)
                .build();

        mRegisteredBuyersApiService = registeredRestAdapter.create(RegisteredBuyersApiService.class);

        RestAdapter unregisteredRestAdapter = new RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setErrorHandler(new ResponseErrorHandler())
                .setLogLevel(logLevel)
                .build();

        mUnregisteredBuyersApiService = unregisteredRestAdapter.create(UnregisteredBuyersApiService.class);

//        mJsonObjectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
//        mJsonObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        mObservableHashMap = new HashMap<>(); // empty hashmap
    }

    /*package*/
    public static void init(MyApplication application) {
        sInstance = new NetworkRequestsManager(application);
    }

    public static NetworkRequestsManager instance() {
        if (sInstance == null)
            throw new IllegalStateException("NetworkRequestsManager.init() not called");

        return sInstance;
    }

    public Observable<Device> authorizeSession(boolean refreshGcmId) {
        String gcmId = getGcmId();
        if (refreshGcmId || gcmId.isEmpty()) {
            fetchGcmRegIdAsync();
        }
        else
            registerNewSession(gcmId);

        return getObservable(Device.class);
    }

    public <T> Observable<T> getObservable(@NonNull Class<T> modelClass) {
        Observable<T> observable = mObservableHashMap.get(modelClass.getSimpleName());
        if (observable == null) {
            observable = Observable.never(); // a placeholder observable so that null isn't returned
            mObservableHashMap.put(modelClass.getSimpleName(), observable);
        }
        return observable;
    }

    // DEPRECATED:
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) mApplication.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    private <T> Observable<T> mergeObservable(@NonNull Class<T> modelClass, Observable<T> newObservable) {
        Observable<T> observable = getObservable(modelClass).mergeWith(newObservable);// merge with existing observable
        mObservableHashMap.put(modelClass.getSimpleName(),
                observable); // replace the old observable
        return observable;
    }

    private void newBuyer(@NonNull Buyer buyer) {
        Log.d(TAG, "onNewBuyer");
        mObservableHashMap.put(Deal.class.getSimpleName(),
                mRegisteredBuyersApiService.getDeals()
                .flatMap(Observable::from)); // flatten returned List<Deal> into Deal

        mObservableHashMap.put(Quote.class.getSimpleName(),
                mRegisteredBuyersApiService.getQuotes().flatMap(Observable::from));

        mObservableHashMap.put(ProductCategories.class.getSimpleName(),
                mRegisteredBuyersApiService.getProductCategories());
    }

//    private Observable<T> exponentialBackoff(Func1<? super Observable<? extends Throwable>,? extends Observable<?>> attempts) {
//
//    }

    private void storeSessionId(String sessionId) {
        Log.v(TAG, "saving Session id: "+sessionId);
        getAppSharedPreferences().edit().putString(SESSION_ID, sessionId).commit();
    }

    private String getSessionId(){
        String sessionId = getAppSharedPreferences().getString(SESSION_ID, "");
        Log.v(TAG, "Saved Session id: "+sessionId);
        return sessionId;
    }

    // TODO: use rxJava instead
    private void fetchGcmRegIdAsync() {
        new AsyncTask<Void,Void,String>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(Void... params) {
                String gcmRegId = "";
                try {
                    gcmRegId = GoogleCloudMessaging.getInstance(mApplication).register(SENDER_ID);
                    // Persist the registration ID - no need to register again.
                    storeGcmId(gcmRegId);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                return gcmRegId;
            }

            @Override
            protected void onPostExecute(String gcmId) {
                registerNewSession(gcmId);
            }
        }.execute();
    }

    private void registerNewSession(String gcmId) {
        if (!gcmId.isEmpty()) {
            Log.v(TAG, ".registerNewSession Send regId  " + gcmId);
            Device device = new Device();
            device.setGcm_registration_id(gcmId);

            mergeObservable(Device.class, mUnregisteredBuyersApiService.registerDevice(device))
                    .subscribe(
                            d -> storeSessionId(d.getSession_id()),
                            throwable -> {
                                if (throwable instanceof ResponseError) {
                                    ResponseError responseError = (ResponseError) throwable;
                                    if (responseError.getErrorType().shouldRefreshGcmId()) {
                                        authorizeSession(true);
                                        return;
                                    }
                                }
                                // else TODO: do an exponential backoff
                            });
            // in case we are waiting for a pending result, new subscribers will get them as well.
        }
        else
            authorizeSession(true);
    }

    private String getGcmId() {
        final SharedPreferences prefs = getAppSharedPreferences();
        String gcmId = prefs.getString(PROPERTY_GCM_ID, "");
        if (gcmId.isEmpty()) {
            Log.v(TAG, "GCM ID not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion();
        if (registeredVersion != currentVersion) {
            Log.v(TAG, "App version changed.");
            return "";
        }
        return gcmId;
    }

    private void storeGcmId(String gcmId) {
        final SharedPreferences prefs = getAppSharedPreferences();
        int appVersion = getAppVersion();
        Log.v(TAG, "Saving gcmId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_GCM_ID, gcmId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.apply();
    }
    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private int getAppVersion() {
        try {
            PackageInfo packageInfo = mApplication.getPackageManager()
                    .getPackageInfo(mApplication.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.fatalError(e);
            return -1;
        }
    }

    private SharedPreferences getAppSharedPreferences() {
        return mApplication.getSharedPreferences(ServicesSingleton.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    }
}
