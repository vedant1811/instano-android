package com.instano.retailer.instano;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.instano.retailer.instano.buyerDashboard.QuotationListActivity;
import com.instano.retailer.instano.utilities.JsonArrayRequest;
import com.instano.retailer.instano.utilities.PeriodicWorker;
import com.instano.retailer.instano.utilities.ProductCategories;
import com.instano.retailer.instano.utilities.Quotation;
import com.instano.retailer.instano.utilities.Quote;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 *
 *
 * Created by vedant on 3/9/14.
 */
public class ServicesSingleton implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        Response.Listener<String>, Response.ErrorListener {

    private final static String TAG = "ServicesSingleton";

    private final static String KEY_BUYER_API_KEY = "com.instano.retailer.instano.ServicesSingleton.buyer_api_key";

    public static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;

    private static ServicesSingleton sInstance;

    private final Context mAppContext;
    private SharedPreferences mSharedPreferences;


    /* location variables */
    private LocationClient mLocationClient;
    private Location mLastLocation;
    private Address mLatestAddress;
    private String locationErrorString;
    private InitialDataCallbacks mInitialDataCallbacks;

    /* network variables */
    private Quote mQuote;
    private QuoteCallbacks mQuoteCallbacks;
    private int mBuyerId;

    private RequestQueue mRequestQueue;
    private QuotationsArrayAdapter mQuotationsArrayAdapter;
    private SellersArrayAdapter mSellersArrayAdapter;
    private ProductCategories mProductCategories;
    private PeriodicWorker mPeriodicWorker;

    public void signInRequest() {
//        if (newBuyer)
//            sendSignInRequest("create");
//        else
        sendSignInRequest(mSharedPreferences.getString(KEY_BUYER_API_KEY, "create"));
    }

    private void postSignIn() {
        Log.d(TAG, "buyer ID: " + mBuyerId);
        Toast.makeText(mAppContext, String.format("you are %d user to sign in", mBuyerId), Toast.LENGTH_SHORT).show();
        mQuotationsArrayAdapter.clear();
    }

    public void runPeriodicTasks() {
        if (mBuyerId != -1) // i.e. if user is signed in
            getQuotationsRequest();
//        else
//            signInRequest(false);

        if (getProductCategories() == null)
            getProductCategoriesRequest();

    }

