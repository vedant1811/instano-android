package com.instano.retailer.instano.application.controller;

/**
 * Created by vedant on 5/2/15.
 */
public class Outlets {
    private static Outlets sInstance;

//    public Observable<Outlet> findByProduct(int productId) {
//
//    }

    public static Outlets controller() {
        if (sInstance == null)
            sInstance = new Outlets();
        return sInstance;
    }

    private Outlets() {

    }
}
