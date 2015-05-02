package com.instano.retailer.instano.utilities.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.instano.retailer.instano.application.ServicesSingleton;

import java.util.Date;

/**
 * Created by vedant on 7/1/15.
 */
public class Deal {
    @JsonProperty("id")
    public int id;
    @JsonProperty("heading")
    public String heading;
    @JsonProperty("subheading")
    public String subheading;
    @JsonProperty("updated_at")
    public Date updatedAt;
    @JsonProperty("expires_at")
    public Date expiresAt;
    @JsonProperty("seller_id")
    public int sellerId;
    @JsonProperty("product")
    public Product product;

    public String expiresAt() {
        return "expires " + ServicesSingleton.instance().getPrettyTimeElapsed(expiresAt);
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
