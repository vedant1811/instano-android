package com.instano.retailer.instano.utilities.model;

import android.graphics.PointF;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.instano.retailer.instano.application.ServicesSingleton;
import com.instano.retailer.instano.utilities.library.Log;

import java.util.Comparator;

/**
 * Created by vedant on 5/1/15.
 */
public class Outlet {
    @JsonProperty("id")
    public int id;

    @JsonProperty("seller_id")
    public int seller_id;

    @JsonProperty("address")
    public String address; // newline separated

    @JsonProperty("latitude")
    public Double latitude;

    @JsonProperty("longitude")
    public Double longitude;

    @JsonProperty("seller_name")
    public String seller_name;

    @JsonProperty("phone")
    public String phone;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Outlet outlet = (Outlet) o;

        return id == outlet.id;

    }

    @Override
    public int hashCode() {
        return id;
    }

    public static class DistanceComparator implements Comparator<Outlet> {

        @Override
        public int compare(@NonNull Outlet lhs, @NonNull Outlet rhs) {

            if (lhs.equals(rhs))
                return 0;

            int lhsDistance = lhs.getDistanceFromLocation();
            int rhsDistance = rhs.getDistanceFromLocation();

            if (lhsDistance == rhsDistance) {// happens if distance is unavailable
                Log.e("Outlet", "comparing without location");
                // compare alphabetically
                return lhs.seller_name.compareTo(rhs.seller_name);
            }
            if (lhsDistance == -1)
                return 1;
            if (rhsDistance == -1)
                return -1;
            return lhsDistance - rhsDistance;
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof DistanceComparator;
        }
    }

    // get distance between to two points given as latitude and longitude or null on error
    @Nullable
    public String getPrettyDistanceFromLocation() {
        int distanceFromLocation = getDistanceFromLocation();
        if (distanceFromLocation == -1)
            return null;
        else
            return String.format("%.2f", distanceFromLocation /100.0) + " km";
    }

    // TODO: cache this value
    /**
     * get distance between to two points in 10x meters or -1 if last location is null or
     * seller's coordinates are invalid
     */
    public int getDistanceFromLocation() {

        Location lastLocation = ServicesSingleton.instance().getUserLocation();

        if (lastLocation == null || latitude == null || longitude == null)
            return -1;

        PointF p1 = new PointF((float) lastLocation.getLatitude(), (float) lastLocation.getLongitude());
        PointF p2 = new PointF(latitude.floatValue(), longitude.floatValue());

        double R = 637100; // 10x meters
        double dLat = Math.toRadians(p2.x - p1.x);
        double dLon = Math.toRadians(p2.y - p1.y);
        double lat1 = Math.toRadians(p1.x);
        double lat2 = Math.toRadians(p2.x);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2)
                * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;
        return (int) distance;
    }
}
