package com.instano.retailer.instano.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.instano.retailer.instano.R;
import com.instano.retailer.instano.utilities.GlobalMenuActivity;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.models.Device;


public class LauncherActivity extends GlobalMenuActivity {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 2500;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "GCM in Launcher ";

    boolean mCancelled = false;
    boolean mTimedOut = false;
    boolean mError = false;

    @Override
    public void onBackPressed() {
        // make sure the next activity is not started
        mCancelled = true;
        // finish the activity
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        if (!getSessionId().isEmpty() && !getRegistrationId().isEmpty())
            mTimedOut = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkPlayServices()) {
            authorizeSession(false, false);
        } else {
            noPlayServicesDialog();
            Log.v(TAG, "No valid Google Play Services APK found.");
            return;
        }
        new Handler().postDelayed(new Runnable() {

        /*
         * Showing splash screen with a timer. This will be useful when you
         * want to show case your app logo / company
         */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                mTimedOut = true;
                closeIfPossible();
            }
        }, SPLASH_TIME_OUT);
    }

    @Override
    public void onSessionResponse(Device device) {
        String sessionId = device.getSession_id();
        if (!sessionId.isEmpty()) {
            super.onSessionResponse(device);
            mError = false;
        }
        else {
            storeSessionId(sessionId);
            closeIfPossible();
        }
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
        if (!mTimedOut || mError)
            return;
        // initialize the app if it wasn't cancelled (like due to back button being pressed):
        if (!mCancelled) {
            Intent i = null;
//        if (ServicesSingleton.getInstance(this).firstTime())
            i = new Intent(this, StartingActivity.class);
            startActivity(i);
        }

        // close this activity
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }
}
