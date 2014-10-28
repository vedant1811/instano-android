package com.instano.retailer.instano.utilities;

import com.instano.retailer.instano.ServicesSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;

/**
 * Represents a single immutable quote request (that is received by the seller)
 */
public class Quote {
    private ServicesSingleton servicesSingleton;
    public final int id;
    public final int buyerId;
    public final String searchString;

    /**
     * comma separated brands eg: "LG, Samsung"
     * can be null
     */
    public final String brands;

    /**
     * human readable display for price
     * can be null
     */
    public final String priceRange;
    public final String productCategory;
    public final long updatedAt; // valid only when constructed from Quote(JSONObject jsonObject)
    public final ArrayList<Integer> sellerIds;

    public Quote(ServicesSingleton servicesSingleton, int id, int buyerId, String searchString, String brands, String priceRange, String productCategory, ArrayList<Integer> sellerIds) {
        this.servicesSingleton = servicesSingleton;
        this.id = id;
        this.buyerId = buyerId;
        this.searchString = searchString;
        this.brands = brands;
        this.priceRange = priceRange;
        this.productCategory = productCategory;
        this.sellerIds = sellerIds;
        updatedAt = 0;
    }

    public Quote(ServicesSingleton servicesSingleton, int buyerId, String searchString, String brands, String priceRange, String productCategory, ArrayList<Integer> sellerIds) {
        this.servicesSingleton = servicesSingleton;
        this.productCategory = productCategory;
        this.sellerIds = sellerIds;
        this.id = -1;
        this.buyerId = buyerId;
        this.searchString = searchString.trim();
        this.brands = brands.trim();
        this.priceRange = priceRange.trim();
        updatedAt = 0;
    }

    public Quote(ServicesSingleton servicesSingleton, JSONObject jsonObject) throws JSONException, ParseException {
        this.servicesSingleton = servicesSingleton;
        String updatedAt = jsonObject.getString("updated_at");
        this.updatedAt = ServicesSingleton.dateFromString(updatedAt);
        id = jsonObject.getInt("id");
        buyerId = jsonObject.getInt("buyer_id");
        searchString = jsonObject.getString("search_string");
        brands = jsonObject.getString("brands");
        priceRange = jsonObject.getString("price_range");
        productCategory = jsonObject.getString("product_category");
        sellerIds = null;
    }

    /**
     *
     * @return Human readable time elapsed. Eg: "42 minutes ago"
     */
    public String getPrettyTimeElapsed() {
        return ServicesSingleton.getInstance(null).getPrettyTimeElapsed(updatedAt);
    }

    public JSONObject toJsonObject() {
        try {
            JSONArray sellerIds = new JSONArray(this.sellerIds);

            JSONObject quoteParamsJsonObject = new JSONObject()
                    .put("buyer_id", buyerId)
                    .put("search_string", searchString)
                    .put("brands", brands)
                    .put("price_range", priceRange)
                    .put("product_category", productCategory)
                    .put("seller_ids", sellerIds);

            if (id != -1)
                quoteParamsJsonObject.put ("id", id);

            JSONObject quoteJsonObject = new JSONObject()
                    .put("quote", quoteParamsJsonObject);
            return quoteJsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
