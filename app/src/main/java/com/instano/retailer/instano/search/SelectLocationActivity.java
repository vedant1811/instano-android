package com.instano.retailer.instano.search;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.BaseActivity;
import com.instano.retailer.instano.application.ServicesSingleton;
import com.instano.retailer.instano.utilities.GetAddressTask;
import com.instano.retailer.instano.utilities.library.Log;

/**
 * A simple {@link android.app.Fragment} subclass.
 */
public class SelectLocationActivity extends BaseActivity implements
        GetAddressTask.AddressCallback, GoogleMap.OnCameraChangeListener {

    public final static String KEY_EXTRA_LATITUDE = "latitude";
    public final static String KEY_EXTRA_LONGITUDE = "longitude";
    public final static String KEY_READABLE_ADDRESS = "address";

    private MapView mMapView;
    private TextView mAddressTextView;

    /* mMap variables */
    private static final LatLng BANGALORE_LOCATION = new LatLng(12.9539974, 77.6309395);
    private static final String SELECT_LOCATION = "Click to select Location";
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
//    private Marker mSelectedLocationMarker;
    private GetAddressTask mAddressTask;

    private CameraPosition mLastCameraPosition;
    private String mLastFetchedAddress;

    @Override
     public void onBackPressed() {
        finish();
    }

    /**
     * This is the where all initialization of the class (or mMap) must take place.
     * It is analogous to activity.onCreate
     * This is where we can add markers or lines, add listeners or move the camera.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        LatLng startLatLng;
        ServicesSingleton servicesSingleton = ServicesSingleton.instance();
        Location userLocation = servicesSingleton.getUserLocation();
        if (userLocation != null)
            startLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
        else
            startLatLng = BANGALORE_LOCATION;
        mLastCameraPosition = new CameraPosition.Builder().target(
                startLatLng)
                .zoom(14)
                .build();

        mMap.setOnCameraChangeListener(this);

//        IconGenerator iconGenerator = new IconGenerator(getActivity());
//        Bitmap markerBitmap = iconGenerator.makeIcon("move me");

//        mSelectedLocationMarker = mMap.addMarker(new MarkerOptions()
//                .position(startLatLng)
//                .title(SELECT_LOCATION)
//                .snippet("Click marker to select new location")
////                .icon(BitmapDescriptorFactory.fromResource(R.drawable.instano_logo))
////                .icon(BitmapDescriptorFactory.fromBitmap(markerBitmap))
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
//        mSelectedLocationMarker.showInfoWindow();
    }

    public void markerButtonClicked(View view) {
//        LatLng latLng = mSelectedLocationMarker.getPosition();
        LatLng latLng = mMap.getCameraPosition().target;

        Intent resultIntent = new Intent();
        resultIntent.putExtra(KEY_EXTRA_LATITUDE, latLng.latitude);
        resultIntent.putExtra(KEY_EXTRA_LONGITUDE, latLng.longitude);
        if (mLastFetchedAddress != null)
            resultIntent.putExtra(KEY_READABLE_ADDRESS, mLastFetchedAddress);
        setResult(RESULT_OK, resultIntent);
        finish();
        Log.d(Log.ADDRESS_UPDATED, "address updated by MapActivity:" + mLastFetchedAddress);
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        // always keep the marker in the center.
//        mSelectedLocationMarker.setPosition(cameraPosition.target);
        refreshMarker();
    }

    private void refreshMarker() {

//        LatLng latLng = mSelectedLocationMarker.getPosition();
        LatLng latLng = mMap.getCameraPosition().target;

        if (mAddressTask != null)
            mAddressTask.cancel(true); // to make sure addressFetched is not called
        mAddressTask = new GetAddressTask(this, this);
        mAddressTask.execute(latLng.latitude, latLng.longitude);
        mLastFetchedAddress = null;
        mAddressTextView.setText("Fetching address...");
//        mSelectedLocationMarker.setTitle(SELECT_LOCATION);
//        mSelectedLocationMarker.setSnippet("");
//        resetInfoWindow();
    }

    @Override
    public void addressFetched(@Nullable Address address) {
        if (address != null && address.getMaxAddressLineIndex() > 0) {
//            mSelectedLocationMarker.setSnippet(address.getAddressLine(0));
            mLastFetchedAddress = ServicesSingleton.readableAddress(address);
            mAddressTextView.setText(mLastFetchedAddress);
        }
//        resetInfoWindow();
    }

    private void resetInfoWindow() {
//        mSelectedLocationMarker.hideInfoWindow();
//        mSelectedLocationMarker.showInfoWindow();
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_location);
        // Gets the MapView from the XML layout and creates it
        mMapView = (MapView) findViewById(R.id.map);
        mAddressTextView = (TextView) findViewById(R.id.addressTextView);

        mMapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        mMap = mMapView.getMap();

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(this);

        setUpMap();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        mLastCameraPosition = mMap.getCameraPosition();
    }

    @Override
    public void onResume() {
        long start = System.nanoTime();
        super.onResume();
        mMapView.onResume();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mLastCameraPosition));
        double timeTaken = (System.nanoTime() - start)/Log.ONE_MILLION;
        // Get the button view
        View locationButton = ((View) mMapView.findViewById(1).getParent()).findViewById(2);

        // and next place it, for exemple, on bottom right (as Google Maps app)
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        // 40 dp in pixels
        int px40 = ServicesSingleton.instance().dpToPixels(40);
        // 8 dp in pixels
        int px8 = ServicesSingleton.instance().dpToPixels(8);
        rlp.setMargins(0, px40, px8, 0);
        Log.d(Log.TIMER_TAG, "MapActivity.onResume took " + timeTaken + "ms");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState (@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }
}
