package com.instano.retailer.instano;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.PointF;
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
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
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


    private final static String SERVER_URL = "http://ec2-54-68-27-25.us-west-2.compute.amazonaws.com/";
//    private final static String SERVER_URL = "http://10.42.0.1:3000/";
//    private final static String SERVER_URL = "http://192.168.1.17:3000/";
//    private final static String SERVER_URL = "http://192.168.0.24:3000/";
    private final static String API_VERSION = "v1/";
    private final static String KEY_BUYER_ID = "com.instano.retailer.instano.ServicesSingleton.buyer_id";

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
    private ArrayList<String> mProductCategories;
    private PeriodicWorker mPeriodicWorker;

    public boolean signInRequest() {
        mBuyerId = mSharedPreferences.getInt(KEY_BUYER_ID, -1);
        if (mBuyerId != -1) {
            postSignIn();
            return true;
        }
        else {
            sendSignInRequest();
            return false;
        }
    }

    private void postSignIn() {
        Log.d(TAG, "buyer ID: " + mBuyerId);
        mPeriodicWorker = new PeriodicWorker();
        mPeriodicWorker.start();
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
                            // TODO: change creating a new list everytime
                            mSellersArrayAdapter.clear();
                            for (int i = 0; i < quotesJsonArray.length(); i++){
                                JSONObject quotationJsonObject = quotesJsonArray.getJSONObject(i);
                                try {
                                    mSellersArrayAdapter.add(new Seller(quotationJsonObject));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            mSellersArrayAdapter.filer();
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
                                 int productCategory, ArrayList<Integer> sellerIds) {

        if (mBuyerId == -1) {
            Log.e(TAG, ".sendQuoteRequest : mBuyerId is -1. Search string: " + searchString);
            return;
        }

        Quote quote =  new Quote(mBuyerId, searchString, brands, priceRange, productCategory, sellerIds);
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

    public void sendSignInRequest() {

        Log.d(TAG, "sign in request: " + new JSONObject().toString());

        JsonObjectRequest request = new JsonObjectRequest(
                getRequestUrl(RequestType.SIGN_IN),
                new JSONObject(), // sending an empty but valid JSON object
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            mBuyerId = response.getInt("id");

                            SharedPreferences.Editor editor = mSharedPreferences.edit();
                            editor.putInt(KEY_BUYER_ID, mBuyerId);
                            editor.apply();

                            postSignIn();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            mBuyerId = -1;
                        }
                    }
                },
                this
        );
        mRequestQueue.add(request);
    }

    public void getProductCategoriesRequest() {
        Log.v(TAG, "get ProductCategories");
        final JsonObjectRequest request = new JsonObjectRequest(
                getRequestUrl(RequestType.GET_PRODUCT_CATEGORIES),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v(TAG, "ProductCategories response:" + response.toString());
                        String[] strings = new String[response.length()];

                        Iterator<String> stringIterator = response.keys();
                        while (stringIterator.hasNext()) {
                            try {
                                String key = stringIterator.next();
                                int value = response.getInt(key);
                                strings [value] = key;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        mProductCategories.clear();
                        mProductCategories.addAll(Arrays.asList(strings));
//                        mInitialDataCallbacks.productCategoriesUpdated(mProductCategories);
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

    public ArrayList<String> getProductCategories() {
        return mProductCategories;
    }

    private String getRequestUrl(RequestType requestType) {
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
                return url + "product_categories";
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
        mProductCategories = new ArrayList<String>();

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

        getProductCategoriesRequest();
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
        public void quoteSent(boolean success);
    }

    public interface InitialDataCallbacks {
//        public void productCategoriesUpdated(ArrayList<String> productCategories);
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

    /**
     * Represents a single immutable Quotation
     */
    public class Quotation {
        public final int id; // server generated
        public final String nameOfProduct; // TODO: in future probably make a generic `Product` class
        public final int price;
        public final String description;
        public final int sellerId;
        public final int quoteId; // the id of the quote being replied to
//        public final URL imageUrl; // can be null

        public Quotation(int id, String nameOfProduct, int price, String description, int sellerId, int quoteId) {
            this.id = id;
            this.nameOfProduct = nameOfProduct;
            this.price = price;
            this.description = description;
            this.sellerId = sellerId;
//            this.imageUrl = imageUrl;
            this.quoteId = quoteId; // the id of the quote being replied to
        }

        public Quotation(String nameOfProduct, int price, String description, int sellerId, int quoteId) {
            this(-1, nameOfProduct.trim(), price, description.trim(), sellerId, quoteId);
        }

        public Quotation(JSONObject quotationJsonObject) throws JSONException {
            id = quotationJsonObject.getInt("id");
            nameOfProduct = quotationJsonObject.getString("name_of_product");
            price = quotationJsonObject.getInt("price");
            String description = quotationJsonObject.getString("description");
            if (description.equalsIgnoreCase("null"))
                this.description = "";
            else
                this.description = description;
            sellerId = quotationJsonObject.getInt("seller_id");
            quoteId = quotationJsonObject.getInt("quote_id");
        }

        public String toChatString() {
            return nameOfProduct + "\n₹ " + price + "\n" + description;
        }

        public JSONObject toJsonObject() {
            try {
                JSONObject quotationParamsJsonObject = new JSONObject()
                        .put("name_of_product", nameOfProduct)
                        .put("price", price)
                        .put("description", description)
                        .put("seller_id", sellerId)
                        .put("quote_id", quoteId);

                if (id != -1)
                    quotationParamsJsonObject.put ("id", id);

                JSONObject quotationJsonObject = new JSONObject()
                        .put("quotation", quotationParamsJsonObject);

                return quotationJsonObject;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    /**
     * Represents a single immutable Seller
     */
    public class Seller {
        public final static double INVALID_COORDINATE = -1000; // an invalid coordinate

        public final int id; // server generated
        public final String nameOfShop;
        public final String nameOfSeller;
        public final String address; // newline separated
        public final double latitude;
        public final double longitude;
        public final String phone; // TODO: maybe make it a list of Strings
        public final int rating; // rating is out of 50, displayed out of 5.0
        public final String email;
        public final ArrayList<Integer> productCategories;

        public Seller(int id, String nameOfShop, String nameOfSeller, String address, double latitude, double longitude, String phone, int rating, String email, ArrayList<Integer> productCategories) {
            this.id = id;
            this.nameOfShop = nameOfShop;
            this.nameOfSeller = nameOfSeller;
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
            this.phone = phone;
            this.rating = rating;
            this.email = email;
            this.productCategories = productCategories;
        }

        /**
         * if id and rating are not available, they are set to invalid i.e. -1
         */
        public Seller(String nameOfShop, String nameOfSeller, String address, double latitude, double longitude, String phone, String email, ArrayList<Integer> productCategories) {
            this.id = -1;
            this.nameOfShop = nameOfShop.trim();
            this.nameOfSeller = nameOfSeller.trim();
            this.address = address.trim();
            this.latitude = latitude;
            this.longitude = longitude;
            this.phone = phone.trim();
            this.rating = -1;
            this.email = email.trim();
            this.productCategories = productCategories;
        }

        public Seller(JSONObject sellerJsonObject) throws JSONException {
            id = sellerJsonObject.getInt("id");
            nameOfShop = sellerJsonObject.getString("name_of_shop");
            nameOfSeller = sellerJsonObject.getString("name_of_seller");
            address = sellerJsonObject.getString("address");

            double latitude = INVALID_COORDINATE;
            double longitude = INVALID_COORDINATE;
            try {
                latitude = sellerJsonObject.getDouble("latitude");
                longitude = sellerJsonObject.getDouble("longitude");
            } catch (JSONException e) {
                latitude = INVALID_COORDINATE;
                longitude = INVALID_COORDINATE;
            } finally {
                this.latitude = latitude;
                this.longitude = longitude;
            }
            phone = sellerJsonObject.getString("phone");
            int rating;
            try {
                rating = Integer.parseInt(sellerJsonObject.getString("rating"));
            } catch (NumberFormatException e) {
                rating = -1;
            }
            this.rating = rating;
            email = sellerJsonObject.getString("email");

            // TODO:
            productCategories = new ArrayList<Integer>();
            JSONArray productCategoriesJsonArray = sellerJsonObject.getJSONArray("product_categories");
            for (int i = 0; i < productCategoriesJsonArray.length(); i++) {
                productCategories.add(productCategoriesJsonArray.getInt(i));
            }
        }

        public JSONObject toJsonObject() throws JSONException {
            JSONObject retailerParamsJsonObject = new JSONObject();
            retailerParamsJsonObject.put("name_of_shop", nameOfShop)
                    .put("name_of_seller", nameOfSeller)
                    .put("address", address)
                    .put("latitude", latitude)
                    .put("longitude", longitude)
                    .put("phone", phone)
                    .put("email", email);

            if (id != -1)
                retailerParamsJsonObject.put("id", id);
            if (rating != -1)
                retailerParamsJsonObject.put("rating", rating);

            JSONArray productCategoriesJsonArray = new JSONArray(productCategories);
            retailerParamsJsonObject.put("product_categories", productCategoriesJsonArray);

            JSONObject retailerJsonObject = new JSONObject();
            retailerJsonObject.put("seller", retailerParamsJsonObject);

            return retailerJsonObject;
        }

        // get distance between to two points given as latitude and longitude or null on error
        public String getPrettyDistanceFromLocation() {
            int distanceFromLocation = getDistanceFromLocation();
            if (distanceFromLocation == -1)
                return null;
            else
                return String.format("%.2f", distanceFromLocation /100.0) + " km";
        }

        // get distance between to two points in 10x meters or -1
        public int getDistanceFromLocation() {
            if (mLastLocation == null)
                return -1;

            PointF p1 = new PointF((float) mLastLocation.getLatitude(), (float) mLastLocation.getLongitude());
            PointF p2 = new PointF((float) latitude, (float) longitude);

            double R = 637100; // 10x meters
            double dLat = Math.toRadians(p2.x - p1.x);
            double dLon = Math.toRadians(p2.y - p1.y);
            double lat1 = Math.toRadians(p1.x);
            double lat2 = Math.toRadians(p2.x);

            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2)
                    * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double distance = R * c;
            return (int) distance;
        }
    }

    /**
     * Represents a single immutable quote request (that is received by the seller)
     */
    public class Quote {
        public final int id;
        public final int buyerId;
        public final String searchString;

        /**
         * comma separated brands eg: "LG, Samsung"
         * can be null
         */
        public final String brands;

        /**
         * human readable display for price
         * can be null
         */
        public final String priceRange;
        public final int productCategory;
        public final long updatedAt; // valid only when constructed from Quote(JSONObject jsonObject)
        public final ArrayList<Integer> sellerIds;

        public Quote(int id, int buyerId, String searchString, String brands, String priceRange, int productCategory, ArrayList<Integer> sellerIds) {
            this.id = id;
            this.buyerId = buyerId;
            this.searchString = searchString;
            this.brands = brands;
            this.priceRange = priceRange;
            this.productCategory = productCategory;
            this.sellerIds = sellerIds;
            updatedAt = 0;
        }

        public Quote(int buyerId, String searchString, String brands, String priceRange, int productCategory, ArrayList<Integer> sellerIds) {
            this.productCategory = productCategory;
            this.sellerIds = sellerIds;
            this.id = -1;
            this.buyerId = buyerId;
            this.searchString = searchString.trim();
            this.brands = brands.trim();
            this.priceRange = priceRange.trim();
            updatedAt = 0;
        }

        public Quote(JSONObject jsonObject) throws JSONException, ParseException {
            String updatedAt = jsonObject.getString("updated_at");
            this.updatedAt = dateFromString(updatedAt);
            id = jsonObject.getInt("id");
            buyerId = jsonObject.getInt("buyer_id");
            searchString = jsonObject.getString("search_string");
            brands = jsonObject.getString("brands");
            priceRange = jsonObject.getString("price_range");
            productCategory = jsonObject.getInt("product_category");
            sellerIds = null;
        }

        /**
         *
         * @return Human readable time elapsed. Eg: "42 minutes ago"
         */
        public String getPrettyTimeElapsed() {
            String dateTimeString = (String) DateUtils.getRelativeDateTimeString(mAppContext, updatedAt,
                    DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);
            return dateTimeString.split(",")[0];
        }

        public JSONObject toJsonObject() {
            try {
                JSONArray sellerIds = new JSONArray(this.sellerIds);

                JSONObject quoteParamsJsonObject = new JSONObject()
                        .put("buyer_id", buyerId)
                        .put("search_string", searchString)
                        .put("brands", brands)
                        .put("price_range", priceRange)
                        .put("product_category", productCategory)
                        .put("seller_ids", sellerIds);

                if (id != -1)
                    quoteParamsJsonObject.put ("id", id);

                JSONObject quoteJsonObject = new JSONObject()
                        .put("quote", quoteParamsJsonObject);
                return quoteJsonObject;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
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

    private static long dateFromString(String sDate) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = simpleDateFormat.parse(sDate);
        return date.getTime();
    }

}
