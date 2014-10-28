package com.instano.retailer.instano.utilities;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by vedant on 8/10/14.
 */
public class ProductCategories {

    public static final String UNDEFINED = "Undefined";

    private static final String TAG = "ProductCategories";

    public ArrayList<Category> getProductCategories() {
        return mCategories;
    }

    private ArrayList<Category> mCategories;

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
        for (Category category : mCategories )
            if (category.name.equals(categoryName))
                return true;
        return false;
    }

    public static class Category implements Comparable<Category> {
        public String name;
        public ArrayList<String> brands;
        /**
         * represents the selected brands. If it is null, the category itself is not selected.
         * In case the Product Categories is part of a seller, this is always null
         */
        public boolean[] selected;

        Category(JSONObject params) {
            selected = null;
            brands = new ArrayList<String>();
            try {
                name = params.getString("category");
                JSONArray brandsJsonArray = params.getJSONArray("brands");
                for (int i = 0; i < brandsJsonArray.length(); i++) {
                    brands.add(brandsJsonArray.getString(i));
                }
            } catch (JSONException e){
                Log.e(TAG, "", e);
            }
            Collections.sort(brands);
        }

        private Category() {

        }

        private static Category undefinedCategory() {
            Category category = new Category();
            category.name = UNDEFINED;
            return category;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public int compareTo(Category another) {
            return name.compareTo(another.name);
        }
    }
}
