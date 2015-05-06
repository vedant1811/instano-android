package com.instano.retailer.instano.application.controller;

import com.instano.retailer.instano.application.network.NetworkRequestsManager;
import com.instano.retailer.instano.utilities.model.Product;

import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

/**
 * Created by vedant on 5/2/15.
 */
public class Products {
    private static Products sInstance;


//    public Observable<Outlet> findByProduct(int productId) {
//
//    }

    /**
     * works only on the latest @param text i.e. prevents previous @return observables from emitting
     * anything, if this method is called again
     */
    public Observable<List<Product>> suggestProducts(String text) {

        return NetworkRequestsManager.instance().queryProducts(text);
    }

    public static Products controller() {
        if (sInstance == null)
            sInstance = new Products();
        return sInstance;
    }

    private Products() {
    }
}
