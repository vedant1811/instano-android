package com.instano.retailer.instano.utilities.models;

import android.support.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.instano.retailer.instano.application.ServicesSingleton;
import com.instano.retailer.instano.utilities.library.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;

/**
 * Represents a single immutable quote request (that is received by the seller)
 */
public class Quote {
    public final static double INVALID_COORDINATE = -1000; // an invalid coordinate

    @JsonProperty("id")
    public  int id;
    @JsonProperty("buyer_id")
    public  int buyerId;
    @JsonProperty("search_string")
    public  String searchString;

    /**
     * human readable display for price
     * can be null
     */
    @JsonProperty("price_range")
    public  String priceRange;
    @JsonProperty("product_category")
    public  ProductCategories.Category productCategory;
    @JsonProperty("brands")
    public  String brands;
//    @JsonProperty("updated_at")
    public  long updatedAt; // valid only when constructed from Quote(JSONObject jsonObject)
    @JsonProperty("seller_ids")
    public  HashSet<Integer> sellerIds;
    @Nullable
    @JsonProperty("address")
    public  String address; // newline separated
    @JsonProperty("latitude")
    public  double latitude;
    @JsonProperty("longitude")
    public  double longitude;

    public static int getIdFrom (JSONObject quoteJsonObject) {
        try {
            return quoteJsonObject.getInt("id");
        } catch (JSONException e) {
            return -1;
        }
    }

    public Quote () {
        // Dummy constructor for Json Jackson
    }

    public Quote(int id, int buyerId, String searchString,
                 String priceRange, ProductCategories.Category productCategory, String brands,
                 HashSet<Integer> sellerIds, String address, double latitude, double longitude) {
        this.id = id;
        this.buyerId = buyerId;
        this.searchString = searchString;
        this.priceRange = priceRange;
        this.productCategory = productCategory;
        this.brands = brands;
        this.sellerIds = sellerIds;
        updatedAt = 0;
        this.address = address.trim();
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Quote(int buyerId, String searchString,
                 String priceRange, ProductCategories.Category productCategory, String brands,
                 String address, double latitude, double longitude) {
        this.productCategory = productCategory;
        this.sellerIds = sellerIds;
        this.id = -1;
        this.buyerId = buyerId;
        this.searchString = searchString.trim();
        this.priceRange = priceRange.trim();
        this.brands = brands.trim();
        updatedAt = 0;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Quote(JSONObject jsonObject) throws JSONException {
        String updatedAt = jsonObject.getString("updated_at");
        this.updatedAt = ServicesSingleton.dateFromString(updatedAt);
        id = jsonObject.getInt("id");
        buyerId = jsonObject.getInt("buyer_id");
        searchString = jsonObject.getString("search_string");
        priceRange = jsonObject.getString("price_range");
        ProductCategories.Category productCategory;

        String address;
        try {
            address = jsonObject.getString("address");
        } catch (JSONException e) {
            address = null;
        }
        this.address = address;

        double latitude = INVALID_COORDINATE;
        double longitude = INVALID_COORDINATE;
        try {
            latitude = jsonObject.getDouble("latitude");
            longitude = jsonObject.getDouble("longitude");
        } catch (JSONException e) {
            latitude = INVALID_COORDINATE;
            longitude = INVALID_COORDINATE;
        } finally {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        try {
            productCategory = new ProductCategories.Category(jsonObject.getJSONObject("product_category"));
        } catch (JSONException e) {
            productCategory = ProductCategories.Category.undefinedCategory();
        }
        this.productCategory = productCategory;

        String brands;

        try {
            brands = jsonObject.getString("brands");
        } catch (JSONException e) {
            brands = "";
        }
        this.brands = brands;

        sellerIds = new HashSet<Integer>();
        JSONArray idJsonArray = jsonObject.getJSONArray("seller_ids");
        for (int i = 0; i < idJsonArray.length(); i++) {
            int sellerId = idJsonArray.getInt(i);
            sellerIds.add(sellerId);
        }
        sellerIds.remove(0); // removing the seller id= 0, if exists.
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Quote quote = (Quote) o;

        if (id != quote.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }

    /**
     *
     * @return Human readable time elapsed. Eg: "42 minutes ago"
     */
    public String getPrettyTimeElapsed() {
        return ServicesSingleton.instance().getPrettyTimeElapsed(updatedAt);
    }

    public JSONObject toJsonObject() {
        try {

            JSONObject params = new JSONObject()
                    .put("buyer_id", buyerId)
                    .put("search_string", searchString)
                    .put("brands", brands)
                    .put("price_range", priceRange)
                    .put("product_category", productCategory.toJsonObject());

            if (sellerIds != null && sellerIds.size() > 0) {
                params.put("seller_ids", new JSONArray(this.sellerIds));
            }

            if (id != -1)
                params.put("id", id);

            if (address != null)
                params.put("address", address);

            if (latitude != INVALID_COORDINATE) {
                params.put("latitude", latitude)
                        .put("longitude", longitude);
            }

            JSONObject quoteJsonObject = new JSONObject()
                    .put("quote", params);
            return quoteJsonObject;
        } catch (JSONException e) {
            Log.fatalError(e);
        }
        return null;
    }
}
