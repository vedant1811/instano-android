package com.instano.retailer.instano.application.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.instano.retailer.instano.BuildConfig;
import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.MyApplication;
import com.instano.retailer.instano.application.ServicesSingleton;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.models.Buyer;
import com.instano.retailer.instano.utilities.models.Categories;
import com.instano.retailer.instano.utilities.models.Deal;
import com.instano.retailer.instano.utilities.models.Device;
import com.instano.retailer.instano.utilities.models.Product;
import com.instano.retailer.instano.utilities.models.Quotation;
import com.instano.retailer.instano.utilities.models.Quote;
import com.instano.retailer.instano.utilities.models.Seller;
import com.instano.retailer.instano.utilities.models.SignIn;

import java.io.IOException;
import java.util.HashMap;
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

    /**
     * stores the different observables based on class name.
     * Do NOT access this directly, use the accessor {@link #getObservable(Class)}
     * TODO: create a separate class to limit that possible keys and clear data on new buyer
     */
    private final HashMap<String, Observable> mObservableHashMap;

    private EchoFunction<Quotation> mQuotationEchoFunction;
    private EchoFunction<Seller> mSellerEchoFunction;
    private EchoFunction<Quote> mQuoteEchoFunction;

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
        Observable<List<Seller>> getSellers();

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

        @GET("/brands_categories")
        Observable<Categories> getProductCategories();

        @GET("/products")
        Observable<List<Product>> queryProducts(@Query("q") String query);
    }

    public interface UnregisteredBuyersApiService {
        @POST("/devices")
        Observable<Device> registerDevice(@Body Device device);
    }

    public void newObject(Quote quote) {
        mQuoteEchoFunction.newEventReceived(quote);
    }

    public void newObject(Quotation quotation) {
        mQuotationEchoFunction.newEventReceived(quotation);
    }

    public void newObject(Seller seller) {
        mSellerEchoFunction.newEventReceived(seller);
    }

    public Observable<Buyer> signIn(String apiKey) {
        SignIn signIn = new SignIn();
        signIn.setApi_key(apiKey);

        return mRegisteredBuyersApiService.signIn(signIn)
                        .retryWhen(new SessionErrorsHandlerFunction());
    }

    public Observable<Buyer> registerBuyer(Buyer buyer) {
        return mRegisteredBuyersApiService.register(buyer)
                        .retryWhen(new SessionErrorsHandlerFunction());
    }

    /**
     * also echo the retrofit returned quote so that observers will get this quote as well.
     * @param quote
     * @return observable that observers this quote response only
     */
    public Observable<Quote> sendQuote(Quote quote) {
        return mRegisteredBuyersApiService.sendQuote(quote)
                .retryWhen(new SessionErrorsHandlerFunction())
                .doOnNext(this::newObject);
    }

    public Observable<Quotation> queryQuotations(int productId) {
        return mRegisteredBuyersApiService.queryQuotations(productId)
                .retryWhen(new SessionErrorsHandlerFunction())
                .flatMap(Observable::from);
    }

    public Observable<Seller> getSeller(int sellerId) {
        return mRegisteredBuyersApiService.getSeller(sellerId);
    }

    public Observable<List<Product>> queryProducts(String query) {
        return mRegisteredBuyersApiService.queryProducts(query)
                .retryWhen(new SessionErrorsHandlerFunction());
    }

    private NetworkRequestsManager(MyApplication application) {
        this.mApplication = application;

        RestAdapter.LogLevel logLevel = BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE;

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
        return observable.doOnNext(
                (device) -> {
                        Log.d(TAG, "authorizeSession.doOnNext");
                        mSellerEchoFunction = new EchoFunction<>();
                        replaceAndCacheObservable(Seller.class,
                                mRegisteredBuyersApiService.getSellers()
                                        .retryWhen(new SessionErrorsHandlerFunction())
                                        .flatMap((List<Seller> t1) -> {
                                            return Observable.from(t1);
                                        }))
                                .doOnNext((Seller seller) -> Log.d(TAG, "new seller: " + seller));
                        mergeCacheAndDistinctObservable(Seller.class,
                                Observable.create(mSellerEchoFunction));
        });
    }

    public <T> Observable<T> getObservable(@NonNull Class<T> modelClass) {
        Observable<T> observable = mObservableHashMap.get(modelClass.getSimpleName());
        if (observable == null) {
            observable = Observable.never(); // a placeholder observable so that null isn't returned
            mObservableHashMap.put(modelClass.getSimpleName(), observable);
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

    /**
     * used to keep previous values
     * BEWARE: only future subscribers will get the new results! existing subscribers will have the old observable
     */
    private <T> Observable<T> mergeCacheAndDistinctObservable(@NonNull Class<T> modelClass, Observable<T> newObservable) {
        Observable<T> observable = getObservable(modelClass)
                .mergeWith(newObservable)// merge with existing observable
                .cache()
                .distinct();
        mObservableHashMap.put(modelClass.getSimpleName(),
                observable); // replace the old observable
        return observable;
    }

    /**
     * used to discard previous values
     */
    private <T> Observable<T> replaceAndCacheObservable(@NonNull Class<T> modelClass, Observable<T> newObservable) {
        Observable<T> observable = newObservable
                .cache();
        mObservableHashMap.put(modelClass.getSimpleName(),
                observable); // replace the old observable
        return observable;
    }

    /**
     * also called by SessionActivity
     * @param buyer
     */
    public void newBuyer(@NonNull Buyer buyer) {
        Log.d(TAG, "onNewBuyer");

        replaceAndCacheObservable(Deal.class,
                mRegisteredBuyersApiService.getDeals()
                        .retryWhen(new SessionErrorsHandlerFunction())
                        .flatMap(Observable::from)); // flatten returned List<Deal> into Deal

        replaceAndCacheObservable(Quote.class,
                mRegisteredBuyersApiService.getQuotes()
                        .retryWhen(new SessionErrorsHandlerFunction())
                        .flatMap(Observable::from)
                        .distinct());

        replaceAndCacheObservable(Quotation.class,
                mRegisteredBuyersApiService.getQuotations()
                        .retryWhen(new SessionErrorsHandlerFunction())
                        .flatMap(Observable::from));

        replaceAndCacheObservable(Categories.class,
                mRegisteredBuyersApiService.getProductCategories()
                        .retryWhen(new SessionErrorsHandlerFunction()));

        mQuotationEchoFunction = new EchoFunction<>();
        mQuoteEchoFunction = new EchoFunction<>();

        mergeCacheAndDistinctObservable(Quotation.class,
                Observable.create(mQuotationEchoFunction));
        mergeCacheAndDistinctObservable(Quote.class,
                Observable.create(mQuoteEchoFunction));
    }

    private class SessionErrorsHandlerFunction implements Func1<Observable<? extends Throwable>, Observable<?>> {
        private static final String TAG = "SessionErrorHandlerFunction";
        private final int maxRetries;
        private int retryCount;

        public SessionErrorsHandlerFunction() {
            this(2);
        }

        public SessionErrorsHandlerFunction(final int maxRetries) {
            this.maxRetries = maxRetries;
            this.retryCount = 0;
        }

        @Override
        public Observable<?> call(Observable<? extends Throwable> observable) {
            return observable
                    .flatMap(error -> {
                        if (++retryCount < maxRetries) {
                            if (error instanceof ResponseError) {
                                ResponseError responseError = (ResponseError) error;
                                ResponseError.Type errorType = responseError.getErrorType();
                                Log.d(TAG, "retrying " + errorType);
                                if (errorType.shouldRefreshGcmId())
                                    return registerAfterFetchingGcm();
                                else if (errorType.shouldRefreshSessionId())
                                    return registerDevice();
                            }
                            Log.d(TAG, "unknown error. failing");
                        }
                        else
                            Log.e(TAG, "max retires hit with known error:", error);
                        return Observable.error(error);
                    });
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
        Device device = new Device();
        device.setGcm_registration_id(getStoredGcmId());
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
        Log.v(TAG, "Saved Session id: "+sessionId);
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
}
