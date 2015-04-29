package com.instano.retailer.instano.sellers;

import android.app.Activity;
import android.util.Pair;
import android.util.SparseArray;
import android.widget.ArrayAdapter;

import com.instano.retailer.instano.application.network.NetworkRequestsManager;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.models.Quotation;
import com.instano.retailer.instano.utilities.models.Seller;

import java.util.ArrayList;

import rx.android.observables.AndroidObservable;
import rx.schedulers.Schedulers;

/**
 * Created by vedant on 4/29/15.
 */
public class QuotationsAndSellersAdapter extends ArrayAdapter<Pair<Seller, Quotation>> {

    private static final String TAG = "QuotationsAndSellersAdapter";
    private final Activity mActivity;
    private SparseArray<Quotation> mQuotations;
    private SparseArray<Seller> mSellers;
    private int mProductId;

    /**
     * Constructor
     *
     * @param activity  The current activity.
     */
    public QuotationsAndSellersAdapter(Activity activity) {
        super(activity, 0);
        mActivity = activity;
        AndroidObservable.bindActivity(mActivity, NetworkRequestsManager.instance().getObservable(Seller.class))
                .observeOn(Schedulers.computation())
                .subscribe(seller -> {
                            Log.d(TAG, "new seller " + seller.hashCode());
                            mSellers.put(seller.hashCode(), seller);
                        },
                        error -> Log.fatalError(new RuntimeException(error)),
                        this::newData);
    }

    public void setProduct(int productId) {
        mProductId = productId;
        AndroidObservable.bindActivity(mActivity, NetworkRequestsManager.instance().queryQuotations(productId))
                .observeOn(Schedulers.computation())
                .subscribe(quotation -> {
                            Log.d(TAG, "new quotation " + quotation.hashCode());
                            mQuotations.put(quotation.hashCode(), quotation);
                        },
                        error -> Log.fatalError(new RuntimeException(error)),
                        this::newData);
    }

    /**
     * runs on the computation thread
     * also, does nothing if any data is missing
     */
    private void newData() {
        Log.d(TAG, "new data");
        if (mQuotations.size() <= 0)
            return; // do nothing
        ArrayList<Pair<Seller, Quotation>> pairs = new ArrayList<>();
        for (int i = 0; i < mQuotations.size(); i++) {
            Quotation quotation = mQuotations.valueAt(i);
            Seller seller = mSellers.get(quotation.sellerId);
            if (seller == null)
                return;
            pairs.add(Pair.create(seller, quotation));
        }
        mActivity.runOnUiThread(() -> {
            clear();
            addAll(pairs);
        });
    }
}
