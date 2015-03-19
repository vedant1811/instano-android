package com.instano.retailer.instano.utilities;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.instano.retailer.instano.R;
import com.instano.retailer.instano.activities.MessageDialogFragment;
import com.instano.retailer.instano.activities.ProfileActivity;
import com.instano.retailer.instano.application.BaseActivity;
import com.instano.retailer.instano.application.GcmIntentService;
import com.instano.retailer.instano.application.NetworkRequestsManager;
import com.instano.retailer.instano.application.ServicesSingleton;
import com.instano.retailer.instano.buyerDashboard.quotes.QuoteListActivity;
import com.instano.retailer.instano.deals.DealListActivity;
import com.instano.retailer.instano.search.SearchTabsActivity;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.models.Device;

import java.io.IOException;

/**
 * Base class for activities with a common menu (menu.global)
 * Actions common to activies should go here (like checking connectivity in {@link Activity#onResume()}
 *
 * different actions call different methods that may be overridden by implementing classes
 *
 * Created by vedant on 15/12/14.
 */
public abstract class GlobalMenuActivity extends BaseActivity implements NetworkRequestsManager.SessionIdCallback {
    private static final String TAG = "GlobalMenuActivity";
    public static final int PICK_CONTACT_REQUEST_CODE = 996;
    public static final int SEND_SMS_REQUEST_CODE = 995;

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final int ERROR_DIALOG_DELAY_MILLIS = 5000;
    private GcmIntentService mGcmIntentService;
    private boolean mRegId = false;
    private boolean mSesId = false;

    String SENDER_ID = "187047464172";
    /**
     * used by subclasses as well
     */
    public static final int MESSAGE_REQUEST_CODE = 997;

    private static final int SHARE_REQUEST_CODE = 998;

    protected static final String HOW_DO_YOU_WANT_TO_CONTACT_US = "How do you want to contact us";
    private static final String TEXT_OFFLINE_QUERY = "You can send a query directly by any of the following";
    private static final String MESSAGE_DIALOG_FRAGMENT = "MessageDialogFragment";

