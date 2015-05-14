package com.instano.retailer.instano.utilities.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * Created by vedant on 18/12/14.
 */

@JsonRootName(value = "buyer")
public class Buyer {

    @JsonProperty("facebook_user_attributes")
    private FacebookUser facebookUser;

    public FacebookUser getFacebookUser() {
        return facebookUser;
    }

    public void setFacebookUser(FacebookUser facebookUser) {
        this.facebookUser = facebookUser;
    }


}
