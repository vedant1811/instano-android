package com.instano.retailer.instano.application.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.instano.retailer.instano.BuildConfig;
import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.MyApplication;
import com.instano.retailer.instano.application.ServicesSingleton;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.model.Buyer;
import com.instano.retailer.instano.utilities.model.Category;
import com.instano.retailer.instano.utilities.model.Deal;
import com.instano.retailer.instano.utilities.model.Device;
import com.instano.retailer.instano.utilities.model.Product;
import com.instano.retailer.instano.utilities.model.Quotation;
import com.instano.retailer.instano.utilities.model.Quote;
import com.instano.retailer.instano.utilities.model.Seller;
import com.instano.retailer.instano.utilities.model.SignIn;

import java.io.IOException;
import java.util.List;

import retrofit.RestAdapter;
import retrofit.converter.JacksonConverter;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by vedant on 18/12/14.
 */
public class NetworkRequestsManager {
    private static final String TAG = "NetworkRequestsManager";
    private static final String API_VERSION = "v1/";
    private static final String API_ERROR_ALREADY_TAKEN = "has already been taken";
    private static final String API_ERROR_IS_BLANK = "can't be blank";
    private static final String SESSION_ID = "session_id";
    private static final String PROPERTY_GCM_ID = "GCM_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String SENDER_ID = "187047464172";

    private static NetworkRequestsManager sInstance;

    private final MyApplication mApplication;
    private final RegisteredBuyersApiService mRegisteredBuyersApiService;
    private final UnregisteredBuyersApiService mUnregisteredBuyersApiService;

    private SessionErrorsHandlerFunction mSessionErrorsHandlerFunction;

    /**
     * always adds a header ("Session-Id", getStoredSessionId())
     */
    public interface RegisteredBuyersApiService {
        @POST("/buyers")
        Observable<Buyer> register(@Body Buyer buyer);

        @POST("/buyers/sign_in")
        Observable<Buyer> signIn(@Body SignIn signIn);

        @POST("/buyers/exists")
        Observable<Boolean> exists(@Body String phone);

        @GET("/buyers/sellers")
        Observable<List<Seller>> querySellers(@Query("p") int productId);

        @GET("/buyers/sellers/{sellerId}")
        Observable<Seller> getSeller(@Path("sellerId") int sellerId);

        @POST("/buyers/quotes")
        Observable<Quote> sendQuote(@Body Quote quote);

        @GET("/buyers/deals")
        Observable<List<Deal>> getDeals();

        @GET("/buyers/quotes")
        Observable<List<Quote>> getQuotes();

        @GET("/buyers/quotations")
        Observable<List<Quotation>> getQuotations();

        @GET("/buyers/quotations")
        Observable<List<Quotation>> queryQuotations(@Query("p") int productId);

        @GET("/brands_categories?short=1")
        Observable<List<Category>> getCategories();

        @GET("/products")
        Observable<List<Product>> queryProducts(@Query("q") String query);

        @GET("/products/{productId}")
        Observable<Product> getProduct(@Path("productId") int productId);
    }

    public interface UnregisteredBuyersApiService {
        @POST("/devices")
        Observable<Device> registerDevice(@Body Device device);
    }

    public Observable<Buyer> signIn(String facebookUserId) {
        SignIn signIn = new SignIn();
        signIn.setFacebookUserId(facebookUserId);

        return mRegisteredBuyersApiService.signIn(signIn)
                .retryWhen(getSessionErrorsHandlerFunction());
    }

    public Observable<Buyer> registerBuyer(Buyer buyer) {
        return mRegisteredBuyersApiService.register(buyer)
                .onErrorResumeNext(throwable -> {
                    Log.d(TAG, "retrying error:" + throwable);
                    if (ResponseError.Type.ALREADY_TAKEN.is(throwable))
                        return signIn(buyer.getFacebookUser().getId());
                    else
                        return Observable.error(throwable);
                })
                .retryWhen(getSessionErrorsHandlerFunction());
    }

