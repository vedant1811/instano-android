package com.instano.retailer.instano.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.controller.Sessions;
import com.instano.retailer.instano.utilities.library.Log;


public class LauncherActivity extends GlobalMenuActivity {
    // Splash screen timer
    private static int SPLASH_TIME_OUT = 2500;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final String TAG = "LauncherActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkPlayServices()) {
            retryableError(Sessions.controller().doFacebookSignIn(this),
                    aClass -> {
                        startActivity(new Intent(this, aClass));
                        finish();
                    });
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.v(TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }
}
