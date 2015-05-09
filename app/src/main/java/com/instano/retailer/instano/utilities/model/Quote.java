package com.instano.retailer.instano.utilities.model;

import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.instano.retailer.instano.application.ServicesSingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Represents a single quote request (that is received by the seller)
 */
@JsonRootName("quote")
public class Quote implements Comparable<Quote> {
    public final static double INVALID_COORDINATE = -1000; // an invalid coordinate
    private static final String TAG = "Quote";

    @JsonProperty("id")
    public int id;

    @JsonProperty("buyer_id")
    public int buyerId;

    @JsonProperty("updated_at")
    private Date updatedAt; // valid only when constructed from Quote(JSONObject jsonObject)

    @Nullable
    @JsonProperty("address")
    public String address; // newline separated

    @JsonProperty("latitude")
    public double latitude;

    @JsonProperty("longitude")
    public double longitude;

    @JsonProperty("product_id")
    public int productId;

    /**
     * fields address, latitude, longitude are added from ServicesSingleton if available
     * @param productId of this quote
     */
    public Quote(int productId) {
        this.productId = productId;
        Location location = ServicesSingleton.instance().getUserLocation();
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
        address = ServicesSingleton.instance().getUserAddress();
    }

    public static int getIdFrom (JSONObject quoteJsonObject) {
        try {
            return quoteJsonObject.getInt("id");
        } catch (JSONException e) {
            return -1;
        }
    }

//    @JsonProperty("updated_at")
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
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

    @Override
    public int compareTo(@NonNull Quote another) {
        return (int) (another.updatedAt.compareTo(updatedAt));
    }
}
