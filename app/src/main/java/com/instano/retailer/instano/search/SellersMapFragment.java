package com.instano.retailer.instano.search;


import android.app.Fragment;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.instano.retailer.instano.SellersArrayAdapter;
import com.instano.retailer.instano.ServicesSingleton;
import com.instano.retailer.instano.utilities.GetAddressTask;
import com.instano.retailer.instano.utilities.Seller;

/**
 * A simple {@link Fragment} subclass.
 */
public class SellersMapFragment extends MapFragment implements GoogleMap.OnMapLongClickListener,
        GetAddressTask.AddressCallback, GoogleMap.OnMarkerDragListener,
        SellersArrayAdapter.SellersListener {

    private static final LatLng BANGALORE_LOCATION = new LatLng(12.9539974, 77.6309395);
    private static final String SELECT_LOCATION = "Select Location";
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Marker mSelectedLocationMarker;
    private GetAddressTask mAddressTask;

    private SparseArray<Marker> mSellerMarkers;

    /**
     * This is the where all initialization of the class (or map) must take place.
     * It is analogous to activity.onCreate
     * This is where we can add markers or lines, add listeners or move the camera.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

        mSellerMarkers = new SparseArray<Marker>();

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        LatLng startLatLng;
        Location userLocation = ServicesSingleton.getInstance(getActivity()).getUserLocation();
        if (userLocation != null)
            startLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
        else
            startLatLng = BANGALORE_LOCATION;
        CameraPosition bangalore = new CameraPosition.Builder().target(
                startLatLng)
                .zoom(11)
                .build();

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(bangalore));
        mMap.setOnMapLongClickListener(this);
        mSelectedLocationMarker = mMap.addMarker(new MarkerOptions()
                .position(startLatLng)
                .title(SELECT_LOCATION)
                .snippet("drag this marker or long click on map to select new location")
                .draggable(true));
        mSelectedLocationMarker.showInfoWindow();
        mMap.setOnMarkerDragListener(this);

        SellersArrayAdapter sellersArrayAdapter = ServicesSingleton.getInstance(getActivity()).getSellersArrayAdapter();
        sellersArrayAdapter.setListener(this);
        SparseArray<Seller> sellers = sellersArrayAdapter.getAllSellers();
        for (int i = 0; i < sellers.size(); i++)
            sellerAdded(sellers.valueAt(i));
        updateMarkers();

    }

    private void updateMarkers() {
        SellersArrayAdapter sellersArrayAdapter = ServicesSingleton.getInstance(getActivity()).getSellersArrayAdapter();

        for (int i = 0; i < mSellerMarkers.size(); i++) {
            Marker marker = mSellerMarkers.valueAt(i);
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        }

        for (Seller seller : sellersArrayAdapter.getFilteredSellers()) {
            Marker marker = mSellerMarkers.get(seller.hashCode());
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        }
    }

    @Override
    public void sellerAdded(Seller seller) {
        Marker newMarker = mMap.addMarker(
                new MarkerOptions()
                .position(new LatLng(seller.latitude, seller.longitude))
                .title(seller.nameOfShop)
                .snippet(seller.address)
        );
        mSellerMarkers.put(seller.hashCode(), newMarker);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        mSelectedLocationMarker.setPosition(latLng);
        refreshMarker();
    }

    private void refreshMarker() {

        LatLng latLng = mSelectedLocationMarker.getPosition();

        if (mAddressTask != null)
            mAddressTask.cancel(true); // to make sure addressFetched is not called
        mAddressTask = new GetAddressTask(getActivity(), this);
        mAddressTask.execute(latLng.latitude, latLng.longitude);

        mSelectedLocationMarker.setTitle("Selected Location");
        mSelectedLocationMarker.setSnippet(null);
        resetInfoWindow();
    }

    @Override
    public void addressFetched(@Nullable Address address) {
        if (address != null && address.getMaxAddressLineIndex() > 0)
            mSelectedLocationMarker.setSnippet(address.getAddressLine(0));
        resetInfoWindow();
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        mSelectedLocationMarker.setSnippet(null);
        resetInfoWindow();
    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        refreshMarker();
    }

    private void resetInfoWindow() {
        mSelectedLocationMarker.hideInfoWindow();
        mSelectedLocationMarker.showInfoWindow();
    }

    public SellersMapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        setUpMapIfNeeded();
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        updateMarkers();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }
}
