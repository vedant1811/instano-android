package com.instano.retailer.instano.utilities.library.old;

import android.content.Intent;
import android.os.Bundle;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.buyerDashboard.QuotationDetailActivity;
import com.instano.retailer.instano.buyerDashboard.QuotationDetailFragment;
import com.instano.retailer.instano.utilities.GlobalMenuActivity;
import com.instano.retailer.instano.utilities.library.Log;

/**
 * An activity representing a list of Quotations. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link com.instano.retailer.instano.buyerDashboard.QuotationDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link QuotationListFragment} and the item details
 * (if present) is a {@link com.instano.retailer.instano.buyerDashboard.QuotationDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link QuotationListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class QuotationListActivity extends GlobalMenuActivity
        implements QuotationListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quotation_list);
        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (findViewById(R.id.quotation_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((QuotationListFragment) getFragmentManager()
                    .findFragmentById(R.id.quotation_list))
                    .setActivateOnItemClick(true);
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * BuyersCallbacks method from {@link QuotationListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(int quotationId) {

        Log.d(QuotationDetailFragment.ARG_QUOTATION_ID, quotationId + "");

        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putInt(QuotationDetailFragment.ARG_QUOTATION_ID, quotationId);
            QuotationDetailFragment fragment = new QuotationDetailFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .replace(R.id.quotation_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, QuotationDetailActivity.class);
            detailIntent.putExtra(QuotationDetailFragment.ARG_QUOTATION_ID, quotationId);
            startActivity(detailIntent);
        }
    }
}
