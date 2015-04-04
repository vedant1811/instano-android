package com.instano.retailer.instano.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.network.NetworkRequestsManager;
import com.instano.retailer.instano.application.ServicesSingleton;
import com.instano.retailer.instano.utilities.GlobalMenuActivity;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.models.Device;

import rx.Observable;
import rx.android.observables.AndroidObservable;


public class LauncherActivity extends GlobalMenuActivity {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 2500;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final String TAG = getClass().getSimpleName();

    boolean mBackPressed = false;
    boolean mTimedOut = false;
    boolean mErrorOccurred = true;

    @Override
    public void onBackPressed() {
        // make sure the next activity is not started
        mBackPressed = true;
        // finish the activity
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        if (!ServicesSingleton.instance().isFirstTime())
            mTimedOut = true; // do not wait for timeout if app is not being used for first time


        // TODO: combine with above
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkPlayServices()) {
            AndroidObservable.bindActivity
                    (this, NetworkRequestsManager.instance().authorizeSession(false))
                    .subscribe((device) -> closeIfPossible(),this::showErrorDialog);
        } else {
            noPlayServicesDialog();
            Log.v(TAG, "No valid Google Play Services APK found.");
            return;
        }
        new Handler().postDelayed(() -> {
            // This method will be executed once the timer is over
            mTimedOut = true;
            closeIfPossible();
        }, SPLASH_TIME_OUT);
    }

    protected boolean checkPlayServices() {
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

    private void closeIfPossible() {
        if (!mTimedOut || mErrorOccurred)
            return;
        // initialize the app if it wasn't cancelled (like due to back button being pressed):
        if (!mBackPressed) {
            Intent i = null;
//        if (ServicesSingleton.getInstance(this).isFirstTime())
            i = new Intent(this, StartingActivity.class);
            startActivity(i);
        }
        // close this activity
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }
}
