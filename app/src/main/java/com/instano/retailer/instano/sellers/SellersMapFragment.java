package com.instano.retailer.instano.sellers;


import android.app.Fragment;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.DataManager;
import com.instano.retailer.instano.application.ServicesSingleton;
import com.instano.retailer.instano.utilities.GetAddressTask;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.models.ProductCategories;
import com.instano.retailer.instano.utilities.models.Seller;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class SellersMapFragment extends Fragment implements GoogleMap.OnMapLongClickListener,
        GetAddressTask.AddressCallback, GoogleMap.OnMarkerDragListener {

    private static BitmapDescriptor BLUE_MARKER;
    private MapView mMapView;

    /* mMap variables */
    private static final LatLng BANGALORE_LOCATION = new LatLng(12.9539974, 77.6309395);
    private static final String SELECT_LOCATION = "Select Location";
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Marker mSelectedLocationMarker;
    private GetAddressTask mAddressTask;

    private HashMap<Marker, Seller> mSellerMarkers;
    private String mCategory = ProductCategories.UNDEFINED;

    /**
     * This is the where all initialization of the class (or mMap) must take place.
     * It is analogous to activity.onCreate
     * This is where we can add markers or lines, add listeners or move the camera.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        BLUE_MARKER = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);

        mSellerMarkers = new HashMap<Marker, Seller>();

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        LatLng startLatLng;
        Location userLocation = ServicesSingleton.instance().getUserLocation();
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


//        IconGenerator iconGenerator = new IconGenerator(getActivity());
//        Bitmap markerBitmap = iconGenerator.makeIcon("move me");

        mSelectedLocationMarker = mMap.addMarker(new MarkerOptions()
                .position(startLatLng)
                .title(SELECT_LOCATION)
                .snippet("drag this marker or long click on map to select new location")
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.instano_logo))
//                .icon(BitmapDescriptorFactory.fromBitmap(markerBitmap))
                .icon(BLUE_MARKER)
                .draggable(true));
        mSelectedLocationMarker.showInfoWindow();
        mMap.setOnMarkerDragListener(this);

        addSellers(DataManager.instance().getSellers());
    }

    /* package private */
    void addSellers(Collection<Seller> sellers) {
        mMap.clear();
        mSelectedLocationMarker = mMap.addMarker(new MarkerOptions()
                .position(mSelectedLocationMarker.getPosition())
                .title(mSelectedLocationMarker.getTitle())
                .snippet(mSelectedLocationMarker.getSnippet())
                .icon(BLUE_MARKER)
                .draggable(true)
        );
        mSelectedLocationMarker.showInfoWindow();
        mSellerMarkers.clear();

        for (Seller seller : sellers)
            addSeller(seller);

        updateMarkers();
    }

    /* package private */
    void setCategory(String category) {
        mCategory = category;
    }

    private void updateMarkers() {
        long start = System.nanoTime();

        for (Map.Entry<Marker, Seller> entry : mSellerMarkers.entrySet()) {
            if (entry.getValue().productCategories.contains(mCategory))
                entry.getKey().setVisible(true);
            else
                entry.getKey().setVisible(false);
        }
        double timeTaken = (System.nanoTime() - start)/1000000;
        Log.d("Timing", "updateMarkers took " + timeTaken + "ms");
    }

    public void addSeller(Seller seller) {
        Marker newMarker = mMap.addMarker(
                new MarkerOptions()
                        .position(new LatLng(seller.latitude, seller.longitude))
                        .title(seller.nameOfShop)
                        .snippet(seller.address)
        );
        mSellerMarkers.put(newMarker, seller);
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
        mSelectedLocationMarker.setSnippet("");
        resetInfoWindow();
    }

    @Override
    public void addressFetched(@Nullable Address address) {
        if (address != null && address.getMaxAddressLineIndex() > 0)
            mSelectedLocationMarker.setSnippet(address.getAddressLine(0));
        resetInfoWindow();
        sendLocation(address);
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
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sellers_map, container, false);

        // Gets the MapView from the XML layout and creates it
        mMapView = (MapView) view.findViewById(R.id.map);

        mMapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        mMap = mMapView.getMap();

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(this.getActivity());

        setUpMap();

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    private void sendLocation(Address address) {
//        if (!SELECT_LOCATION.equals(mSelectedLocationMarker.getTitle())) {// i.e. if location has been updated
//            ServicesSingleton.instance().userSelectsLocation(
//                    mSelectedLocationMarker.getPosition(), address);
//            Log.d("address", "address updated by SellersMapFragment:" + address);
//        }
    }

    @Override
    public void onResume() {
        long start = System.nanoTime();
        super.onResume();
        mMapView.onResume();
        updateMarkers();
        double timeTaken = (System.nanoTime() - start)/1000000;
        Log.d("Timing", "SellersMapFragment.onResume took " + timeTaken + "ms");
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
    public void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }
}