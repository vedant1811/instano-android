package com.instano.retailer.instano.utilities.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * Created by vedant on 4/7/15.
 */
@JsonRootName("Constraint")
public class Constraint {
    @JsonProperty
    public Integer min_distance;
    @JsonProperty
    public String category;

    @Override
    public String toString() {
        return "Constraint{" +
                "min_distance=" + min_distance +
                ", category=" + category +
                '}';
    }
}
