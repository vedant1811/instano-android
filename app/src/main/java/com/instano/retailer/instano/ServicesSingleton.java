package com.instano.retailer.instano;

import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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
import java.util.List;
import java.util.Locale;

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

    //    private final static String SERVER_URL = "http://ec2-54-68-27-25.us-west-2.compute.amazonaws.com/";
    private final static String SERVER_URL = "http://10.42.0.1:3000/";
    private final static String API_VERSION = "v1/";
    private final static String KEY_BUYER_ID = "com.instano.retailer.instano.ServicesSingleton.buyer_id";

    public static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;

    private static ServicesSingleton sInstance;

    private final Context mAppContext;
    private SharedPreferences mSharedPreferences;


    /* location variables */
    private LocationClient mLocationClient;
    private Address mLatestAddress;
    private String locationErrorString;
    private LocationCallbacks mLocationCallbacks;

    /* network variables */
    private Quote mQuote;
    private BuyersCallbacks mBuyersCallbacks;
    private int mBuyerId;

    private RequestQueue mRequestQueue;
    private QuotationsArrayAdapter mQuotationsArrayAdapter;
    private SellersArrayAdapter mSellersArrayAdapter;

    public boolean signInRequest() {
        mBuyerId = mSharedPreferences.getInt(KEY_BUYER_ID, -1);
        if (mBuyerId != -1) {
            getQuotationsRequest();
            return true;
        }
        else
            return false;
    }

    public void getQuotationsRequest () {
        StringRequest request = new StringRequest(
                getRequestUrl(RequestType.GET_QUOTATIONS),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.v(TAG, "Quotations response:" + response.toString());
                        try {
                            JSONArray quotesJsonArray = new JSONArray(response);
                            // TODO: change creating a new list everytime
                            mQuotationsArrayAdapter.clear();
                            for (int i = 0; i < quotesJsonArray.length(); i++){
                                JSONObject quotationJsonObject = quotesJsonArray.getJSONObject(i);
                                mQuotationsArrayAdapter.insertAtStart(new Quotation(quotationJsonObject));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                this
        );
        mRequestQueue.add(request);

        // also get sellers
        getSellersRequest();
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
                                mSellersArrayAdapter.add(new Seller(quotationJsonObject));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                this
        );
        mRequestQueue.add(request);
    }

    public void sendQuoteRequest(String searchString, String brands, String priceRange) {
        Quote quote =  new Quote(mBuyerId, searchString, brands, priceRange);

        JsonObjectRequest request = new JsonObjectRequest(
                getRequestUrl(RequestType.SEND_QUOTE),
                quote.toJsonObject(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v (TAG, response.toString());
//                        mBuyersCallbacks.quotationSent(true); TODO
                    }
                },
                this
        );
        mRequestQueue.add(request);
    }

    public void sendSignInRequest() {

        JsonObjectRequest request = new JsonObjectRequest(
                getRequestUrl(RequestType.SIGN_IN),
                new JSONObject(), // sending an empty but valid JSON object
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            mBuyerId = response.getInt("id");

                            getQuotationsRequest();

                            SharedPreferences.Editor editor = mSharedPreferences.edit();
                            editor.putInt(KEY_BUYER_ID, mBuyerId);
                            editor.apply();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            mBuyerId = -1;
                        }
                    }
                },
                this
        );
    }

    private String getRequestUrl(RequestType requestType) {
        String url = SERVER_URL + API_VERSION;
        switch (requestType) {
//            case REGISTER:
//                return url + "sellers";
            case SIGN_IN:
                return url + "buyers";
            case GET_QUOTATIONS:
                return url + "quotations";
            case SEND_QUOTE:
                return url + "quotations";
            case GET_SELLERS:
                return url + "sellers";
        }

        throw new IllegalArgumentException();
    }

    private ServicesSingleton(Context appContext) {
        mAppContext = appContext.getApplicationContext();
        mLatestAddress = null;
        mBuyerId = -1;

        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        mLocationClient = new LocationClient(mAppContext, this, this);
        mLocationClient.connect();
        checkPlayServices(); // not performing checkUserAccount
        // see http://www.androiddesignpatterns.com/2013/01/google-play-services-setup.html



        mRequestQueue = Volley.newRequestQueue(mAppContext);

        mQuotationsArrayAdapter = new QuotationsArrayAdapter(mAppContext);
        mSellersArrayAdapter = new SellersArrayAdapter(mAppContext);
    }

    public static ServicesSingleton getInstance(Context appContext) {

        if(sInstance == null)
            sInstance = new ServicesSingleton(appContext);

        return sInstance;
    }

    public boolean checkPlayServices() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mAppContext);
        if (status != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(status) && getmLocationCallbacks() != null) {
                getmLocationCallbacks().showErrorDialog(status);
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
            if (getmLocationCallbacks() != null)
                getmLocationCallbacks().searchingForAddress();
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

    private LocationCallbacks getmLocationCallbacks() {
        return mLocationCallbacks;
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
            if (getmLocationCallbacks() != null)
                getmLocationCallbacks().addressFound(address);
        }
    }

    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        Location lastLocation = mLocationClient.getLastLocation();
        if (lastLocation != null)
            searchAddress(lastLocation);
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
        if (getmLocationCallbacks() == null)
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
                getmLocationCallbacks().resolvableConnectionResultError(connectionResult);
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
            getmLocationCallbacks().showErrorDialog(connectionResult.getErrorCode());
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

    public void registerBuyer (BuyersCallbacks buyersCallbacks) {

        this.mBuyersCallbacks = buyersCallbacks;
    }

    public void registerCallback (LocationCallbacks locationCallbacks) {

        this.mLocationCallbacks = locationCallbacks;

    }

    public interface BuyersCallbacks {
        public void quotationReceived();
    }

    public interface LocationCallbacks {
        public void searchingForAddress();
        public void addressFound(Address address);
        /*
         * Implementation activities need to listen for result of the errorDialog in Activity.onActivityResult
         */
        public void showErrorDialog(int errorCode);
        public void resolvableConnectionResultError(ConnectionResult connectionResult) throws IntentSender.SendIntentException;
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
            this(-1, nameOfProduct, price, description, sellerId, quoteId);
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
            return nameOfProduct + "\nRs. " + price + "\n" + description;
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

        public Seller(int id, String nameOfShop, String nameOfSeller, String address, double latitude, double longitude, String phone, int rating, String email) {
            this.id = id;
            this.nameOfShop = nameOfShop;
            this.nameOfSeller = nameOfSeller;
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
            this.phone = phone;
            this.rating = rating;
            this.email = email;
        }

        /**
         * if id and rating are not available, they are set to invalid i.e. -1
         */
        public Seller(String nameOfShop, String nameOfSeller, String address, double latitude, double longitude, String phone, String email) {
            this.id = -1;
            this.nameOfShop = nameOfShop;
            this.nameOfSeller = nameOfSeller;
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
            this.phone = phone;
            this.rating = -1;
            this.email = email;
        }

        public Seller(JSONObject quotationJsonObject) throws JSONException {
            id = quotationJsonObject.getInt("id");
            nameOfShop = quotationJsonObject.getString("name_of_shop");
            nameOfSeller = quotationJsonObject.getString("name_of_seller");
            address = quotationJsonObject.getString("address");
            latitude = quotationJsonObject.getDouble("latitude");
            longitude = quotationJsonObject.getDouble("longitude");
            phone = quotationJsonObject.getString("phone");
            int rating;
            try {
                rating = Integer.parseInt(quotationJsonObject.getString("rating"));
            } catch (NumberFormatException e) {
                rating = -1;
            }
            this.rating = rating;
            email = quotationJsonObject.getString("email");
        }

        public JSONObject toJsonObject() throws JSONException {
            JSONObject retailerParamsJsonObject = new JSONObject();
            retailerParamsJsonObject.put("name_of_shop", nameOfShop)
                    .put("name_of_seller", nameOfSeller)
                    .put("address", address)
                    .put("latitude", latitude)
                    .put("longitude", longitude)
                    .put("email", email);

            if (id != -1)
                retailerParamsJsonObject.put("id", id);
            if (rating != -1)
                retailerParamsJsonObject.put("rating", rating);

            JSONObject retailerJsonObject = new JSONObject();
            retailerJsonObject.put("seller", retailerParamsJsonObject);

            return retailerJsonObject;
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

        public Quote(int id, int buyerId, String searchString, String brands, String priceRange) {
            this.id = id;
            this.buyerId = buyerId;
            this.searchString = searchString;
            this.brands = brands;
            this.priceRange = priceRange;
        }

        public Quote(int buyerId, String searchString, String brands, String priceRange) {
            this.id = -1;
            this.buyerId = buyerId;
            this.searchString = searchString;
            this.brands = brands;
            this.priceRange = priceRange;
        }

        public Quote (JSONObject jsonObject) throws JSONException {
            id = jsonObject.getInt("id");
            buyerId = jsonObject.getInt("buyer_id");
            searchString = jsonObject.getString("search_string");
            brands = jsonObject.getString("brands");
            priceRange = jsonObject.getString("price_range");
        }

        public JSONObject toJsonObject() {
            try {
                JSONObject quoteParamsJsonObject = new JSONObject()
                        .put("buyer_id", buyerId)
                        .put("search_string", searchString)
                        .put("brands", brands)
                        .put("price_range", priceRange);

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

    private enum RequestType {
        SIGN_IN,
        GET_QUOTATIONS,
        SEND_QUOTE,
        GET_SELLERS
    }

}
