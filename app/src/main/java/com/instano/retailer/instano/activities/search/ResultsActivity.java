package com.instano.retailer.instano.activities.search;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.activities.SearchableActivity;
import com.instano.retailer.instano.application.network.NetworkRequestsManager;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.model.Quote;

import java.util.Locale;

import rx.android.observables.AndroidObservable;

public class ResultsActivity extends SearchableActivity implements ActionBar.TabListener {

    private static final String TAG = "ResultsActivity";
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    private QuotationsAndSellersAdapter mAdapter;
    private SellersListFragment mSellersListFragment;
    private SellersMapFragment mSellersMapFragment;
    private SellersListFragment mTab3;
    private SearchView mSearchView;
    private int mProductId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        mProductId = getIntent().getIntExtra(KEY_PRODUCT_ID, -1);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "new intent");
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String query = intent.getStringExtra(KEY_PRODUCT);
        Log.v(TAG, "on ResultsActivity query is "+ query);
        Log.v(TAG, "on ResultsActivity ACTION_KEY is " + intent.getIntExtra(KEY_PRODUCT_ID, -1));
        mProductId = intent.getIntExtra(KEY_PRODUCT_ID, -1);
        Log.v(TAG, "Product ID in handleIntent : " + mProductId);
        if (query == null)
            throw new IllegalStateException("no string with KEY_PRODUCT");
        mSearchView.setQuery(query, false);

        Quote quote = new Quote(mProductId);
        // TODO: improve the look of the toast:
        AndroidObservable.bindActivity(this, NetworkRequestsManager.instance().sendQuote(quote))
                .subscribe(q -> Toast.makeText(this, "sellers have been notified", Toast.LENGTH_SHORT).show(),
                        throwable -> Log.fatalError(new RuntimeException(throwable)));

        getSellersListFragment().setProduct(mProductId);
        getSellersMapFragment().setProduct(mProductId);
        getmTab3().setProduct(mProductId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean b = super.onCreateOptionsMenu(menu);
        mSearchView = (SearchView) menu.findItem(R.id.action_example).getActionView();
        handleIntent(getIntent());
        return b;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /*package private*/ QuotationsAndSellersAdapter getAdapter() {
        // make sure adapter exists before any fragment may be created
        if (mAdapter == null)
            mAdapter = new QuotationsAndSellersAdapter(this);
        return mAdapter;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            Log.v(TAG, "position : " + position);

            switch (position) {
                case 0 :
                    return getSellersListFragment();

                case 1 :
                    return getSellersMapFragment();

                case 2 :
                    return getmTab3();
            }
            throw new RuntimeException("Item not found") ;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    private SellersListFragment getSellersListFragment() {
        if (mSellersListFragment == null)
            mSellersListFragment = new SellersListFragment();
        return mSellersListFragment;
    }

    private SellersMapFragment getSellersMapFragment() {
        if(mSellersMapFragment == null)
            mSellersMapFragment = new SellersMapFragment();
        return mSellersMapFragment;
    }

    private SellersListFragment getmTab3() {
        if(mTab3 == null)
            mTab3 = new SellersListFragment();
        return mTab3;
    }
}
