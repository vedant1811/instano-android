package com.instano.retailer.instano.activities;

import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Solo;

import java.util.Random;

/**
 * Created by ROHIT on 25-Mar-15.
 */
public class AppTest extends ActivityInstrumentationTestCase2<LauncherActivity> {

    private static final String TAG = "ActivityInstrumentationTestCase2";
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

        Random random = new Random();
        int phno = random.nextInt()*1000000000;
        if(phno<0)
            phno = phno * -1;
        solo.unlockScreen();
        solo.waitForActivity("StartingActivity"); // Waiting for StartingActivity
        solo.assertCurrentActivity("Expected Starting Activity", "StartingActivity");
        solo.clickOnButton("Get Started"); //clicking Get Started Button in StartingActivity
        //Profile Activity for Registration
        solo.enterText(0,"Rohit");
        solo.enterText(1,""+phno);    //TODO: Change Phone number manually
        solo.clickOnButton("Set Up");
        boolean textExist = solo.waitForText("already taken. Contact us if this is an error",1,1000)
                || solo.waitForText("Enter 10 digits",1,1000);
        while(textExist) {
            phno = random.nextInt()*1000000000;
            if(phno<0)
                phno = phno * -1;

            solo.clearEditText(1);
            solo.enterText(1, "" + phno);
            solo.clickOnButton("Set Up");
//            solo.wait(1500);
            textExist = solo.waitForText("already taken. Contact us if this is an error",1,1000)||
                    solo.waitForText("Enter 10 digits",1,1000);
        }
        // Waiting for StartingActivity after Registration
        solo.waitForActivity("StartingActivity");
        solo.assertCurrentActivity("Expected Starting Activity","StartingActivity");
/*
        NetworkRequestsManager networkRequestsManager = NetworkRequestsManager.instance();

        Method privateStringField = null;
        try {
            privateStringField = NetworkRequestsManager.class.getDeclaredMethod("getSessionId", null);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        privateStringField.setAccessible(true);
        String fieldValue = null;
        fieldValue = privateStringField.toString();
        Log.v(TAG,".testLauncher fieldValue(sessionId) = " + fieldValue);
        Log.v(TAG,".testLauncher "+ fieldValue);
*/

        solo.clickOnButton("Get Started");
        // SearchTabActivity
        solo.pressSpinnerItem(0,10); //Selecting Product Category
        solo.enterText(0, "Fake Note Edge"); // Entering model description
        if(solo.searchButton("near your location")) {
            solo.clickOnButton("near your location");
        }
        else if(solo.searchButton("please select location")) {
            solo.clickOnButton("please select location");
        }
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
        solo.assertCurrentActivity("Expected Starting Activity", "StartingActivity");
        solo.clickOnButton("Get Started");
        solo.pressSpinnerItem(0, 8); //Selecting Product Category
        solo.enterText(0, "Fake Hp Pavillion"); // Entering model description
        if(solo.searchButton("near your location")) {
            solo.clickOnButton("near your location");
        }
        else if(solo.searchButton("please select location")) {
            solo.clickOnButton("please select location");
        }
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
