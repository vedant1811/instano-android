package com.instano.retailer.instano.application.controller;

/**
 * Created by vedant on 5/2/15.
 */
public class Sellers {
    private static Sellers sInstance;

    public static Sellers controller() {
        if (sInstance == null)
            sInstance = new Sellers();
        return sInstance;
    }

    private Sellers() {

    }
}
