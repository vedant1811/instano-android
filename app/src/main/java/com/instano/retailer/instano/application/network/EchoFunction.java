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
    public static final String TAG = "EchoFunction";
    private ArrayList<T> mTList = new ArrayList<>();
    private Subscriber<? super T> mSubscriber = null;

    public void newEventReceived(T object) {
        mTList.add(object);
        doEcho();
    }

    @Override
    public void call(Subscriber<? super T> subscriber) {
        mSubscriber = subscriber;
        doEcho();
    }

    private void doEcho() {
        if (mSubscriber == null) {
            Log.e(TAG, "new events without any subscriber");
            return;
        }
        for (T t : mTList)
            mSubscriber.onNext(t);
        Log.v(TAG, "sending " + mTList.size() + " items");
        mTList.clear();
    }
}
