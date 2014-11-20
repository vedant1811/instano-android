package com.instano.retailer.instano.activities;

import android.app.Activity;
import android.location.Address;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.instano.retailer.instano.R;
import com.instano.retailer.instano.ServicesSingleton;
import com.instano.retailer.instano.utilities.GetAddressTask;

public class MapsActivity extends Activity implements GoogleMap.OnMapLongClickListener,
        GetAddressTask.AddressCallback, GoogleMap.OnMarkerDragListener {

    private static final LatLng BANGALORE_LOCATION = new LatLng(12.9539974, 77.6309395);
    private static final String SELECT_LOCATION = "Select Location";
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Marker mSelectedLocationMarker;
    private Address mAddressOfSelectedLocation;
    private GetAddressTask mAddressTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public void onBackPressed()
    {
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return(super.onOptionsItemSelected(item));
    }


    @Override
    public void finish() {
        if (!mSelectedLocationMarker.getTitle().equals(SELECT_LOCATION)) {
            // also calls initial data update callback which should be connected to Purchase Activity
            ServicesSingleton.getInstance(null).userSelectsLocation(
                    mSelectedLocationMarker.getPosition(), mAddressOfSelectedLocation);
        }

        super.finish();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link MapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
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
                .snippet("drag this marker or long click on map\nto select new location")
                .draggable(true));
        mSelectedLocationMarker.showInfoWindow();
        mMap.setOnMarkerDragListener(this);
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
        mAddressTask = new GetAddressTask(this, this);
        mAddressTask.execute(latLng.latitude, latLng.longitude);

        mSelectedLocationMarker.setTitle("Selected Location");
        mSelectedLocationMarker.setSnippet(null);
        resetInfoWindow();
    }

    @Override
    public void addressFetched(@Nullable Address address) {
        if (address != null && address.getMaxAddressLineIndex() > 0)
            mSelectedLocationMarker.setSnippet(address.getAddressLine(0));
        mAddressOfSelectedLocation = address;
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
}
