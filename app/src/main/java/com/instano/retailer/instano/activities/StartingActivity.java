package com.instano.retailer.instano.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.instano.retailer.instano.IntroductionFragment;
import com.instano.retailer.instano.R;
import com.instano.retailer.instano.ServicesSingleton;
import com.instano.retailer.instano.utilities.GlobalMenuActivity;

public class StartingActivity extends GlobalMenuActivity {

    private static final int REQUEST_CODE = 1001;
    private static final int EXIT_DELAY = 1000; // in ms

    View mContainerView;

    IntroductionFragment mIntroductionFragment;

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
        mContainerView = findViewById(R.id.container);

        // be sure to initialize ServicesSingleton:
        ServicesSingleton instance = ServicesSingleton.getInstance(this);

        mIntroductionFragment = new IntroductionFragment();

        getFragmentManager().beginTransaction()
                .replace(mContainerView.getId(), mIntroductionFragment)
                .commit();

//        if (instance.signIn())
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (resultCode == RESULT_OK)
                    onProfileSetUp();
                // else user must have clicked back button
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void onProfileSetUp() {
        mIntroductionFragment.onProfileSetUp();
    }

    public void getStartedClicked(View view) {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

}
