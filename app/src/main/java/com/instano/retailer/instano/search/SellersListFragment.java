package com.instano.retailer.instano.search;

import android.app.Fragment;
import android.content.Intent;
import android.location.Address;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.SellersArrayAdapter;
import com.instano.retailer.instano.ServicesSingleton;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p />
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p />
 */
public class SellersListFragment extends Fragment implements
        ServicesSingleton.AddressCallbacks,
        SellersArrayAdapter.ItemInteractionListener {

    private static final String PLEASE_SELECT_LOCATION = "please select location";
    /**
     * The fragment's ListView
     */
    private ListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private SellersArrayAdapter mAdapter;

    private ViewFlipper mSearchButtonViewFlipper;
    private TextView mAddressTextView;
    private Button mSearchButton;
    private TextView mWithinTextView;
    private SeekBar mWithinSeekBar;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SellersListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = ServicesSingleton.getInstance(getActivity()).getSellersArrayAdapter();
        mAdapter.filter();
    }

    @Override
    public void onResume() {
        super.onResume();

        ServicesSingleton servicesSingleton = ServicesSingleton.getInstance(getActivity());
        Address address = servicesSingleton.getUserAddress();
        addressFound(address, false);

        itemCheckedStateChanged(mAdapter.getSelectedSellerIds().size());
    }

    /* package private */
    void sendingQuote(boolean isSending) {
        if (isSending)
            mSearchButtonViewFlipper.setDisplayedChild(1);
        else
            mSearchButtonViewFlipper.setDisplayedChild(0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sellers, container, false);

//        mHeader = inflater.inflate(R.layout.layout_select_sellers, null, false);
//        mHeaderButton = (Button) mHeader.findViewById(R.id.searchButton);
        mSearchButtonViewFlipper = (ViewFlipper) view.findViewById(R.id.searchButtonViewFlipper);
        mAddressTextView = (TextView) view.findViewById(R.id.addressTextView);
        mSearchButton = (Button) view.findViewById(R.id.searchButton);
        mWithinSeekBar = (SeekBar) view.findViewById(R.id.withinSeekBar);
        mWithinTextView = (TextView) view.findViewById(R.id.withinTextView);

//        setEmptyText("No sellers nearby. Try relaxing location/categories/brands constraints");

        // Set the adapter
        mListView = (ListView) view.findViewById(R.id.listView);
//        mListView.addHeaderView(mHeader);
        mListView.setAdapter(mAdapter);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        mAdapter.setListener(this);

        ServicesSingleton.getInstance(getActivity()).registerCallback(this);

        mWithinSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // progress ranges from 0 to 99 while distance needs to be from 1km to 100km
                mWithinTextView.setText(String.format("Within %dkm:", progress + 1));
                ServicesSingleton.getInstance(getActivity()).getSellersArrayAdapter().filter(
                        (progress + 1) * 100); // * 100 since it needs to be sent in 10x meters
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        return view;
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    // TODO: fix this method
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyText instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    @Override
    public void itemCheckedStateChanged(int selected) {
        switch (selected) {
            case 0:
                mSearchButton.setText("Send to zero Sellers");
                mSearchButton.setEnabled(false);
                return;
            case 1:
                mSearchButton.setText("Send to 1 seller");
                break;
            default:
                mSearchButton.setText(String.format("Send to %d sellers", selected));
        }
        long start = System.nanoTime();
        if (!mAddressTextView.getText().toString().equals(PLEASE_SELECT_LOCATION))
            mSearchButton.setEnabled(true);
        double timeTaken = (System.nanoTime() - start)/1000;
        Log.d("Timing", "getSelectedSellerIds took " + timeTaken + "μs");
    }

    @Override
    public void callButtonClicked(String number) {
        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
        startActivity(callIntent);
    }

    public ArrayList<Integer> getSellerIds() {
        return mAdapter.getSelectedSellerIds();
    }

    @Override
    public void searchingForAddress() {

    }

    @Override
    public void addressFound(Address address, boolean userSelected) {
        if (address == null) {
            mAddressTextView.setText(PLEASE_SELECT_LOCATION);
            mSearchButton.setEnabled(false);
        }
        else
        {
            // display a readable address, street if available or city
            mAddressTextView.setText("near " + (
                            address.getMaxAddressLineIndex() > 0 ?
                                    address.getAddressLine(0) : address.getLocality())
            );
            if (mAdapter.getSelectedSellerIds().size() > 0)
                mSearchButton.setEnabled(true);
        }
    }
}
