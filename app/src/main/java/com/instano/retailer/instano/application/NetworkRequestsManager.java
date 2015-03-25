package com.instano.retailer.instano.application;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.instano.retailer.instano.BuildConfig;
import com.instano.retailer.instano.utilities.library.JsonArrayRequest;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.models.Buyer;
import com.instano.retailer.instano.utilities.models.Device;
import com.instano.retailer.instano.utilities.models.ProductCategories;
import com.instano.retailer.instano.utilities.models.Quote;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vedant on 18/12/14.
 */
public class NetworkRequestsManager implements Response.ErrorListener {

    private static final String TAG = "NetworkRequestsManager";
    private static final String LOCAL_SERVER_URL = "http://52.1.202.4/";
//    private static final String LOCAL_SERVER_URL = "http://192.168.1.36:3000/";

    private final static String API_ERROR_ALREADY_TAKEN = "has already been taken";
    private final static String API_ERROR_IS_BLANK = "can't be blank";
    public static final String SESSION_ID = "session_id";

    private static final String PROPERTY_GCM_ID = "GCM_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String SENDER_ID = "187047464172";

    private static NetworkRequestsManager sInstance;

    private final MyApplication mApplication;

    private QuoteCallbacks mQuoteCallbacks;
    private RegistrationCallback mRegistrationCallback;
    private SignInCallbacks mSignInCallbacks;
    private SessionIdCallback mSessionIdCallback;

    private RequestQueue mRequestQueue;
    private ObjectMapper mJsonObjectMapper;


    public void registerCallback(QuoteCallbacks quoteCallbacks) {
        mQuoteCallbacks = quoteCallbacks;
    }

    public void registerCallback(RegistrationCallback registrationCallback) {
        this.mRegistrationCallback = registrationCallback;
    }

    public void registerCallback(SignInCallbacks signInCallbacks) {
        this.mSignInCallbacks = signInCallbacks;
    }

    public void registerCallback(SessionIdCallback sessionIdCallback) {
        mSessionIdCallback = sessionIdCallback;
    }

    public interface QuoteCallbacks {
        public void productCategoriesUpdated(List<ProductCategories.Category> productCategories);

        public void onQuoteSent(boolean success);
    }

    public interface RegistrationCallback {
        public void phoneExists(boolean exists);

        public void onRegistration(ResponseError result);
    }

    public interface SignInCallbacks {
        public void signedIn(ResponseError error);
    }

    public interface SessionIdCallback {
        public void onSessionResponse(ResponseError error);
    }

