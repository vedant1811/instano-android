package com.instano.retailer.instano.utilities.models;

import com.fasterxml.jackson.annotation.JsonRootName;

import org.json.JSONObject;

/**
 * Created by vedant on 18/12/14.
 */

@JsonRootName(value = "buyer")
public class Buyer {

    private Integer id ;
    private String name;
    private String phone;
   // private JSONObject response;

    public Buyer(JSONObject response) {

    }

    public Buyer() {

    }

    public Integer getId() {

        return id;
    }

    public void setId(Integer id) {

        if(this.id != -1)
        {
            this.id = id;
        }
        else
            this.id = -1;

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

    public JSONObject getResponse(JSONObject response) {
        return response;
    }

   /* public void setResponse(JSONObject response) {
        this.response = response;
    }*/
}
