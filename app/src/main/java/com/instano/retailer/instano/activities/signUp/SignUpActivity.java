package com.instano.retailer.instano.activities.signUp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.BaseActivity;
import com.instano.retailer.instano.application.ServicesSingleton;
import com.instano.retailer.instano.utilities.library.Log;

import java.util.Arrays;

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
                Session session = Session.getActiveSession().openActiveSessionFromCache(SignUpActivity.this);
                Log.v(TAG, "Session getActiveSession :"+Session.getActiveSession());
                if(Session.getActiveSession() == null){
                    if(session == null)
                        openFacebookSession();
                    else
                        Log.v(TAG, "Session getActiveSessionFromCache is not null");
                }

                else
                    Log.v(TAG, "Session getActiveSession is not null");
            }
        });
    }

    private void openFacebookSession(){
        String[] list={ "user_likes", "user_status","user_birthday","email","public_profile"};
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
                                Log.v(TAG, "Birthday: " + user.getBirthday());
                                Log.v(TAG, "UserId: " + user.getId());
                                Log.v(TAG, "User birthday : " + user.getName());
                                Log.v(TAG, "Response : " + response);
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
