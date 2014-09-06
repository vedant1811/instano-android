package com.instano.retailer.instano;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.StringReader;
import java.net.URL;

/**
 * Asynchronously fetches quotations for each query
 *
 * Is filterable
 *
 * Created by vedant on 3/9/14.
 */
public class FetchQuotations implements Response.Listener<String>, Response.ErrorListener {

    private final static String TAG = "FetchQuotations";
    private final static String SERVER_URL = "";

    private String mQuery;
    private final Context mAppContext;

    private RequestQueue mRequestQueue;

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

    public FetchQuotations(Context mAppContext) {
        this.mAppContext = mAppContext.getApplicationContext();

        mRequestQueue = Volley.newRequestQueue(mAppContext);

    }

    public void runQuery(String mQuery) {
        this.mQuery = mQuery;
        sendServerRequest();
    }

    /**
     * ** Volley **
     *
     * Callback method that an error has been occurred with the
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
