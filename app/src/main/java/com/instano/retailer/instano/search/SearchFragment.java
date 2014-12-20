package com.instano.retailer.instano.search;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.DataManager;
import com.instano.retailer.instano.application.ServicesSingleton;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.models.ProductCategories;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class SearchFragment extends Fragment
        implements ServicesSingleton.InitialDataCallbacks,
        ServicesSingleton.AddressCallbacks {
    private static final String PLEASE_SELECT_LOCATION = "please select location";
    private static final String NEAR_YOUR_LOCATION = "near your location";
    private static final String NEAR = "near ";

    private EditText mSearchEditText;
    private Spinner mProductCategorySpinner;
    private Button mLocationButton;

    private ArrayAdapter<ProductCategories.Category> mCategoryAdapter;

    private CharSequence mSearchString;
    private CharSequence mLocationButtonText;
    private String mAddress;
    private boolean mUserSelectedCategory = false;

    private Toast mToast;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment PurchaseFragment.
     */
    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
        return fragment;
    }
    public SearchFragment() {
        // Required empty public constructor
    }


    @Override
    public void searchingForAddress() {

    }

    @Override
    public void addressUpdated(String address, boolean userSelected) {
        Log.d(Log.ADDRESS_UPDATED, "SellersListFragment.address updated " + address);

        // if it was userSelected, the activity is paused and we need to edit the saved state as well,
        // so it is updated in onResume()
        if (address != null) {
            // display a readable address, street if available or city
            mLocationButtonText = NEAR + (address);
        }
        else if (ServicesSingleton.instance().getUserLocation() == null)
            mLocationButtonText = PLEASE_SELECT_LOCATION; // we have no address and no location
        else
            mLocationButtonText = NEAR_YOUR_LOCATION; // we have location but no address

        // we updating it right now in case it is resumed
        mLocationButton.setText(mLocationButtonText);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final ServicesSingleton servicesSingleton = ServicesSingleton.instance();
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        mSearchEditText = (EditText) view.findViewById(R.id.searchEditText);
        mProductCategorySpinner = (Spinner) view.findViewById(R.id.productCategorySpinner);
        mLocationButton = (Button) view.findViewById(R.id.locationButton);

        addressUpdated(null, false); // to setup initial state of button

        mCategoryAdapter = new ArrayAdapter<ProductCategories.Category>(getActivity(),
                android.R.layout.simple_spinner_item);
        ArrayList<ProductCategories.Category> categories = DataManager.instance().getProductCategories();
        if (categories != null)
            mCategoryAdapter.addAll(categories);
        mCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!mUserSelectedCategory)
                    guessCategory();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mProductCategorySpinner.setAdapter(mCategoryAdapter);
        mProductCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mUserSelectedCategory = true;
                ((SearchTabsActivity) getActivity()).onCategorySelected(mCategoryAdapter.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return view;
    }

    public String getSearchString() {
        String s = mSearchEditText.getText().toString();
        if (s.equals("")) {
            return null;
        }
        return s;
    }

    // TODO: animate in case category is guessed
    private void guessCategory() {
        long start = System.nanoTime();
        String search = mSearchEditText.getText().toString().toLowerCase();
        for (int i = 0; i < mCategoryAdapter.getCount(); i++) {
            if (mCategoryAdapter.getItem(i).matches(search)) {
                mProductCategorySpinner.setSelection(i);
                return;
            }
        }
        mProductCategorySpinner.setSelection(0); // setting to undefined
        double time = (System.nanoTime() - start)/Log.ONE_MILLION;
        Log.v(Log.TIMER_TAG, "guessCategory took " + time + "ms");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mToast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);

        ServicesSingleton.instance().registerCallback(
                (ServicesSingleton.AddressCallbacks) this);
        ServicesSingleton.instance().registerCallback(
                (ServicesSingleton.InitialDataCallbacks) this);
    }

    /**
     * Called when searchButton has been clicked
     */
//    public void searchButtonClicked() {
//        ServicesSingleton servicesSingleton = ServicesSingleton.getInstance(getActivity());
//
//        if (!servicesSingleton.checkPlayServices()){
//            String error = servicesSingleton.getLocationErrorString();
//            if (error != null) {
//                mToast.setText(error);
//                mToast.show();
//            }
//            return;
//        }
//
//        String searchString = mSearchEditText.getText().toString();
//        if (searchString == null) {
//            mToast.setText("Enter something to search");
//            mToast.show();
//            return;
//        }
//    }

    /* package private */
    void showSearchEmptyError() {
        mSearchEditText.setError("enter something");
    }

    /* package private */
    void updateProductCategories(ArrayList<ProductCategories.Category> categories) {
        if (mCategoryAdapter != null) {
            mCategoryAdapter.clear();
            mCategoryAdapter.addAll(categories);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mSearchString = mSearchEditText.getText();
        mLocationButtonText = mLocationButton.getText();
    }

    @Override
    public void onResume() {
        super.onResume();
        mSearchEditText.setText(mSearchString);
        int position = mCategoryAdapter.getPosition(
                ((SearchTabsActivity)getActivity()).getSelectedCategory());
        mProductCategorySpinner.setSelection(position);

        mLocationButton.setText(mLocationButtonText);
    }

    @Override
    public void resolvableConnectionResultError(ConnectionResult connectionResult)
            throws IntentSender.SendIntentException {
        connectionResult.startResolutionForResult(getActivity(), ServicesSingleton.REQUEST_CODE_RECOVER_PLAY_SERVICES);
    }

    @Override
    public void showErrorDialog(int errorCode) {
        GooglePlayServicesUtil.getErrorDialog(errorCode, getActivity(),
                ServicesSingleton.REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ServicesSingleton.REQUEST_CODE_RECOVER_PLAY_SERVICES:
                if (resultCode == Activity.RESULT_CANCELED) {
                    mToast.setText ("Google Play Services must be installed.");
                    mToast.show();
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
