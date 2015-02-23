package com.instano.retailer.instano.utilities.models;

import com.fasterxml.jackson.annotation.JsonRootName;

import org.json.JSONObject;

/**
 * Created by vedant on 18/12/14.
 */

@JsonRootName(value = "buyer")
public class Buyer {

    private Integer id;
    private String name;
    private String phone;

    public Buyer(JSONObject response) {

    }

    public Buyer() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