    public Observable<Deal> getDeals() {
        return mRegisteredBuyersApiService.getDeals()
                .retryWhen(getSessionErrorsHandlerFunction())
                .flatMap(Observable::from);
    }

    public Observable<Quote> sendQuote(Quote quote) {
        return mRegisteredBuyersApiService.sendQuote(quote)
                .retryWhen(getSessionErrorsHandlerFunction());
    }

    public Observable<Quotation> queryQuotations(int productId) {
        return mRegisteredBuyersApiService.queryQuotations(productId)
                .retryWhen(getSessionErrorsHandlerFunction())
                .flatMap(Observable::from);
    }

    public Observable<Seller> getSeller(int sellerId) {
        return mRegisteredBuyersApiService.getSeller(sellerId)
                .retryWhen(getSessionErrorsHandlerFunction());
    }

    public Observable<Product> getProduct(int productId) {
        return mRegisteredBuyersApiService.getProduct(productId)
                .retryWhen(new SessionErrorsHandlerFunction());
    }

    public Observable<Seller> querySellers(int productId) {
        return mRegisteredBuyersApiService.querySellers(productId)
                .retryWhen(getSessionErrorsHandlerFunction())
                .flatMap(Observable::from);
    }

    public Observable<List<Category>> getCategories() {
        return mRegisteredBuyersApiService.getCategories()
                .retryWhen(getSessionErrorsHandlerFunction());
    }

    public Observable<List<Product>> queryProducts(String query) {
        return mRegisteredBuyersApiService.queryProducts(query)
                .retryWhen(getSessionErrorsHandlerFunction());
    }

    private NetworkRequestsManager(MyApplication application) {
        this.mApplication = application;

        RestAdapter.LogLevel logLevel = BuildConfig.DEBUG ? RestAdapter.LogLevel.BASIC : RestAdapter.LogLevel.NONE;

        ObjectMapper objectMapper = ServicesSingleton.instance().getDefaultObjectMapper();

        String endpoint = application.getResources().getString(R.string.server_url) + API_VERSION; // append api version
        JacksonConverter jacksonConverter = new JacksonConverter(objectMapper);
        RestAdapter registeredRestAdapter = new RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setConverter(jacksonConverter)
                .setRequestInterceptor(request -> request.addHeader("Session-Id", getStoredSessionId()))
                .setErrorHandler(new ResponseErrorHandler())
                .setLogLevel(logLevel)
                .build();

        mRegisteredBuyersApiService = registeredRestAdapter.create(RegisteredBuyersApiService.class);

        RestAdapter unregisteredRestAdapter = new RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setConverter(jacksonConverter)
                .setErrorHandler(new ResponseErrorHandler())
                .setLogLevel(logLevel)
                .build();

        mUnregisteredBuyersApiService = unregisteredRestAdapter.create(UnregisteredBuyersApiService.class);
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

    /**
     * TODO: move the member to another class and use them to handle errors
     */
    public Observable<Device> authorizeSession() {
        Observable<Device> observable;
        String gcmId = getStoredGcmId();
        if (gcmId.isEmpty()) {
            observable = registerAfterFetchingGcm();
        }
        else {
            String sessionId = getStoredSessionId();
            if (sessionId.isEmpty()) {
                observable = registerDevice();
            }
            else {
                Device device = new Device();
                device.setGcm_registration_id(gcmId);
                device.setSession_id(sessionId);
                observable = Observable.just(device);
            }
        }
        return observable;
    }

    @Deprecated
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) mApplication.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    private class SessionErrorsHandlerFunction implements Func1<Observable<? extends Throwable>, Observable<?>> {
        private static final String TAG = "SessionErrorHandlerFunction";
        private final int maxRetries;
        private int retryCount;
        private Observable<Device> mDeviceObservable;

        public SessionErrorsHandlerFunction() {
            this(3);
        }

        public SessionErrorsHandlerFunction(final int maxRetries) {
            this.maxRetries = maxRetries;
            this.retryCount = 0;
        }

        @Override
        public synchronized Observable<?> call(Observable<? extends Throwable> observable) {
            if (mDeviceObservable == null) {
                mDeviceObservable = observable.flatMap(error -> {
                    Observable<Device> deviceObservable = Observable.error(error);
                    if (++retryCount < maxRetries) {
                        if (error instanceof ResponseError) {
                            ResponseError responseError = (ResponseError) error;
                            ResponseError.Type errorType = responseError.getErrorType();
                            Log.d(TAG, "retrying " + errorType);
                            if (errorType.shouldRefreshGcmId())
                                deviceObservable = registerAfterFetchingGcm();
                            else if (errorType.shouldRefreshSessionId())
                                deviceObservable = registerDevice();
                        }
                        else
                            Log.d(TAG, "unknown error. failing");
                    } else
                        Log.e(TAG, "max retires hit with error:", error);
                    return deviceObservable // reset the info
                            .doOnNext(d -> resetInfo())
                            .doOnError(e -> resetInfo());
                });
            }
            else
                Log.d(TAG, "returning old observable");
            // if a device observable already exists, ignore the error and pass the existing observable
            return mDeviceObservable;
        }

        private void resetInfo() {
            mDeviceObservable = null;
            retryCount = 0;
        }
    }