    public void createNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mAppContext)
                .setSmallIcon(R.drawable.instano_launcher)
                .setContentTitle("New Quotations")
                .setContentText("Click to view your new quotations");

        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                mAppContext,
                0,
                new Intent(mAppContext, QuotationListActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        mBuilder.setContentIntent(resultPendingIntent);

    }

    public void getQuotationsRequest () {
        if (mBuyerId == -1){
            Log.e(TAG, "getQuotationsRequest: buyer id == -1");
            return;
        }

        JSONObject postData;
        try {
             postData = new JSONObject()
                    .put("id", mBuyerId);
        } catch (JSONException e) {
            Log.e(TAG, "getQuotationsRequest", e);
            return;
        }

        // first get sellers so that we enter only quotations with valid sellers
        getSellersRequest();
        JsonArrayRequest request = new JsonArrayRequest(
                getRequestUrl(RequestType.GET_QUOTATIONS),
                postData,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.v(TAG, "Quotations response:" + response.toString());
                        try {
                            boolean dataChanged = false;
                            for (int i = 0; i < response.length(); i++){
                                JSONObject quotationJsonObject = response.getJSONObject(i);
                                try {
                                    // TODO: create a notification based on return value
                                    dataChanged |= mQuotationsArrayAdapter.insertIfNeeded(new Quotation(quotationJsonObject));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (dataChanged)
                                Log.d(TAG, "new quotations received");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                this
        );
        mRequestQueue.add(request);
    }

    public void getSellersRequest() {
        StringRequest request = new StringRequest(
                getRequestUrl(RequestType.GET_SELLERS),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.v(TAG, "Sellers response:" + response.toString());
                        try {
                            JSONArray quotesJsonArray = new JSONArray(response);
                            mSellersArrayAdapter.addAll(quotesJsonArray);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                this
        );
        mRequestQueue.add(request);
    }

    public void sendQuoteRequest(String searchString, String brands, String priceRange,
                                 String productCategory, ArrayList<Integer> sellerIds) {

        if (mBuyerId == -1) {
            Log.e(TAG, ".sendQuoteRequest : mBuyerId is -1. Search string: " + searchString);
            return;
        }

        Quote quote =  new Quote(this, mBuyerId, searchString, brands, priceRange, productCategory, sellerIds);
        Log.d(TAG, "sendQuoteRequest request: " + quote.toJsonObject());

        JsonObjectRequest request = new JsonObjectRequest(
                getRequestUrl(RequestType.SEND_QUOTE),
                quote.toJsonObject(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v (TAG, response.toString());
                        if (mQuoteCallbacks != null)
                            mQuoteCallbacks.quoteSent(true);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (mQuoteCallbacks != null)
                            mQuoteCallbacks.quoteSent(false);
                    }
                }
        );
        mRequestQueue.add(request);
    }

    public void sendSignInRequest(String apiKey) {

        JsonObjectRequest request = null;
        try {
            Log.d(TAG, "sign in request: " + new JSONObject().toString());
            JSONObject apiKeyJson = new JSONObject().put("api_key", apiKey);
            JSONObject postData = new JSONObject().put("buyer", apiKeyJson);
            request = new JsonObjectRequest(
                    getRequestUrl(RequestType.SIGN_IN),
                    postData,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                mBuyerId = response.getInt("id");

                                SharedPreferences.Editor editor = mSharedPreferences.edit();
                                editor.putString(KEY_BUYER_API_KEY, response.getString("api_key"));
                                editor.apply();

                                postSignIn();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                mBuyerId = -1;
                                // try again
                                signInRequest();
                            }
                        }
                    },
                    this
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mRequestQueue.add(request);
    }

    public void getProductCategoriesRequest() {
        final JsonObjectRequest request = new JsonObjectRequest(
                getRequestUrl(RequestType.GET_PRODUCT_CATEGORIES),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v(TAG, "ProductCategories response:" + response.toString());
                        mProductCategories = new ProductCategories(response, true);
                        if (mQuoteCallbacks != null)
                            mQuoteCallbacks.productCategoriesUpdated(getProductCategories());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, ".getProductCategoriesRequest error: ", error);
                    }
                }
        );
        mRequestQueue.add(request);
    }

    public ArrayList<ProductCategories.Category> getProductCategories() {
        if (mProductCategories != null)
            return mProductCategories.getProductCategories();
        else
            return null;
    }

    private String getRequestUrl(RequestType requestType) {

        final String SERVER_URL = "http://instano.in/";
//        final String SERVER_URL = "http://10.42.0.1:3000/";
//        final String SERVER_URL = "http://192.168.43.81:3000/";
        final String API_VERSION = "v1/";
        String url = SERVER_URL + API_VERSION;
        switch (requestType) {
//            case REGISTER:
//                return url + "sellers";
            case SIGN_IN:
                return url + "buyers";
            case GET_QUOTATIONS:
                return url + "quotations/for_buyer";
            case SEND_QUOTE:
                return url + "quotes";
            case GET_SELLERS:
                return url + "sellers";
            case GET_PRODUCT_CATEGORIES:
                return url + "brands_categories";
        }

        throw new IllegalArgumentException();
    }

    private ServicesSingleton(Activity startingActivity) {
        mAppContext = startingActivity.getApplicationContext();
        mSharedPreferences = mAppContext.getSharedPreferences(
                "com.instano.SHARED_PREFERENCES_FILE", Context.MODE_PRIVATE);
        mLatestAddress = null;
        mLastLocation = null;
        mBuyerId = -1;
        mProductCategories = null;

        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        mLocationClient = new LocationClient(mAppContext, this, this);
        mLocationClient.connect();
        checkPlayServices(); // not performing checkUserAccount
        // see http://www.androiddesignpatterns.com/2013/01/google-play-services-setup.html



        mRequestQueue = Volley.newRequestQueue(mAppContext);

        mQuotationsArrayAdapter = new QuotationsArrayAdapter(startingActivity);
        mSellersArrayAdapter = new SellersArrayAdapter(startingActivity);

        mPeriodicWorker = new PeriodicWorker(this);
        mPeriodicWorker.start();
        signInRequest();
    }

    public static ServicesSingleton getInstance(Activity startingActivity) {

        if(sInstance == null)
            sInstance = new ServicesSingleton(startingActivity);

        return sInstance;
    }

    public boolean checkPlayServices() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mAppContext);
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

    public void searchAddress(Location location) {
        // Ensure that a Geocoder services is available and a locationCallback is registered
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Geocoder.isPresent()) {
            // Show the activity indicator
            if (mInitialDataCallbacks != null)
                mInitialDataCallbacks.searchingForAddress();
            /*
             * Reverse geocoding is long-running and synchronous.
             * Run it on a background thread.
             * Pass the current location to the background task.
             * When the task finishes,
             * onPostExecute() displays the address.
             */
            (new GetAddressTask(mAppContext)).execute(location);
        }
    }

    public String getLocationErrorString() {
        return locationErrorString;
    }

    public Address getLatestAddress() {
        return mLatestAddress;
    }

    public SellersArrayAdapter getSellersArrayAdapter() {
        return mSellersArrayAdapter;
    }

    public Location getLastLocation() {
        return mLastLocation;
    }

    /**
     * A subclass of AsyncTask that calls getFromLocation() in the
     * background. The class definition has these generic types:
     * Location - A Location object containing
     * the current location.
     * Void     - indicates that progress units are not used
     * String   - An address passed to onPostExecute()
     */
    private class GetAddressTask extends
            AsyncTask<Location, Void, Address> {
        Context mContext;
        public GetAddressTask(Context context) {
            super();
            mContext = context;
        }

        /**
         * Get a Geocoder instance, get the latitude and longitude
         * look up the address, and return it
         *
         * @params params One or more Location objects
         * @return A string containing the address of the current
         * location, or an empty string if no address can be found,
         * or an error message
         */
        @Override
        protected Address doInBackground(Location... params) {
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
            // Get the current location from the input parameter list
            Location loc = params[0];
            // Create a list to contain the result address
            List<Address> addresses = null;
            try {
                /*
                 * Return 1 address.
                 */
                addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
            } catch (IOException e1) {
                Log.e("LocationSampleActivity", "IO Exception in getFromLocation()");
                e1.printStackTrace();
                return null;
            } catch (IllegalArgumentException e2) {
                // Error message to post in the log
                String errorString = "Illegal arguments " + Double.toString(loc.getLatitude()) +
                        " , " + Double.toString(loc.getLongitude()) + " passed to address service";
                Log.e("LocationSampleActivity", errorString);
                e2.printStackTrace();
                return null;
            }
            // If the reverse geocode returned an address
            if (addresses != null && addresses.size() > 0) {
                // Get the first address
                return addresses.get(0);
                /*
                 * Format the first line of address (if available),
                 * city, and country name.
                 */
//                String addressText = String.format( "%s, %s, %s",
//                        // If there's a street address, add it
//                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
//                        // Locality is usually a city
//                        address.getLocality(),
//                        // The country of the address
//                        address.getCountryName());
//                // Return the text
//                return addressText;
            } else {
                return null;
            }
        }
        /**
         * A method that's called once doInBackground() completes. Turn
         * off the indeterminate activity indicator and set
         * the text of the UI element that shows the address. If the
         * lookup failed, display the error message.
         */
        @Override
        protected void onPostExecute(Address address) {
            mLatestAddress = address;
            if (mInitialDataCallbacks != null)
                mInitialDataCallbacks.addressFound(address);
        }
    }

    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        mLastLocation = mLocationClient.getLastLocation();
        if (mLastLocation != null)
            searchAddress(mLastLocation);
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

    /**
     * ** Volley **
     *
     * BuyersCallbacks method that an error has been occurred with the
     * provided error code and optional user-readable message.
     */
    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d(TAG + ".onErrorResponse", "", error);

    }

    /**
     * ** Volley **
     *
     * Called when a response is received.
     */
    @Override
    public void onResponse(String response) {
        Log.d(TAG + ".onResponse", response);



        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(new StringReader(response));
        } catch (XmlPullParserException e) {
            Log.d(TAG + ".onResponse", "XmlPullParser ERROR", e);
        }
    }

    public QuotationsArrayAdapter getQuotationArrayAdapter() {
        return mQuotationsArrayAdapter;
    }

    public void registerCallback (InitialDataCallbacks initialDataCallbacks) {
        this.mInitialDataCallbacks = initialDataCallbacks;
    }

    public void registerCallback (QuoteCallbacks quoteCallbacks) {
        mQuoteCallbacks = quoteCallbacks;
    }

    public interface QuoteCallbacks {
        public void productCategoriesUpdated(ArrayList<ProductCategories.Category> productCategories);
        public void quoteSent(boolean success);
    }

    public interface InitialDataCallbacks {
        public void searchingForAddress();
        public void addressFound(Address address);
        /*
         * Implementation activities need to listen for result of the errorDialog in Activity.onActivityResult
         */
        public void showErrorDialog(int errorCode);
        public void resolvableConnectionResultError(ConnectionResult connectionResult) throws IntentSender.SendIntentException;
    }


    private enum RequestType {
        SIGN_IN,
        GET_QUOTATIONS,
        SEND_QUOTE,
        GET_PRODUCT_CATEGORIES,
        GET_SELLERS
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) mAppContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    /**
     *
     * @return Human readable time elapsed. Eg: "42 minutes ago"
     */
    public String getPrettyTimeElapsed(long updatedAt) {
        String dateTimeString = (String) DateUtils.getRelativeDateTimeString(mAppContext, updatedAt,
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
