package com.instano.retailer.instano.search;

import android.app.Activity;
import android.widget.ArrayAdapter;

import com.instano.retailer.instano.application.network.NetworkRequestsManager;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.models.Product;

import java.util.Iterator;

import rx.android.observables.AndroidObservable;

/**
* Created by vedant on 4/28/15.
*/
class ProductSuggestionsAdapter extends ArrayAdapter<Product> {
    private Activity mActivity;
    private String mQuery;

    public ProductSuggestionsAdapter(Activity activity) {
        super(activity, android.R.layout.simple_list_item_1, android.R.id.text1);
        mActivity = activity;
    }

    public void newSearch(String query) {
        mQuery = query.toLowerCase();
        AndroidObservable.bindActivity(mActivity, NetworkRequestsManager.instance().queryProducts(query)
                // query could have been updated before results were received so filter the results again
                .map(products -> {
                    Iterator<Product> iterator = products.iterator();
                    while (iterator.hasNext())
                        if (!iterator.next().name.toLowerCase().contains(mQuery))
                            iterator.remove();
                    return products;
                }))
                .subscribe(
                        products -> {
                            clear();
                            addAll(products);
                        },
                        error -> Log.fatalError(new RuntimeException(error))
                );
    }
}
