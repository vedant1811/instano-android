package com.instano.retailer.instano.utilities.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.instano.retailer.instano.application.ServicesSingleton;

import java.util.Date;

/**
 * Represents a single Quotation uniquely identifiable by its @field id
 */
@JsonRootName("quotation")
public class Quotation {

    private final static String STATUS_UNREAD  = "unread";
    private final static String STATUS_READ  = "read";

    @JsonProperty("id")
    public int id; // server generated
    @JsonProperty("name_of_product")
    public String nameOfProduct; // TODO: in future probably make a generic `Product` class
    @JsonProperty("price")
    public int price;
    @JsonProperty("description")
    public String description;
    @JsonProperty("seller_id")
    public int sellerId;
    @JsonProperty("quote_id")
    public int quoteId; // the id of the quote being replied to
    @JsonIgnore
    private Date updatedAt;
//        public final URL imageUrl; // can be null

    /* modifiable fields */
    @JsonProperty("status")
    private String mStatus;

    @JsonProperty("updated_at")
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Quotation() {

    }

    public boolean isRead() {
        if (STATUS_READ.equals(mStatus))
            return true;
        else
            return false;
    }

    public void setStatusRead() {
        mStatus = STATUS_READ;
    }

    public String toChatString() {
        return nameOfProduct + "\n₹ " + price + "\n" + description;
    }

    /**
     *
     * @return Human readable time elapsed. Eg: "42 minutes ago"
     */
    public String getPrettyTimeElapsed() {
        return ServicesSingleton.instance().getPrettyTimeElapsed(updatedAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Quotation quotation = (Quotation) o;

        if (id != quotation.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
