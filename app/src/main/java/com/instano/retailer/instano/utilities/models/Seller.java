package com.instano.retailer.instano.utilities.models;

import android.support.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single immutable Seller
 */
public class Seller {
    public final static double INVALID_COORDINATE = -1000; // an invalid coordinate
    public static final String UNDEFINED = "Select Category";
    private static final String TAG = "Seller";

    @JsonProperty("id")
    public int id; // server generated

    @JsonProperty("name_of_shop")
    public String name_of_shop;

    @JsonProperty("image")
    public String image;

    @JsonProperty("outlets")
    public List<Outlet> outlets;

    @JsonProperty("brands")
    public ArrayList<Brand> brands;

    public Seller() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Seller seller = (Seller) o;

        if (id != seller.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public boolean containsCategory(@NonNull String categoryToMatch) {
        if (categoryToMatch.equals(UNDEFINED))
            return true;

        for (Brand brand : brands)
            if (brand.category.equals(categoryToMatch))
                return true;
        return false;
    }

    public boolean contains(String category) {
        if (Category.UNDEFINED.equals(category))
            return true;
        if (category != null) {
            for (Brand brand : brands) {
                if (category.equals(brand.category)) {
                    return true;
                }
            }
        }
        return false;
    }
}
