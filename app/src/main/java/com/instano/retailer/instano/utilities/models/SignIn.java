package com.instano.retailer.instano.utilities.models;

import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * Created by vedant on 4/5/15.
 */

@JsonRootName(value = "sign_in")
public class SignIn {
    private String api_key;

    public String getApi_key() {
        return api_key;
    }

    public void setApi_key(String api_key) {
        this.api_key = api_key;
    }
}
