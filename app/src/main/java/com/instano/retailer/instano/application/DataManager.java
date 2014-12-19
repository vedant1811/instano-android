package com.instano.retailer.instano.application;

import android.support.annotation.NonNull;

import com.instano.retailer.instano.utilities.models.ProductCategories;
import com.instano.retailer.instano.utilities.models.Quotation;
import com.instano.retailer.instano.utilities.models.Quote;
import com.instano.retailer.instano.utilities.models.Seller;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by vedant on 19/12/14.
 */
public class DataManager {

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
        ProductCategories productCategories = new ProductCategories(response, true);
    }

    /*package*/ boolean updateQuotations(JSONArray response) {

    }

    /*package*/ boolean updateQuotes(JSONArray response) {

    }

    /*package*/ boolean updateSellers(JSONArray response) {

    }

    /*package*/ void onNewBuyer() {
        mQuotations.clear();
        mQuotes.clear();
    }

}
