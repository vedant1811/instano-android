package com.instano.retailer.instano.activities.search;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.instano.retailer.instano.R;
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
    private static final String KEY_IS_NOTIFICATION_CANCELLED = "IsNotificationCancelled";

    private boolean mShown = false;
    private int mProductId;
    private View mHeaderView;

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

        getListView().addHeaderView(mHeaderView);
        QuotationsAndSellersAdapter adapter = activity.getAdapter();
        adapter.clear();
        setListAdapter(adapter);

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
        mShown = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mHeaderView = inflater.inflate(R.layout.header_notification, null);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setListShown(mShown);
        setProduct(mProductId);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    private void setShown(boolean shown) {
        mShown = shown;
        if (getView() != null)
            setListShown(mShown);
    }
}
