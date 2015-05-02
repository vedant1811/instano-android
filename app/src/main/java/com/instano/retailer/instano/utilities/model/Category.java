package com.instano.retailer.instano.utilities.model;

import android.support.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.instano.retailer.instano.utilities.library.Log;

import java.util.ArrayList;

/**
* Created by vedant on 4/7/15.
*/
public class Category implements Comparable<Category> {

    public static final String UNDEFINED = "Select Category";

    private static final String TAG = "ProductCategories";
    @JsonProperty("category")
    public String name;
    @JsonProperty("brands")
    public ArrayList<String> brands;

    private boolean[] selected;
    private boolean mUserSelected = false;

    public boolean matches(String lowerCaseString) {
        return lowerCaseString.contains(name.toLowerCase());
    }

    public void guessBrands(String searchString) {
        if (!mUserSelected) {
            boolean[] selectedBrands = null;
            boolean anythingSelected = false;
            if (searchString != null) {
                String lowerCaseSearch = searchString.toLowerCase();
                // so guess brands:
                selectedBrands = new boolean[brands.size()];
                anythingSelected = false;
                for (int i = 0; i < selectedBrands.length; i++) {
                    String brand = brands.get(i);
                    selectedBrands[i] = lowerCaseSearch.contains(brand.toLowerCase());
                    anythingSelected |= selectedBrands[i]; // becomes true if something is selected in any iteration
                }
            }
            if (anythingSelected)
                setSelected(selectedBrands, false);
            else
                setSelected(null, false);
            // in case no brands are guessed, selectedCategory.getSelected() is set to null (i.e. all is selected)
        }
    }

    public Category() {
    }

    public String asAdditionalInfo() {
        StringBuilder builder = new StringBuilder();
        builder.append("category: ").append(name).append("\n");
        if (isAllSelected())
            builder.append("any brand");
        else {
            builder.append("brands: ");
            for (int i = 0; i < selected.length; i++) {
                if (selected[i]) {
                    builder.append(brands.get(i));
                    if (i < selected.length - 1)
                        builder.append(", ");
                }
            }
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        Category category = (Category) o;

        if (name != null ? !name.equals(category.name) : category.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    public static Category undefinedCategory() {
        Category category = new Category();
        category.name = UNDEFINED;
        category.brands = new ArrayList<String>();
        category.brands.add("brands");
        return category;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(@NonNull Category another) {
        return name.compareTo(another.name);
    }

    /**
     * represents the selected brands. If it is null, the category itself is not selected.
     * In case the Product Categories is part of a seller, this is always null
     */
    public boolean[] getSelected() {
        return selected;
    }

    public boolean isAllSelected() {
        if (selected == null)
            return true;
        for (boolean b : selected)
            if (!b)
                return false;
        return true;
    }

    /**
     * also sets user selected to true
     * @param selected
     */
    public void setSelected(boolean[] selected, boolean byUser) {
        Log.d(getClass().getSimpleName(), String.format(
                "Setting %s to %b, null:%b", name, byUser, selected==null));
        mUserSelected = byUser;
        this.selected = selected;
    }
}
