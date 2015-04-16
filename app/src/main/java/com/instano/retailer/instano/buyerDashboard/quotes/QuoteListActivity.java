package com.instano.retailer.instano.buyerDashboard.quotes;

import android.os.Bundle;
import android.view.View;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.activities.GlobalMenuActivity;
import com.instano.retailer.instano.buyerDashboard.QuotationDetailFragment;
import com.instano.retailer.instano.utilities.library.Log;

/**
 * An activity representing a list of Quotes. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link QuoteDetailFragment} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link QuoteListFragment} and the item details
 * (if present) is a {@link QuoteDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link QuoteListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class QuoteListActivity extends GlobalMenuActivity
        implements QuoteListFragment.Callbacks,
        QuoteDetailFragment.Callbacks {

    private static final String TAG = "QuoteListActivity";

    public void contactUsClicked(View view) {
        contactUs();
    }

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quote_list);
        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (findViewById(R.id.quote_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((QuoteListFragment) getFragmentManager()
                    .findFragmentById(R.id.quote_list))
                    .setActivateOnItemClick(true);
        }
        else {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new QuoteListFragment(), QuoteListFragment.class.getSimpleName())
                    .commit();
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }

    /**
     * Callback method from {@link QuoteListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onQuoteSelected(int quote_id) {
        QuoteDetailFragment fragment = QuoteDetailFragment.create(quote_id);
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            getFragmentManager().beginTransaction()
                    .replace(R.id.quote_detail_container, fragment)
                    .addToBackStack(null)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    /**
     * Callback for when an item has been selected.
     *
     * @param quotation_id
     */
    @Override
    public void onQuotationSelected(QuotationDetailFragment fragment) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            getFragmentManager().beginTransaction()
                    .replace(R.id.quote_detail_container, fragment)
                    .addToBackStack(null)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}
