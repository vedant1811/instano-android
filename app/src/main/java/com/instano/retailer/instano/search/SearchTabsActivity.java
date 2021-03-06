package com.instano.retailer.instano.search;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.instano.retailer.instano.R;
import com.instano.retailer.instano.activities.GlobalMenuActivity;
import com.instano.retailer.instano.application.ServicesSingleton;
import com.instano.retailer.instano.application.network.NetworkRequestsManager;
import com.instano.retailer.instano.utilities.GetAddressTask;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.model.Buyer;
import com.instano.retailer.instano.utilities.model.Category;
import com.instano.retailer.instano.utilities.model.Quote;

import rx.android.observables.AndroidObservable;


public class SearchTabsActivity extends GlobalMenuActivity implements
        NoLocationErrorDialogFragment.Callbacks {

    private static final int RESULT_CODE_LOCATION = 990;

    private final static String[] TABS = {"Search", "Constraints"};
    private static final String TAG = "SearchTabsActivity";
    private static final String NO_LOCATION_ERROR_DIALOG_FRAGMENT = "NoLocationErrorDialogFragment";
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

    // should never be null
    private Category mSelectedCategory = Category.undefinedCategory();

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case RESULT_CODE_LOCATION:
                if (resultCode == RESULT_OK) {
                    final LatLng latLng = new LatLng(
                            data.getDoubleExtra(SelectLocationActivity.KEY_EXTRA_LATITUDE, 0.0),
                            data.getDoubleExtra(SelectLocationActivity.KEY_EXTRA_LONGITUDE, 0.0)
                    );
                    String address = data.getStringExtra(SelectLocationActivity.KEY_READABLE_ADDRESS);
                    if (address == null) // try to fetch location again
                        new GetAddressTask(this, address1 -> {
                            String addressString = ServicesSingleton.readableAddress(address1);
                            ServicesSingleton.instance().userSelectsLocation(latLng, addressString);
                        }).execute(latLng.latitude, latLng.longitude);
                    ServicesSingleton.instance().userSelectsLocation(latLng, address);
                }
        }
    }
    public void locationButtonClicked(View view) {
        selectLocationClicked();
    }

    public void searchButtonClicked(View view) {
        String searchString = mSearchFragment.getSearchString();
        if (searchString == null) {
            mViewPager.setCurrentItem(0);
            mSearchFragment.showSearchEmptyError();
            return;
        }

        Location userLocation = ServicesSingleton.instance().getUserLocation();
        double latitude, longitude;
        if (userLocation == null) {
            // show the NoLocationErrorDialogFragment

            // DialogFragment.show() will take care of adding the fragment
            // in a transaction.  We also want to remove any currently showing
            // dialog, so make our own transaction and take care of that here.
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Fragment prev = getFragmentManager().findFragmentByTag(NO_LOCATION_ERROR_DIALOG_FRAGMENT);
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            // Create and show the dialog.
            DialogFragment newFragment = NoLocationErrorDialogFragment.newInstance();
            newFragment.show(ft, NO_LOCATION_ERROR_DIALOG_FRAGMENT);
            Log.e(TAG, "continuing to send quote without location");
        }
        else {
            latitude = userLocation.getLatitude();
            longitude = userLocation.getLongitude();
            sendQuote(searchString, latitude, longitude, ServicesSingleton.instance().getUserAddress());
        }
    }

    private void sendQuote(String searchString, double latitude, double longitude, String address) {
        Buyer buyer = ServicesSingleton.instance().getBuyer();
        if (buyer == null) {
            if (NetworkRequestsManager.instance().isOnline())
                Toast.makeText(this, "Error! check your profile", Toast.LENGTH_LONG).show();
            else
                noInternetDialog();
            Log.e(TAG, "buyer is null but a search button has been clicked");
            return;
        }

        Quote quote = new Quote();
        quote.address = address;
        quote.latitude = latitude;
        quote.longitude = longitude;

        AndroidObservable.bindActivity(this, NetworkRequestsManager.instance().sendQuote(quote))
                .subscribe(
                        (returnedQuote) -> {
                            quoteList();
                            Toast.makeText(this, "quote sent successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        },
                        (throwable) -> {
                            Toast.makeText(this, "quote send error. please try again later", Toast.LENGTH_LONG).show();
                            mSearchConstraintsFragment.sendingQuote(false);
                        });
        mSearchConstraintsFragment.sendingQuote(true);
    }

    @Override
    public void selectLocationClicked() {
        startActivityForResult(new Intent(this, SelectLocationActivity.class), RESULT_CODE_LOCATION);
    }

    @Override
    public void addressEntered(@NonNull String address) {
        // TODO: remove this code duplication:
        String searchString = mSearchFragment.getSearchString();
        if (searchString == null) {
            mViewPager.setCurrentItem(0);
            mSearchFragment.showSearchEmptyError();
            return;
        }

        sendQuote(searchString, 0.0, 0.0, address);
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
                if (i == 1)
                    mSearchConstraintsFragment.refreshSelectedCategory(mSelectedCategory, mSearchFragment.getSearchString());

                double timeTaken = (System.nanoTime() - start)/Log.ONE_MILLION;
                Log.v(Log.TIMER_TAG, "onPageSelected took " + timeTaken + "ms");
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
    }

    @Override
    public void onBackPressed() {
        int currentItem = mViewPager.getCurrentItem();
        if (currentItem > 0)
            mViewPager.setCurrentItem(currentItem - 1, true);
        else
            super.onBackPressed();
    }

    public void onCategorySelected(Category selectedCategory) {
        mSelectedCategory = selectedCategory;
    }

    public String getSearchString() {
        return mSearchFragment.getSearchString();
    }

    public Category getSelectedCategory() {
        if (mSelectedCategory == null)
            return Category.undefinedCategory();
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

}
