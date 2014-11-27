package com.instano.retailer.instano;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.instano.retailer.instano.utilities.ProductCategories;
import com.instano.retailer.instano.utilities.Seller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * TODO: do more
 * displays a list of sellers sorted by Seller.id
 * Created by vedant on 24/9/14.
 */
public class SellersArrayAdapter extends BaseAdapter {

    private static final String TAG = "sellers array adapter";

    private SparseArray<Seller> mCompleteSet;

    private ArrayList<Seller> mFilteredList;
    private DistanceFilter mDistanceFilter;
    private BrandsCategoryFilter mBrandsCategoryFilter;
    private SparseBooleanArray mCheckedItems;
    private Context mContext;

    private ItemInteractionListener mItemInteractionListener;
    private SellersListener mSellersListener;

    public SellersArrayAdapter(Context context) {
        mContext = context;

        mCompleteSet = new SparseArray<Seller>();
        mFilteredList = new ArrayList<Seller>();
        mDistanceFilter = new DistanceFilter();
        mBrandsCategoryFilter = new BrandsCategoryFilter();
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

    public ArrayList<Integer> getSelectedSellerIds() {
        ArrayList<Integer> ids = new ArrayList<Integer>();
        for (Seller seller : mFilteredList)
            if (mCheckedItems.get(seller.id))
                ids.add(seller.id);
        return ids;
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
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
        ImageButton callImageButton = (ImageButton) view.findViewById(R.id.callImageButton);

        final Seller seller = getItem(position);

        checkBox.setOnCheckedChangeListener (new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (mItemInteractionListener != null)
                    mItemInteractionListener.itemCheckedStateChanged(position, isChecked);
                mCheckedItems.put(seller.id, isChecked);
            }
        });
        // initially setting all to checked
        mCheckedItems.put(seller.id, true);
        if (mItemInteractionListener != null)
            mItemInteractionListener.itemCheckedStateChanged(position, true);

        shopNameTextView.setText(seller.nameOfShop);
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
        mDistanceFilter.runOldFilter();
        mBrandsCategoryFilter.runOldFilter();
    }

    public void filter(int minDist) {
        mDistanceFilter.filter(String.valueOf(minDist));
    }

    public void filter(ProductCategories.Category category) {
        mBrandsCategoryFilter.filter(category.toJsonObject().toString());
    }

    public interface ItemInteractionListener {
        /**
         *
         * @param pos position whose checkedState has changed, or -1 if listItems changed
         * @param checkedState
         */
        public void itemCheckedStateChanged(int pos, boolean checkedState);
        public void callButtonClicked(String number);
    }

    public interface SellersListener {

        public void sellerAdded(Seller seller);
    }

    private class DistanceFilter extends Filter {

        private CharSequence mLastConstraint = "100000";

        private void runOldFilter() {
            filter(mLastConstraint);
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            this.mLastConstraint = constraint;
            ArrayList<Seller> filteredList = new ArrayList<Seller>();

            try {
                int minDist = Integer.parseInt(String.valueOf(constraint));
                for (int i = 0; i < mCompleteSet.size(); i++) {
                    Seller seller = mCompleteSet.valueAt(i);
                    if (seller.getDistanceFromLocation() <= minDist)
                        filteredList.add(seller);
                }
            } catch (NumberFormatException e) {
                // add all in that case
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
            if (results.count == 0)
                notifyDataSetInvalidated();
            else
                notifyDataSetChanged();
            if (mItemInteractionListener != null)
                mItemInteractionListener.itemCheckedStateChanged(-1, false);
        }
    }

    private class BrandsCategoryFilter extends Filter {

        private CharSequence mLastConstraint = null;

        private void runOldFilter() {
            filter(mLastConstraint);
        }

        /**
         * <p>Invoked in a worker thread to filter the data according to the
         * constraint. Subclasses must implement this method to perform the
         * filtering operation. Results computed by the filtering operation
         * must be returned as a {@link android.widget.Filter.FilterResults} that
         * will then be published in the UI thread through
         * {@link #publishResults(CharSequence,
         * android.widget.Filter.FilterResults)}.</p>
         * <p/>
         * <p><strong>Contract:</strong> When the constraint is null, the original
         * data must be restored.</p>
         *
         * @param constraint the constraint used to filter the data
         * @return the results of the filtering operation
         * @see #filter(CharSequence, android.widget.Filter.FilterListener)
         * @see #publishResults(CharSequence, android.widget.Filter.FilterResults)
         * @see android.widget.Filter.FilterResults
         */
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            this.mLastConstraint = constraint;
            ArrayList<Seller> filteredList = new ArrayList<Seller>();


            try {
                ProductCategories.Category category = new ProductCategories.Category(
                        new JSONObject(String.valueOf(constraint)));
                for (int i = 0; i < mCompleteSet.size(); i++) {
                    Seller seller = mCompleteSet.valueAt(i);
                    if (seller.productCategories.containsCategoryAndOneBrand(category))
                        filteredList.add(seller);
                }
            } catch (JSONException e) {
                // add all in that case
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

        /**
         * <p>Invoked in the UI thread to publish the filtering results in the
         * user interface. Subclasses must implement this method to display the
         * results computed in {@link #performFiltering}.</p>
         *
         * @param constraint the constraint used to filter the data
         * @param results    the results of the filtering operation
         * @see #filter(CharSequence, android.widget.Filter.FilterListener)
         * @see #performFiltering(CharSequence)
         * @see android.widget.Filter.FilterResults
         */
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFilteredList = (ArrayList<Seller>) results.values;
            if (results.count == 0)
                notifyDataSetInvalidated();
            else
                notifyDataSetChanged();
            if (mItemInteractionListener != null)
                mItemInteractionListener.itemCheckedStateChanged(-1, false);
        }
    }
}
