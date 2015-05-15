package com.instano.retailer.instano.application.controller;

import android.util.SparseArray;

import com.instano.retailer.instano.application.network.NetworkRequestsManager;
import com.instano.retailer.instano.utilities.model.Seller;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by vedant on 5/2/15.
 */
public class Sellers {
    private static Sellers sInstance;

    // TODO: remove this and use network caching
    private SparseArray<Seller> mSellers;

    public static Sellers controller() {
        if (sInstance == null)
            sInstance = new Sellers();
        return sInstance;
    }

    public Observable<Seller> getSeller(int id) {
        Seller seller = mSellers.get(id);
        if (seller == null) {
            return NetworkRequestsManager.instance().getSeller(id)
                    .doOnNext(s -> mSellers.put(s.id, s));
        }
        else {
            return Observable.just(seller).subscribeOn(Schedulers.computation());
        }
    }

    private Sellers() {
        mSellers = new SparseArray<>();
    }
}
