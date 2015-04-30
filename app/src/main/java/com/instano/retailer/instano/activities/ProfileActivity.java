package com.instano.retailer.instano.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ViewFlipper;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.ServicesSingleton;
import com.instano.retailer.instano.application.network.NetworkRequestsManager;
import com.instano.retailer.instano.application.network.ResponseError;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.models.Buyer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    Session facebookSession;
    List<String> mPermissions = new ArrayList<String>();

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
       public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
         }
    };

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (state.isOpened()) {
            Log.v(TAG, "Logged in...");

        } else if (state.isClosed()) {
            Log.v(TAG, "Logged out...");
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
        LoginButton facebookLogin = (LoginButton) findViewById(R.id.fbLogin);

        mPermissions.add("public_profile");
        mPermissions.add("user_location");
        mPermissions.add("email");
        mPermissions.add("user_birthday");
        mPermissions.add("user_friends");
        facebookLogin.setReadPermissions(mPermissions);

        // TODO:
//        mPhoneEditText.setOnFocusChangeListener((v, hasFocus) -> {
//            String text = mPhoneEditText.getText().toString();
//            if (!hasFocus && !text.equals("")) {
//                if (checkPhoneNumber())
//                    NetworkRequestsManager.instance().buyerExistsRequest(text);
//            }
//        });

        facebookLogin.setOnClickListener(new View.OnClickListener() {
            @Override
           public void onClick(View view) {
                if(Session.getActiveSession() == null )
                    openActiveSession(ProfileActivity.this, true, mPermissions,callback);
                else {
                    Request.newMeRequest(Session.getActiveSession(), new Request.GraphUserCallback() {
                        @Override
                        public void onCompleted(GraphUser user, Response response) {
                            Log.v(TAG, "Birthday: "+user.getBirthday());
                            Log.v(TAG, "UserId: "+user.getId());
                            Log.v(TAG, "User birthday : " + user.getName());
                            Log.v(TAG,"Response : "+response);

                        }
                    }).executeAsync();
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
                String[] list={"user_likes", "user_status","name","user_birthday","email","public_profile"};
                Session.openActiveSession(this, true, Arrays.asList(list), new Session.StatusCallback() {
                    @Override
                    public void call(Session session, SessionState state, Exception exception) {
                        if (state.isOpened()) {
                            ServicesSingleton.instance().facebookSession = session;
                            Log.v(TAG, "Logged in...");
                        }
                        else if (state.isClosed()) {
                             Log.v(TAG, "Logged out...");
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
        retryableError(
                NetworkRequestsManager.instance().registerBuyer(newBuyer),
                buyer -> {
                    ServicesSingleton.instance().saveBuyer(buyer);
                    NetworkRequestsManager.instance().newBuyer(buyer);
                    finishWithResultOk();
                },
                throwable -> {
                    ServicesSingleton.instance().removeFirstTime();
                    mSetUpViewFlipper.setDisplayedChild(0); // button
                    mViewFlipperState = 0; // so as to update it if activity is not resumed
                    if (ResponseError.Type.PHONE_EXISTS.is(throwable)) {
                        mPhoneEditText.setError(ALREADY_TAKEN_ERROR);
                        mPhoneEditText.requestFocus();
                        return true; // error was handled, no need to show dialog
                    } else
                        return false; // another error, show dialog
                });
    }

    private void finishWithResultOk() {
        setResult(RESULT_OK);
        finish();
    }
    @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }


}
