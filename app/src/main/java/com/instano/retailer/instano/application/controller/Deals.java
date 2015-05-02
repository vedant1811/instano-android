package com.instano.retailer.instano.application.controller;

/**
 * Created by vedant on 5/2/15.
 */
public class Deals {
    private static Deals sInstance;

//    public Observable<Outlet> findByProduct(int productId) {
//
//    }

    public static Deals controller() {
        if (sInstance == null)
            sInstance = new Deals();
        return sInstance;
    }

    private Deals() {

    }
}
