package com.instano.retailer.instano.activities.signUp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.Session;
import com.instano.retailer.instano.R;
import com.instano.retailer.instano.activities.home.HomeActivity;
import com.instano.retailer.instano.application.BaseActivity;
import com.instano.retailer.instano.application.controller.Sessions;
import com.instano.retailer.instano.utilities.library.Log;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.observables.AndroidObservable;
import rx.subscriptions.BooleanSubscription;

/**
 * Created by Rohit on 30/4/15.
 */
public class SignUpActivity extends BaseActivity {

    private static final String TAG = "SignUpActivity";
    Button connectWithFacebook;

    Subscription mTimerSubscription = BooleanSubscription.create();
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        connectWithFacebook = (Button) findViewById(R.id.connectWithFacebook);

        connectWithFacebook.setOnClickListener(v -> {
            mDialog = ProgressDialog.show(this, "Signing in", "Connecting with facebook");
            AndroidObservable.bindActivity(this, Sessions.controller().doFacebookSignUp(this)
                    .doOnUnsubscribe(() -> Log.d(TAG, "unsubscribed")))
                    .subscribe(aClass -> {
                        Log.d(TAG, "starting home activity");
                        startActivity(new Intent(this, HomeActivity.class));
                        mDialog.dismiss();
                        finish();
                    }, Log::fatalError);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "on resume, finishing:" + isFinishing());
        if (mDialog != null && mDialog.isShowing()) {
            mTimerSubscription = Observable.timer(5, TimeUnit.SECONDS).subscribe(t -> {
                        mDialog.hide();
                        Toast.makeText(this, "Sorry an error occoured. Try again", Toast.LENGTH_LONG).show();
                    },
                    throwable -> Log.fatalError(new RuntimeException(throwable)));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "on pause, finishing:" + isFinishing());
        mTimerSubscription.unsubscribe();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "on destroy");
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session activeSession = Session.getActiveSession();
        if (activeSession != null)
            activeSession.onActivityResult(this, requestCode, resultCode, data);
        Log.d(TAG, "on onActivityResult, finishing:" + isFinishing());
    }

}
