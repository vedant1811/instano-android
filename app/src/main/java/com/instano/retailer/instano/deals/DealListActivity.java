package com.instano.retailer.instano.deals;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;


import com.instano.retailer.instano.R;

/**
 * An activity representing a list of Deals. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link DealDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link DealListFragment} and the item details
 * (if present) is a {@link DealDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link DealListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class DealListActivity extends Activity
        implements DealListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal_list);

        if (findViewById(R.id.deal_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((DealListFragment) getFragmentManager()
                    .findFragmentById(R.id.deal_list))
                    .setActivateOnItemClick(true);
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }

    /**
     * Callback method from {@link DealListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(int id) {
        if (mTwoPane) {
            DealDetailFragment fragment = DealDetailFragment.create(id);
            getFragmentManager().beginTransaction()
                    .replace(R.id.deal_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, DealDetailActivity.class);
            detailIntent.putExtra(DealDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }
}
