package com.instano.retailer.instano.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.instano.retailer.instano.NetworkRequestsManager;
import com.instano.retailer.instano.R;
import com.instano.retailer.instano.ServicesSingleton;
import com.instano.retailer.instano.utilities.GlobalMenuActivity;

public class StartingActivity extends GlobalMenuActivity implements ServicesSingleton.SignInCallbacks {

    private static final String SEARCH_ICON_HELP = "You can Search for products by clicking the icon in the action bar";
    private static final int SETUP_REQUEST_CODE = 1001;
    private static final int EXIT_DELAY = 1000; // in ms

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
        ServicesSingleton instance = ServicesSingleton.getInstance(this);

        if (instance.signIn(this)) {
            mText = "Welcome back! " + SEARCH_ICON_HELP;
            mStatus = Status.SIGNING_IN;
        }
        else {
            mText = mTextView.getText();
            mStatus = Status.FIRST_TIME;
        }
    }

    @Override
    public void signedIn(boolean success) {
        if (success) {
            mStatus = Status.SIGNED_IN;
            return;
        }
        else if (NetworkRequestsManager.instance().isOnline(true))
            Toast.makeText(this, "sign in error!\ncreate a new profile or contact us", Toast.LENGTH_LONG).show();

        mStatus = Status.ERROR_SIGN_IN;
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
        switch (mStatus) {
            case FIRST_TIME:
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivityForResult(intent, SETUP_REQUEST_CODE);
                break;
            default:
                search();
        }
    }

}
