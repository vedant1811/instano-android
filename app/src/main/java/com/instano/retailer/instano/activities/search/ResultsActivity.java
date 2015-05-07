package com.instano.retailer.instano.activities.search;

import android.app.SearchManager;
import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.BaseActivity;
import com.instano.retailer.instano.application.network.NetworkRequestsManager;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.model.Product;

import java.util.Locale;

import rx.Subscription;
import rx.android.observables.AndroidObservable;
import rx.subscriptions.BooleanSubscription;

public class ResultsActivity extends BaseActivity implements ActionBar.TabListener {

    private static final String TAG = "ResultsActivity";
    private static final String PRODUCT_NAME = "productName";
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
    private String mQuery;
    private SimpleCursorAdapter mCursorAdapter;

    private static final String[] SUGGESTIONS = {
            "Bauru", "Sao Paulo", "Rio de Janeiro",
            "Bahia", "Mato Grosso", "Minas Gerais",
            "Tocantins", "Rio Grande do Sul"
    };
    private Subscription mSuggestionsSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // make sure adapter exists before any fragment may be created
        mAdapter = new QuotationsAndSellersAdapter(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        handleIntent(getIntent());

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mSuggestionsSubscription = BooleanSubscription.create();

        final String[] from = new String[] {PRODUCT_NAME};
        final int[] to = new int[] {android.R.id.text1};
        mCursorAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1,
                null,
                from,
                to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

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
        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            mQuery = query;
            Log.v(TAG, "on ResultsActivity query is "+ query);
            //use the query to search your data somehow
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        // Associate searchable configuration with the SearchView
        SearchView searchView = (SearchView) menu.findItem(R.id.action_example).getActionView();
        if(mQuery != null) {
            searchView.setQuery(mQuery,true);
        }
        searchView.setSuggestionsAdapter(mCursorAdapter);
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int i) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int i) {
                return true;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Log.v(TAG, "Query submitted " + s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.v(TAG, "Query text changed");
                populateAdapter(s);
                return false;
            }
        });
        return true;
    }

    private void populateAdapter(String query) {
        mSuggestionsSubscription.unsubscribe();
        mSuggestionsSubscription = AndroidObservable.bindActivity(this, NetworkRequestsManager.instance().queryProducts(query))
                .subscribe(products -> {
                    MatrixCursor cursor = new MatrixCursor(new String[]{BaseColumns._ID, PRODUCT_NAME});
                    for (Product product : products) {
                        cursor.addRow(new Object[]{product.id, product.name});
                    }
                    mCursorAdapter.changeCursor(cursor);
                }, throwable -> Log.fatalError(new RuntimeException(throwable)));

    }

    @Override
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

    private SellersListFragment getSellersListFragment() {
        if (mSellersListFragment == null)
            mSellersListFragment = new SellersListFragment();
        return mSellersListFragment;
    }

    /*package private*/ QuotationsAndSellersAdapter getAdapter() {
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
            // Return a PlaceholderFragment (defined as a static inner class below).
            Log.v(TAG, "position : " + position);
            switch (position) {
                case 0 : return new SellersListFragment();

                case 1 : return new SellersMapFragment();
            }
            return new SellersListFragment();
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
}
