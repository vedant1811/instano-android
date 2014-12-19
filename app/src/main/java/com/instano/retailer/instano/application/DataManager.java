package com.instano.retailer.instano.application;

import android.support.annotation.NonNull;

import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.models.ProductCategories;
import com.instano.retailer.instano.utilities.models.Quotation;
import com.instano.retailer.instano.utilities.models.Quote;
import com.instano.retailer.instano.utilities.models.Seller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by vedant on 19/12/14.
 */
public class DataManager {
    private static final String TAG = "DataManager";

    private static DataManager sInstance;

    private ArrayList<Quote> mQuotes;
    private ArrayList<Quotation> mQuotations;
    private ArrayList<Seller> mSellers;
    private ProductCategories mProductCategories;

    @NonNull
    public static DataManager instance() {
        if (sInstance == null)
            throw new IllegalStateException("DataManager.Init() never called");
        return sInstance;
    }

    public ArrayList<ProductCategories.Category> getProductCategories() {
        if (mProductCategories != null)
            return mProductCategories.getProductCategories();
        else
            return null;
    }

    private DataManager() {
        mQuotes = new ArrayList<Quote>();
        mQuotations = new ArrayList<Quotation>();
        mSellers = new ArrayList<Seller>();
    }

    /*package*/ static void init() {
        sInstance = new DataManager();
    }

    /*package*/ boolean updateProductCategories(JSONObject response) {
        long start = System.nanoTime();
        ProductCategories productCategories = new ProductCategories(response, true);
        if (productCategories.equals(mProductCategories)) {
            double time = (System.nanoTime() - start)/1000000.0;
            Log.d(Log.TIMER_TAG, String.format("updateProductCategories took %.4fms", time));
            return false;
        }
        else {
            mProductCategories = productCategories;
            double time = (System.nanoTime() - start)/1000000.0;
            Log.d(Log.TIMER_TAG, String.format("updateProductCategories took %.4fms", time));
            return true;
        }
    }

    /*package*/ boolean updateQuotations(JSONArray response) {
        long start = System.nanoTime();
        boolean newEntries = false;
        for (int i = 0; i < response.length(); i++) {
            try {
                Quote quote = new Quote(response.getJSONObject(i));
                if (!mQuotes.contains(quote)) {
                    mQuotes.add(quote);
                    newEntries = true;
                }
            } catch (JSONException e) {
                Log.e(TAG + ".updateQuotations", String.format("response: %s, i=%d", String.valueOf(response), i), e);
            }
        }
        double time = (System.nanoTime() - start)/1000000.0;
        Log.d(Log.TIMER_TAG, String.format("updateQuotations took %.4fms", time));
        return newEntries;
    }

    /*package*/ boolean updateQuotes(JSONArray response) {
        long start = System.nanoTime();
        boolean newEntries = false;
        for (int i = 0; i < response.length(); i++) {
            try {
                Quotation quotation= new Quotation(response.getJSONObject(i));
                if (!mQuotations.contains(quotation)) {
                    mQuotations.add(quotation);
                    newEntries = true;
                }
            } catch (JSONException e) {
                Log.e(TAG + ".updateQuotes", String.format("response: %s, i=%d", String.valueOf(response), i), e);
            }
        }
        double time = (System.nanoTime() - start)/1000000.0;
        Log.d(Log.TIMER_TAG, String.format("updateQuotes took %.4fms", time));
        return newEntries;
    }

    /*package*/ boolean updateSellers(JSONArray response) {
        long start = System.nanoTime();
        boolean newEntries = false;
        for (int i = 0; i < response.length(); i++) {
            try {
                Seller seller = new Seller(response.getJSONObject(i));
                if (!mSellers.contains(seller)) {
                    mSellers.add(seller);
                    newEntries = true;
                }
            } catch (JSONException e) {
                Log.e(TAG + ".updateSellers", String.format("response: %s, i=%d", String.valueOf(response), i), e);
            }
        }
        double time = (System.nanoTime() - start)/1000000.0;
        Log.d(Log.TIMER_TAG, String.format("updateSellers took %.4fms", time));
        return newEntries;
    }

    /*package*/ void onNewBuyer() {
        mQuotations.clear();
        mQuotes.clear();
    }

}
