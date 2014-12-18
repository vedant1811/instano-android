package com.instano.retailer.instano.utilities.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by vedant on 18/12/14.
 */
public class Buyer {
    public final int id;
    public final String name;
    public final String phone;

    public Buyer(String name, String phone) {
        id = -1;
        this.name = name;
        this.phone = phone;
    }

    public Buyer(JSONObject jsonObject) throws JSONException {
        id = jsonObject.getInt("id");
        name = jsonObject.getString("name");
        phone = jsonObject.getString("phone");
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject data = new JSONObject()
                .put("name", name)
                .put("phone", phone);
        if (id != -1)
            data.put("id", id);
        JSONObject buyer = new JSONObject()
                .put("buyer", data);
        return buyer;
    }
}
