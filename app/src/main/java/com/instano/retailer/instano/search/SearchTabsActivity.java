package com.instano.retailer.instano.search;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.ServicesSingleton;
import com.instano.retailer.instano.buyerDashboard.QuotationListActivity;
import com.instano.retailer.instano.utilities.ProductCategories;

import java.util.ArrayList;


public class SearchTabsActivity extends Activity implements
        ServicesSingleton.QuoteCallbacks {

    private final static String[] TABS = {"Search", "Constraints", "Sellers list"};

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
    private ViewPager mViewPager;

    private SearchFragment mSearchFragment;
    private SearchConstraintsFragment mSearchConstraintsFragment;
    private SellersListFragment mSellersListFragment;
    private SellersMapFragment mSellersMapFragment;

    private ProductCategories.Category mSelectedCategory;

    public void searchButtonClicked(View view) {
        String searchString = mSearchFragment.getSearchString();
        if (searchString == null) {
            mViewPager.setCurrentItem(0);
            mSearchFragment.showSearchEmptyError();
            return;
        }
        ServicesSingleton.getInstance(this).sendQuoteRequest(
                searchString,
                mSearchConstraintsFragment.getPriceRange(),
                mSelectedCategory,
                mSearchConstraintsFragment.getAdditionalInfo(),
                mSellersListFragment.getSellerIds()
        );
        sendingQuote(true);
    }

    public void nextButtonClicked(View view) {
//        FragmentManager fragmentManager = getFragmentManager();
//        fragmentManager.beginTransaction()
//                .replace(R.id.container, mSearchConstraintsFragment)
//                .commit();
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_tabs);

        mSellersListFragment = new SellersListFragment();
        mSearchFragment = SearchFragment.newInstance();
        mSearchConstraintsFragment = SearchConstraintsFragment.newInstance();

        mSellersMapFragment = new SellersMapFragment();
        mViewPager = (ViewPager) findViewById(R.id.pager);

        // Set up the action bar.
//        final ActionBar actionBar = getActionBar();
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.

        mViewPager.setAdapter(mSectionsPagerAdapter);

//        // When swiping between different sections, select the corresponding
//        // tab. We can also use ActionBar.Tab#select() to do this if we have
//        // a reference to the Tab.
//        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
//            @Override
//            public void onPageSelected(int position) {
//                actionBar.setSelectedNavigationItem(position);
//            }
//        });
//
//        // For each of the sections in the app, add a tab to the action bar.
//        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
//            // Create a tab with text corresponding to the page title defined by
//            // the adapter. Also specify this Activity object, which implements
//            // the TabListener interface, as the callback (mListener) for when
//            // this tab is selected.
//            actionBar.addTab(
//                    actionBar.newTab()
//                            .setText(mSectionsPagerAdapter.getPageTitle(i))
//                            .setTabListener(this));
//        }

        ServicesSingleton.getInstance(this).registerCallback(this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_tabs, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
//        // When the given tab is selected, switch to the corresponding page in
//        // the ViewPager.
//        int tabPosition = tab.getPosition();
//        mViewPager.setCurrentItem(tabPosition);
//        Log.d("timer", tab.getText() + " tab selected");
////        if (tabPosition != 0)
////            mSearchButtonViewFlipper.setVisibility(View.VISIBLE);
//    }
//
//    @Override
//    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
//        Log.d("timer", tab.getText() + " tab unselected");
//    }
//
//    @Override
//    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
//    }

    @Override
    public void productCategoriesUpdated(ArrayList<ProductCategories.Category> productCategories) {
        mSearchFragment.updateProductCategories(productCategories);
    }

    @Override
    public void quoteSent(boolean success) {
        if (success) {
            startActivity(new Intent(this, QuotationListActivity.class));
            Toast.makeText(this, "quote sent successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "quote send error. please try again later", Toast.LENGTH_LONG).show();
            sendingQuote(false);
        }
    }

    private void sendingQuote(boolean isSending) {
        mSellersListFragment.sendingQuote(isSending);
    }

    public void onCategorySelected(ProductCategories.Category selectedCategory) {

        mSelectedCategory = selectedCategory;

        ServicesSingleton.getInstance(this).getSellersArrayAdapter().filter(mSelectedCategory);
        mSearchConstraintsFragment.onCategorySelected(selectedCategory);
    }

    public ProductCategories.Category getSelectedCategory() {
        if (mSelectedCategory == null)
            return ProductCategories.Category.undefinedCategory();
        return mSelectedCategory;
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
            switch (position) {
                case 0:
                    return mSearchFragment;
                case 1:
                    return mSearchConstraintsFragment;
                case 2:
                    return mSellersListFragment;
            }

            throw new IllegalArgumentException("Invalid parameter position: " + position);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TABS[position];
        }
    }

}
