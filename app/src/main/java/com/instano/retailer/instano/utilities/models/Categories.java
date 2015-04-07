package com.instano.retailer.instano.utilities.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

/**
 * Created by vedant on 8/10/14.
 */
public class Categories {
    @JsonProperty("categories")
    public ArrayList<Category> mCategories;
}
