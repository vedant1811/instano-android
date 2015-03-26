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

    public void testLauncher() throws InterruptedException {

        solo.waitForActivity("StartingActivity"); // Waiting for StartingActivity
        solo.assertCurrentActivity("Expected Starting Activity", "StartingActivity");
        solo.clickOnButton("Get Started"); //clicking Get Started Button in StartingActivity
        //Profile Activity for Registration
        solo.enterText(0,"Rohit");
        solo.enterText(1,"2345678910");    //TODO: Change Phone number manually
        solo.clickOnButton("Set Up");
        // Waiting for StartingActivity after Registration
        solo.waitForActivity("StartingActivity");
        solo.assertCurrentActivity("Expected Starting Activity","StartingActivity");
        solo.clickOnButton("Get Started");
        // SearchTabActivity
        solo.pressSpinnerItem(0,10); //Selecting Product Category
        solo.enterText(0,"Note Edge"); // Entering model description
        solo.clickOnButton("near your location");
        // waiting for SelectLocationActivity
        solo.waitForActivity("SelectLocationActivity");
        while(solo.getText(0).getText().toString() == "Fetching address...") {
            solo.sleep(5000);  // making sure that address has been fetched
        }
        solo.clickOnImageButton(0); // Selecting current Location
        solo.waitForActivity("SearchTabsActivity");
        solo.clickOnButton("Next"); // Proceeding to SearchConstraintFragment
        solo.clickOnButton("Search");
        solo.sleep(5000);
        solo.finishOpenedActivities();
    }

    public void testSignedIn() throws InterruptedException {
        // Waiting for StartingActivity after Registration
        solo.waitForActivity("StartingActivity");
        solo.assertCurrentActivity("Expected Starting Activity","StartingActivity");
        solo.clickOnButton("Get Started");
        solo.pressSpinnerItem(0,8); //Selecting Product Category
        solo.enterText(0,"Hp Pavillion"); // Entering model description
        solo.clickOnButton("near your location");
        solo.waitForActivity("SelectLocationActivity");
        while(solo.getText(0).getText().toString() == "Fetching address...") {
            solo.sleep(5000);  // making sure that address has been fetched
        }
        solo.clickOnImageButton(0); // Selecting current Location
        solo.waitForActivity("SearchTabsActivity");
        solo.clickOnButton("Next"); // Proceeding to SearchConstraintFragment
        solo.clickOnButton("Search");
        solo.sleep(5000);
        solo.finishOpenedActivities();

    }


    @Override
    protected void tearDown() throws Exception {

        solo.finishOpenedActivities();
    }
}
