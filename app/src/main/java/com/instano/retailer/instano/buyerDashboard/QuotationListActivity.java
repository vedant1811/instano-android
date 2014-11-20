package com.instano.retailer.instano.buyerDashboard;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import com.instano.retailer.instano.R;
import com.instano.retailer.instano.ServicesSingleton;

/**
 * An activity representing a list of Quotations. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link QuotationDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link QuotationListFragment} and the item details
 * (if present) is a {@link QuotationDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link QuotationListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class QuotationListActivity extends ActionBarActivity
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
//        getActionBar().setDisplayHomeAsUpEnabled(true);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.abc_btn_check_material));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

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
        ServicesSingleton.getInstance(this).getQuotationsRequest();
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
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * BuyersCallbacks method from {@link QuotationListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(int pos) {

        int quotationId = ServicesSingleton.getInstance(null).getQuotationArrayAdapter().getItem(pos).id;
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
