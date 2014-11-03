package com.instano.retailer.instano.utilities;

import android.graphics.PointF;
import android.location.Location;

import com.instano.retailer.instano.ServicesSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a single immutable Seller
 */
public class Seller {
    public final static double INVALID_COORDINATE = -1000; // an invalid coordinate

    public final int id; // server generated
    public final String nameOfShop;
    public final String nameOfSeller;
    public final String address; // newline separated
    public final double latitude;
    public final double longitude;
    public final String phone; // TODO: maybe make it a list of Strings
    public final int rating; // rating is out of 50, displayed out of 5.0
    public final String email;
    public final ProductCategories productCategories;

    public Seller(int id, String nameOfShop, String nameOfSeller, String address, double latitude, double longitude, String phone, int rating, String email, ProductCategories productCategories) {
        this.id = id;
        this.nameOfShop = nameOfShop;
        this.nameOfSeller = nameOfSeller;
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
        this.nameOfShop = nameOfShop.trim();
        this.nameOfSeller = nameOfSeller.trim();
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
        nameOfShop = sellerJsonObject.getString("name_of_shop");
        nameOfSeller = sellerJsonObject.getString("name_of_seller");
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
        retailerParamsJsonObject.put("name_of_shop", nameOfShop)
                .put("name_of_seller", nameOfSeller)
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
    public String getPrettyDistanceFromLocation() {
        int distanceFromLocation = getDistanceFromLocation();
        if (distanceFromLocation == -1)
            return null;
        else
            return String.format("%.2f", distanceFromLocation /100.0) + " km";
    }

    // get distance between to two points in 10x meters or -1
    public int getDistanceFromLocation() {

        Location lastLocation = ServicesSingleton.getInstance(null).getLastLocation();

        if (lastLocation == null)
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
