package com.instano.retailer.instano.utilities.models;

import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * Created by vedant on 18/12/14.
 */

@JsonRootName(value = "buyer")
public class Buyer {

    private Integer id ;
    private String name;
    private String phone;
    private String api_key;
    private String created_at;
    private String updated_at;
    private String facebook_id;



    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
       this.id=id;
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

    public String getApi_key() {
        return api_key;
    }

    public void setApi_key(String api_key) {
        this.api_key = api_key;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getFacebook_id() {
        return facebook_id;
    }

    public void setFacebook_id(String facebook_id) {
        this.facebook_id = facebook_id;
    }
}
