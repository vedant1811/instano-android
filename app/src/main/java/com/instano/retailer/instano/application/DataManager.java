package com.instano.retailer.instano.application;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.models.Deal;
import com.instano.retailer.instano.utilities.models.ProductCategories;
import com.instano.retailer.instano.utilities.models.Quotation;
import com.instano.retailer.instano.utilities.models.Quote;
import com.instano.retailer.instano.utilities.models.Seller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Created by vedant on 19/12/14.
 */
public class DataManager {
    private static final String TAG = "DataManager";

    private static DataManager sInstance;

    // TODO: probably convert Lists to a SortedSet or SortedMap
    private ArrayList<Quote> mQuotes;
    private ArrayList<Quotation> mQuotations;
    private ArrayList<Seller> mSellers;
    private ArrayList<Deal> mDeals;
    private ProductCategories mProductCategories;

    private HashSet<QuotesListener> mQuotesListeners;
    private HashSet<DealsListener> mDealsListeners;
    private HashSet<SellersListener> mSellersListeners;

    public interface QuotesListener {
        public void quotesUpdated();
        public void quotationsUpdated();
    }

    public interface DealsListener {
        public void dealsUpdated();
    }

    public interface SellersListener {
        public void sellersUpdated();
    }

    public void registerListener(@NonNull QuotesListener quotesListener) {
        mQuotesListeners.add(quotesListener);
    }

    public void unregisterListener(@Nullable QuotesListener quotesListener) {
        mQuotesListeners.remove(quotesListener);
    }

    public void registerListener(@NonNull DealsListener dealsListener) {
        mDealsListeners.add(dealsListener);
    }

    public void unregisterListener(@Nullable DealsListener dealsListener) {
        mDealsListeners.remove(dealsListener);
    }

    public void registerListener(@NonNull SellersListener sellersListener) {
        mSellersListeners.add(sellersListener);
    }

    public void unregisterListener(@Nullable SellersListener sellersListener) {
        mSellersListeners.remove(sellersListener);
    }

    public List<ProductCategories.Category> getProductCategories(boolean clearSelected) {
        if (mProductCategories != null) {
            if (clearSelected)
                mProductCategories.clearSelected();
            return mProductCategories.getProductCategories();
        }
        else
            return null;
    }

    /**
     * modifiable copy of quotations
     * @param id
     * @return an {@link ArrayList} of objects
     */
    @NonNull
    public ArrayList<Object> quotationsBySeller(int id) {
        ArrayList<Object> quotations = new ArrayList<Object>();

        for (Quotation quotation : mQuotations) {
            if (quotation.sellerId == id)
                quotations.add(quotation);
        }
        return quotations;
    }

    @Nullable
    public Quote getQuote(int id) {
        for (Quote quote : mQuotes)
            if (quote.id == id)
                return quote;
        return null;
    }

    @Nullable
    public Deal getDeal(int id) {
        for (Deal deal : mDeals)
            if (deal.id == id)
                return deal;
        return null;
    }

    @NonNull
    public List<Quote> getQuotes() {
        return Collections.unmodifiableList(mQuotes);
    }

    @NonNull
    public List<Quotation> getQuotations() {
        return Collections.unmodifiableList(mQuotations);
    }

    @NonNull
    public List<Seller> getSellers() {
        return Collections.unmodifiableList(mSellers);
    }

    @NonNull
    public  List<Deal> getDeals() {
        return Collections.unmodifiableList(mDeals);
    }

    @Nullable
    public Quotation getQuotation(int id) {
        for (Quotation quotation : mQuotations)
            if (quotation.id == id)
                return quotation;
        return null;
    }

    @Nullable
    public Seller getSeller(int id) {
        for (Seller seller : mSellers)
            if (seller.id == id)
                return seller;
        return null;
    }

    public HashSet<Integer> get5NearbySellers(ProductCategories.Category category) {
        long start = System.nanoTime();

        // sorted now so that we have latest location
        Collections.sort(mSellers, new Seller.DistanceComparator());

        HashSet <Integer> nearbySellers = new HashSet<Integer>(5);
        for (Seller seller : mSellers) {
            if (seller.productCategories.containsCategoryAndOneBrand(category)) {
                nearbySellers.add(seller.id);
                if (nearbySellers.size() == 5)
                    break;
            }
        }
        double time = (System.nanoTime() - start)/ Log.ONE_MILLION;
        Log.v(Log.TIMER_TAG, String.format("getNearbySellers took %.4fms", time));
        return nearbySellers;
    }

    @NonNull
    public static DataManager instance() {
        if (sInstance == null)
            throw new IllegalStateException("DataManager.Init() never called");
        return sInstance;
    }

    /*package*/ static void init() {
        sInstance = new DataManager();
    }

