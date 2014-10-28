package com.instano.retailer.instano.utilities;

import com.instano.retailer.instano.ServicesSingleton;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a single immutable Quotation
 */
public class Quotation {
    public final int id; // server generated
    public final String nameOfProduct; // TODO: in future probably make a generic `Product` class
    public final int price;
    public final String description;
    public final int sellerId;
    public final int quoteId; // the id of the quote being replied to
    public final long updatedAt;
//        public final URL imageUrl; // can be null

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
        String updatedAt = quotationJsonObject.getString("updated_at");
        this.updatedAt = ServicesSingleton.dateFromString(updatedAt);
    }

    public String toChatString() {
        return nameOfProduct + "\n₹ " + price + "\n" + description;
    }

    public JSONObject toJsonObject() {
        try {
            JSONObject quotationParamsJsonObject = new JSONObject()
                    .put("name_of_product", nameOfProduct)
                    .put("price", price)
                    .put("description", description)
                    .put("seller_id", sellerId)
                    .put("quote_id", quoteId);

            if (id != -1)
                quotationParamsJsonObject.put ("id", id);

            JSONObject quotationJsonObject = new JSONObject()
                    .put("quotation", quotationParamsJsonObject);

            return quotationJsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     * @return Human readable time elapsed. Eg: "42 minutes ago"
     */
    public String getPrettyTimeElapsed() {
        return ServicesSingleton.getInstance(null).getPrettyTimeElapsed(updatedAt);
    }
}
