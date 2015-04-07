package com.instano.retailer.instano.utilities.models;

import android.support.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by vedant on 8/10/14.
 */
public class Categories {
    public static final String UNDEFINED = "Select Category";
    private static final String TAG = "ProductCategories";

    @JsonProperty("categories")
    public ArrayList<Category> mCategories;

    @NonNull
    public List<Category> getProductCategories() {
        return Collections.unmodifiableList(mCategories);
    }
    public void clearSelected() {
        for (Category category : mCategories)
            category.setSelected(null, false);
    }

    public Categories() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Categories that = (Categories) o;

        if (!mCategories.equals(that.mCategories)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return mCategories.hashCode();
    }


    public boolean contains(String categoryName) {

        if (categoryName.equals(UNDEFINED))
            return true; // undefined is contained in every category

        // TODO: make it a check via hash
        for (Category category : mCategories)
            if (category.name.equals(categoryName))
                return true;
        return false;
    }

    public boolean containsCategoryAndOneBrand(Category categoryToMatch) {

        if (categoryToMatch.name.equals(UNDEFINED))
            return true;

        for (Category oneCategory : mCategories)
            if (oneCategory.name.equals(categoryToMatch.name)) { // category is matched

                // if no brands specified either category, then consider it matched
                if (categoryToMatch.brands.isEmpty() || oneCategory.brands.isEmpty())
                    return true;

                // true if atleast one brand is common
                return !Collections.disjoint(oneCategory.brands, categoryToMatch.brands);
            }
        return false;
    }
}
