package com.instano.retailer.instano.activities.search;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;

import com.instano.retailer.instano.application.controller.Quotations;
import com.instano.retailer.instano.utilities.library.Log;

import rx.android.observables.AndroidObservable;

/**
 * A fragment representing a list of Items.
 * <p />
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p />
 */
public class SellersListFragment extends ListFragment {
    private static final String TAG = "SellersListFragment";

    private boolean mShown = false;
    private int mProductId;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SellersListFragment() {
    }

    public void setProduct(int productId) {
        mProductId = productId;
        setShown(false);
        ResultsActivity activity = (ResultsActivity) getActivity();
        if (activity == null)
            return;

        QuotationsAndSellersAdapter adapter = activity.getAdapter();
        adapter.clear();

        Log.d(TAG, "calling query quotation");
        AndroidObservable.bindFragment(this, Quotations.controller().fetchQuotationsForProduct(productId))
                .subscribe(quotationCard -> {
                        setShown(true);
                        adapter.add(quotationCard);
                }, error -> Log.fatalError(new RuntimeException(error)));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QuotationsAndSellersAdapter adapter = ((ResultsActivity)getActivity()).getAdapter();
        mShown = false;

        setListAdapter(adapter);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setListShown(mShown);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setProduct(mProductId);
    }

    private void setShown(boolean shown) {
        mShown = shown;
        if (getView() != null)
            setListShown(mShown);
    }
}
