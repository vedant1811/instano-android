package com.instano.retailer.instano.activities;

import com.instano.retailer.instano.utilities.library.Log;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.BaseActivity;
import com.instano.retailer.instano.application.NetworkRequestsManager;
import com.instano.retailer.instano.utilities.models.Device;

import java.io.IOException;


public class LauncherActivity extends BaseActivity {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 1500;
    boolean mCancelled = false;
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    String SENDER_ID = "187047464172";

    static final String TAG = "GCMDemo";

 //   TextView mDisplay;
    GoogleCloudMessaging gcm;
 //   AtomicInteger msgId = new AtomicInteger();
 //   SharedPreferences prefs;
    Context context;
 //   private NetworkRequestsManager mNetworkRequestManager;
    private Device mDevice;

    String regid;

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

        context = getApplicationContext();

        // Check device for Play Services APK. If check succeeds, proceed with
        //  GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
            }
        } else {
            Log.v(TAG, "No valid Google Play Services APK found.");
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
                onSplashTimeOut();
            }
        }, SPLASH_TIME_OUT);

    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.v(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.v(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the registration ID in your app is up to you.
        return getSharedPreferences(LauncherActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void registerInBackground() {
        new AsyncTask<Void,Void,String>() {
            @Override
            protected String doInBackground(Void... params) {
                long start = System.nanoTime();
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the registration ID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex;
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }

                double time = (System.nanoTime() - start)/ com.instano.retailer.instano.utilities.library.Log.ONE_MILLION;
                Log.d(Log.TIMER_TAG, String.format("registerInBackground took %.4fms", time));
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.v(TAG, "onPost " + msg);
            }


        }.execute();

    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {
        // Your implementation here.
        Log.v(TAG, "Send regId  " + regid);

        mDevice = new Device();
        mDevice.setGcm_registration_id(regid);
        NetworkRequestsManager.instance().getDeviceId(mDevice);
    }
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.v(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }


    private void onSplashTimeOut() {
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

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.v(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

}
