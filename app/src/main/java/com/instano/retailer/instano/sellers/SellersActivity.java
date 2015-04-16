package com.instano.retailer.instano.sellers;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.activities.GlobalMenuActivity;
import com.instano.retailer.instano.utilities.models.Category;

public class SellersActivity extends GlobalMenuActivity {
    private static final String CURRENT_FRAGMENT = "current fragment";
    private static final String FILTERS_DIALOG_FRAGMENT = "Filters Dialog Fragment";
    private SellersListFragment mSellersListFragment;
    private SellersMapFragment mSellersMapFragment;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private SellersArrayAdapter mAdapter;
    private Category mSelectedCategory = Category.undefinedCategory();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sellers);
        mSellersListFragment = new SellersListFragment();
        mSellersMapFragment = new SellersMapFragment();
        getFragmentManager().beginTransaction()
                .add(R.id.fragment_container, mSellersListFragment, CURRENT_FRAGMENT)
                .commit();

        mAdapter = new SellersArrayAdapter(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sellers, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_list:
                onBackPressed();
                return true;
            case R.id.action_map:
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, mSellersMapFragment, CURRENT_FRAGMENT)
                        .addToBackStack(null)
                        .commit();
                invalidateOptionsMenu();
                return true;
            case R.id.action_filter:
                // DialogFragment.show() will take care of adding the fragment
                // in a transaction.  We also want to remove any currently showing
                // dialog, so make our own transaction and take care of that here.
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag(FILTERS_DIALOG_FRAGMENT);
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                // Create and show the dialog.
                DialogFragment dialogFragment = FiltersDialogFragment.newInstance(mSelectedCategory);

                dialogFragment.show(ft, FILTERS_DIALOG_FRAGMENT);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* package private */
    void onCategorySelected(Category category) {
        mSelectedCategory = category;
        mAdapter.filter(category);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        Fragment currentFragment = getFragmentManager().findFragmentByTag(CURRENT_FRAGMENT);
        if (currentFragment.equals(mSellersListFragment)) {
            menu.findItem(R.id.action_list).setVisible(false);
            menu.findItem(R.id.action_map).setVisible(true);
        }
        else {
            menu.findItem(R.id.action_list).setVisible(true);
            menu.findItem(R.id.action_map).setVisible(false);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        invalidateOptionsMenu();
    }

    /* package private */
    SellersArrayAdapter getAdapter() {
        return mAdapter;
    }
}
