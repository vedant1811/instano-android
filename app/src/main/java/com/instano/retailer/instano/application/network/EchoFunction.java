package com.instano.retailer.instano.application.network;

import com.instano.retailer.instano.utilities.library.Log;

import java.util.ArrayList;

import rx.Observable;
import rx.Subscriber;

/**
 * Caches and echoes any object(s) to future subscribers
 * Created by vedant on 4/7/15.
 */
public class EchoFunction<T> implements Observable.OnSubscribe<T> {
    private ArrayList<T> mTList = new ArrayList<>();

    public void newObjectReceived(T object) {
        mTList.add(object);
    }

    @Override
    public void call(Subscriber<? super T> subscriber) {
        for (T t : mTList)
            subscriber.onNext(t);
        Log.v("EchoObservable", "sending " + mTList.size() + " items");
        mTList.clear();
    }
}
