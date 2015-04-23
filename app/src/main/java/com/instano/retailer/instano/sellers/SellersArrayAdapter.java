package com.instano.retailer.instano.sellers;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.SparseArray;
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
import com.instano.retailer.instano.application.network.NetworkRequestsManager;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.models.Category;
import com.instano.retailer.instano.utilities.models.Constraint;
import com.instano.retailer.instano.utilities.models.Seller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.subjects.BehaviorSubject;

/**
 * TODO: do more
 * displays a list of sellers sorted by Seller.id
 * Created by vedant on 24/9/14.
 */
public class SellersArrayAdapter extends BaseAdapter implements Filterable {

    private static final String TAG = "sellers array adapter";

    private SparseArray<Seller> mCompleteSet;

    private ArrayList<Seller> mFilteredList;
//    private SparseBooleanArray mCheckedItems;
//    private HashSet<Integer> mSelectedSellerIDs;
    private DistanceAndCategoryFilter mDistanceAndCategoryFilter;
    private Context mContext;

    private ItemInteractionListener mItemInteractionListener;

    private BehaviorSubject<List<Seller>> mSellersListSubject;

    public SellersArrayAdapter(Activity activity) {
        mContext = activity;

        mCompleteSet = new SparseArray<>();
        mFilteredList = new ArrayList<>();
        mDistanceAndCategoryFilter = new DistanceAndCategoryFilter();
//        mCheckedItems = new SparseBooleanArray();
        mSellersListSubject = BehaviorSubject.create();

        Log.v(TAG, "NetworkRequestsManager.instance().getObservable(Seller.class)");
        NetworkRequestsManager.instance().getObservable(Seller.class)
                .subscribe(seller -> {
                    mCompleteSet.put(seller.hashCode(), seller);
                    filter();
                }, throwable -> Log.fatalError(new RuntimeException(
                                "error response in subscribe to getObservable(Seller.class)",
                                throwable)
                ));
    }

    public void setListener(ItemInteractionListener listener) {
        this.mItemInteractionListener = listener;
    }

    public Observable<List<Seller>> getFilteredSellersObservable() {
        return mSellersListSubject.asObservable()
                // send only the latest list of sellers in last 10ms
                .debounce(10, TimeUnit.MILLISECONDS);
    }

//    public HashSet<Integer> getSelectedSellerIds() {
//        if (mSelectedSellerIDs == null)
//            updateSelectedSellers();
//        return mSelectedSellerIDs;
//    }

//    private void updateSelectedSellers() {
//        long start = System.nanoTime();
//        mSelectedSellerIDs = new HashSet<Integer>();
//        for (Seller seller : mFilteredList)
//            if (mCheckedItems.get(seller.hashCode())) {
//                mSelectedSellerIDs.add(seller.hashCode());
//                Log.d("mCheckedItems", String.format("getSelectedSellerIds %s (%d,%b)",seller.name_of_shop,seller.hashCode(),true));
//            }
//        double timeTaken = (System.nanoTime() - start)/1000;
//        Log.v(Log.TIMER_TAG, "updateSelectedSellers took " + timeTaken + "μs");
//    }

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

        callImageButton.setOnClickListener(v -> {
            if (mItemInteractionListener != null)
                mItemInteractionListener.callButtonClicked(seller.phone);
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
        mDistanceAndCategoryFilter.runFilter();
    }

    public void filter(int minDist) {
        mDistanceAndCategoryFilter.filter(minDist);
    }

    public void filter(Category category) {
        mDistanceAndCategoryFilter.filter(category);
    }

    private void newData() {
//        mSelectedSellerIDs = null;
        notifyDataSetChanged();
        Log.d(TAG, "new data");
        mSellersListSubject.onNext(mFilteredList);
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
            mObjectMapper = new ObjectMapper();
            mLastConstraint = new Constraint();
            mLastConstraint.category = Category.UNDEFINED;
            mLastConstraint.min_distance = INITIAL_MIN_DIST;
        }

        private void runFilter() {
            Log.d("filter", "runFilter");
            try {
                filter(mObjectMapper.writeValueAsString(mLastConstraint));
            } catch (JsonProcessingException e) {
                Log.fatalError(e);
            }
        }

        private void filter(int minDist) {
            mLastConstraint.min_distance = minDist;
            runFilter();
        }

        private void filter(Category category) {
            mLastConstraint.category = category.name;
            runFilter();
        }

        @Override
        protected FilterResults performFiltering(CharSequence serializedConstraint) {
            ArrayList<Seller> filteredList = new ArrayList<>();
            Constraint constraint = null;
            try {
                Log.d("filter", ".performFiltering serialized constraint=" + serializedConstraint);
                constraint = mObjectMapper.readValue(serializedConstraint.toString(), Constraint.class);
            } catch (IOException e) {
                Log.fatalError(e);
            }

            try {
                Log.d("filter", ".performFiltering constraint=" + constraint);
                for (int i = 0; i < mCompleteSet.size(); i++) {
                    Seller seller = mCompleteSet.valueAt(i);
                    if (seller.getDistanceFromLocation() <= constraint.min_distance && // first filter distance
                            seller.containsCategory(constraint.category)) // filter category
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
            if ((ArrayList<Seller>) results.values != null) {
                mFilteredList = (ArrayList<Seller>) results.values;
                Log.v(TAG, "publishResults size :" + mFilteredList.size());
            }
            else
                mFilteredList.clear();

            newData();
        }
    }
}
