package com.instano.retailer.instano.application.network;

import com.instano.retailer.instano.utilities.library.Log;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by vedant on 4/11/15.
 */
public class ExponentialBackoffFunction implements Func1<Observable<? extends Throwable>, Observable<?>> {

    private static final String TAG = "ExponentialBackoffFunction";
    private final int maxRetries;
    private final int retryDelay;
    private int retryCount;

    public ExponentialBackoffFunction() {
        this(2, 3);
    }

    public ExponentialBackoffFunction(final int maxRetries, final int retryDelay) {
        this.maxRetries = maxRetries;
        this.retryDelay = retryDelay;
        this.retryCount = 0;
    }

    @Override
    public Observable<?> call(Observable<? extends Throwable> observable) {
        return observable
                .flatMap(e -> {
                    if (++retryCount < maxRetries) {
                        // When this Observable calls onNext, the original
                        // Observable will be retried (i.e. re-subscribed).
                        Log.v(TAG, "retrying after " + ((long) Math.pow(retryDelay, retryCount) + 1));
                        return Observable.timer((long) Math.pow(retryDelay, retryCount) + 1,
                                TimeUnit.SECONDS);
                    }
                    Log.d(TAG, "max retires hit");
                    // Max retries hit. Just pass the error along.
                    return Observable.error(e);
                });
    }
}