package com.instano.retailer.instano.utilities.models;

import android.graphics.PointF;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.instano.retailer.instano.application.ServicesSingleton;
import com.instano.retailer.instano.utilities.library.Log;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Represents a single immutable Seller
 */
public class Seller {
    public final static double INVALID_COORDINATE = -1000; // an invalid coordinate
    public static final String UNDEFINED = "Select Category";
    private static final String TAG = "Seller";

    @JsonProperty("id")
    public int id; // server generated
    @JsonProperty("name_of_shop")
    public String name_of_shop;
    @JsonProperty("name_of_seller")
    public String name_of_seller;
    @JsonProperty("address")
    public String address; // newline separated
    // TODO: convert to Pointer Double that can be null instead of being INVALID_COORDINATE
    @JsonProperty("latitude")
    public double latitude = INVALID_COORDINATE;
    @JsonProperty("longitude")
    public double longitude = INVALID_COORDINATE;
    @JsonProperty("phone")
    public String phone; // TODO: maybe make it a list of Strings
    @JsonProperty("status")
    public String status;
    public int rating; // rating is out of 50, displayed out of 5.0
    @JsonProperty("email")
    public String email;
////    @JsonProperty("categories")
//    @JsonIgnore
//    public Categories categories;
    @JsonProperty("brands")
    public ArrayList<Brand> brands;

    public Seller() {
        Log.v(TAG, "Constructor of  Seller ");
    }

    // get distance between to two points given as latitude and longitude or null on error
    @Nullable
    @JsonIgnore
    public String getPrettyDistanceFromLocation() {
        int distanceFromLocation = getDistanceFromLocation();
        if (distanceFromLocation == -1)
            return null;
        else
            return String.format("%.2f", distanceFromLocation /100.0) + " km";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Seller seller = (Seller) o;

        if (id != seller.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public static class DistanceComparator implements Comparator<Seller> {

        @Override
        public int compare(@NonNull Seller lhs, @NonNull Seller rhs) {

            if (lhs.equals(rhs))
                return 0;

            int lhsDistance = lhs.getDistanceFromLocation();
            int rhsDistance = rhs.getDistanceFromLocation();

            if (lhsDistance == rhsDistance) // happens if distance is unavailable
                // compare alphabetically
                return lhs.name_of_shop.compareTo(rhs.name_of_shop);
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

    // TODO: cache this value
    /**
     * get distance between to two points in 10x meters or -1 if last location is null or
     * seller's coordinates are invalid
     */
    @JsonIgnore
    public int getDistanceFromLocation() {

        Location lastLocation = ServicesSingleton.instance().getUserLocation();

        if (lastLocation == null || latitude == INVALID_COORDINATE || longitude == INVALID_COORDINATE)
            return -1;

        PointF p1 = new PointF((float) lastLocation.getLatitude(), (float) lastLocation.getLongitude());
        PointF p2 = new PointF((float) latitude, (float) longitude);

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

    public boolean containsCategoryAndOneBrand(@NonNull Category categoryToMatch) {

        if (categoryToMatch.name.equals(UNDEFINED))
            return true;

//        for (Category oneCategory : mCategories)
//            if (oneCategory.name.equals(categoryToMatch.name)) { // category is matched
//
//                // if no brands specified either category, then consider it matched
//                if (categoryToMatch.brands.isEmpty() || oneCategory.brands.isEmpty())
//                    return true;
//
//                // true if atleast one brand is common
//                return !Collections.disjoint(oneCategory.brands, categoryToMatch.brands);
//            }
        return false;
    }

    public boolean contains(String category) {
        if (Category.UNDEFINED.equals(category))
            return true;
        if (category != null) {
            for (Brand brand : brands) {
                if (category.equals(brand.category)) {
                    return true;
                }
            }
        }
        return false;
    }
}
