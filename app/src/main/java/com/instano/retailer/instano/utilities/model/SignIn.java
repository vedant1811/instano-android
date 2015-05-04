package com.instano.retailer.instano.utilities.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * Created by vedant on 4/5/15.
 */
@JsonRootName("sign_in")
public class SignIn {
    @JsonProperty("user_id")
    private String facebookUserId;

    public String getFacebookUserId() {
        return facebookUserId;
    }

    public void setFacebookUserId(String facebookUserId) {
        this.facebookUserId = facebookUserId;
    }
}
