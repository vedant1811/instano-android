package com.instano.retailer.instano.application;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.instano.retailer.instano.BuildConfig;
import com.instano.retailer.instano.utilities.library.JsonArrayRequest;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.library.StringRequest;
import com.instano.retailer.instano.utilities.models.Buyer;
import com.instano.retailer.instano.utilities.models.ProductCategories;
import com.instano.retailer.instano.utilities.models.Quote;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by vedant on 18/12/14.
 */
public class NetworkRequestsManager implements Response.ErrorListener{

    private static final String TAG = "NetworkRequestsManager";
    private static final String LOCAL_SERVER_URL = "http://192.168.1.13:3000/";

    private final static String API_ERROR_ALREADY_TAKEN = "has already been taken";
    private final static String API_ERROR_IS_BLANK = "can't be blank";

    private static NetworkRequestsManager sInstance;

    private final MyApplication mApplication;

    private QuoteCallbacks mQuoteCallbacks;
    private RegistrationCallback mRegistrationCallback;
    private RequestQueue mRequestQueue;

    public void registerCallback (QuoteCallbacks quoteCallbacks) {
        mQuoteCallbacks = quoteCallbacks;
    }

    public void registerCallback(RegistrationCallback registrationCallback) {
        this.mRegistrationCallback = registrationCallback;
    }

    public interface QuoteCallbacks {
        public void productCategoriesUpdated(List<ProductCategories.Category> productCategories);
        public void onQuoteSent(boolean success);
    }

    public interface RegistrationCallback {
        enum Result {
            NO_ERROR,
            PHONE_EXISTS,
            UNKNOWN_ERROR
        }
        public void phoneExists (boolean exists);
        public void onRegistration(Result result);
    }

    private NetworkRequestsManager(MyApplication application) {
        this.mApplication = application;
        mRequestQueue = Volley.newRequestQueue(application);
    }

    /*package*/ static void init(MyApplication application) {
        sInstance = new NetworkRequestsManager(application);
    }

    public static NetworkRequestsManager instance() {
        if (sInstance == null)
            throw new IllegalStateException("NetworkRequestsManager.init() not called");

        return sInstance;
    }

    public void getQuotationsRequest (@NonNull Buyer buyer) {

        JSONObject postData;
        try {
            postData = new JSONObject()
                    .put("id", buyer.id);
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
        );
        mRequestQueue.add(request);
    }

