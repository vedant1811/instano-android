package com.instano.retailer.instano.sellers;

import android.app.Activity;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.ServicesSingleton;
import com.instano.retailer.instano.application.network.NetworkRequestsManager;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.models.Category;
import com.instano.retailer.instano.utilities.models.Constraint;
import com.instano.retailer.instano.utilities.models.Seller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import rx.android.observables.AndroidObservable;

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

    public SellersArrayAdapter(Activity activity) {
        mContext = activity;

        mCompleteSet = new SparseArray<>();
        mFilteredList = new ArrayList<>();
        mDistanceAndCategoryFilter = new DistanceAndCategoryFilter();
        mCheckedItems = new SparseBooleanArray();

        AndroidObservable.bindActivity(activity, NetworkRequestsManager.instance().getObservable(Seller.class))
                .subscribe(seller -> {
                    mCompleteSet.put(seller.hashCode(), seller);
                    filter();
                });
    }

    public void setListener(ItemInteractionListener listener) {
        this.mItemInteractionListener = listener;
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

        shopNameTextView.setText(seller.name_of_shop);
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

    public void filter() {
        mDistanceAndCategoryFilter.runOldFilter();
    }

    public void filter(int minDist) {
        mDistanceAndCategoryFilter.filter(minDist);
    }

    public void filter(Category category) {
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

    /**
     * do not call filter directly, use the overloaded helper methods: filter(...)
     */
    private class DistanceAndCategoryFilter extends Filter {
        private static final int INITIAL_MIN_DIST = 1000;

        private Constraint mLastConstraint;
        private final ObjectMapper mObjectMapper;

        public DistanceAndCategoryFilter() {
            mObjectMapper = ServicesSingleton.instance().getDefaultObjectMapper();
            mLastConstraint = new Constraint();
            mLastConstraint.category = Category.undefinedCategory();
            mLastConstraint.min_distance = INITIAL_MIN_DIST;
        }

        private void runOldFilter() {
            Log.d("filter", "DistanceAndCategoryFilter: filtering by " + mLastConstraint);
            try {
                filter(mObjectMapper.writeValueAsString(mLastConstraint));
            } catch (JsonProcessingException e) {
                Log.fatalError(e);
            }
        }

        private void filter(int minDist) {
            Log.d("filter", "DistanceAndCategoryFilter: filtering by " + mLastConstraint);
            mLastConstraint.min_distance = minDist;
            runOldFilter();
        }

        private void filter(Category category) {
            Log.d("filter", "DistanceAndCategoryFilter: filtering by " + mLastConstraint);
            mLastConstraint.category = category;
        }

        @Override
        protected FilterResults performFiltering(CharSequence serializedConstraint) {
            ArrayList<Seller> filteredList = new ArrayList<>();

            Constraint constraint = new Constraint();
            try {
                constraint = mObjectMapper.readValue(serializedConstraint.toString(), Constraint.class);
            } catch (IOException e) {
                Log.fatalError(e);
            }

            try {
                for (int i = 0; i < mCompleteSet.size(); i++) {
                    Seller seller = mCompleteSet.valueAt(i);
                    if (seller.getDistanceFromLocation() <= constraint.min_distance && // first filter distance
                            seller.categories.containsCategoryAndOneBrand(constraint.category)) // filter category
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
    }
}
