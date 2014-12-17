package com.instano.retailer.instano.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ViewFlipper;

import com.instano.retailer.instano.R;

public class ProfileActivity extends Activity {

    EditText mNameEditText;
    EditText mPhoneEditText;
    ViewFlipper mSetUpViewFlipper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mNameEditText = (EditText) findViewById(R.id.nameEditText);
        mPhoneEditText = (EditText) findViewById(R.id.phoneEditText);
        mSetUpViewFlipper = (ViewFlipper) findViewById(R.id.setUpViewFlipper);

        mPhoneEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String text = mPhoneEditText.getText().toString();
                if (!hasFocus && !text.equals("")) {
                    // replace all non-digit characters with "" and get the length()
                    // so that we can get the number of digits in `text`
                    if (text.replaceAll("\\D", "").length() != 10)
                        mPhoneEditText.setError("Enter 10 digits");
                }
            }
        });
    }

    public void setUpClicked(View view) {

        if ("".contentEquals(mNameEditText.getText())) {
            mNameEditText.setError("Cannot be empty");
            mNameEditText.requestFocus();
            return;
        }

        if ("".contentEquals(mPhoneEditText.getText())) {
            mPhoneEditText.setError("Cannot be empty");
            mPhoneEditText.requestFocus();
            return;
        }

        if (mPhoneEditText.getError() != null) {
            mPhoneEditText.requestFocus();
            return;
        }

        // all is good so proceed:
        mSetUpViewFlipper.showNext();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 2000);
    }
}
