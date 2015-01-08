package com.instano.retailer.instano.utilities.models;

import com.instano.retailer.instano.application.ServicesSingleton;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by vedant on 7/1/15.
 */
public class Deal {
    public final int id;
    public final String heading;
    public final String subheading;
    public final long updatedAt;
    public final long expiresAt;
    public final int sellerId;

    public Deal(int id, String heading, String subheading, long updatedAt, long expiresAt, int sellerId) {
        this.id = id;
        this.heading = heading;
        this.subheading = subheading;
        this.updatedAt = updatedAt;
        this.expiresAt = expiresAt;
        this.sellerId = sellerId;
    }

    public Deal(JSONObject json) throws JSONException {
        id = json.getInt("id");
        heading = json.getString("heading");
        if (!json.isNull("subheading"))
            subheading = json.getString("subheading");
        else
            subheading = "";
        String updatedAt = json.getString("updated_at");
        this.updatedAt = ServicesSingleton.dateFromString(updatedAt);
        String expiresAt = json.getString("expires_at");
        this.expiresAt = ServicesSingleton.dateFromString(expiresAt);
        this.sellerId = json.getInt("seller_id");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Deal deal = (Deal) o;

        if (id != deal.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