    public static final String PLAY_STORE_LINK = "http://play.google.com/store/apps/details?id=com.instano.buyer";
    private GoogleCloudMessaging mGcm;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // TODO: handle separately
            case SHARE_REQUEST_CODE:
            case MESSAGE_REQUEST_CODE:
            case PICK_CONTACT_REQUEST_CODE:
            case SEND_SMS_REQUEST_CODE:
                if (resultCode == RESULT_OK)
                    Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!NetworkRequestsManager.instance().isOnline())
            noInternetDialog();
    }

    public void authorizeSession(boolean refreshRegistrationId, boolean refreshSessionId) {
        String registrationId = getRegistrationId();
        if (refreshSessionId &&(refreshRegistrationId || registrationId.isEmpty())) {
            fetchGcmRegIdAsync();
        }
        else if(refreshSessionId)
                registerNewSession(registrationId);
        else onSessionResponse(NetworkRequestsManager.ResponseError.NO_SESSION_ID);
        }

        /*else {
            Device device = new Device();
            device.setSession_id(sessionId);
            onSessionResponse();
        }*/


    protected String getRegistrationId() {
        Context context = getApplicationContext();
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.v(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.v(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }



    public void onSessionResponse(NetworkRequestsManager.ResponseError error) {
        Log.v(TAG, "Response Error in Global "+error);
        if(error == NetworkRequestsManager.ResponseError.NO_SESSION_ID ||
                error ==NetworkRequestsManager.ResponseError.INCORRECT_SESSION_ID) {
            contactUs("Trouble connecting", "Please wait we are trying to connect or contact us",false);
            authorizeSession(false, true);
        }
        else {
            cancelDialog();
        }
    }
    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the registration ID in your app is up to you.
        return getSharedPreferences(GlobalMenuActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    protected void fetchGcmRegIdAsync() {
        new AsyncTask<Void,Void,String>() {

            ProgressDialog progress = new ProgressDialog(GlobalMenuActivity.this);
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(Void... params) {
                String regid = "";
                try {
                    if (mGcm == null) {
                        mGcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    regid = mGcm.register(SENDER_ID);
                    // Persist the registration ID - no need to register again.
                    storeRegistrationId(regid);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                return regid;
            }

            @Override
            protected void onPostExecute(String registrationId) {
                if(!registrationId.isEmpty())
                    registerNewSession(registrationId);
                progress.dismiss();
            }


        }.execute();

    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     * @param registrationId
     */
    private void registerNewSession(String registrationId) {
        // Your implementation here.
        Log.v(TAG, "Send regId  " + registrationId);
        Device device = new Device();
        device.setGcm_registration_id(registrationId);
        NetworkRequestsManager.instance().registerCallback(this);
        NetworkRequestsManager.instance().sendDeviceRegisterRequest(device);
    }
    private void storeRegistrationId(String regId) {
        Context context = getApplicationContext();
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.v(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.apply();
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {

//            case R.id.action_about_us:
//                return true;

            case R.id.action_search:
                search();
                return true;

            case R.id.action_contact_us:
                contactUs();
                return true;

            case R.id.action_quote_list:
                quoteList();
                return true;

            case R.id.action_deals:
                deals();
                return true;

            case R.id.action_profile:
                profile();
                return true;

            case R.id.action_share:
                share();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void serverErrorDialog() {
        contactUs("Server error :(", TEXT_OFFLINE_QUERY);
    }

    protected void noInternetDialog() {
        contactUs("No internet", TEXT_OFFLINE_QUERY);
    }

    protected void noPlayServicesDialog() {
        contactUs("No Play Services", "Google Play Services is needed for the app. " +
                "Contact us directly instead.",false);
    }

    protected MessageDialogFragment contactUs() {
        return contactUs("Contact us", HOW_DO_YOU_WANT_TO_CONTACT_US);
    }
    protected MessageDialogFragment contactUs(String heading, String title) {
        return contactUs(heading, title, true);
    }

    protected MessageDialogFragment contactUs(String heading, String title, boolean cancelable) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(MESSAGE_DIALOG_FRAGMENT);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = MessageDialogFragment.newInstance(heading, title);
        newFragment.setCancelable(cancelable);
        newFragment.show(ft, MESSAGE_DIALOG_FRAGMENT);
        return (MessageDialogFragment) newFragment;
    }

    protected void nonCancelableError(String heading, String title) {
        final MessageDialogFragment messageDialogFragment = contactUs(heading,title,false);
        messageDialogFragment.showNext();
        new Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        messageDialogFragment.showNext();
                    }
                },
                ERROR_DIALOG_DELAY_MILLIS
        );
    }

    protected void cancelDialog() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        MessageDialogFragment prev = (MessageDialogFragment) getFragmentManager().findFragmentByTag(MESSAGE_DIALOG_FRAGMENT);
        if(prev !=null) {
            prev.dismiss();
        }
    }

    protected void search() {
        ServicesSingleton instance = ServicesSingleton.instance();
        // TODO: decide behaviour
        if ( /* instance.firstTime() && */ instance.getBuyer() == null) {
            Toast.makeText(this, "please create a profile first", Toast.LENGTH_LONG).show();
            profile();
        }
        else
            startActivity(new Intent(this, SearchTabsActivity.class));
    }

    protected void deals() {
        startActivity(new Intent(this, DealListActivity.class));
    }

    protected void quoteList() {
        startActivity(new Intent(this, QuoteListActivity.class));
    }

    protected void profile() {
        startActivity(new Intent(this, ProfileActivity.class));
    }

    protected void share() {
        Intent intent;
        intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Instano");
        String sAux = "Let me recommend you this application\n";
        sAux = sAux + PLAY_STORE_LINK;
        intent.putExtra(Intent.EXTRA_TEXT, sAux);
        intent = Intent.createChooser(intent, "choose one");
        try {
            startActivityForResult(intent, SHARE_REQUEST_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "There are no clients to share links", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.global, menu);

        return true;
    }
}
