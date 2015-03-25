package com.instano.retailer.instano.activities;

import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Solo;

/**
 * Created by ROHIT on 25-Mar-15.
 */
public class AppTest extends ActivityInstrumentationTestCase2<LauncherActivity> {

    private Solo solo;

    public AppTest() {
        super(LauncherActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void testLauncher() {
        
        solo.assertCurrentActivity("Expected Launcher Activity","LauncherActivity");
    }


    @Override
    protected void tearDown() throws Exception {

        solo.finishOpenedActivities();
    }
}
