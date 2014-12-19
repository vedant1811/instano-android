package com.instano.retailer.instano.utilities.models;

import com.instano.retailer.instano.application.ServicesSingleton;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a single Quotation uniquely identifiable by its @field id
 */
public class Quotation {

    private final static String STATUS_UNREAD  = "unread";
    private final static String STATUS_READ  = "read";

    public final int id; // server generated
    public final String nameOfProduct; // TODO: in future probably make a generic `Product` class
    public final int price;
    public final String description;
    public final int sellerId;
    public final int quoteId; // the id of the quote being replied to
    public final long createdAt;
//        public final URL imageUrl; // can be null

    /* modifiable fields */
    private String mStatus;

    public boolean isRead() {
        if (STATUS_READ.equals(mStatus))
            return true;
        else
            return false;
    }

    public void setStatusRead() {
        mStatus = STATUS_READ;
    }

    public Quotation(JSONObject quotationJsonObject) throws JSONException {
        id = quotationJsonObject.getInt("id");
        nameOfProduct = quotationJsonObject.getString("name_of_product");
        price = quotationJsonObject.getInt("price");
        String description = quotationJsonObject.getString("description");
        if (description.equalsIgnoreCase("null"))
            this.description = "";
        else
            this.description = description;
        sellerId = quotationJsonObject.getInt("seller_id");
        quoteId = quotationJsonObject.getInt("quote_id");
        String updatedAt = quotationJsonObject.getString("created_at");
        this.createdAt = ServicesSingleton.dateFromString(updatedAt);

        mStatus = quotationJsonObject.getString("status");
    }

    public String toChatString() {
        return nameOfProduct + "\n₹ " + price + "\n" + description;
    }

    /**
     *
     * @return Human readable time elapsed. Eg: "42 minutes ago"
     */
    public String getPrettyTimeElapsed() {
        return ServicesSingleton.getInstance(null).getPrettyTimeElapsed(createdAt);
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
