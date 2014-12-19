package com.instano.retailer.instano.search;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.instano.retailer.instano.NetworkRequestsManager;
import com.instano.retailer.instano.R;
import com.instano.retailer.instano.ServicesSingleton;
import com.instano.retailer.instano.buyerDashboard.QuotationListActivity;
import com.instano.retailer.instano.utilities.GlobalMenuActivity;
import com.instano.retailer.instano.utilities.models.Buyer;
import com.instano.retailer.instano.utilities.models.ProductCategories;

import java.util.ArrayList;


public class SearchTabsActivity extends GlobalMenuActivity implements
        NetworkRequestsManager.QuoteCallbacks {

    private final static String[] TABS = {"Search", "Constraints"};
//    private enum TABS {Search, Constraints, Sellers_list, Sellers_Map};

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
//    private SellersListFragment mSellersListFragment;
//    private SellersMapFragment mSellersMapFragment;

    private ProductCategories.Category mSelectedCategory;

    public void locationButtonClicked(View view) {
        startActivity(new Intent(this, SelectLocationActivity.class));
    }

    public void searchButtonClicked(View view) {
        String searchString = mSearchFragment.getSearchString();
        if (searchString == null) {
            mViewPager.setCurrentItem(0);
            mSearchFragment.showSearchEmptyError();
            return;
        }
        Buyer buyer = ServicesSingleton.getInstance(this).getBuyer();

        if (ServicesSingleton.getInstance(this).getUserLocation() == null)
            if (!noLocationError())
                return;

        if (buyer == null) {
            if (NetworkRequestsManager.instance().isOnline(true))
                Toast.makeText(this, "Error! check your profile", Toast.LENGTH_LONG).show();
            Log.e("SearchTabsActivity", "buyer is null but a search button has been clicked");
            return;
        }

        NetworkRequestsManager.instance().sendQuoteRequest(
                buyer,
                searchString,
                mSearchConstraintsFragment.getPriceRange(),
                mSelectedCategory,
                mSelectedCategory.asAdditionalInfo(),
                null
        );
        mSearchConstraintsFragment.sendingQuote(true);
    }

    /**
     * Handles the case of no location selected
     * @return true if the error should be ignored
     */
    private boolean noLocationError() {
        return true;
    }

    public void nextButtonClicked(View view) {
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_tabs);

//        mSellersListFragment = new SellersListFragment();
        mSearchFragment = SearchFragment.newInstance();
        mSearchConstraintsFragment = SearchConstraintsFragment.newInstance();

//        mSellersMapFragment = new SellersMapFragment();
        mViewPager = (ViewPager) findViewById(R.id.pager);

        // Set up the action bar.
//        final ActionBar actionBar = getActionBar();
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.

        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                long start = System.nanoTime();
                View focusedView = getCurrentFocus();
                if (focusedView != null){
                    InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    keyboard.hideSoftInputFromWindow(focusedView.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
                double timeTaken = (System.nanoTime() - start)/1000000;

                Log.d("Timing", "onPageSelected took " + timeTaken + "ms");
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

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

        NetworkRequestsManager.instance().registerCallback(this);

    }

    @Override
    public void onBackPressed() {
        int currentItem = mViewPager.getCurrentItem();
        if (currentItem > 0)
            mViewPager.setCurrentItem(currentItem - 1, true);
        else
            super.onBackPressed();
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
            mSearchConstraintsFragment.sendingQuote(false);
        }
    }

    public void onCategorySelected(ProductCategories.Category selectedCategory) {

        mSelectedCategory = selectedCategory;

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
//                case 2:
//                    return mSellersListFragment;
//                case 3:
//                    return mSellersMapFragment;
            }

            throw new IllegalArgumentException("Invalid parameter position: " + position);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TABS[position];
        }
    }

}
