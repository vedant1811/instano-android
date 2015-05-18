package com.instano.retailer.instano.activities.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.widget.Toast;

import com.instano.retailer.instano.AboutUsDialogFragment;
import com.instano.retailer.instano.R;
import com.instano.retailer.instano.activities.SearchableActivity;
import com.instano.retailer.instano.deals.DealDetailActivity;
import com.instano.retailer.instano.deals.DealDetailFragment;

public class HomeActivity extends SearchableActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, DealListFragment.Callbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to StoreActivity the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private boolean mTwoPane;

    public static final String PLAY_STORE_LINK = "http://play.google.com/StoreActivity/apps/details?id=com.instano.buyer";
    private static final int SHARE_REQUEST_CODE = 998;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        if (findViewById(R.id.deal_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            //TODO for two pane
//            ((DealListFragment) getFragmentManager()
//                    .findFragmentById(R.id.deal_list_home))
//                    .setActivateOnItemClick(true);
        }
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
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

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = null;
        switch(position) {
            default:
            case R.id.homeButton:
                fragment = fragmentManager.findFragmentByTag(CategoriesGridFragment.TAG);
                if (fragment == null)
                    fragment = new CategoriesGridFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, fragment, CategoriesGridFragment.TAG)
                        .commit();
                break;
            case R.id.bestInCityButton:
                fragment = fragmentManager.findFragmentByTag(DealListFragment.TAG);
                if (fragment == null)
                    fragment = new DealListFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, fragment, DealListFragment.TAG)
                        .commit();
                break;
            case R.id.chatButton:
                break;
            case R.id.bookingButton:
                break;
            case R.id.settingButton:
                break;
            case R.id.aboutButton:
                AboutUsDialogFragment about = AboutUsDialogFragment.newInstnace();
                about.show(fragmentManager,"About Fragment");
                break;
            case R.id.rateButton:
                break;
            case R.id.shareButton:
                Intent intent;
                intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Instano");
                String sAux = "Let me recommend you this application\n";
                sAux = sAux + PLAY_STORE_LINK;
                intent.putExtra(Intent.EXTRA_TEXT, sAux);
                intent = Intent.createChooser(intent, "choose one");
                try {
                    startActivityForResult(intent, SHARE_REQUEST_CODE);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(this, "There are no clients to share links", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                onNavigationDrawerItemSelected(R.id.homeButton);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
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
}
