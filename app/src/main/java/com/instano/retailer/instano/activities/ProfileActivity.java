package com.instano.retailer.instano.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ViewFlipper;

import com.instano.retailer.instano.R;

public class ProfileActivity extends Activity {

    ViewFlipper mSetUpViewFlipper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mSetUpViewFlipper = (ViewFlipper) findViewById(R.id.setUpViewFlipper);
    }

    public void setUpClicked(View view) {
        mSetUpViewFlipper.showNext();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 2000);
    }
}
