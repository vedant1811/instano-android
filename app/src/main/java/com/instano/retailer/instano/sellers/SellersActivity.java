package com.instano.retailer.instano.sellers;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.DataManager;
import com.instano.retailer.instano.utilities.GlobalMenuActivity;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.models.Seller;

import java.util.List;

public class SellersActivity extends GlobalMenuActivity implements DataManager.SellersListener{
    private static final String CURRENT_FRAGMENT = "current fragment";
    private SellersListFragment mSellersListFragment;
    private SellersMapFragment mSellersMapFragment;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private SellersArrayAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sellers);
        mSellersListFragment = new SellersListFragment();
        mSellersMapFragment = new SellersMapFragment();
        getFragmentManager().beginTransaction()
                .add(R.id.fragment_container, mSellersListFragment, CURRENT_FRAGMENT)
                .commit();
        DataManager.instance().registerListener(this);

        mAdapter = new SellersArrayAdapter(this);
        List<Seller> sellers = DataManager.instance().getSellers();
        Log.d(getClass().getSimpleName(), "no of sellers being added: " + sellers.size());
        mAdapter.addAll(sellers);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataManager.instance().unregisterListener(this);
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
                invalidateOptionsMenu();
                return true;
            case R.id.action_map:
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, mSellersMapFragment, CURRENT_FRAGMENT)
                        .addToBackStack(null)
                        .commit();
                invalidateOptionsMenu();
                return true;
            case R.id.action_filter:

                return true;
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    public void sellersUpdated() {
        List<Seller> sellers = DataManager.instance().getSellers();
        mAdapter.addAll(sellers);
        if (mSellersMapFragment != null)
            mSellersMapFragment.addSellers(sellers);
    }

    /* package private */
    SellersArrayAdapter getAdapter() {
        return mAdapter;
    }
}
