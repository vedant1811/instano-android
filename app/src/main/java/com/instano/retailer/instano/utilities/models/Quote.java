package com.instano.retailer.instano.utilities.models;

import android.support.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.instano.retailer.instano.application.ServicesSingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;

/**
 * Represents a single quote request (that is received by the seller)
 */
@JsonRootName("quote")
public class Quote {
    public final static double INVALID_COORDINATE = -1000; // an invalid coordinate

    @JsonProperty("id")
    public int id;
    @JsonProperty("buyer_id")
    public int buyerId;
    @JsonProperty("search_string")
    public String searchString;

    /**
     * human readable display for price
     * can be null
     */
    @JsonProperty("price_range")
    public String priceRange;
//    @JsonProperty("product_category")
//    public Category productCategory;
    @JsonProperty("brands")
    public String brands;
    @JsonProperty("updated_at")
    public long updatedAt; // valid only when constructed from Quote(JSONObject jsonObject)
    @JsonProperty("seller_ids")
    public HashSet<Integer> sellerIds;
    @Nullable
    @JsonProperty("address")
    public String address; // newline separated
    @JsonProperty("latitude")
    public double latitude;
    @JsonProperty("longitude")
    public double longitude;

    public static int getIdFrom (JSONObject quoteJsonObject) {
        try {
            return quoteJsonObject.getInt("id");
        } catch (JSONException e) {
            return -1;
        }
    }

    @JsonProperty("updated_at")
    public void setUpdatedAt(String updatedAtString) {
        this.updatedAt = ServicesSingleton.dateFromString(updatedAtString);
    }

    public Quote () {
        // Dummy constructor for Json Jackson
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
}
