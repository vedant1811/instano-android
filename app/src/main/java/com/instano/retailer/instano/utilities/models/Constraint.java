package com.instano.retailer.instano.utilities.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by vedant on 4/7/15.
 */
public class Constraint {
    @JsonProperty
    public Integer min_distance;
    @JsonProperty
    public Category category;
}