    private NetworkRequestsManager(MyApplication application) {
        this.mApplication = application;
        mRequestQueue = Volley.newRequestQueue(application);
        mJsonObjectMapper = new ObjectMapper();
        mJsonObjectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
        mJsonObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /*package*/
    static void init(MyApplication application) {
        sInstance = new NetworkRequestsManager(application);
    }

    public static NetworkRequestsManager instance() {
        if (sInstance == null)
            throw new IllegalStateException("NetworkRequestsManager.init() not called");

        return sInstance;
    }

    public void getQuotationsRequest() {

        JsonArrayRequest request = new JsonArrayRequest(
                getRequestUrl(RequestType.GET_QUOTATIONS, -1),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.v(TAG, "Quotations response:" + response.toString());
                        boolean dataChanged = DataManager.instance().updateQuotations(response);
                    }
                },
                this
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<String,String>();
                headers.put("Session-Id",getSessionId() );
                return headers;
            }
        };
        mRequestQueue.add(request);
    }

    public void getQuotesRequest() {

        JsonArrayRequest request = new JsonArrayRequest(
                getRequestUrl(RequestType.GET_QUOTES, -1),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.v(TAG, "Quotes response:" + response.toString());
                        DataManager.instance().updateQuotes(response);
                        // fetch quotations once quotes are fetched:
                        getQuotationsRequest();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, ".getQuotesRequest networkResponse: " + error.networkResponse, error);
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<String,String>();
                headers.put("Session-Id",getSessionId() );
                return headers;
            }
        };
        mRequestQueue.add(request);
    }

    public void getSellersRequest() {
        JsonArrayRequest request = new JsonArrayRequest(
                getRequestUrl(RequestType.GET_SELLERS, -1),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.v(TAG, "Sellers response:" + response.toString());
                        DataManager.instance().updateSellers(response);
                    }
                },
                this
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<String,String>();
                headers.put("Session-Id",getSessionId() );
                return headers;
            }
        };
        mRequestQueue.add(request);
    }

    public void getDealsRequest() {
        JsonArrayRequest request = new JsonArrayRequest(
                getRequestUrl(RequestType.GET_DEALS, -1),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.v(TAG, "Deals response:" + response.toString());
                        DataManager.instance().updateDeals(response);
                    }
                },
                this
        )
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<String,String>();
                headers.put("Session-Id",getSessionId() );
                return headers;
            }
        };
        mRequestQueue.add(request);
    }

    public void sendQuoteRequest(@NonNull Quote quote) {
        Log.v(TAG, "sendQuoteRequest request: " + quote.toJsonObject());

        JsonObjectRequest request = new AuthenticatedJsonRequest(
                getRequestUrl(RequestType.SEND_QUOTE, -1),
                quote.toJsonObject(),
                new ResponseListener() {
                    @Override
                    public void onResponse(ResponseError error, JSONObject response) {

                        boolean success = error == ResponseError.NO_ERROR;

                        if (mQuoteCallbacks != null)
                            mQuoteCallbacks.onQuoteSent(success);

                    }
                }
        );
        mRequestQueue.add(request);
    }

    public void sendSignInRequest(@NonNull final String apiKey) {

        JsonObjectRequest request = null;
        try {
            JSONObject postData = new JSONObject().put("api_key", apiKey);
            Log.v(TAG, "sign in request: " + postData.toString());
            request = new AuthenticatedJsonRequest(
                    getRequestUrl(RequestType.SIGN_IN, -1),
                    postData,
                    new ResponseListener() {
                        @Override
                        public void onResponse(ResponseError error, JSONObject response) {
                            Log.d(TAG + "sendSignInRequest.onResponse", String.valueOf(response));
                            Buyer responseBuyer = null;
                            if (error == ResponseError.NO_ERROR) {
                                try {
                                    responseBuyer = mJsonObjectMapper.readValue(response.toString(), Buyer.class);
                                } catch (JsonMappingException e) {
                                    error = ResponseError.UNKNOWN_ERROR;
                                    Log.fatalError(e);
                                } catch (JsonParseException e) {
                                    error = ResponseError.UNKNOWN_ERROR;
                                    Log.fatalError(e);
                                } catch (IOException e) {
                                    error = ResponseError.UNKNOWN_ERROR;
                                    Log.fatalError(e);
                                }
                            }
                            ServicesSingleton.instance().afterSignIn(responseBuyer);
                            if (mSignInCallbacks != null)
                                mSignInCallbacks.signedIn(error);
                        }
                    }
            );
        } catch (JSONException e) {
            Log.e(TAG, "sendSignInRequest.onResponse", e);
        }
        mRequestQueue.add(request);
    }

    public void registerRequest(final Buyer requestBuyer) {
        JSONObject jsonRequest = null;
        try {
            jsonRequest = new JSONObject(mJsonObjectMapper.writeValueAsString(requestBuyer));
            Log.v(TAG ,".registerRequest" + jsonRequest.toString());

            JsonObjectRequest request = new AuthenticatedJsonRequest(
                    getRequestUrl(RequestType.REGISTER_BUYER, -1) , // String url
                    jsonRequest, // JSONObject jsonRequest
                    new ResponseListener() {
                        @Override
                        public void onResponse(ResponseError responseError, JSONObject response) {
                            Log.v(TAG + "registerRequest.onResponse", response.toString());

                            if (responseError == ResponseError.NO_ERROR) {
                                try {
                                    Buyer responseBuyer = mJsonObjectMapper.readValue(response.toString(), Buyer.class);
                                    //response value is stored in Buyer object
                                    Log.v(TAG + "registerRequest.onResponseFromJson", responseBuyer.toString());
                                    ServicesSingleton.instance().afterSignIn(responseBuyer);
                                    Log.v(TAG + "registerRequest.onResponse", responseBuyer.getApi_key());
                                } catch (JsonMappingException e) {
                                    Log.fatalError(e);
                                    try {
                                        if (API_ERROR_ALREADY_TAKEN.equals(response.getJSONArray("phone").getString(0)))
                                            responseError = ResponseError.PHONE_EXISTS;
                                    } catch (JSONException e1) {
                                        responseError = ResponseError.UNKNOWN_ERROR;
                                        Log.fatalError(e1);
                                    }
                                } catch (IOException e) {
                                    Log.fatalError(e);
                                    responseError = ResponseError.UNKNOWN_ERROR;
                                }
                            }
                            if (mRegistrationCallback != null)
                                mRegistrationCallback.onRegistration(responseError);
                        }
                    });
            mRequestQueue.add(request);
        } catch (JsonProcessingException e) {
            Log.fatalError(e);
            Log.v(TAG ,".registerRequest Error" + e.toString());
        } catch (JSONException e) {
            Log.fatalError(e);
            Log.v(TAG ,".registerRequest Error" + e.toString());
        }

    }

    public void buyerExistsRequest(String phone) {
        Log.e(TAG, ".buyerExistsRequest");
        try {
            JsonObjectRequest request = new AuthenticatedJsonRequest(
                    getRequestUrl(RequestType.BUYER_EXISTS, -1),
                    new JSONObject().put("phone", phone),
                    new ResponseListener() {
                        @Override
                        public void onResponse(ResponseError error, JSONObject response) {
                            try {
                                Log.v(TAG, ".buyerExistsRequest response: " + response);
                                mRegistrationCallback.phoneExists(response.getBoolean("exists"));
                            } catch (JSONException e) {
                                Log.e(TAG, ".buyerExistsRequest error: ", e);
                            }
                        }
                    }
            );
            mRequestQueue.add(request);
        } catch (JSONException e) {
            Log.fatalError(e);
        }
    }

    public void getProductCategoriesRequest() {
        final JsonObjectRequest request = new AuthenticatedJsonRequest(
                getRequestUrl(RequestType.GET_PRODUCT_CATEGORIES, -1),
                null,
                new ResponseListener() {
                    @Override
                    public void onResponse(ResponseError error, JSONObject response) {
                        if (error == ResponseError.NO_ERROR) {
                            Log.v(TAG, "ProductCategories response:" + response.toString());
                            boolean updated = DataManager.instance().updateProductCategories(response);
                            if (mQuoteCallbacks != null && updated)
                                mQuoteCallbacks.productCategoriesUpdated(DataManager.instance().getProductCategories(false));
                        }
                    }
                }
        );
        mRequestQueue.add(request);
    }

    public void setQuotationStatusReadRequest(int quotationId) {
        JSONObject requestData;
        try {
            requestData = new JSONObject()
                    .put("status", "read");
        } catch (JSONException e) {
            Log.e(TAG, "setQuotationStatusReadRequest exception", e);
            return;
        }
        Log.d(TAG, "setQuotationStatusReadRequest requestData" + requestData);
        Request request = new AuthenticatedJsonRequest(
                Request.Method.PUT,
                getRequestUrl(RequestType.PATCH_QUOTATION_STATUS, quotationId),
                requestData,
                new ResponseListener() {
                    @Override
                    public void onResponse(ResponseError error, JSONObject jsonResponse) {
                    }
                }
        );
        mRequestQueue.add(request);
    }

    /**
     * @param requestType
     * @param id          The id to be used for specific URLs. unused for others
     * @return the complete URL to be sent
     */
    private  String getRequestUrl(RequestType requestType, int id) {

        String SERVER_URL;
        if (BuildConfig.DEBUG)
            SERVER_URL = LOCAL_SERVER_URL;
        else
            SERVER_URL = "http://www.instano.in/";
        final String API_VERSION = "v1/";
        String url = SERVER_URL + API_VERSION;
        switch (requestType) {
            case REGISTER_BUYER:
                return url + "buyers";
            case SIGN_IN:
                return url + "buyers/sign_in";
            case BUYER_EXISTS:
                return url + "buyers/exists";
            case SEND_QUOTE:
            case GET_QUOTES:
                return url + "buyers/quotes";
            case GET_QUOTATIONS:
                return url + "buyers/quotations";
            case GET_SELLERS:
                return url + "buyers/sellers";
            case GET_PRODUCT_CATEGORIES:
                return url + "brands_categories";
            case GET_DEALS:
                return url + "buyers/deals";

            case PATCH_QUOTATION_STATUS:
                return url + "buyers/quotations/" + id;
            case REGISTER_DEVICE:
                return  url + "devices/";
        }

        throw new IllegalArgumentException();
    }

    private enum RequestType {
        REGISTER_BUYER,
        SIGN_IN,
        GET_QUOTES,
        GET_QUOTATIONS,
        SEND_QUOTE,
        GET_PRODUCT_CATEGORIES,
        GET_SELLERS,
        GET_DEALS,
        REGISTER_DEVICE,
        BUYER_EXISTS,
        PATCH_QUOTATION_STATUS
    }

    public static enum ResponseError {
        NO_ERROR,
        UNKNOWN_ERROR,
        // authorization errors (422):
        NO_GCM_ID,
        INVALID_GCM_ID,
        GCM_NOT_REGISTERED,
        OTHER_GCM_ERROR,

        // authorization errors (403/401):
        NO_SESSION_ID,
        INCORRECT_SESSION_ID,
        NO_BUYER_ASSOCIATED,
        SOME_OTHER_401,
        SIGN_IN_FAILED,

        // Not acceptable (406):
        INCORRECT_API_KEY,
        SOME_OTHER_406,

        // network errors:
        SERVER_HANG_UP, // 502 a.k.a. bad gateway
        NETWORK_RESPONSE_NULL,
        VOLLEY_TIMEOUT,

        // fatal
        BAD_REQUEST, // 400
        SERVER_ERROR,
        // 422 specific errors:
        PHONE_EXISTS,
        SOME_OTHER_422;

        public boolean shouldRefreshGcmId() {
            if (this.toString().contains("GCM"))
                return true;
            else
                return false;
        }

        public boolean shouldRefreshSessionId() {
            if (this == NO_SESSION_ID || this == INCORRECT_SESSION_ID)
                return true;
            else
                return false;
        }

        public boolean isLongWaiting() {
            if (this == SERVER_HANG_UP || this == VOLLEY_TIMEOUT)
                return true;
            else
                return false;
        }

        public boolean isFatal() {
            if (this == BAD_REQUEST || this == SERVER_ERROR || this == SERVER_HANG_UP)
                return true;
            else
                return false;
        }
    }

    private static ResponseError getResponseError(VolleyError volleyError) {
        ResponseError error = ResponseError.UNKNOWN_ERROR;
        NetworkResponse networkResponse = volleyError.networkResponse;
        String responseString="";
        if (networkResponse != null) {
            try {
                Log.v(TAG , ".getResponseError code :"+networkResponse.statusCode);
                responseString = new String(networkResponse.data, HttpHeaderParser.parseCharset(networkResponse.headers));
                Log.v(TAG, ".getResponseError body :" + responseString);
            } catch (UnsupportedEncodingException e) {
                Log.fatalError(e);
            }
            switch (networkResponse.statusCode) {
                case HttpStatus.SC_UNAUTHORIZED: // volley fails for 401
                case HttpStatus.SC_FORBIDDEN:
                    if (responseString.contains("incorrect session_id"))
                        error = ResponseError.INCORRECT_SESSION_ID;
                    else if (responseString.contains("no buyer associated"))
                        error = ResponseError.NO_BUYER_ASSOCIATED;
                    else
                        error = ResponseError.SOME_OTHER_401;
                    break;

                case HttpStatus.SC_NOT_ACCEPTABLE:
                    if (responseString.contains("incorrect api_key"))
                        error = ResponseError.INCORRECT_API_KEY;
                    else
                        error = ResponseError.SOME_OTHER_406;
                    break;

                case HttpStatus.SC_UNPROCESSABLE_ENTITY:
                    if (responseString.contains("GCM errors")) {
                        if (responseString.contains("NotRegistered"))
                            error = ResponseError.GCM_NOT_REGISTERED;
                        else if (responseString.contains("InvalidRegistration"))
                            error = ResponseError.INVALID_GCM_ID;
                        else
                            error = ResponseError.OTHER_GCM_ERROR;
                    }
                    else
                        error = ResponseError.SOME_OTHER_422;
                    break;

                case HttpStatus.SC_BAD_REQUEST:
                    error = ResponseError.BAD_REQUEST;
                    break;

                case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                    error = ResponseError.SERVER_ERROR;
                    break;

                case HttpStatus.SC_BAD_GATEWAY:
                    error = ResponseError.SERVER_HANG_UP;
                    break;
            }
        }
        else if (volleyError instanceof TimeoutError)
            error = ResponseError.VOLLEY_TIMEOUT;
        else
            error = ResponseError.NETWORK_RESPONSE_NULL;

        if (error.isFatal())
            Log.e(TAG, ".getResponseError returned " + error);
        else
            Log.v(TAG, ".getResponseError returned " + error);
        return error;
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

    /**
     * just log the error
     */
    @Override
    public void onErrorResponse(VolleyError error) {
        getResponseError(error);
        error.printStackTrace();
    }


    public void sendDeviceRegisterRequest(Device device) {
        Log.v(TAG,"device object"+device);
        JSONObject jsonRequest = null;
        Log.v(TAG, "Device object  " + device.toString());
        try {
            jsonRequest = new JSONObject(mJsonObjectMapper.writeValueAsString(device));
            Log.v(TAG + ".sendDeviceRegisterRequest GcmId in Device", device.getGcm_registration_id());
            Log.v(TAG + ".DeviceId in Json request", jsonRequest.toString());

            JsonObjectRequest request = new JsonObjectRequest(getRequestUrl(RequestType.REGISTER_DEVICE,-1),
                    jsonRequest,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            ResponseError error = ResponseError.UNKNOWN_ERROR;
                            try{
                                Device responseDevice = mJsonObjectMapper.readValue(response.toString(), Device.class);
                                Log.v(TAG + "sendDeviceRegisterRequest response: ",responseDevice.toString());
                                String session_id = responseDevice.getSession_id();
                                storeSessionId(session_id);
                                if(session_id == null || session_id.isEmpty())
                                    error = ResponseError.NO_SESSION_ID;
                                else
                                    error = ResponseError.NO_ERROR;
                            } catch (JsonMappingException e) {
                                Log.fatalError(e);
                            } catch (JsonParseException e) {
                                Log.fatalError(e);
                            } catch (IOException e) {
                                Log.fatalError(e);
                            }
                            if(mSessionIdCallback !=null)
                                mSessionIdCallback.onSessionResponse(error);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            if(mSessionIdCallback !=null)
                                mSessionIdCallback.onSessionResponse(getResponseError(volleyError));
                        }
                    }
            );
            mRequestQueue.add(request);
        } catch (JsonProcessingException e) {
            Log.fatalError(e);
        } catch (JSONException e) {
            Log.fatalError(e);
        }

    }

    private void storeSessionId(String sessionId) {
        Log.v(TAG, "saving Session id: "+sessionId);
        getAppSharedPreferences().edit().putString(SESSION_ID, sessionId).commit();
    }

    private String getSessionId(){
        String sessionId = getAppSharedPreferences().getString(SESSION_ID, "");
        Log.v(TAG, "Saved Session id: "+sessionId);
        return sessionId;
    }

    public interface ResponseListener{
        public void onResponse(ResponseError error, JSONObject jsonResponse);
    }


    /**
     * Created by ROHIT on 18-Mar-15.
     */
    private class AuthenticatedJsonRequest extends JsonObjectRequest {

        /**
         * either GET or POST
         * @param url
         * @param request
         * @param responseListener
         */
        public AuthenticatedJsonRequest(String url, final JSONObject request,
                                        final ResponseListener responseListener) {
            this(request == null ? Method.GET : Method.POST, url, request,
                    responseListener);
        }

        public AuthenticatedJsonRequest(int method, String url, final JSONObject request,
                                        final ResponseListener responseListener) {
            super(method, url, request, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    responseListener.onResponse(ResponseError.NO_ERROR, response);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    ResponseError error;
                    error = getResponseError(volleyError);
                    responseListener.onResponse(error, null);
                }
            });
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            String sessionId = getSessionId();
            if(sessionId.isEmpty())
                throw new AuthFailureError("session is empty");
            Map<String,String> headers = new HashMap<String,String>();
            headers.put("Session-Id", sessionId);
            return headers;
        }
    }

    public void authorizeSession(boolean refreshGcmId) {
        String gcmId = getGcmId();
        if (refreshGcmId || gcmId.isEmpty()) {
            fetchGcmRegIdAsync();
        }
        else
            registerNewSession(gcmId);
    }

    protected void fetchGcmRegIdAsync() {
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
            // Your implementation here.
            Log.v(TAG, ".registerNewSession Send regId  " + gcmId);
            Device device = new Device();
            device.setGcm_registration_id(gcmId);
            sendDeviceRegisterRequest(device);
        }
        else {
            if (mSessionIdCallback != null)
                mSessionIdCallback.onSessionResponse(NetworkRequestsManager.ResponseError.NO_GCM_ID);
        }
    }

    protected String getGcmId() {
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
    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private int getAppVersion() {
        try {
            PackageInfo packageInfo = mApplication.getPackageManager()
                    .getPackageInfo(mApplication.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
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

    private SharedPreferences getAppSharedPreferences() {
        return mApplication.getSharedPreferences(ServicesSingleton.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    }
}
