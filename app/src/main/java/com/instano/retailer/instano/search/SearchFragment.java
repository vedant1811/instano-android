package com.instano.retailer.instano.search;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.instano.retailer.instano.R;
import com.instano.retailer.instano.ServicesSingleton;
import com.instano.retailer.instano.utilities.ProductCategories;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class SearchFragment extends Fragment implements ServicesSingleton.InitialDataCallbacks {

    private final static String CURRENT_LOCATION = "Current Location";
    private final static String SELECT_LOCATION = "Select Location";

    private Button mSearchButton;
    private Button mLocationButton;
    private ProgressBar mProgressBar;
    private EditText mSearchEditText;
    private Spinner mProductCategorySpinner;
    private ProductCategories.Category mSelectedCategory;
    private ArrayAdapter<ProductCategories.Category> mCategoryAdapter;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final ServicesSingleton servicesSingleton = ServicesSingleton.getInstance(getActivity());
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        // mSearchButton is not enabled in XML
//        mSearchButton = (Button) view.findViewById(R.id.search_button);
//        mSearchButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                searchButtonClicked();
//            }
//        });
//
//        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mSearchEditText = (EditText) view.findViewById(R.id.searchEditText);
        mProductCategorySpinner = (Spinner) view.findViewById(R.id.productCategorySpinner);
//
//        mLocationButton = (Button) view.findViewById(R.id.locationButton);

        mCategoryAdapter = new ArrayAdapter<ProductCategories.Category>(getActivity(),
                android.R.layout.simple_spinner_item);
        ArrayList<ProductCategories.Category> categories = servicesSingleton.getProductCategories();
        if (categories != null)
            mCategoryAdapter.addAll(categories);
        mCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                guessCategory();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        mProductCategorySpinner.setAdapter(mCategoryAdapter);
//        mProductCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                mSelectedCategory = mCategoryAdapter.getItem(position);
//                if (mSelectedCategory.name.equals(ProductCategories.UNDEFINED)) {
//                    mBrandsMultiSpinner.setEnabled(false);
//                } else {
//                    mBrandsMultiSpinner.setEnabled(true);
//                }
//                mBrandsMultiSpinner.setItems(mSelectedCategory.brands, mSelectedCategory.getSelected(), "Select brands", new MultiSpinner.MultiSpinnerListener() {
//                    @Override
//                    public void onItemsSelected(boolean[] selected) {
//                        mSelectedCategory.setSelected(selected);
//                        filter(mSelectedCategory);
//                    }
//                });
//
//                filter(mSelectedCategory);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                // TODO: do something
//            }
//        });
        guessCategory(); // also triggers a mProductCategorySpinner...onItemSelected

        return view;
    }

    public String getSearchString() {
        String string = mSearchEditText.getText().toString();
        if (string.equals("")) {
            mSearchEditText.setError("enter something");
            return null;
        }
        return string;
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
        double time = (System.nanoTime() - start)/1000;
        Log.v("guessCategory", "time = " + time);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mToast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);

        ServicesSingleton.getInstance(getActivity()).registerCallback(this);
    }

    @Override
    public void onResume() {
        super.onResume();

//        ServicesSingleton servicesSingleton = ServicesSingleton.getInstance(getActivity());
//        Address address = servicesSingleton.getUserAddress();
//        if (address == null)
//            mLocationButton.setText(SELECT_LOCATION);
//        else
//            // display a readable address, street if available or city
//            mLocationButton.setText(address.getMaxAddressLineIndex() > 0 ?
//                    address.getAddressLine(0) : address.getLocality());
    }

    /**
     * Called when mSearchButton has been clicked
     */
    public void searchButtonClicked() {
        ServicesSingleton servicesSingleton = ServicesSingleton.getInstance(getActivity());

        if (!servicesSingleton.checkPlayServices()){
            String error = servicesSingleton.getLocationErrorString();
            if (error != null) {
                mToast.setText(error);
                mToast.show();
            }
            return;
        }

        String searchString = mSearchEditText.getText().toString();
        if (searchString == null) {
            mToast.setText("Enter something to search");
            mToast.show();
            return;
        }

        getActivity().startActivity(new Intent(getActivity(), SearchTabsActivity.class)
                .putExtra(SearchConstraintsFragment.ARG_SEARCH_STRING, searchString));
    }

    @Override
    public void searchingForAddress() {
//        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void addressFound(Address address, boolean userSelected) {
//        mProgressBar.setVisibility(View.GONE);
//        if (address != null)
//            // display a readable address, street if available or city
//            mLocationButton.setText(address.getMaxAddressLineIndex() > 0 ?
//                    address.getAddressLine(0) : address.getLocality());
//        else if (userSelected)
//            mLocationButton.setText(CURRENT_LOCATION);
//        else
//            mLocationButton.setText(SELECT_LOCATION);
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

    /* package private */ void updateProductCategories(ArrayList<ProductCategories.Category> categories) {
        mCategoryAdapter.clear();
        mCategoryAdapter.addAll(categories);
    }

    public ProductCategories.Category getProductCategory() {
        return mSelectedCategory;
    }
}
