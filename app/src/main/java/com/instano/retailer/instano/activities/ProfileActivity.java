package com.instano.retailer.instano.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ViewFlipper;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.network.NetworkRequestsManager;
import com.instano.retailer.instano.application.ServicesSingleton;
import com.instano.retailer.instano.application.network.ResponseError;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.models.Buyer;

import rx.android.observables.AndroidObservable;

public class ProfileActivity extends GlobalMenuActivity {

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
        Log.v(TAG, ".onResume");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mNameEditText = (EditText) findViewById(R.id.nameEditText);
        mPhoneEditText = (EditText) findViewById(R.id.phoneEditText);
        mSetUpViewFlipper = (ViewFlipper) findViewById(R.id.setUpViewFlipper);
        mSetUpButton = (Button) findViewById(R.id.setUpButton);

        // TODO:
//        mPhoneEditText.setOnFocusChangeListener((v, hasFocus) -> {
//            String text = mPhoneEditText.getText().toString();
//            if (!hasFocus && !text.equals("")) {
//                if (checkPhoneNumber())
//                    NetworkRequestsManager.instance().buyerExistsRequest(text);
//            }
//        });

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
            finishWithResultOk();

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
        Buyer newBuyer = new Buyer();
        newBuyer.setName(mNameEditText.getText().toString());
        newBuyer.setPhone(mPhoneEditText.getText().toString()) ;
        Log.v(TAG, "Buyer in setup clicked" + newBuyer);
        AndroidObservable.bindActivity(this,
                NetworkRequestsManager.instance().registerBuyer(newBuyer))
                .subscribe(createdBuyer -> finishWithResultOk(),
                        throwable -> {
                            mSetUpViewFlipper.setDisplayedChild(0); // button
                            mViewFlipperState = 0; // so as to update it if activity is not resumed
                            if (ResponseError.Type.PHONE_EXISTS.is(throwable)) {
                                mPhoneEditText.setError(ALREADY_TAKEN_ERROR);
                                mPhoneEditText.requestFocus();
                            } else
                                showErrorDialog(throwable);
                        });
    }

    private void finishWithResultOk() {
        setResult(RESULT_OK);
        finish();
    }
}
