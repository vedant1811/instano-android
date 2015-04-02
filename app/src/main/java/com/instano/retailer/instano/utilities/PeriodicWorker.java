package com.instano.retailer.instano.utilities;

import android.os.Handler;
import com.instano.retailer.instano.utilities.library.Log;

import com.instano.retailer.instano.application.ServicesSingleton;

/**
 * Created by vedant on 29/9/14.
 */
public class PeriodicWorker {

    private static final int REPEAT_INTERVAL = 5*1000; // In ms
    private static final String TAG = "PeriodicWorker";

    private Handler mHandler;
    private Runnable mRunnable;
    private boolean mIsRunning = false;

    public PeriodicWorker(final ServicesSingleton services) {
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "running");
//                services.runPeriodicTasks();
                mHandler.postDelayed(this, REPEAT_INTERVAL);
            }
        };
    }

    /**
     * idempotent
     */
    public void start() {
        if (!mIsRunning) {
            mHandler.post(mRunnable);
            mIsRunning = true;
        }
    }

    public void stop() {
        if (mIsRunning) {
            mHandler.removeCallbacks(mRunnable);
            mIsRunning = false;
        }
    }
}
