package com.instano.retailer.instano.utilities.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Rohit on 2/5/15.
 */
public class FacebookUser {

    @JsonProperty("user_id")
    private String id;

    @JsonProperty("email")
    private String email;

    @JsonProperty("name")
    private String name;

    @JsonProperty("gender")
    private String gender;

    @JsonProperty("verified")
    private String verified;

    @JsonProperty("user_updated_at")
    private String userUpdatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getVerified() {
        return verified;
    }

    public void setVerified(String verified) {
        this.verified = verified;
    }

    public String getUserUpdatedAt() {
        return userUpdatedAt;
    }

    public void setUserUpdatedAt(String userUpdatedAt) {
        this.userUpdatedAt = userUpdatedAt;
    }
}
// PostMan
//        "facebook_user_attributes": {
//        "user_id": "777235068998151",
//        "name": "Test",
//        "email": "test@test.com",
//        "verified": "true",
//        "gender": "male",
//        "user_updated_at": "04052015"
//        }

//Facebook Response
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