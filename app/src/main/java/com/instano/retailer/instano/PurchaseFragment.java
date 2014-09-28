package com.instano.retailer.instano;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PurchaseFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class PurchaseFragment extends Fragment implements ServicesSingleton.InitialDataCallbacks {

    private final static String CURRENT_LOCATION = "Current Location";
    private final static String SELECT_LOCATION = "Select Location";

    private Button mSearchButton;
    private Button mLocationButton;
    private ProgressBar mProgressBar;
    private EditText mSearchEditText;
    private Spinner mProductCategorySpinner;

    private Toast mToast;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment PurchaseFragment.
     */
    public static PurchaseFragment newInstance() {
        PurchaseFragment fragment = new PurchaseFragment();
        return fragment;
    }
    public PurchaseFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_purchase, container, false);
        // mSearchButton is not enabled in XML
        mSearchButton = (Button) view.findViewById(R.id.search_button);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchButtonClicked();
            }
        });

        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mSearchEditText = (EditText) view.findViewById(R.id.searchEditText);

        mLocationButton = (Button) view.findViewById(R.id.locationButton);
        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLocationButtonClicked();
            }
        });

        mProductCategorySpinner = (Spinner) view.findViewById(R.id.productCategorySpinner);

        ArrayAdapter adapter = new ArrayAdapter(getActivity(),
                android.R.layout.simple_spinner_item, new ArrayList());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mProductCategorySpinner.setAdapter(adapter);

        return view;
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

        ServicesSingleton servicesSingleton = ServicesSingleton.getInstance(getActivity());
        Address address = servicesSingleton.getLatestAddress();
        if (address == null)
            mLocationButton.setText(SELECT_LOCATION);
        else
            // display a readable address, street if available or city
            mLocationButton.setText(address.getMaxAddressLineIndex() > 0 ?
                    address.getAddressLine(0) : address.getLocality());
        ArrayAdapter adapter = (ArrayAdapter) mProductCategorySpinner.getAdapter();
        adapter.addAll(servicesSingleton.getProductCategories());
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

        int pos = mProductCategorySpinner.getSelectedItemPosition();

        if (pos == AdapterView.INVALID_POSITION)
            pos = 0; // "unspecified"

        servicesSingleton.sendQuoteRequest(searchString, "", "", pos);
        getFragmentManager().beginTransaction()
                .replace(getId(), SearchingFragment.newInstance(searchString))
                .commit();
    }

    /**
     * Called when mLocationButton has been clicked
     */
    private void mLocationButtonClicked() {
        getActivity().startActivity(new Intent(getActivity(), MapsActivity.class));
    }

    @Override
    public void productCategoriesUpdated(ArrayList<String> productCategories) {
        ArrayAdapter adapter = (ArrayAdapter) mProductCategorySpinner.getAdapter();
        adapter.addAll(productCategories);
    }

    @Override
    public void searchingForAddress() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void addressFound(Address address) {
        mProgressBar.setVisibility(View.GONE);
        if (address != null)
            // display a readable address, street if available or city
            mLocationButton.setText(address.getMaxAddressLineIndex() > 0 ?
                    address.getAddressLine(0) : address.getLocality());
        else
            mLocationButton.setText(SELECT_LOCATION);
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
