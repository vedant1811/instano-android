package com.instano.retailer.instano.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.instano.retailer.instano.R;
import com.instano.retailer.instano.activities.home.HomeActivity;
import com.instano.retailer.instano.activities.signUp.SignUpActivity;
import com.instano.retailer.instano.application.ServicesSingleton;
import com.instano.retailer.instano.application.network.NetworkRequestsManager;
import com.instano.retailer.instano.application.network.ResponseError;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.models.Buyer;

import rx.Observable;


public class LauncherActivity extends GlobalMenuActivity {

    private static final String MESSAGE_DIALOG_FRAGMENT = "MessageDialogFragment";
    // Splash screen timer
    private static int SPLASH_TIME_OUT = 2500;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final String TAG = "LauncherActivity";

    boolean mBackPressed = false;
    boolean mTimedOut = false;
    boolean mErrorOccurred = true;
    boolean mFacebookSession = false;
    private ProgressDialog mProgressDialog;
    boolean mOpenHome = false;

    @Override
    public void onBackPressed() {
        // make sure the next activity is not started
        mBackPressed = true;
        // finish the activity
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        if (!ServicesSingleton.instance().isFirstTime())
            mTimedOut = true; // do not wait for timeout if app is not being used for first time
            Session session = Session.openActiveSessionFromCache(LauncherActivity.this);
            Log.v(TAG, "Session getActiveSession :"+Session.getActiveSession());
            if(Session.getActiveSession() == null) {
                if(session == null)
                    mFacebookSession = false;
                else {
                    Log.v(TAG, "Session getActiveSessionFromCache is not null");
                    mFacebookSession = true;
                    Log.v(TAG, "Logged in...");
                    Request.newMeRequest(session, new Request.GraphUserCallback() {
                        @Override
                        public void onCompleted(GraphUser user, Response response) {
                            if (user != null) {
                                Log.v(TAG, "Birthday: " + user.getBirthday());
                                Log.v(TAG, "UserId: " + user.getId());
                                Log.v(TAG, "User birthday : " + user.getName());
                                Log.v(TAG, "Response : " + response);
                            } else
                                Log.v(TAG, "user is null");

                        }
                    }).executeAsync();
                }
            }
            else {
                Log.v(TAG, "Session getActiveSession is not null");
                mFacebookSession = true;
                Request.newMeRequest(session, new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        if (user != null) {
                            Log.v(TAG, "Birthday: " + user.getBirthday());
                            Log.v(TAG, "UserId: " + user.getId());
                            Log.v(TAG, "User birthday : " + user.getName());
                            Log.v(TAG, "Response : " + response);
                        } else
                            Log.v(TAG, "user is null");

                    }
                }).executeAsync();
            }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mProgressDialog = new ProgressDialog(this);
        if (checkPlayServices()) {
            retryableError(NetworkRequestsManager.instance().authorizeSession(),
                    (device) -> {
                        mErrorOccurred = false;
                        if(mFacebookSession) {
                            ServicesSingleton instance = ServicesSingleton.instance();

                            Observable<Buyer> buyerObservable = instance.signIn();
                            if (instance.getBuyer() != null || buyerObservable != null) {
                                mProgressDialog = ProgressDialog.show(this, "Signing In", "Please wait...", false, false);
                            //TODO Remove Signing in whats the point of signing when Sesion is active
                                retryableError(buyerObservable,
                                        buyer -> {
                                            mProgressDialog.dismiss();
                                            Toast.makeText(this, String.format("Welcome %s", buyer.getName()), Toast.LENGTH_SHORT).show();
                                            ServicesSingleton.instance().saveBuyer(buyer);
                                            NetworkRequestsManager.instance().newBuyer(buyer);
                                            mOpenHome = true;
                                            closeIfPossible();
                                        },
                                        error -> {
                                            mProgressDialog.dismiss();
                                            if (ResponseError.Type.INCORRECT_API_KEY.is(error)) {
                                                ServicesSingleton.instance().removeFirstTime();
                                                Toast.makeText(this, "Saved data error. Create a new profile", Toast.LENGTH_SHORT).show();
                                                return true;
                                            }
                                            return false;
                                        });
                            }
                        }
                        else
                            closeIfPossible();

                            });
        } else {
            contactUs("No Play Services", "Google Play Services is needed for the app. " +
                    "Contact us directly instead.");
            Log.v(TAG, "No valid Google Play Services APK found.");
            return;
        }
        if (!mTimedOut)
            new Handler().postDelayed(() -> {
                // This method will be executed once the timer is over
                mTimedOut = true;
                closeIfPossible();
            }, SPLASH_TIME_OUT);
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.v(TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }

    private void closeIfPossible() {
        if (!mTimedOut || mErrorOccurred)
            return;
        // initialize the app if it wasn't cancelled (like due to back button being pressed):
        if (!mBackPressed) {
            Intent i = null;
//        if (ServicesSingleton.getInstance(this).isFirstTime())
            if(mOpenHome)
                i = new Intent(this, HomeActivity.class);
            else
                i = new Intent(this, SignUpActivity.class);
            startActivity(i);
        }
        // close this activity
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }
}