    public void getQuotesRequest (@NonNull final Buyer buyer) {

        JSONObject requestData;
        try {
            requestData = new JSONObject()
                    .put("id", buyer.id);
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
        );
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
        );
        mRequestQueue.add(request);
    }

    public void sendQuoteRequest(@NonNull Quote quote) {
        Log.v(TAG, "sendQuoteRequest request: " + quote.toJsonObject());

        JsonObjectRequest request = new JsonObjectRequest(
                getRequestUrl(RequestType.SEND_QUOTE, -1),
                quote.toJsonObject(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v (TAG, response.toString());
                        if (mQuoteCallbacks != null)
                            mQuoteCallbacks.onQuoteSent(true);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (mQuoteCallbacks != null)
                            mQuoteCallbacks.onQuoteSent(false);
                        NetworkRequestsManager.this.onErrorResponse(error);
                    }
                }
        );
        mRequestQueue.add(request);
    }

    public void signInRequest(@NonNull final String apiKey, @NonNull final ServicesSingleton.SignInCallbacks callback) {

        JsonObjectRequest request = null;
        try {
            JSONObject postData = new JSONObject().put("api_key", apiKey);
            Log.v(TAG, "sign in request: " + postData.toString());
            request = new JsonObjectRequest(
                    getRequestUrl(RequestType.SIGN_IN, -1),
                    postData,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG + "signInRequest.onResponse", response.toString());
                            try {
                                if (response.getInt("id") != -1) {
                                    Buyer buyer = new Buyer(response);
                                    ServicesSingleton.instance().afterSignIn(buyer, response.getString("api_key"));
                                    callback.signedIn(true);
                                }
                                else {
                                    ServicesSingleton.instance().afterSignIn(null, null);
                                    callback.signedIn(false);
                                }
                            } catch (JSONException e) {
                                Log.e(TAG + "signInRequest.onResponse", response.toString(), e);
                                ServicesSingleton.instance().afterSignIn(null, null);
                                callback.signedIn(false);
                            }
                        }
                    },
                    this
            );
        } catch (JSONException e) {
            Log.e(TAG, "signInRequest.onResponse", e);
        }
        mRequestQueue.add(request);
    }

    public void registerRequest(final Buyer buyer) {

        try {
            JSONObject jsonRequest = buyer.toJsonObject();
            Log.v(TAG + ".registerRequest", jsonRequest.toString(4));
            JsonObjectRequest request = new JsonObjectRequest(
                    getRequestUrl(RequestType.REGISTER_BUYER, -1), // String url
                    jsonRequest, // JSONObject jsonRequest
                    // Listener<JSONObject> listener: since jsonRequest is not null, method defaults to POST
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.v(TAG + ".onResponse", response.toString());

                            RegistrationCallback.Result result = RegistrationCallback.Result.UNKNOWN_ERROR;
                            try {
                                Buyer buyer = new Buyer(response);
                                result = RegistrationCallback.Result.NO_ERROR;
                                ServicesSingleton.instance().afterSignIn(buyer, response.getString("api_key"));
                            } catch (JSONException e) {
                                try {
                                    if (API_ERROR_ALREADY_TAKEN.equals(response.getJSONArray("phone").getString(0)))
                                        result = RegistrationCallback.Result.PHONE_EXISTS;
                                } catch (JSONException e1) {
                                    Log.e (TAG + ".onResponse", response.toString(), e);
                                }
                            }
                            if (mRegistrationCallback != null)
                                mRegistrationCallback.onRegistration(result);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (mRegistrationCallback != null)
                                mRegistrationCallback.onRegistration(RegistrationCallback.Result.UNKNOWN_ERROR);
                            NetworkRequestsManager.this.onErrorResponse(error);
                        }
                    }); // ErrorListener

            mRequestQueue.add(request);
        } catch (JSONException e) {
            Log.e(TAG, "RegisterRequest exception", e);
        }
    }

    public void buyerExistsRequest(String phone) {
        Log.e(TAG, ".buyerExistsRequest");
        try {
            JsonObjectRequest request = new JsonObjectRequest(
                    getRequestUrl(RequestType.BUYER_EXISTS, -1),
                    new JSONObject().put("phone", phone),
                    new Response.Listener<JSONObject>() {
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
                    }
            );
            mRequestQueue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getProductCategoriesRequest() {
        final JsonObjectRequest request = new JsonObjectRequest(
                getRequestUrl(RequestType.GET_PRODUCT_CATEGORIES, -1),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v(TAG, "ProductCategories response:" + response.toString());
                        boolean updated = DataManager.instance().updateProductCategories(response);
                        if (mQuoteCallbacks != null && updated)
                            mQuoteCallbacks.productCategoriesUpdated(DataManager.instance().getProductCategories());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, ".getProductCategoriesRequest network response: " + error.networkResponse, error);
                    }
                }
        );
        mRequestQueue.add(request);
    }

    public void setQuotationStatusReadRequest (int quotationId) {
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
        );
        mRequestQueue.add(request);
    }

    /**
     *
     * @param requestType
     * @param id The id to be used for specific URLs. unused for others
     * @return the complete URL to be sent
     */
    private static String getRequestUrl(RequestType requestType, int id) {

        String SERVER_URL;
        if (BuildConfig.DEBUG)
            SERVER_URL = LOCAL_SERVER_URL;
        else
            SERVER_URL = "http://instano.in/";
//        final String SERVER_URL = "http://10.42.0.1:3000/";
//        final String SERVER_URL = "http://192.168.1.15:3000/";
        final String API_VERSION = "v1/";
        String url = SERVER_URL + API_VERSION;
        switch (requestType) {
            case REGISTER_BUYER:
                return url + "buyers";
            case SIGN_IN:
                return url + "buyers/sign_in";
            case BUYER_EXISTS:
                return url + "buyers/exists";
            case GET_QUOTES:
                return url + "quotes/for_buyer";
            case GET_QUOTATIONS:
                return url + "quotations/for_buyer";
            case SEND_QUOTE:
                return url + "quotes";
            case GET_SELLERS:
                return url + "sellers";
            case GET_PRODUCT_CATEGORIES:
                return url + "brands_categories";

            case PATCH_QUOTATION_STATUS:
                return url + "quotations/" + id;
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

        BUYER_EXISTS,
        PATCH_QUOTATION_STATUS
    }

    /**
     *
     * @param showToast if true, this method shows a toast if the app is not connected
     * @return
     */
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
}
