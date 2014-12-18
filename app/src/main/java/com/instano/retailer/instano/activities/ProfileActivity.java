package com.instano.retailer.instano.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.instano.retailer.instano.NetworkRequestsManager;
import com.instano.retailer.instano.R;
import com.instano.retailer.instano.ServicesSingleton;
import com.instano.retailer.instano.utilities.models.Buyer;

public class ProfileActivity extends Activity implements NetworkRequestsManager.RegistrationCallback {

    EditText mNameEditText;
    EditText mPhoneEditText;
    ViewFlipper mSetUpViewFlipper;
    private ServicesSingleton mServicesSingleton;
    private Toast mErrorToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mNameEditText = (EditText) findViewById(R.id.nameEditText);
        mPhoneEditText = (EditText) findViewById(R.id.phoneEditText);
        mSetUpViewFlipper = (ViewFlipper) findViewById(R.id.setUpViewFlipper);

        mServicesSingleton = ServicesSingleton.getInstance(this);
        NetworkRequestsManager.instance().registerCallback(this);

        mPhoneEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String text = mPhoneEditText.getText().toString();
                if (!hasFocus && !text.equals("")) {
                    checkPhoneNumber();
                }
            }
        });
    }

    private boolean checkPhoneNumber() {
        String text = mPhoneEditText.getText().toString();

        if ("".contentEquals(text)) {
            mPhoneEditText.setError("Cannot be empty");
            mPhoneEditText.requestFocus();
            return false;
        }

        // replace all non-digit characters with "" and get the length()
        // so that we can get the number of digits in `text`
        if (text.replaceAll("\\D", "").length() != 10) {
            mPhoneEditText.setError("Enter 10 digits");
            return false;
        }
        else {
            NetworkRequestsManager.instance().buyerExistsRequest(text);
            return true;
        }
    }

    public void setUpClicked(View view) {

        if ("".contentEquals(mNameEditText.getText())) {
            mNameEditText.setError("Cannot be empty");
            mNameEditText.requestFocus();
            return;
        }

        if (!checkPhoneNumber()) {
            return;
        }

        // all is good so proceed:
        mSetUpViewFlipper.showNext(); // progressbar
        Buyer buyer = new Buyer(
                mNameEditText.getText().toString(),
                mPhoneEditText.getText().toString()
        ) ;
        NetworkRequestsManager.instance().registerRequest(buyer);
    }

    @Override
    public void phoneExists(boolean exists) {
        if (exists) {
            mPhoneEditText.setError("already taken. Contact us if this is an error");
            if (mErrorToast != null)
                mErrorToast.cancel();
        }
    }

    @Override
    public void onRegistration(Result result) {
        mSetUpViewFlipper.setDisplayedChild(0); // button
        if (result == Result.NO_ERROR) {
            setResult(RESULT_OK);
            finish();
        }
        else if (result == Result.PHONE_EXISTS)
                mPhoneEditText.requestFocus();
        else if (!NetworkRequestsManager.instance().isOnline())
            Toast.makeText(this, "you are not connected to the internet", Toast.LENGTH_LONG).show();
        else {
            mErrorToast = Toast.makeText(this, "Server error :(\nplease send us an email", Toast.LENGTH_LONG);
            mErrorToast.show();
        }
    }
}
