package com.instano.retailer.instano.utilities.models;

import android.graphics.PointF;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.instano.retailer.instano.application.ServicesSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;

/**
 * Represents a single immutable Seller
 */
public class Seller {
    public  static double INVALID_COORDINATE = -1000; // an invalid coordinate

    @JsonProperty("id")
    public  int id; // server generated
    @JsonProperty("name_of_shop")
    public  String name_of_shop;
    @JsonProperty("name_of_seller")
    public  String name_of_seller;
    @JsonProperty("address")
    public  String address; // newline separated
    // TODO: convert to Pointer Double that can be null instead of being INVALID_COORDINATE
    @JsonProperty("latitude")
    public  double latitude;
    @JsonProperty("longitude")
    public  double longitude;
    @JsonProperty("phone")
    public  String phone; // TODO: maybe make it a list of Strings
    @JsonProperty("status")
    public String status;
    public  int rating; // rating is out of 50, displayed out of 5.0
    @JsonProperty("email")
    public  String email;
//    @JsonProperty("categories")
    public  ProductCategories productCategories;

    public Seller() {

    }

    public Seller(int id, String nameOfShop, String nameOfSeller, String address, double latitude, double longitude, String phone, int rating, String email, ProductCategories productCategories) {
        this.id = id;
        this.name_of_shop = nameOfShop;
        this.name_of_seller = nameOfSeller;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phone = phone;
        this.rating = rating;
        this.email = email;
        this.productCategories = productCategories;
    }

    /**
     * if id and rating are not available, they are set to invalid i.e. -1
     */
    public Seller(String nameOfShop, String nameOfSeller, String address, double latitude, double longitude, String phone, String email, ProductCategories productCategories) {
        this.id = -1;
        this.name_of_shop = nameOfShop.trim();
        this.name_of_seller = nameOfSeller.trim();
        this.address = address.trim();
        this.latitude = latitude;
        this.longitude = longitude;
        this.phone = phone.trim();
        this.rating = -1;
        this.email = email.trim();
        this.productCategories = productCategories;
    }

    public Seller(JSONObject sellerJsonObject) throws JSONException {
        id = sellerJsonObject.getInt("id");
        name_of_shop = sellerJsonObject.getString("name_of_shop");
        name_of_seller = sellerJsonObject.getString("name_of_seller");
        address = sellerJsonObject.getString("address");

        double latitude = INVALID_COORDINATE;
        double longitude = INVALID_COORDINATE;
        try {
            latitude = sellerJsonObject.getDouble("latitude");
            longitude = sellerJsonObject.getDouble("longitude");
        } catch (JSONException e) {
            latitude = INVALID_COORDINATE;
            longitude = INVALID_COORDINATE;
        } finally {
            this.latitude = latitude;
            this.longitude = longitude;
        }
        phone = sellerJsonObject.getString("phone");
        int rating;
        try {
            rating = Integer.parseInt(sellerJsonObject.getString("rating"));
        } catch (NumberFormatException e) {
            rating = -1;
        }
        this.rating = rating;
        email = sellerJsonObject.getString("email");


        productCategories = new ProductCategories(sellerJsonObject, false);
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject retailerParamsJsonObject = new JSONObject();
        retailerParamsJsonObject.put("name_of_shop", name_of_shop)
                .put("name_of_seller", name_of_seller)
                .put("address", address)
                .put("latitude", latitude)
                .put("longitude", longitude)
                .put("phone", phone)
                .put("email", email);

        if (id != -1)
            retailerParamsJsonObject.put("id", id);
        if (rating != -1)
            retailerParamsJsonObject.put("rating", rating);

        JSONArray productCategoriesJsonArray = new JSONArray();

        for (ProductCategories.Category category : productCategories.getProductCategories()) {
            if (category.getSelected() == null)
                continue;

            // create the json object to be added to the json array
            JSONObject categoryJsonObject = new JSONObject()
                    .put("name", category.name);

            JSONArray brandsJsonArray = new JSONArray();

            for (int i = 0; i < category.getSelected().length; i++) {
                if(category.getSelected()[i])
                    brandsJsonArray.put(category.brands.get(i));
            }

            categoryJsonObject.put("brands", brandsJsonArray);

            // add the created object to the json array
            productCategoriesJsonArray.put(categoryJsonObject);
        }

        retailerParamsJsonObject.put("categories", productCategoriesJsonArray);

        JSONObject retailerJsonObject = new JSONObject();
        retailerJsonObject.put("seller", retailerParamsJsonObject);

        return retailerJsonObject;
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
}
