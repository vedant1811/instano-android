package com.instano.retailer.instano.chat;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.utilities.GlobalMenuActivity;

/**
 * An activity representing a list of ChatSellers. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
  representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ChatSellerListFragment} and the item details
 * (if present) is a {@link ChatSellerDetailFragment}.
 * <p/>
 * This activity also implements the required
 * to listen for item selections.
 */
public class ChatSellerListActivity extends GlobalMenuActivity
        implements ChatSellerListFragment.Callbacks {

    public static final String PARAM_ID = "param.extra.id";
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    String TAG = "ChatSellerListActivity";
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatseller_list);

        if (findViewById(R.id.chatseller_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((ChatSellerListFragment) getFragmentManager()
                    .findFragmentById(R.id.fragment_chatseller_list))
                    .setActivateOnItemClick(true);
        }
        else {
            Fragment newFragment = new ChatSellerListFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, newFragment)
                    .commit();
        }

        Intent intent = getIntent();
        int intExtra = intent.getIntExtra(PARAM_ID, -1);
        if(intExtra != -1) {
            onItemSelected(intExtra);
        }
    }

    @Override
    public void onItemSelected(int sellerId) {
        ChatSellerDetailFragment fragment = ChatSellerDetailFragment.newInstance(sellerId);
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            getFragmentManager().beginTransaction()
                    .replace(R.id.chatseller_detail_container, fragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}
