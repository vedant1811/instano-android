package com.instano.retailer.instano.application;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.instano.retailer.instano.BuildConfig;
import com.instano.retailer.instano.utilities.library.AuthenticatedJsonRequest;
import com.instano.retailer.instano.utilities.library.JsonArrayRequest;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.library.StringRequest;
import com.instano.retailer.instano.utilities.models.Buyer;
import com.instano.retailer.instano.utilities.models.Device;
import com.instano.retailer.instano.utilities.models.ProductCategories;
import com.instano.retailer.instano.utilities.models.Quote;

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
//    private static final String LOCAL_SERVER_URL = "http://192.168.0.140:3000/";

    private final static String API_ERROR_ALREADY_TAKEN = "has already been taken";
    private final static String API_ERROR_IS_BLANK = "can't be blank";
    public static final String SESSION_ID = "session_id";

    private static NetworkRequestsManager sInstance;

    private final MyApplication mApplication;

    private QuoteCallbacks mQuoteCallbacks;
    private RegistrationCallback mRegistrationCallback;
    private SignInCallbacks mSignInCallbacks;
    private SessionIdCallback mSessionIdCallback;

    private RequestQueue mRequestQueue;
    private ObjectMapper mJsonObjectMapper;
    private Buyer mBuyer;


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
        public void signedIn(boolean success);
    }

    public interface SessionIdCallback {
        public void onSessionResponse(ResponseError error);
    }

    private NetworkRequestsManager(MyApplication application) {
        this.mApplication = application;
        mRequestQueue = Volley.newRequestQueue(application);
        mJsonObjectMapper = new ObjectMapper();
        mJsonObjectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
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

    public void getQuotationsRequest(@NonNull Buyer buyer) {

        JSONObject postData;
        try {
            postData = new JSONObject()
                    .put("id", buyer.getId());
        } catch (JSONException e) {
            Log.e(TAG, "getQuotationsRequest", e);
            return;
        }

        JsonArrayRequest request = new JsonArrayRequest(
                getRequestUrl(RequestType.GET_QUOTATIONS, -1),
                postData,
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

    public void getQuotesRequest(@NonNull final Buyer buyer) {

        JSONObject requestData;
        try {
            requestData = new JSONObject()
                    .put("id", buyer.getId());
        } catch (JSONException e) {
            Log.e(TAG, "getQuotesRequest exception", e);
            return;
        }
        Log.v(TAG, "getQuotesRequest requestData" + requestData);

        JsonArrayRequest request = new JsonArrayRequest(
                getRequestUrl(RequestType.GET_QUOTES, -1),
                requestData,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.v(TAG, "Quotes response:" + response.toString());
                        DataManager.instance().updateQuotes(response);
                        // fetch quotations once quotes are fetched:
                        getQuotationsRequest(buyer);

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

    public void sendQuoteRequest(@NonNull Quote quote) {
        Log.v(TAG, "sendQuoteRequest request: " + quote.toJsonObject());

        JsonObjectRequest request = new AuthenticatedJsonRequest(
                getRequestUrl(RequestType.SEND_QUOTE, -1),
                quote.toJsonObject(),
                new AuthenticatedJsonRequest.Listener() {
                    @Override
                    public void onResponse(ResponseError error, JSONObject response) {

                        boolean success = error == ResponseError.NO_ERROR;

                        if (mQuoteCallbacks != null)
                            mQuoteCallbacks.onQuoteSent(success);

                    }
                },
                getSessionId()
        );
        mRequestQueue.add(request);
    }

    public void signInRequest(@NonNull final String apiKey) {

        JsonObjectRequest request = null;
        try {
            JSONObject postData = new JSONObject().put("api_key", apiKey);
            Log.v(TAG, "sign in request: " + postData.toString());
            request = new AuthenticatedJsonRequest(
                    getRequestUrl(RequestType.SIGN_IN, -1),
                    postData,
                    new AuthenticatedJsonRequest.Listener() {
                        @Override
                        public void onResponse(ResponseError error, JSONObject response) {
                            try {
                                if (response.getInt("id") != -1) {
                                    Log.d(TAG + "check", response.toString());
                                    mBuyer = new Buyer();
                                    Log.d(TAG + "check", mBuyer.toString());
                                    mBuyer.setId(response.getInt("id"));
                                    mBuyer.setName(response.getString("name"));
                                    mBuyer.setPhone(response.getString("phone"));

                                    ServicesSingleton.instance().afterSignIn(mBuyer, response.getString("api_key"));
                                    if (mSignInCallbacks != null)
                                        mSignInCallbacks.signedIn(true);
                                } else {
                                    ServicesSingleton.instance().afterSignIn(null, null);
                                    if (mSignInCallbacks != null)
                                        mSignInCallbacks.signedIn(false);
                                }
                            } catch (JSONException e) {
                                Log.e(TAG + "signInRequest.onResponse", response.toString(), e);
                                ServicesSingleton.instance().afterSignIn(null, null);
                                if (mSignInCallbacks != null)
                                    mSignInCallbacks.signedIn(false);
                            }
                        }
                    }
                    ,getSessionId()
            );
        } catch (JSONException e) {
            Log.e(TAG, "signInRequest.onResponse", e);
        }
        mRequestQueue.add(request);
    }

    public void registerRequest(final Buyer requestBuyer) {
        JSONObject jsonRequest = null;
        try {
            jsonRequest = new JSONObject(mJsonObjectMapper.writeValueAsString(requestBuyer));
            Log.v(TAG ,".registerRequest" + jsonRequest.toString());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            Log.v(TAG ,".registerRequest Error" + e.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.v(TAG ,".registerRequest Error" + e.toString());
        }
        Log.v(TAG ,".registerRequest" + jsonRequest.toString());

        JsonObjectRequest request = new AuthenticatedJsonRequest(
                getRequestUrl(RequestType.REGISTER_BUYER, -1) , // String url
                jsonRequest, // JSONObject jsonRequest
                new AuthenticatedJsonRequest.Listener() {
                    @Override
                    public void onResponse(ResponseError error, JSONObject response) {
                        Log.v(TAG + ".onResponse", response.toString());

                        ResponseError responseError = ResponseError.UNKNOWN_ERROR;
                        try {
                            Log.v(TAG + ".onResponseInsideTry", response.toString());
                            Buyer responseBuyer = mJsonObjectMapper.readValue(response.toString(), Buyer.class);
                            //response value is stored in Buyer object
                            Log.v(TAG + ".onResponseFromJson", responseBuyer.toString());
                            responseError = ResponseError.NO_ERROR;
                            ServicesSingleton.instance().afterSignIn(responseBuyer, response.getString("api_key"));
                            Log.v(TAG + ".onResponse", response.getString("api_key"));
                        } catch (JsonMappingException e) {
                            try {
                                if (API_ERROR_ALREADY_TAKEN.equals(response.getJSONArray("phone").getString(0)))
                                    responseError = ResponseError.PHONE_EXISTS;
                            } catch (JSONException e1) {
                                Log.v(TAG ,".onResponse Error"+ response.toString()+ " " +e);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.v(TAG ,".onResponse Error"+ response.toString()+ " " +e);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.v(TAG ,".onResponse Error"+ response.toString()+ " " +e);
                        }
                        if (mRegistrationCallback != null)
                            mRegistrationCallback.onRegistration(responseError);
                    }
                }
                // Listener<JSONObject> listener: since jsonRequest is not null, method defaults to POST
                /*new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v(TAG + ".onResponse", response.toString());

                        ResponseError responseError = ResponseError.UNKNOWN_ERROR;
                        try {
                            Log.v(TAG + ".onResponseInsideTry", response.toString());
                            Buyer responseBuyer = mJsonObjectMapper.readValue(response.toString(), Buyer.class);
                            //response value is stored in Buyer object
                            Log.v(TAG + ".onResponseFromJson", responseBuyer.toString());
                            responseError = ResponseError.NO_ERROR;
                            ServicesSingleton.instance().afterSignIn(responseBuyer, response.getString("api_key"));
                            Log.v(TAG + ".onResponse", response.getString("api_key"));
                        } catch (JsonMappingException e) {
                            try {
                                if (API_ERROR_ALREADY_TAKEN.equals(response.getJSONArray("phone").getString(0)))
                                    responseError = ResponseError.PHONE_EXISTS;
                            } catch (JSONException e1) {
                                Log.v(TAG ,".onResponse Error"+ response.toString()+ " " +e);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.v(TAG ,".onResponse Error"+ response.toString()+ " " +e);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.v(TAG ,".onResponse Error"+ response.toString()+ " " +e);
                        }
                        if (mRegistrationCallback != null)
                            mRegistrationCallback.onRegistration(responseError);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error)  {
*//*
                        if (mRegistrationCallback != null)
                            mRegistrationCallback.onRegistration(RegistrationCallback.Result.UNKNOWN_ERROR);
*//*
                        Log.v(TAG, "onErrorResponse error : " + String.valueOf(getError(error)));
                        if (mRegistrationCallback != null ) {
                            Log.v(TAG, "onErrorResponse error : " + String.valueOf(getError(error)));
                            mRegistrationCallback.onRegistration(ResponseError.UNKNOWN_ERROR);
                        }
                    }
                }*/, getSessionId());
/*{
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map headers = new HashMap();
                headers.put("Session-Id", getSessionId());
                return headers;
            }
        }; // ErrorListener*/

        mRequestQueue.add(request);

    }

    public void buyerExistsRequest(String phone) {
        Log.e(TAG, ".buyerExistsRequest");
        try {
            JsonObjectRequest request = new AuthenticatedJsonRequest(
                    getRequestUrl(RequestType.BUYER_EXISTS, -1),
                    new JSONObject().put("phone", phone),
                    new AuthenticatedJsonRequest.Listener() {
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
                    /*new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.v(TAG, ".buyerExistsRequest response: " + response);
                                mRegistrationCallback.phoneExists(response.getBoolean("exists"));
                            } catch (JSONException e) {
                                Log.e(TAG, ".buyerExistsRequest error: ", e);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, ".buyerExistsRequest error: ", error);
                        }
                    }*/,getSessionId()
            );
            mRequestQueue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getProductCategoriesRequest() {
        final JsonObjectRequest request = new AuthenticatedJsonRequest(
                getRequestUrl(RequestType.GET_PRODUCT_CATEGORIES, -1),
                null,
                new AuthenticatedJsonRequest.Listener() {
                    @Override
                    public void onResponse(ResponseError error, JSONObject response) {
                        Log.v(TAG, "ProductCategories response:" + response.toString());
                        boolean updated = DataManager.instance().updateProductCategories(response);
                        if (mQuoteCallbacks != null && updated)
                            mQuoteCallbacks.productCategoriesUpdated(DataManager.instance().getProductCategories(false));
                    }
                }
                /*new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v(TAG, "ProductCategories response:" + response.toString());
                        boolean updated = DataManager.instance().updateProductCategories(response);
                        if (mQuoteCallbacks != null && updated)
                            mQuoteCallbacks.productCategoriesUpdated(DataManager.instance().getProductCategories(false));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, ".getProductCategoriesRequest network response: " + error.networkResponse, error);
                    }
                }*/,getSessionId()
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
        StringRequest request = new StringRequest(
                Request.Method.PUT,
                getRequestUrl(RequestType.PATCH_QUOTATION_STATUS, quotationId),
                requestData,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, ".setQuotationStatusReadRequest networkResponse: " + error.networkResponse, error);
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
            SERVER_URL = " LOCAL_SERVER_URL";
//        final String SERVER_URL = "http://10.42.0.1:3000/";
//        final String SERVER_URL = "http://192.168.1.15:3000/";
        final String API_VERSION = "v1/";
        String url = SERVER_URL + API_VERSION;
        switch (requestType) {
            case REGISTER_BUYER:
                Log.v(TAG,"SessionId in getUrl"+ getSessionId());
                return url + "buyers";
            case SIGN_IN:
                return url + "buyers/sign_in";
            case BUYER_EXISTS:
                return url + "buyers/exists";
            case GET_QUOTES:
                return url + "buyers/quotes/for_buyer";
            case GET_QUOTATIONS:
                return url + "buyers/quotations/for_buyer";
            case SEND_QUOTE:
                return url + "buyers/quotes";
            case GET_SELLERS:
                return url + "sellers";
            case GET_PRODUCT_CATEGORIES:
                return url + "buyers/brands_categories";
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

    public enum ResponseError {
        NO_ERROR,
        PHONE_EXISTS,
        AUTHENTICATION_ERROR,
        TIME_OUT,
        INCORRECT_GCM_ID,
        UNKNOWN_ERROR,
        NETWORK_RESPONSE_NULL,
        INCORRECT_SESSION_ID,
        NO_SESSION_ID

    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) mApplication.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
//        if (showToast)
//            Toast.makeText(mApplication, "you are not connected to the internet", Toast.LENGTH_LONG).show();
        return false;
    }

    /**
     * ** Volley **
     */
    @Override
    public void onErrorResponse(VolleyError error) {
        Log.e(TAG + ".onErrorResponse", "", error);
    }

    public ResponseError getError(VolleyError error) {
        NetworkRequestsManager.this.onErrorResponse(error);
        NetworkResponse networkResponse = error.networkResponse;
        String jsonString="";
        if (networkResponse != null) {
            try {
                jsonString = new String(networkResponse.data, HttpHeaderParser.parseCharset(networkResponse.headers));
                Log.v(TAG , ".onErrorResponse body :"+jsonString);
                Log.v(TAG , ".onErrorResponse code :"+networkResponse.statusCode);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            switch (networkResponse.statusCode) {
                case 401:
                    Log.v(TAG, "401 error : " + jsonString);
                    if(!jsonString.isEmpty() && jsonString.contains("incorrect session_id"))
                        Log.v(TAG , ".onErrorResponse code inside if :"+networkResponse.statusCode);

                    return ResponseError.AUTHENTICATION_ERROR;

                case 502:
                    Log.v(TAG, "502 error : " + jsonString);
                    return ResponseError.TIME_OUT;

                default:
                    Log.v(TAG, "unknown error : " + jsonString);
                    return ResponseError.UNKNOWN_ERROR;

            }
        }
        else
            return ResponseError.NETWORK_RESPONSE_NULL;

    }

    public void sendDeviceRegisterRequest(Device device) {
        Log.v(TAG,"device object"+device);
        JSONObject jsonRequest = null;
        Log.v(TAG, "Device object  " + device.toString());
        try {
            jsonRequest = new JSONObject(mJsonObjectMapper.writeValueAsString(device));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.v(TAG + ".DeviceId in response", jsonRequest.toString());

        JsonObjectRequest request = new JsonObjectRequest(getRequestUrl(RequestType.REGISTER_DEVICE,-1),
                jsonRequest,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v(TAG + "check deviceid response json: ",response.toString());
                        ResponseError error = ResponseError.UNKNOWN_ERROR;
                        try{
                            Device responseDevice = mJsonObjectMapper.readValue(response.toString(), Device.class);
                            Log.v(TAG + "check deviceid device: ",responseDevice.toString());
                            String session_id = responseDevice.getSession_id();
                            storeSessionId(session_id);
                            if(session_id ==null || session_id.isEmpty())
                                error = ResponseError.NO_SESSION_ID;
                        } catch (JsonMappingException e) {
                            e.printStackTrace();
                            Log.v(TAG + "check deviceid error ",e.toString());
                        } catch (JsonParseException e) {
                            e.printStackTrace();
                            Log.v(TAG + "check deviceid error ",e.toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.v(TAG + "check deviceid error ",e.toString());
                        }
                        if(mSessionIdCallback !=null)
                            mSessionIdCallback.onSessionResponse(error);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v(TAG + "check deviceid error ",error.toString());
                        NetworkRequestsManager.this.onErrorResponse(error);
                    }
                }
        );

        mRequestQueue.add(request);

    }

    private void storeSessionId(String sesId) {
//        Context context = getApplicationContext();
        PreferenceManager.getDefaultSharedPreferences(mApplication).edit()
                .putString(SESSION_ID,sesId).commit();

 /*       final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.v(TAG, "Saving sesId on app version: "+appVersion +" sesid : " + sesId);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_SESSION_ID, sesId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.apply();
  */  }

    private String getSessionId(){
        String sessionId = PreferenceManager.getDefaultSharedPreferences(mApplication)
                .getString(SESSION_ID, "");
        Log.v(TAG, "Session id (mSesid)"+sessionId);
        return sessionId;
    }


}
