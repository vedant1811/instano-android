package com.instano.retailer.instano.utilities;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * A subclass of AsyncTask that calls getFromLocation() in the
 * background. The class definition has these generic types:
 * Location - A Location object containing
 * the current location.
 * Void     - indicates that progress units are not used
 * String   - An address passed to onPostExecute()
 */
public class GetAddressTask extends
        AsyncTask<Double, Void, Address> {
    Context mContext;
    AddressCallback mAddressCallback;
    public GetAddressTask(Context context, AddressCallback addressCallback) {
        super();
        mContext = context;
        mAddressCallback = addressCallback;
    }

    /**
     * Get a Geocoder instance, get the latitude and longitude
     * look up the address, and return it
     *
     * @params params {latitude, longitude}
     * @return A string containing the address of the current
     * location, or an empty string if no address can be found,
     * or an error message
     */
    @Override
    protected Address doInBackground(Double... params) {
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        // Create a list to contain the result address
        List<Address> addresses = null;
        try {
            /*
             * Return 1 address.
             */
            addresses = geocoder.getFromLocation(params[0], params[1], 1);
        } catch (IOException e1) {
            Log.e("LocationSampleActivity", "IO Exception in getFromLocation()");
            e1.printStackTrace();
            return null;
        } catch (IllegalArgumentException e2) {
            // Error message to post in the log
            String errorString = "Illegal arguments " + Double.toString(params[0]) +
                    " , " + Double.toString(params[1]) + " passed to address service";
            Log.e("LocationSampleActivity", errorString);
            e2.printStackTrace();
            return null;
        }
        // If the reverse geocode returned an address
        if (addresses != null && addresses.size() > 0) {
            // Get the first address
            Address address = addresses.get(0);
            Log.d("Address", (params[0] == address.getLatitude()) + " " + (params[1] == address.getLongitude()));
            return address;
        } else {
            return null;
        }
    }

    @Override
    protected void onPostExecute(Address address) {
        mAddressCallback.addressFetched(address);
    }

    public interface AddressCallback {
        void addressFetched(@Nullable Address address);
    }
}
