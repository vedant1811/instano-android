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
import rx.Observable;
import rx.Subscriber;
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

        @GET("/buyers/deals")
        Observable<List<Deal>> getDeals();

        @GET("/buyers/quotes")
        Observable<List<Quote>> getQuotes();

        @GET("/buyers/quotations")
        Observable<List<Quotation>> getQuotations();

        @GET("/buyers/sellers")
        Observable<List<Seller>> getSellers();

        @GET("/brands_categories")
        Observable<Categories> getProductCategories();
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

        Observable<Buyer> buyerObservable = replaceAndCacheObservable(Buyer.class,
                mRegisteredBuyersApiService.signIn(signIn));
        buyerObservable.subscribe(this::newBuyer);
        return buyerObservable;
    }

    public Observable<Buyer> registerBuyer(Buyer buyer) {
        Observable<Buyer> buyerObservable = replaceAndCacheObservable(Buyer.class,
                mRegisteredBuyersApiService.register(buyer));
        buyerObservable.subscribe(this::newBuyer);
        return buyerObservable;
    }

    /**
     * also echo the retrofit returned quote so that observers will get this quote as well.
     * @param quote
     * @return observable that observers this quote response only
     */
    public Observable<Quote> sendQuote(Quote quote) {
        Observable<Quote> quoteObservable = mRegisteredBuyersApiService.sendQuote(quote);
        quoteObservable.subscribe(this::newObject);
        return quoteObservable;
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
                .setRequestInterceptor(request -> request.addHeader("Session-Id", getSessionId()))
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

    public Observable<Device> authorizeSession() {
        String gcmId = getGcmId();
        if (gcmId.isEmpty())
            return registerAfterFetchingGcm();
        else {
            String sessionId = getSessionId();
            Device device = new Device();
            device.setGcm_registration_id(gcmId);
            if (sessionId.isEmpty()) {
                return registerDevice(device);
            }
            else {
                device.setSession_id(sessionId);
                return Observable.just(device);
            }
        }
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

    private void newBuyer(@NonNull Buyer buyer) {
        Log.d(TAG, "onNewBuyer");

        replaceAndCacheObservable(Deal.class,
                mRegisteredBuyersApiService.getDeals()
                        .flatMap(Observable::from)); // flatten returned List<Deal> into Deal

        replaceAndCacheObservable(Quote.class,
                mRegisteredBuyersApiService.getQuotes()
                        .flatMap(Observable::from)
                        .distinct());

        replaceAndCacheObservable(Quotation.class,
                mRegisteredBuyersApiService.getQuotations()
                        .flatMap(Observable::from));

        replaceAndCacheObservable(Seller.class,
                mRegisteredBuyersApiService.getSellers()
                        .flatMap((List<Seller> t1) -> {
                            Log.v(TAG,t1.toString());
                            return Observable.from(t1);
                        }));

        replaceAndCacheObservable(Categories.class,
                mRegisteredBuyersApiService.getProductCategories());

        mQuotationEchoFunction = new EchoFunction<>();
        mSellerEchoFunction = new EchoFunction<>();
        mQuoteEchoFunction = new EchoFunction<>();

        mergeCacheAndDistinctObservable(Quotation.class,
                Observable.create(mQuotationEchoFunction));
        mergeCacheAndDistinctObservable(Seller.class,
                Observable.create(mSellerEchoFunction));
        mergeCacheAndDistinctObservable(Quote.class,
                Observable.create(mQuoteEchoFunction));
    }

    private Observable<Device> registerAfterFetchingGcm() {
        return Observable.create(new Observable.OnSubscribe<Device>() {
            @Override
            public void call(Subscriber<? super Device> subscriber) {
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
            }
        }).subscribeOn(Schedulers.io())
                .retryWhen(new ExponentialBackoffFunction())
                .flatMap(this::registerDevice);
    }

    private Observable<Device> registerDevice(Device device) {
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

    private String getSessionId(){
        String sessionId = getAppSharedPreferences().getString(SESSION_ID, "");
        Log.v(TAG, "Saved Session id: "+sessionId);
        return sessionId;
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

    private void storeSessionId(String sessionId) {
        Log.v(TAG, "saving Session id: "+sessionId);
        getAppSharedPreferences().edit().putString(SESSION_ID, sessionId).commit();
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
