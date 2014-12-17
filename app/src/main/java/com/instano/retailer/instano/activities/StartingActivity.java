package com.instano.retailer.instano.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.instano.retailer.instano.IntroductionFragment;
import com.instano.retailer.instano.R;
import com.instano.retailer.instano.utilities.GlobalMenuActivity;

public class StartingActivity extends GlobalMenuActivity {

    View mContainerView;

    IntroductionFragment mIntroductionFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starting);
        mContainerView = findViewById(R.id.container);

        mIntroductionFragment = new IntroductionFragment();

        getFragmentManager().beginTransaction()
                .replace(mContainerView.getId(), mIntroductionFragment)
                .commit();
    }

    public void getStartedClicked(View view) {
        startActivity(new Intent(this, ProfileActivity.class));
    }

}
