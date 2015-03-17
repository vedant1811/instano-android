package com.instano.retailer.instano.sellers;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.DataManager;
import com.instano.retailer.instano.utilities.GlobalMenuActivity;

public class SellersActivity extends GlobalMenuActivity implements DataManager.SellersListener{

    SellersListFragment mSellersListFragment;
    SellersMapFragment mSellersMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sellers);
        startRequiredFragment(R.id.action_list);
        DataManager.instance().registerListener(this);
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
            case R.id.action_map:
                item.setVisible(false);
                invalidateOptionsMenu();
                startRequiredFragment(id);
                return true;
            case R.id.action_filter:

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startRequiredFragment(int id) {
        Fragment fragment = null;
        switch (id) {
            case R.id.action_list:
                if (mSellersListFragment == null) {
                    mSellersListFragment = new SellersListFragment();
                }
                fragment = mSellersListFragment;
                break;
            case R.id.action_map:
                if (mSellersMapFragment == null) {
                    mSellersMapFragment = new SellersMapFragment();
                }
                fragment = mSellersMapFragment;
                break;
            default:
                throw new IllegalArgumentException("no fragment for id:" + id);
        }
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void sellersUpdated() {

    }
}
