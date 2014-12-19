package com.instano.retailer.instano.utilities.models;

import com.instano.retailer.instano.application.ServicesSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;

/**
 * Represents a single immutable quote request (that is received by the seller)
 */
public class Quote {
    public final int id;
    public final int buyerId;
    public final String searchString;

    /**
     * human readable display for price
     * can be null
     */
    public final String priceRange;
    public final ProductCategories.Category productCategory;
    public final String additionalInfo;
    public final long updatedAt; // valid only when constructed from Quote(JSONObject jsonObject)
    public final HashSet<Integer> sellerIds;

    public static int getIdFrom (JSONObject quoteJsonObject) {
        try {
            return quoteJsonObject.getInt("id");
        } catch (JSONException e) {
            return -1;
        }
    }

    public Quote(int id, int buyerId, String searchString,
                 String priceRange, ProductCategories.Category productCategory, String additionalInfo,
                 HashSet<Integer> sellerIds) {
        this.id = id;
        this.buyerId = buyerId;
        this.searchString = searchString;
        this.priceRange = priceRange;
        this.productCategory = productCategory;
        this.additionalInfo = additionalInfo;
        this.sellerIds = sellerIds;
        updatedAt = 0;
    }

    public Quote(int buyerId, String searchString,
                 String priceRange, ProductCategories.Category productCategory, String additionalInfo,
                 HashSet<Integer> sellerIds) {
        this.productCategory = productCategory;
        this.sellerIds = sellerIds;
        this.id = -1;
        this.buyerId = buyerId;
        this.searchString = searchString.trim();
        this.priceRange = priceRange.trim();
        this.additionalInfo = additionalInfo.trim();
        updatedAt = 0;
    }

    public Quote(JSONObject jsonObject) throws JSONException {
        String updatedAt = jsonObject.getString("updated_at");
        this.updatedAt = ServicesSingleton.dateFromString(updatedAt);
        id = jsonObject.getInt("id");
        buyerId = jsonObject.getInt("buyer_id");
        searchString = jsonObject.getString("search_string");
        priceRange = jsonObject.getString("price_range");
        ProductCategories.Category productCategory;


        try {
            productCategory = new ProductCategories.Category(jsonObject.getJSONObject("product_category"));
        } catch (JSONException e) {
            productCategory = ProductCategories.Category.undefinedCategory();
        }
        this.productCategory = productCategory;

        String additionalInfo;

        try {
            additionalInfo = jsonObject.getString("additional_info");
        } catch (JSONException e) {
            additionalInfo = "";
        }
        this.additionalInfo = additionalInfo;

        sellerIds = null;
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
        return ServicesSingleton.getInstance(null).getPrettyTimeElapsed(updatedAt);
    }

    public JSONObject toJsonObject() {
        try {

            JSONObject quoteParamsJsonObject = new JSONObject()
                    .put("buyer_id", buyerId)
                    .put("search_string", searchString)
                    .put("additional_info", additionalInfo)
                    .put("price_range", priceRange)
                    .put("product_category", productCategory.toJsonObject());

            if (sellerIds != null && sellerIds.size() > 0) {
                quoteParamsJsonObject.put("seller_ids", new JSONArray(this.sellerIds));
            }

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
