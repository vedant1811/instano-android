package com.instano.retailer.instano.utilities.library.old;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.models.ProductCategories;
import com.instano.retailer.instano.utilities.models.Seller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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
    private SellersListener mSellersListener;

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

    public void setListener(SellersListener sellersListener) {
        mSellersListener = sellersListener;
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
                Log.d("mCheckedItems", String.format("getSelectedSellerIds %s (%d,%b)",seller.name_of_shop,seller.hashCode(),true));
            }
        double timeTaken = (System.nanoTime() - start)/1000;
        Log.d("Timing", "updateSelectedSellers took " + timeTaken + "μs");
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
        CheckBox checkBox = null;
        ImageButton callImageButton = (ImageButton) view.findViewById(R.id.callImageButton);

        final Seller seller = getItem(position);

        boolean checked = mCheckedItems.get(seller.hashCode(), true);
        checkBox.setChecked(checked);
        Log.d("mCheckedItems", String.format("initialized %s (%d,%b)", seller.name_of_shop, seller.hashCode(), checked));

        checkBox.setOnCheckedChangeListener (new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCheckedItems.put(seller.hashCode(), isChecked);
                mSelectedSellerIDs = null;
                if (mItemInteractionListener != null)
                    mItemInteractionListener.itemCheckedStateChanged(getSelectedSellerIds().size());
                Log.d("mCheckedItems", String.format("changed %s (%d,%b)",seller.name_of_shop,seller.hashCode(),isChecked));
            }
        });

        shopNameTextView.setText(seller.name_of_shop);
        addressTextView.setText(seller.address);
        String distanceFromLocation = seller.getPrettyDistanceFromLocation();
        if (distanceFromLocation != null)
            distanceTextView.setText(distanceFromLocation);
        else
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

    // TODO: equality of sellers is checked based on seller ID (i.e.seller.hashCode() ). In case a seller has updated details
    // the seller will be skipped. Fix this.
    /**
     * Add a seller to this adapter if it does not already exist
     * @param seller to be added
     * @return true if the seller was successfully added, false if it already existed
     */
    private boolean add(Seller seller) {
        int hash = seller.hashCode();
        if (mCompleteSet.get(hash) == null) {
            mCompleteSet.put(hash, seller);
            if (mSellersListener != null)
                mSellersListener.sellerAdded(seller);
            // initially setting all sellers to checked
            mCheckedItems.put(hash, true);
            Log.d("mCheckedItems", String.format("added %s (%d,%b)",seller.name_of_shop,hash,true));
            return true;
        } else
            return false;

    }

    public boolean addAll(JSONArray sellersJsonArray) throws JSONException {

        boolean newAdded = false;

        for (int i = 0; i < sellersJsonArray.length(); i++){
            JSONObject sellerJsonObject = sellersJsonArray.getJSONObject(i);
            try {
                newAdded |= add(new Seller(sellerJsonObject));
            } catch (JSONException e) {
                Log.d(TAG, "", e);
            }
        }

        Log.d(TAG, "newAdded = " + newAdded);

        if (newAdded) {
            filter();
        }
        return newAdded;
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
        if (mItemInteractionListener != null)
            mItemInteractionListener.itemCheckedStateChanged(getSelectedSellerIds().size());
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
        /**
         *
         * @param selected number of selected items
         */
        public void itemCheckedStateChanged(int selected);
        public void callButtonClicked(String number);
    }

    public interface SellersListener {

        public void sellerAdded(Seller seller);
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
                Log.fatalError(e);
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
                Log.fatalError(e);
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
                Log.fatalError(e);
                constraint = null;
            }
            return constraint;
        }

    }
}
