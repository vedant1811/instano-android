package com.instano.retailer.instano.application.controller;

import android.util.SparseArray;

import com.instano.retailer.instano.utilities.model.Seller;

/**
 * Created by vedant on 5/2/15.
 */
public class Sellers {
    private static Sellers sInstance;
    private SparseArray<Seller> mSellers;

    public static Sellers controller() {
        if (sInstance == null)
            sInstance = new Sellers();
        return sInstance;
    }

    private Sellers() {
        mSellers = new SparseArray<>();
    }
}
