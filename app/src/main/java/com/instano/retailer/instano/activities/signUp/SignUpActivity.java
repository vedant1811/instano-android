package com.instano.retailer.instano.activities.signUp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.instano.retailer.instano.R;
import com.instano.retailer.instano.activities.home.HomeActivity;
import com.instano.retailer.instano.application.BaseActivity;
import com.instano.retailer.instano.application.ServicesSingleton;
import com.instano.retailer.instano.application.controller.User;
import com.instano.retailer.instano.application.network.NetworkRequestsManager;
import com.instano.retailer.instano.application.network.ResponseError;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.model.Buyer;
import com.instano.retailer.instano.utilities.models.FacebookUser;

import java.util.Arrays;

import rx.functions.Action1;

/**
 * Created by Rohit on 30/4/15.
 */
public class SignUpActivity extends BaseActivity {

    private static final String TAG = "SignUp";
    Button connectWithFacebook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        connectWithFacebook = (Button) findViewById(R.id.connectWithFacebook);

        connectWithFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Session session = Session.openActiveSessionFromCache(SignUpActivity.this);
                Log.v(TAG, "Session getActiveSession :" + Session.getActiveSession());
                if (Session.getActiveSession() == null) {
                    if (session == null)
                        openFacebookSession();
                    else
                        Log.v(TAG, "Session getActiveSessionFromCache is not null");
                } else
                    Log.v(TAG, "Session getActiveSession is not null");
            }
        });
    }

    private void openFacebookSession(){
        String[] list={ "user_likes", "user_status","user_birthday","email","public_profile","user_friends"};
        Session.openActiveSession(this, true, Arrays.asList(list), new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                if (state.isOpened()) {
                    ServicesSingleton.instance().facebookSession = session;
                    Log.v(TAG, "Logged in...");
                    Request.newMeRequest(Session.getActiveSession(), new Request.GraphUserCallback() {
                        @Override
                        public void onCompleted(GraphUser user, Response response) {
                            if (user != null) {
                                Log.v(TAG, "Email: " + user.getProperty("email"));
                                Log.v(TAG, "UserId: " + user.getId());
                                Log.v(TAG, "User name : " + user.getName());
                                Log.v(TAG, "Response : " + response);

                                Buyer newBuyer = new Buyer();
                                newBuyer.setFacebookUser(new FacebookUser());
                                FacebookUser newUser = newBuyer.getFacebookUser();
                                newUser.setId(user.getId());
                                newUser.setName(user.getName());
                                newUser.setEmail(user.getProperty("email").toString());
                                newUser.setVerified(user.getProperty("verified").toString());
                                newUser.setUserUpdatedAt(user.getProperty("updated_time").toString());
                                newUser.setGender(user.getProperty("gender").toString());
                                Log.v(TAG, "Buyer in SignUp clicked" + newBuyer);
                                retryableError(
                                        NetworkRequestsManager.instance().registerBuyer(newBuyer),
                                        new Action1<Buyer>() {
                                            @Override
                                            public void call(Buyer buyer) {
                                                User.controller().newSignUp(newBuyer);
                                                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                                finish();
                                            }
                                        },
                                        throwable -> {
                                            User.controller().removeFirstTime();

                                            if (ResponseError.Type.PHONE_EXISTS.is(throwable)) {
                                                //TODO
                                                return true; // error was handled, no need to show dialog
                                            } else
                                                return false; // another error, show dialog
                                        });
                                new Request(Session.getActiveSession(),
                                        "/me/friends",
                                        null,
                                        HttpMethod.GET,
                                        new Request.Callback() {
                                            public void onCompleted(Response response) {
            /* handle the result */
                                                Log.v(TAG, "response for friends : " + response);
                                            }
                                        }
                                ).executeAsync();
                            } else
                                Log.v(TAG, "user is null");

                        }
                    }).executeAsync();


                } else if (state.isClosed()) {
                    Log.v(TAG, "Logged out...");
                }
                // you can make request to the /me API or do other stuff like post, etc. here
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }
}
