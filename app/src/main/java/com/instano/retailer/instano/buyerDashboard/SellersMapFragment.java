package com.instano.retailer.instano.buyerDashboard;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.MarkerOptions;
import com.instano.retailer.instano.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SellersMapFragment extends Fragment {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    public SellersMapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        if (mMap != null)
            setUpMap();

        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((MapFragment) getActivity().getFragmentManager()
                    .findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null)
                setUpMap();
        }
    }
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((com.google.android.gms.maps.MapFragment) getFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        CameraPosition bangalore = new CameraPosition.Builder().target(
                BANGALORE_LOCATION)
                .zoom(11)
                .build();

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(bangalore));
        mMap.setOnMapLongClickListener(this);
        mSelectedLocationMarker = mMap.addMarker(new MarkerOptions()
                .position(BANGALORE_LOCATION)
                .title(SELECT_LOCATION)
                .snippet("drag this marker or long click on map to select new location")
                .draggable(true));
        mSelectedLocationMarker.showInfoWindow();
        mMap.setOnMarkerDragListener(this);
    }

}
