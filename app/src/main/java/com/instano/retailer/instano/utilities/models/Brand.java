package com.instano.retailer.instano.utilities.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Rohit on 14/4/15.
 */
//@JsonRootName("brands")
public class Brand {
    public static final String ANY = "any";

    @JsonProperty("name")
    public String name = ANY;
    @JsonProperty("category")
    public String category = ANY;

    public Brand(){

    }
}
