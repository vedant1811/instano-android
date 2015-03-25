package com.instano.retailer.instano.utilities;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.activities.MessageDialogFragment;
import com.instano.retailer.instano.activities.ProfileActivity;
import com.instano.retailer.instano.application.BaseActivity;
import com.instano.retailer.instano.application.NetworkRequestsManager;
import com.instano.retailer.instano.application.ServicesSingleton;
import com.instano.retailer.instano.buyerDashboard.quotes.QuoteListActivity;
import com.instano.retailer.instano.deals.DealListActivity;
import com.instano.retailer.instano.search.SearchTabsActivity;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.sellers.SellersActivity;

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

    /**
     * used by subclasses as well
     */
    public static final int MESSAGE_REQUEST_CODE = 997;

    private static final int SHARE_REQUEST_CODE = 998;

    protected static final String HOW_DO_YOU_WANT_TO_CONTACT_US = "How do you want to contact us";
    private static final String TEXT_OFFLINE_QUERY = "You can send a query directly by any of the following";
    private static final String MESSAGE_DIALOG_FRAGMENT = "MessageDialogFragment";

    public static final String PLAY_STORE_LINK = "http://play.google.com/store/apps/details?id=com.instano.buyer";

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
        // register this callback each time an activity is resumed
        NetworkRequestsManager.instance().registerCallback(this);
        if (!NetworkRequestsManager.instance().isOnline())
            noInternetDialog();
    }

    public void onSessionResponse(NetworkRequestsManager.ResponseError error) {
        Log.v(TAG, "Response Error in Global "+error);
        if (error == NetworkRequestsManager.ResponseError.NO_ERROR) {
            cancelDialog();
        } else {
            MessageDialogFragment.Type type = error.isLongWaiting() ?
                    MessageDialogFragment.Type.NON_CANCELLABLE_WITHOUT_PROGRESSBAR: // don't make user wait more
                    MessageDialogFragment.Type.NON_CANCELLABLE_WITH_TIMEOUTABLE_PROGRESSBAR;

            contactUs("Trouble connecting", "Please wait we are trying to connect or contact us", type);
            NetworkRequestsManager.instance().authorizeSession(error.shouldRefreshGcmId());
        }
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

            case R.id.action_sellers_list:
                sellersList();
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
                "Contact us directly instead.", MessageDialogFragment.Type.NON_CANCELLABLE_WITHOUT_PROGRESSBAR);
    }

    protected MessageDialogFragment contactUs() {
        return contactUs("Contact us", HOW_DO_YOU_WANT_TO_CONTACT_US);
    }
    protected MessageDialogFragment contactUs(String heading, String title) {
        return contactUs(heading, title, MessageDialogFragment.Type.CANCELLABLE);
    }

    protected MessageDialogFragment contactUs(String heading, String subheading, MessageDialogFragment.Type type) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(MESSAGE_DIALOG_FRAGMENT);
        if (prev != null) {
            try {
                MessageDialogFragment dialogFragment = (MessageDialogFragment) prev;
                if (dialogFragment.isHeadingSameAs(heading))
                    return dialogFragment; // the same dialog is already showing
            } catch (ClassCastException e) {
                ft.remove(prev);
                Log.fatalError(e);
            }
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = MessageDialogFragment.newInstance(heading, subheading, type);
        newFragment.show(ft, MESSAGE_DIALOG_FRAGMENT);
        return (MessageDialogFragment) newFragment;
    }

    protected void cancelDialog() {
        MessageDialogFragment prev = (MessageDialogFragment) getFragmentManager().findFragmentByTag(MESSAGE_DIALOG_FRAGMENT);
        if(prev !=null) {
            prev.dismiss();
        }
    }

    protected void search() {
        ServicesSingleton instance = ServicesSingleton.instance();
        // TODO: decide behaviour
        if ( /* instance.isFirstTime() && */ instance.getBuyer() == null) {
            Toast.makeText(this, "please create a profile first", Toast.LENGTH_LONG).show();
            profile();
        }
        else
            startActivity(new Intent(this, SearchTabsActivity.class));
    }

    protected void sellersList() {
        startActivity(new Intent(this, SellersActivity.class));
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
