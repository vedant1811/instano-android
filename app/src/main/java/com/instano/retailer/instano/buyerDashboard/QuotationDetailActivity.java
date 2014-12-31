package com.instano.retailer.instano.buyerDashboard;

import android.os.Bundle;
import android.view.MenuItem;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.utilities.GlobalMenuActivity;

/**
 * An activity representing a single Quotation detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link com.instano.retailer.instano.utilities.library.old.QuotationListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link QuotationDetailFragment}.
 */
public class QuotationDetailActivity extends GlobalMenuActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quotation_detail);

        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putInt(QuotationDetailFragment.ARG_QUOTATION_ID,
                    getIntent().getIntExtra(QuotationDetailFragment.ARG_QUOTATION_ID, -1));
            QuotationDetailFragment fragment = new QuotationDetailFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .add(R.id.quotation_detail_container, fragment)
                    .commit();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            // so that a new fragment is not created
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
