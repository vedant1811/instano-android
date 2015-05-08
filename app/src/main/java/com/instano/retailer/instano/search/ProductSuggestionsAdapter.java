package com.instano.retailer.instano.search;

import android.app.Activity;
import android.widget.ArrayAdapter;

import com.instano.retailer.instano.application.network.NetworkRequestsManager;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.model.Product;

import rx.Subscription;
import rx.android.observables.AndroidObservable;
import rx.subscriptions.Subscriptions;

/**
* Created by vedant on 4/28/15.
*/
class ProductSuggestionsAdapter extends ArrayAdapter<Product> {
    private Activity mActivity;
    private Subscription mSuggestProductsSubscription;

    public ProductSuggestionsAdapter(Activity activity) {
        super(activity, android.R.layout.simple_list_item_1, android.R.id.text1);
        mActivity = activity;
        mSuggestProductsSubscription = Subscriptions.unsubscribed();
    }

    public void newSearch(String query) {
        mSuggestProductsSubscription.unsubscribe();
        mSuggestProductsSubscription = AndroidObservable.bindActivity(mActivity,
                NetworkRequestsManager.instance().queryProducts(query))
                        .subscribe(
                                products -> {
                                    clear();
                                    addAll(products);
                                },
                                error -> Log.fatalError(new RuntimeException(error))
                        );
    }
}
