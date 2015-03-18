package com.instano.retailer.instano.sellers;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.models.ProductCategories;
import com.instano.retailer.instano.utilities.models.Seller;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * TODO: do more
 * displays a list of sellers sorted by Seller.id
 * Created by vedant on 24/9/14.
 */
public class SellersArrayAdapter extends BaseAdapter implements Filterable {

    private static final String TAG = "sellers array adapter";

    private SparseArray<Seller> mCompleteSet;

    private ArrayList<Seller> mFilteredList;
    private SparseBooleanArray mCheckedItems;
    private HashSet<Integer> mSelectedSellerIDs;
    private DistanceAndCategoryFilter mDistanceAndCategoryFilter;
    private Context mContext;

    private ItemInteractionListener mItemInteractionListener;

    public SellersArrayAdapter(Context context) {
        mContext = context;

        mCompleteSet = new SparseArray<Seller>();
        mFilteredList = new ArrayList<Seller>();
        mDistanceAndCategoryFilter = new DistanceAndCategoryFilter();
        mCheckedItems = new SparseBooleanArray();
    }

    public void setListener(ItemInteractionListener listener) {
        this.mItemInteractionListener = listener;
    }

    public SparseArray<Seller> getAllSellers() {
        return mCompleteSet;
    }

    public ArrayList<Seller> getFilteredSellers() {
        return mFilteredList;
    }

    public HashSet<Integer> getSelectedSellerIds() {
        if (mSelectedSellerIDs == null)
            updateSelectedSellers();
        return mSelectedSellerIDs;
    }

    private void updateSelectedSellers() {
        long start = System.nanoTime();
        mSelectedSellerIDs = new HashSet<Integer>();
        for (Seller seller : mFilteredList)
            if (mCheckedItems.get(seller.hashCode())) {
                mSelectedSellerIDs.add(seller.hashCode());
                Log.d("mCheckedItems", String.format("getSelectedSellerIds %s (%d,%b)",seller.nameOfShop,seller.hashCode(),true));
            }
        double timeTaken = (System.nanoTime() - start)/1000;
        Log.v(Log.TIMER_TAG, "updateSelectedSellers took " + timeTaken + "μs");
    }

    @Override
    public int getCount() {
        return mFilteredList.size();
    }

    @Override
    public Seller getItem (int index) {
        return mFilteredList.get(index);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final View view;

        // first check to see if the view is null. if so, we have to inflate it.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_shop, parent, false);
        }
        else
            view = convertView;

        TextView shopNameTextView = (TextView) view.findViewById(R.id.shopNameTextView);
        TextView addressTextView = (TextView) view.findViewById(R.id.addressTextView);
        TextView distanceTextView = (TextView) view.findViewById(R.id.distanceTextView);
        ImageButton callImageButton = (ImageButton) view.findViewById(R.id.callImageButton);

        final Seller seller = getItem(position);

        shopNameTextView.setText(seller.nameOfShop);
        addressTextView.setText(seller.address);
        String distanceFromLocation = seller.getPrettyDistanceFromLocation();
        if (distanceFromLocation != null) {
            distanceTextView.setVisibility(View.VISIBLE);
            distanceTextView.setText(distanceFromLocation);
        } else
            distanceTextView.setVisibility(View.INVISIBLE);

        callImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemInteractionListener != null)
                    mItemInteractionListener.callButtonClicked(seller.phone);
            }
        });

        return view;
    }

    /**
     * returns a seller from this adpater
     * @param sellerId of the seller to be returned
     * @return the seller, or null if no such seller exists
     */
    @Nullable
    public Seller getSeller (int sellerId) {
        return mCompleteSet.get(sellerId);
    }

    @Override
    public long getItemId (int pos){
        return getItem(pos).hashCode();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public void addAll(Collection<Seller> collection) {
        mCompleteSet.clear();
        for (Seller seller : collection){
            mCompleteSet.put(seller.id, seller);
        }
        filter();
    }

    public void filter() {
        mDistanceAndCategoryFilter.runOldFilter();
    }

    public void filter(int minDist) {
        mDistanceAndCategoryFilter.filter(minDist);
    }

    public void filter(ProductCategories.Category category) {
        mDistanceAndCategoryFilter.filter(category);
    }

    private void newData() {
        mSelectedSellerIDs = null;
        if (mFilteredList.size() == 0)
            notifyDataSetInvalidated();
        else
            notifyDataSetChanged();
    }

    /**
     * <p>Returns a filter that can be used to constrain data with a filtering
     * pattern.</p>
     * <p/>
     * <p>This method is usually implemented by {@link android.widget.Adapter}
     * classes.</p>
     *
     * @return a filter used to constrain data
     */
    @Override
    public Filter getFilter() {
        return mDistanceAndCategoryFilter;
    }

    public interface ItemInteractionListener {
        public void callButtonClicked(String number);
    }

    private class DistanceAndCategoryFilter extends Filter {

        private static final String PRODUCT_CATEGORY = "product_category";
        private static final String MIN_DIST = "min_dist";
        private static final int INITIAL_MIN_DIST = 1000;

        private CharSequence mLastConstraint =
                getConstraint(INITIAL_MIN_DIST, ProductCategories.Category.undefinedCategory());

        private void runOldFilter() {
            filter(mLastConstraint);
            Log.d("filter", "DistanceAndCategoryFilter: filtering by " + mLastConstraint);
        }

        private void filter(int minDist) {
            try {
                mLastConstraint = (new JSONObject(mLastConstraint.toString())
                        .put(MIN_DIST, minDist)).toString();
            } catch (JSONException e) {
                e.printStackTrace();
                mLastConstraint = getConstraint(minDist, ProductCategories.Category.undefinedCategory());
            }
            filter(mLastConstraint);
            Log.d("filter", "DistanceAndCategoryFilter: filtering by " + mLastConstraint);
        }

        private void filter(ProductCategories.Category category) {
            try {
                mLastConstraint = (new JSONObject(mLastConstraint.toString())
                        .put(PRODUCT_CATEGORY, category.toJsonObject())).toString();
            } catch (JSONException e) {
                e.printStackTrace();
                mLastConstraint = getConstraint(INITIAL_MIN_DIST, category);
            }
            filter(mLastConstraint);
            Log.d("filter", "DistanceAndCategoryFilter: filtering by " + mLastConstraint);
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            this.mLastConstraint = constraint;
            ArrayList<Seller> filteredList = new ArrayList<Seller>();

            try {
                // first filter distance
                int minDist = getMinDist(constraint);
                // filter category
                ProductCategories.Category category = getProductCategory(constraint);
                for (int i = 0; i < mCompleteSet.size(); i++) {
                    Seller seller = mCompleteSet.valueAt(i);
                    if (seller.getDistanceFromLocation() <= minDist &&
                            seller.productCategories.containsCategoryAndOneBrand(category))
                        filteredList.add(seller);
                }
            } catch (Exception e) {
                // add all in case of an Exception (NumberFormatException, JSONException, etc.)
                Log.e("filter", "DistanceAndCategoryFilter: filtering by " + mLastConstraint, e);
                for (int i = 0; i < mCompleteSet.size(); i++) {
                    Seller seller = mCompleteSet.valueAt(i);
                    filteredList.add(seller);
                }
            }

            Collections.sort(filteredList, new Seller.DistanceComparator());

            FilterResults filterResults = new FilterResults();
            filterResults.count = filteredList.size();
            filterResults.values = filteredList;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFilteredList = (ArrayList<Seller>) results.values;
            newData();
        }

        private ProductCategories.Category getProductCategory(CharSequence constraint) throws JSONException {
            JSONObject jsonObject = new JSONObject(constraint.toString());
            return new ProductCategories.Category(jsonObject.getJSONObject(PRODUCT_CATEGORY));
        }

        private int getMinDist(CharSequence constraint) throws JSONException {
            JSONObject jsonObject = new JSONObject(constraint.toString());
            return jsonObject.getInt(MIN_DIST);
        }

        private CharSequence getConstraint(int minDist, ProductCategories.Category category) {
            String constraint;
            try {
                JSONObject jsonObject = new JSONObject()
                        .put(MIN_DIST, minDist)
                        .put(PRODUCT_CATEGORY, category.toJsonObject());
                constraint = jsonObject.toString();
            } catch (JSONException e) {
                e.printStackTrace();
                constraint = null;
            }
            return constraint;
        }

    }
}
