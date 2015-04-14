package com.instano.retailer.instano.utilities.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Rohit on 14/4/15.
 */
public class Brand {

    @JsonProperty("name")
    public String name;
    @JsonProperty("category")
    public String category;

    public Brand(){

    }
}
