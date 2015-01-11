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

/**
 * Base class for activities with a common menu (menu.global)
 * Actions common to activies should go here (like checking connectivity in {@link Activity#onResume()}
 *
 * different actions call different methods that may be overridden by implementing classes
 *
 * Created by vedant on 15/12/14.
 */
public abstract class GlobalMenuActivity extends BaseActivity {
    public static final int PICK_CONTACT_REQUEST_CODE = 996;
    public static final int SEND_SMS_REQUEST_CODE = 995;

    private static final int SHARE_REQUEST_CODE = 998;
    private static final int MESSAGE_REQUEST_CODE = 997;

    protected static final String HOW_DO_YOU_WANT_TO_CONTACT_US = "How do you want to contact us";
    private static final String TEXT_OFFLINE_QUERY = "You can send a query directly by any of the following";

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

    protected void contactUs() {
        contactUs("Contact us", HOW_DO_YOU_WANT_TO_CONTACT_US);
    }

    protected void contactUs(String heading, String title) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = MessageDialogFragment.newInstance(heading, title);
        newFragment.show(ft, "dialog");
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
        intent.putExtra(Intent.EXTRA_SUBJECT, "Instano Retailer");
        String sAux = "Let me recommend you this application\n";
        sAux = sAux + "http://play.google.com/store/apps/details?id=com.instano.buyer";
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
