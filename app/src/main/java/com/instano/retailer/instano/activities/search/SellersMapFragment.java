package com.instano.retailer.instano.activities.search;


import android.app.Fragment;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

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
import com.instano.retailer.instano.application.ServicesSingleton;
import com.instano.retailer.instano.application.controller.Quotations;
import com.instano.retailer.instano.utilities.GetAddressTask;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.model.Seller;

import java.util.HashMap;

import rx.Subscription;
import rx.android.observables.AndroidObservable;
import rx.subscriptions.BooleanSubscription;

/**
 * A simple {@link Fragment} subclass.
 */
public class SellersMapFragment extends Fragment implements GoogleMap.OnMapLongClickListener,
        GetAddressTask.AddressCallback, GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener {

    private static final String TAG = "SellerMapFragment";
    private static BitmapDescriptor BLUE_MARKER;
    private MapView mMapView;

    /* mMap variables */
    private static final LatLng BANGALORE_LOCATION = new LatLng(12.9539974, 77.6309395);
    private static final String SELECT_LOCATION = "Select Location";
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Marker mSelectedLocationMarker;
    private GetAddressTask mAddressTask;

    /* seller's info included layout*/
    private Seller mSelectedSeller;
    private View mTopmostSellerInfoView;
    private TextView mShopName;
    private TextView mShopAddress;
    private TextView mDistanceTextView;

    private HashMap<Marker, Seller> mSellerMarkers;
    private Subscription mSellersSubscription;
    private int mProductId;

    public void setProduct(int productId) {
        QuotationsAndSellersAdapter adapter = ((ResultsActivity)getActivity()).getAdapter();

        adapter.clear();
//        setShown(false);

        Log.d(TAG, "calling query quotation");
        AndroidObservable.bindFragment(this, Quotations.controller().fetchQuotationsForProduct(productId))
                .subscribe(quotationCard -> {
                    AndroidObservable.bindFragment(this, Quotations.controller().fetchQuotationsForProduct(productId))
                            .subscribe(list -> { // first just clear the map
                                Log.d(TAG, ".setUpMap clearing map");
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
                            }, throwable -> Log.fatalError(new RuntimeException(
                                            "error response in subscribe to getFilteredSellersObservable",
                                            throwable)
                            ));
//                    setShown(true);
                    adapter.add(quotationCard);
                }, error -> Log.fatalError(new RuntimeException(error)));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QuotationsAndSellersAdapter adapter= ((ResultsActivity)getActivity()).getAdapter();

        setProduct(6739);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        setListShown(mShown);
    }

    /**
     * This is the where all initialization of the class (or mMap) must take place.
     * It is analogous to activity.onCreate
     * This is where we can add markers or lines, add listeners or move the camera.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        BLUE_MARKER = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);

        mSellerMarkers = new HashMap<>();
        mSellersSubscription = BooleanSubscription.create(); // just a place holder instead of null

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
                .zoom(13)
                .build();

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(bangalore));
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);

        mSelectedLocationMarker = mMap.addMarker(new MarkerOptions()
                .position(startLatLng)
                .title(SELECT_LOCATION)
                .snippet("drag this marker or long click on map to select new location")
                .icon(BLUE_MARKER)
                .draggable(true));
        mSelectedLocationMarker.showInfoWindow();
        mMap.setOnMarkerDragListener(this);

        ResultsActivity activity = (ResultsActivity) getActivity();

        Log.d(TAG, ".setUpMap setUpMap");

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        mSelectedLocationMarker.setPosition(latLng);
        refreshMarker();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Seller seller = mSellerMarkers.get(marker);
        if (seller != null) { // can be another marker
            mSelectedSeller = seller;
            mTopmostSellerInfoView.setVisibility(View.VISIBLE);
            mShopName.setText(mSelectedSeller.name_of_shop);
            mDistanceTextView.setText(mSelectedSeller.outlets.get(0).getPrettyDistanceFromLocation());
            mShopAddress.setText(mSelectedSeller.outlets.get(0).address);
        }
        return false;
    }

    private void refreshMarker() {

        LatLng latLng = mSelectedLocationMarker.getPosition();

        if (mAddressTask != null)
            mAddressTask.cancel(true); // to make sure addressFetched is not called
        mAddressTask = new GetAddressTask(getActivity(), this);
        mAddressTask.execute(latLng.latitude, latLng.longitude);
        sendLocation(null);

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

        // set up seller info:
        mTopmostSellerInfoView = view.findViewById(R.id.includedListItem);
        mShopName = (TextView) view.findViewById(R.id.shopNameTextView);
        mShopAddress = (TextView) view.findViewById(R.id.addressTextView);
        mDistanceTextView = (TextView) view.findViewById(R.id.distanceTextView);

        ImageButton callImageButton = (ImageButton) view.findViewById(R.id.callImageButton);
        callImageButton.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mSelectedSeller.outlets.get(0).phone));
            startActivity(callIntent);
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    private void sendLocation(Address address) {
        if (!SELECT_LOCATION.equals(mSelectedLocationMarker.getTitle())) {// i.e. if location has been updated
            ServicesSingleton.instance().userSelectsLocation(
                    mSelectedLocationMarker.getPosition(), ServicesSingleton.readableAddress(address));
            Log.d("address", "address updated by SellersMapFragment:" + address);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
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
