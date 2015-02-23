package com.instano.retailer.instano.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ViewFlipper;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.NetworkRequestsManager;
import com.instano.retailer.instano.application.ServicesSingleton;
import com.instano.retailer.instano.utilities.GlobalMenuActivity;
import com.instano.retailer.instano.utilities.models.Buyer;

import java.util.Arrays;
import java.util.List;

public class ProfileActivity extends GlobalMenuActivity
        implements NetworkRequestsManager.RegistrationCallback {

    private static final String ALREADY_TAKEN_ERROR = "already taken. Contact us if this is an error";
    private static final String TAG = "Fb Login";

    EditText mNameEditText;
    EditText mPhoneEditText;
    ViewFlipper mSetUpViewFlipper;
    Button mSetUpButton;

    CharSequence mName;
    CharSequence mPhone;
    int mViewFlipperState;
    String mFbId,mFbName;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (state.isOpened()) {
            Log.i(TAG, "Logged in...");

        } else if (state.isClosed()) {
            Log.i(TAG, "Logged out...");
        }
    }

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

        NetworkRequestsManager.instance().registerCallback(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mNameEditText = (EditText) findViewById(R.id.nameEditText);
        mPhoneEditText = (EditText) findViewById(R.id.phoneEditText);
        mSetUpViewFlipper = (ViewFlipper) findViewById(R.id.setUpViewFlipper);
        mSetUpButton = (Button) findViewById(R.id.setUpButton);
        Button connect = (Button) findViewById(R.id.connect);


        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFacebookSession();
            }
        });



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

    private void openFacebookSession(){
        String[] list={"user_likes", "user_status"};
        Session.openActiveSession(this, true, Arrays.asList( list   ), new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                if (state.isOpened()) {
                    Log.i(TAG, "Logged in...");
                    Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {
                        public void onCompleted(GraphUser user, Response response) {
                            if (response != null) {
                                // do something with <response> now
                                try {
                                    mFbId = user.getUsername();
                                    mFbName = user.getName();
                            /*get_gender = (String) user.getProperty("gender");
                            get_email = (String) user.getProperty("email");
                            get_birthday = user.getBirthday();
                            get_locale = (String) user.getProperty("locale");
                            get_location = user.getLocation().toString();
                            */
                                    Log.d(TAG, user.getId() + "; " +
                                            user.getName() + "; " +
                                            (String) user.getProperty("gender") + "; " +
                                            (String) user.getProperty("email") + "; " +
                                            user.getBirthday() + "; " +
                                            (String) user.getProperty("locale") + "; " +
                                            user.getLocation());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Log.d(TAG, "Exception e");
                                }

                            }
                        }
                    });

                } else if (state.isClosed()) {
                    Log.i(TAG, "Logged out...");
                }
                // you can make request to the /me API or do other stuff like post, etc. here
            }
        });
    }

    private static Session openActiveSession(Activity activity, boolean allowLoginUI, List permissions, Session.StatusCallback callback) {
        Session.OpenRequest openRequest = new Session.OpenRequest(activity).setPermissions(permissions).setCallback(callback);
        Session session = new Session.Builder(activity).build();
        if (SessionState.CREATED_TOKEN_LOADED.equals(session.getState()) || allowLoginUI) {
            Session.setActiveSession(session);
            session.openForRead(openRequest);
            return session;
        }
        return null;
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
            onRegistration(Result.NO_ERROR);

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
        NetworkRequestsManager.instance().registerRequest(buyer);
    }

    @Override
    public void phoneExists(boolean exists) {
        if (exists) {
            mPhoneEditText.setError(ALREADY_TAKEN_ERROR);
        }
    }

    @Override
    public void onRegistration(Result result) {
        mSetUpViewFlipper.setDisplayedChild(0); // button
        mViewFlipperState = 0; // so as to update it if activity is not resumed
        if (result == Result.NO_ERROR) {
            setResult(RESULT_OK);
            finish();
        }
        else if (result == Result.PHONE_EXISTS) {
            mPhoneEditText.setError(ALREADY_TAKEN_ERROR);
            mPhoneEditText.requestFocus();
        }
        else if (NetworkRequestsManager.instance().isOnline()) {
            serverErrorDialog();
        }
        else
            noInternetDialog();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }
}