    private Observable<Device> registerAfterFetchingGcm() {
        return Observable.create((Subscriber<? super Device> subscriber) -> {
            String gcmRegId = "";
            try {
                Log.d(TAG, "fetching gcm");
                gcmRegId = GoogleCloudMessaging.getInstance(mApplication).register(SENDER_ID);
                NetworkRequestsManager.this.storeGcmId(gcmRegId);
                Device device = new Device();
                device.setGcm_registration_id(gcmRegId);
                subscriber.onNext(device);
            } catch (IOException e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io())
                .retryWhen(new ExponentialBackoffFunction())
                // do not call registerDevice() as we do not want to re-run fetching of GCM in any case
                .flatMap(mUnregisteredBuyersApiService::registerDevice)
                .retryWhen(new ExponentialBackoffFunction())
                .doOnNext(d -> storeSessionId(d.getSession_id()));
    }

    private Observable<Device> registerDevice() {
        String storedGcmId = getStoredGcmId();
        if (storedGcmId.isEmpty())
            return registerAfterFetchingGcm();

        Device device = new Device();
        device.setGcm_registration_id(storedGcmId);
        return mUnregisteredBuyersApiService.registerDevice(device)
                .onErrorResumeNext(throwable -> {
                    if (throwable instanceof ResponseError) {
                        ResponseError responseError = (ResponseError) throwable;
                        ResponseError.Type errorType = responseError.getErrorType();
                        if (errorType.shouldRefreshGcmId())
                            return registerAfterFetchingGcm();
                    }
                    return Observable.error(throwable);
                })
                .retryWhen(new ExponentialBackoffFunction())
                .doOnNext(d -> storeSessionId(d.getSession_id()));
    }

    private String getStoredSessionId(){
        String sessionId = mApplication.getSharedPreferences().getString(SESSION_ID, "");
        Log.v(TAG, "Saved Session id: " + sessionId);
        return sessionId;
    }

    private String getStoredGcmId() {
        final SharedPreferences prefs = mApplication.getSharedPreferences();
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

    private void storeSessionId(String sessionId) {
        Log.v(TAG, "saving Session id: "+sessionId);
        mApplication.getSharedPreferences().edit().putString(SESSION_ID, sessionId).commit();
    }

    private void storeGcmId(String gcmId) {
        final SharedPreferences prefs = mApplication.getSharedPreferences();
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

    private SessionErrorsHandlerFunction getSessionErrorsHandlerFunction() {
        if (mSessionErrorsHandlerFunction == null)
            mSessionErrorsHandlerFunction = new SessionErrorsHandlerFunction();
        return mSessionErrorsHandlerFunction;
    }
}
