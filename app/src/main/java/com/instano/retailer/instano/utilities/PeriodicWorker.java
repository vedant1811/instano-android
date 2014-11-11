package com.instano.retailer.instano.utilities;

import android.os.Handler;
import android.util.Log;

import com.instano.retailer.instano.ServicesSingleton;

/**
 * Created by vedant on 29/9/14.
 */
public class PeriodicWorker {

    private static final int REPEAT_INTERVAL = 5*1000; // In ms
    private static final String TAG = "PeriodicWorker";

    private Handler mHandler;
    private Runnable mRunnable;

    public PeriodicWorker(final ServicesSingleton services) {
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "running");
                services.runPeriodicTasks();
                mHandler.postDelayed(this, REPEAT_INTERVAL);
            }
        };
    }

    public void start() {
        mHandler.post(mRunnable);
    }

    public void stop() {
        mHandler.removeCallbacks(mRunnable);
    }
}
