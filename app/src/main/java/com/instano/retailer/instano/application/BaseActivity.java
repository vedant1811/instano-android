package com.instano.retailer.instano.application;

import android.app.Activity;

import com.facebook.AppEventsLogger;

/**
 * Created by vedant on 29/12/14.
 */
public abstract class BaseActivity extends Activity {

    @Override
    protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // To calculate time spent on app
        AppEventsLogger.deactivateApp(this);
    }
}
