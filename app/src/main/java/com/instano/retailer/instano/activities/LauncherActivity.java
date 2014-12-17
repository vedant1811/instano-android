package com.instano.retailer.instano.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.ServicesSingleton;


public class LauncherActivity extends Activity {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

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

    private void onSplashTimeOut() {
        // initialize:
        Intent i = null;
//        if (ServicesSingleton.getInstance(this).firstTime())
            i = new Intent(this, StartingActivity.class);

        startActivity(i);

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

}
