package com.instano.retailer.instano.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ViewFlipper;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.NetworkRequestsManager;
import com.instano.retailer.instano.application.ServicesSingleton;
import com.instano.retailer.instano.utilities.GlobalMenuActivity;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.models.Buyer;

public class ProfileActivity extends GlobalMenuActivity
        implements NetworkRequestsManager.RegistrationCallback {

    private static final String ALREADY_TAKEN_ERROR = "already taken. Contact us if this is an error";
    private static final String TAG ="ProfileActivity";
    EditText mNameEditText;
    EditText mPhoneEditText;
    ViewFlipper mSetUpViewFlipper;
    Button mSetUpButton;

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
        Log.v(TAG,".onResume");
        NetworkRequestsManager.instance().registerCallback((NetworkRequestsManager.RegistrationCallback) this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mNameEditText = (EditText) findViewById(R.id.nameEditText);
        mPhoneEditText = (EditText) findViewById(R.id.phoneEditText);
        mSetUpViewFlipper = (ViewFlipper) findViewById(R.id.setUpViewFlipper);
        mSetUpButton = (Button) findViewById(R.id.setUpButton);

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
            mName = buyer.getName();
            mPhone = buyer.getPhone();
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

        // TODO: add feature of updating profile
        if (ServicesSingleton.instance().getBuyer() != null)
            onRegistration(NetworkRequestsManager.ResponseError.NO_ERROR);

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
        Buyer buyer = new Buyer();
        buyer.setName(mNameEditText.getText().toString());
        buyer.setPhone(mPhoneEditText.getText().toString()) ;
        Log.v(TAG,"Buyer in setup clicked"+buyer);
        NetworkRequestsManager.instance().registerRequest(buyer);
    }

    @Override
    public void phoneExists(boolean exists) {
        if (exists) {
            mPhoneEditText.setError(ALREADY_TAKEN_ERROR);
        }
    }

    @Override
    public void onRegistration(NetworkRequestsManager.ResponseError result) {
        mSetUpViewFlipper.setDisplayedChild(0); // button
        mViewFlipperState = 0; // so as to update it if activity is not resumed
        if (result == NetworkRequestsManager.ResponseError.NO_ERROR) {
            setResult(RESULT_OK);
            finish();
        }
        else if (result == NetworkRequestsManager.ResponseError.PHONE_EXISTS) {
            mPhoneEditText.setError(ALREADY_TAKEN_ERROR);
            mPhoneEditText.requestFocus();
        }
        else if(result == NetworkRequestsManager.ResponseError.AUTHENTICATION_ERROR) {
            authorizeSession(false,true);
            nonCancelableError("Authenticating","Syncing");
        }

        else if (NetworkRequestsManager.instance().isOnline()) {
            serverErrorDialog();
        }
        else if (result == NetworkRequestsManager.ResponseError.TIME_OUT) {
            nonCancelableError("Timed out","Trying again");
            onResume();
        }
        else
            noInternetDialog();

/*

        switch (result) {
            case NO_ERROR:
                setResult(RESULT_OK);
                finish();
                break;
            case PHONE_EXISTS:
                mPhoneEditText.setError(ALREADY_TAKEN_ERROR);
                mPhoneEditText.requestFocus();
                break;
            case AUTHENTICATION_ERROR:
                authorizeSession(false,true);
                nonCancelableError("Authenticating","Syncing");
                break;
            case TIME_OUT:

                break;
        }
*/
    }


}
