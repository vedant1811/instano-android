package com.instano.retailer.instano.activities.signUp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.facebook.Session;
import com.instano.retailer.instano.R;
import com.instano.retailer.instano.activities.home.HomeActivity;
import com.instano.retailer.instano.application.BaseActivity;
import com.instano.retailer.instano.application.controller.Sessions;
import com.instano.retailer.instano.utilities.library.Log;

import rx.android.observables.AndroidObservable;

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

        connectWithFacebook.setOnClickListener(v ->
                AndroidObservable.bindActivity(this, Sessions.controller().doFacebookSignUp(this))
                        .subscribe(buyer -> {
                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                            finish();
                        }, Log::fatalError));
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session activeSession = Session.getActiveSession();
        if (activeSession != null)
            activeSession.onActivityResult(this, requestCode, resultCode, data);
    }
}
