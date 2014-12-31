package com.instano.retailer.instano.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.instano.retailer.instano.application.NetworkRequestsManager;
import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.ServicesSingleton;
import com.instano.retailer.instano.utilities.GlobalMenuActivity;

public class StartingActivity extends GlobalMenuActivity implements NetworkRequestsManager.SignInCallbacks {

    private static final String SEARCH_ICON_HELP = "You can Search for products by clicking the icon in the action bar";
    private static final int SETUP_REQUEST_CODE = 1001;
    private static final int EXIT_DELAY = 2000; // in ms
    private static final String WELCOME_BACK = "Welcome back! ";

    TextView mTextView;
    Button mButton;

    CharSequence mText;
    boolean mIsExiting = false;

    @Override
    public void onBackPressed() {
        if (mIsExiting)
            super.onBackPressed(); // basically exit
        else {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
            mIsExiting = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mIsExiting = false;
                }
            }, EXIT_DELAY);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starting);

        mTextView = (TextView) findViewById(R.id.textView);
        mButton = (Button) findViewById(R.id.button);
        // be sure to initialize ServicesSingleton:
        ServicesSingleton instance = ServicesSingleton.instance();

        // in case buyer already has signed in (activity was killed for some reason) do not sign in again
        NetworkRequestsManager.instance().registerCallback(this);
        if (instance.getBuyer() != null || instance.signIn()) {
            mText = WELCOME_BACK + SEARCH_ICON_HELP;
        }
        else {
            mText = mTextView.getText();
        }
    }

    @Override
    public void signedIn(boolean success) {
        if (!success){
            if (NetworkRequestsManager.instance().isOnline())
                serverErrorDialog();
            else
                noInternetDialog();
        }
    }

    /**
     * You will receive this call immediately before onResume() when your activity is re-starting.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SETUP_REQUEST_CODE:
                if (resultCode == RESULT_OK)
                    // since onResume is yet to be called.
                    mText = "Your profile has been set up. " + SEARCH_ICON_HELP;
                // else user must have clicked back button
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTextView.setText(mText);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mText = mTextView.getText();
    }

    public void getStartedClicked(View view) {
        if (mText.toString().contains(SEARCH_ICON_HELP)) {
            search();
        } else {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivityForResult(intent, SETUP_REQUEST_CODE);
        }
    }

}
