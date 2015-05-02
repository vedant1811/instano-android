package com.instano.retailer.instano.utilities.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * Created by Rohit on 2/5/15.
 */
@JsonRootName("facebook_user")
public class FacebookUser {

    @JsonProperty("id")
    private String id;
    @JsonProperty("email")
    private String email;
    @JsonProperty("name")
    private String name;
}

//state={"id":"777235068998158",
//        "first_name":"Rohit",
//        "timezone":5.5,
//        "email":"rohit7roy@gmail.com",
//        "verified":true,
//        "name":"Rohit Roy",
//        "locale":"en_US",
//        "link":"https:\/\/www.facebook.com\/app_scoped_user_id\/777235068998158\/",
//        "last_name":"Roy","gender":"male",
//        "updated_time":"2014-11-22T22:03:10+0000"}},
//        error: null,
//        isFromCache:false}