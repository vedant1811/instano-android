package com.instano.retailer.instano;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
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
    private final static String SERVER_URL = "";

    public static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;

    private static ServicesSingleton sInstance;

    private final Context mAppContext;


    /* location variables */
    private LocationClient mLocationClient;
    private String mLatestAddress;
    private String locationErrorString;
    private LocationCallbacks mLocationCallbacks;

    /* network variables */
    private String mQuery;
    private QuotationsCallback mQuotationsCallback;

    private RequestQueue mRequestQueue;
    private ArrayAdapter<Quotation> mQuotationArrayAdapter;

    /**
     * asynchronously sends the query request to server
     */
    private void sendServerRequest() {
        if (mQuery == null || mQuery.equals(""))
            return;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, requestUrl(), this, this);

        mRequestQueue.add(stringRequest);
    }

    private String requestUrl() {
        return "http://altj-db.hol.es/default.php?text1=abhinav&text2=yashkar";
    }

    private ServicesSingleton(Context appContext) {
        mAppContext = appContext.getApplicationContext();

        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        mLocationClient = new LocationClient(mAppContext, this, this);
        mLocationClient.connect();
        checkPlayServices(); // not performing checkUserAccount
        // see http://www.androiddesignpatterns.com/2013/01/google-play-services-setup.html



        mRequestQueue = Volley.newRequestQueue(mAppContext);

        // TODO: initialize properly, i.e. with a meaningful layout ID
        mQuotationArrayAdapter = new ArrayAdapter<Quotation>(mAppContext, android.R.layout.simple_list_item_1);

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

    public void getAddress(Location location) {
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

    private QuotationsCallback getmQuotationsCallback() {
        return mQuotationsCallback;
    }

    public String getLocationErrorString() {
        return locationErrorString;
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
        getAddress(mLocationClient.getLastLocation());
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

    public void runQuery(String mQuery) {
        this.mQuery = mQuery;
        sendServerRequest();
    }

    /**
     * ** Volley **
     *
     * QuotationsCallback method that an error has been occurred with the
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

    public ArrayAdapter<Quotation> getQuotationArrayAdapter() {
        return mQuotationArrayAdapter;
    }

    public void registerCallback (QuotationsCallback quotationsCallback) {

        // Be sure not to override previous quotationsCallback
        assert mQuotationsCallback == null;

        this.mQuotationsCallback = quotationsCallback;
    }

    public void registerCallback (LocationCallbacks locationCallbacks) {

        // Be sure not to override previous quotationsCallback
        assert mLocationCallbacks == null;

        this.mLocationCallbacks= locationCallbacks;

    }

    public interface QuotationsCallback {
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
        public final int price; // price in paise. To display divide by 100
        public final String description;
        public final Seller seller;
        public final URL imageUrl; // can be null

        public Quotation(int id, String nameOfProduct, int price, String description, Seller seller, URL imageUrl) {
            this.id = id;
            this.nameOfProduct = nameOfProduct;
            this.price = price;
            this.description = description;
            this.seller = seller;
            this.imageUrl = imageUrl;
        }
    }

    /**
     * Represents a single immutable Seller
     */
    public class Seller {
        public final int id; // server generated
        public final String address;
        public final String phone; // TODO: maybe make it a list of Strings
        public final int rating; // rating is out of 50, displayed out of 5.0

        public Seller(int id, String address, String phone, int rating) {
            this.id = id;
            this.address = address;
            this.phone = phone;
            this.rating = rating;
        }
    }


}
