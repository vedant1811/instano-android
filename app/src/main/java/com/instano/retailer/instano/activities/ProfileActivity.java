package com.instano.retailer.instano.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.instano.retailer.instano.application.NetworkRequestsManager;
import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.ServicesSingleton;
import com.instano.retailer.instano.utilities.GlobalMenuActivity;
import com.instano.retailer.instano.utilities.models.Buyer;

public class ProfileActivity extends GlobalMenuActivity
        implements NetworkRequestsManager.RegistrationCallback {

    private static final String ALREADY_TAKEN_ERROR = "already taken. Contact us if this is an error";

    EditText mNameEditText;
    EditText mPhoneEditText;
    ViewFlipper mSetUpViewFlipper;
    Button mSetUpButton;

    private Toast mErrorToast;

    CharSequence mName;
    CharSequence mPhone;
    int mViewFlipperState;

    @Override
    protected void onPause() {
        super.onPause();
        mName = mNameEditText.getText();
        mPhone = mPhoneEditText.getText();
        mViewFlipperState = mSetUpViewFlipper.getDisplayedChild();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNameEditText.setText(mName);
        mPhoneEditText.setText(mPhone);
        mSetUpViewFlipper.setDisplayedChild(mViewFlipperState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mNameEditText = (EditText) findViewById(R.id.nameEditText);
        mPhoneEditText = (EditText) findViewById(R.id.phoneEditText);
        mSetUpViewFlipper = (ViewFlipper) findViewById(R.id.setUpViewFlipper);
        mSetUpButton = (Button) findViewById(R.id.setUpButton);

        NetworkRequestsManager.instance().registerCallback(this);

        mPhoneEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String text = mPhoneEditText.getText().toString();
                if (!hasFocus && !text.equals("")) {
                    if (checkPhoneNumber())
                        NetworkRequestsManager.instance().buyerExistsRequest(text);
                }
            }
        });

        // check if a user exists:
        Buyer buyer = ServicesSingleton.instance().getBuyer();
        if (buyer != null) {
            mName = buyer.name;
            mPhone = buyer.phone;
            // TODO: this and other things, including saving state onPause
//            mSetUpButton.setText("Update");
        }
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
            mPhoneEditText.requestFocus();
            return false;
        }
        else {
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
                "+91 " + mPhoneEditText.getText().toString()
        ) ;
        NetworkRequestsManager.instance().registerRequest(buyer);
    }

    @Override
    public void phoneExists(boolean exists) {
        if (exists) {
            mPhoneEditText.setError(ALREADY_TAKEN_ERROR);
            if (mErrorToast != null)
                mErrorToast.cancel();
        }
    }

    @Override
    public void onRegistration(Result result) {
        mSetUpViewFlipper.setDisplayedChild(0); // button
        mViewFlipperState = 0; // so as update it if activity is not resumed
        if (result == Result.NO_ERROR) {
            setResult(RESULT_OK);
            finish();
        }
        else if (result == Result.PHONE_EXISTS) {
            mPhoneEditText.setError(ALREADY_TAKEN_ERROR);
            mPhoneEditText.requestFocus();
        }
        else if (NetworkRequestsManager.instance().isOnline(true)) {
            mErrorToast = Toast.makeText(this, "Server error :(\nplease send us an email", Toast.LENGTH_LONG);
            mErrorToast.show();
        } // if it is not not online a toast is already shown. see link{NetworkRequestsManager#isOnline(true)}
    }
}
