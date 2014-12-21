package com.instano.retailer.instano.utilities.models;

import android.support.annotation.NonNull;

import com.instano.retailer.instano.utilities.library.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by vedant on 8/10/14.
 */
public class ProductCategories {

    public static final String UNDEFINED = "Select Category";

    private static final String TAG = "ProductCategories";

    @NonNull
    public List<Category> getProductCategories() {
        return Collections.unmodifiableList(mCategories);
    }

    private ArrayList<Category> mCategories;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProductCategories that = (ProductCategories) o;

        if (!mCategories.equals(that.mCategories)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return mCategories.hashCode();
    }

    public ProductCategories(JSONObject json, boolean allowUndefined) {
        mCategories = new ArrayList<Category>();
        try {
            JSONArray categories = json.getJSONArray("categories");
            for (int i = 0; i < categories.length(); i++) {
                mCategories.add(new Category(
                        categories.getJSONObject(i)
                ));
            }
        } catch (JSONException e) {
            Log.e(TAG, "", e);
        }
        Collections.sort(mCategories);
        if (allowUndefined)
            mCategories.add(0, Category.undefinedCategory()); // insert at start
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

    public boolean containsCategoryAndOneBrand(ProductCategories.Category category) {

        if (category.name.equals(UNDEFINED))
            return true;

        for (Category c : mCategories)
            if (c.name.equals(category.name)) {
                return !Collections.disjoint(c.brands, category.brands); // true if atleast one brand is common
            }
        return false;
    }

    public static class Category implements Comparable<Category> {
        public final String name;
        public final ArrayList<String> brands;
        private boolean[] selected;

        private final ArrayList<String> nameVariants;

        public boolean matches(String lowerCaseString) {
            for (String variant : nameVariants)
                if(lowerCaseString.contains(variant))
                    return true;
            return false;
        }

        public Category(JSONObject params) {
            selected = null;
            brands = new ArrayList<String>();
            String name = null;
            try {
                name = params.getString("category");
                JSONArray brandsJsonArray = params.getJSONArray("brands");
                for (int i = 0; i < brandsJsonArray.length(); i++) {
                    brands.add(brandsJsonArray.getString(i));
                }
            } catch (JSONException e){
                Log.e(TAG, "", e);
            }
            this.name = name;
            Collections.sort(brands);
            nameVariants = new ArrayList<String>();
            nameVariants.add(name.toLowerCase());
        }

        public JSONObject toJsonObject() {
            try {
                JSONArray jsonArray = new JSONArray();
                if (selected != null) {
                    for (int i = 0; i < selected.length; i++) {
                        if (selected[i])
                            jsonArray.put(brands.get(i));
                    }
                } // TODO: else when selected is null
                JSONObject jsonObject = new JSONObject()
                        .put("category", name)
                        .put("brands", jsonArray);
                return jsonObject;
            } catch (JSONException e) {
                Log.e(TAG, "", e);
                return null;
            }
        }

        public String asAdditionalInfo() {
            StringBuilder builder = new StringBuilder();
            builder.append("category: ").append(name).append("\n");
            if (isAllSelected())
                builder.append("any brand");
            else {
                builder.append("brands: ");
                for (int i = 0; i < selected.length; i++) {
                    if (selected[i])
                        builder.append(brands.get(i));
                    if (i < selected.length - 1)
                        builder.append(", ");
                }
            }
            return builder.toString();
        }

        /**
         * dummy category
         */
        private Category() {
            this.name = UNDEFINED;
            brands = new ArrayList<String>();
            brands.add("brands");
            nameVariants = new ArrayList<String>();
            nameVariants.add(name.toLowerCase());
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
            return new Category();
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

        public void setSelected(boolean[] selected) {
            this.selected = selected;
        }
    }
}