    /**
     * also clears selected brands in product categories in case it is updated
     * @param response
     * @return
     */
    /*package*/ boolean updateProductCategories(JSONObject response) {
        long start = System.nanoTime();
        ProductCategories productCategories = new ProductCategories(response, true);
        if (productCategories.equals(mProductCategories)) {
            double time = (System.nanoTime() - start)/ Log.ONE_MILLION;
            Log.v(Log.TIMER_TAG, String.format("updateProductCategories took %.4fms", time));
            return false;
        }
        else {
            mProductCategories = productCategories;
            double time = (System.nanoTime() - start)/ Log.ONE_MILLION;
            Log.v(Log.TIMER_TAG, String.format("updateProductCategories took %.4fms", time));
            return true;
        }
    }

    /*package*/ boolean updateQuotations(JSONArray response) {
        long start = System.nanoTime();
        boolean newEntries = false;
        boolean newUnread = false;
        for (int i = 0; i < response.length(); i++) {
            try {
                Quotation quotation= new Quotation(response.getJSONObject(i));
                if (!mQuotations.contains(quotation)) {
                    mQuotations.add(quotation);
                    newEntries = true;
                    newUnread |= !quotation.isRead();
                }
            } catch (JSONException e) {
                Log.e(TAG + ".updateQuotations", String.format("response: %s, i=%d", String.valueOf(response), i), e);
                Log.fatalError(e);
            }
        }
        if (newUnread)
            ServicesSingleton.instance().createNotification();
        if (newEntries)
            for (QuotesListener quotesListener : mQuotesListeners)
                quotesListener.quotationsUpdated();
        double time = (System.nanoTime() - start)/ Log.ONE_MILLION;
        Log.v(Log.TIMER_TAG, String.format("updateQuotations took %.4fms", time));
        return newEntries;
    }

    /*package*/ boolean updateQuotes(JSONArray response) {
        long start = System.nanoTime();
        boolean newEntries = false;
        for (int i = 0; i < response.length(); i++) {
            try {
                Quote quoteInResponse = new Quote(response.getJSONObject(i));
                Quote matchingQuote = null;
                for (Quote quote : mQuotes)
                    if (quote.equals(quoteInResponse))
                        matchingQuote = quote;
                // else matching quote is null

                if (matchingQuote == null) {
                    mQuotes.add(quoteInResponse);
                    newEntries = true;
                } else if (matchingQuote.updatedAt != quoteInResponse.updatedAt) { // means quote has been updated
                    mQuotes.remove(matchingQuote);
                    mQuotes.add(quoteInResponse);
                    newEntries = true;
                }

            } catch (JSONException e) {
                Log.e(TAG + ".updateQuotes", String.format("response: %s, i=%d", String.valueOf(response), i), e);
                Log.fatalError(e);
            }
        }

        if (newEntries)
            for (QuotesListener quotesListener : mQuotesListeners)
                quotesListener.quotesUpdated();
        double time = (System.nanoTime() - start)/ Log.ONE_MILLION;
        Log.v(Log.TIMER_TAG, String.format("updateQuotes took %.4fms", time));
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
                Log.fatalError(e);
            }
        }
        if (newEntries) {
            for (SellersListener listener : mSellersListeners)
                listener.sellersUpdated();
        }

        double time = (System.nanoTime() - start)/ Log.ONE_MILLION;
        Log.v(Log.TIMER_TAG, String.format("updateSellers took %.4fms", time));
        return newEntries;
    }

    /*package*/ boolean updateDeals(JSONArray response) {
        long start = System.nanoTime();
        boolean newEntries = false;
        for (int i = 0; i < response.length(); i++) {
            try {
                Deal deal = new Deal(response.getJSONObject(i));
                if (!mDeals.contains(deal)) {
                    mDeals.add(deal);
                    newEntries = true;
                }
            } catch (JSONException e) {
                Log.e(TAG + ".updateSellers", String.format("response: %s, i=%d", String.valueOf(response), i), e);
                Log.fatalError(e);
            }
        }
        if (newEntries)
            for (DealsListener dealsListener : mDealsListeners)
                dealsListener.dealsUpdated();

        double time = (System.nanoTime() - start)/ Log.ONE_MILLION;
        Log.v(Log.TIMER_TAG, String.format("updateDeals took %.4fms", time));
        return newEntries;
    }

    /*package*/ void onNewBuyer() {
        mQuotations.clear();
        mQuotes.clear();
    }

    private DataManager() {
        mQuotes = new ArrayList<Quote>();
        mQuotations = new ArrayList<Quotation>();
        mSellers = new ArrayList<Seller>();
        mDeals = new ArrayList<Deal>();
        mQuotesListeners = new HashSet<QuotesListener>();
        mDealsListeners = new HashSet<DealsListener>();
        mSellersListeners = new HashSet<SellersListener>();
    }
}
