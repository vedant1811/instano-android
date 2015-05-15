package com.instano.retailer.instano.buyerDashboard.quotes;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.buyerDashboard.QuotationDetailFragment;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.model.Quotation;
import com.instano.retailer.instano.utilities.model.Quote;
import com.instano.retailer.instano.utilities.model.Seller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A fragment representing a single Quote detail screen.
 * This fragment is either contained in a {@link QuoteListActivity}
 * on handsets.
 */
public class QuoteDetailFragment extends Fragment {
    private final String TAG = getClass().getSimpleName();
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_QUOTE_ID = "quote_id";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    private Quote mItem;
    private Adapter mAdapter;
    private TextView mSubheadingTextView;
    private TextView mHeadingTextView;
    private ProgressBar mProgressBar;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onQuotationSelected(QuotationDetailFragment fragment);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = id -> {};

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     * Do not use. use {@link #create} instead
     */
    public QuoteDetailFragment() {
    }

    public static QuoteDetailFragment create(int id) {
        Bundle arguments = new Bundle();
        arguments.putInt(QuoteDetailFragment.ARG_QUOTE_ID, id);
        QuoteDetailFragment fragment = new QuoteDetailFragment();
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getArguments().containsKey(ARG_QUOTE_ID)) {
            if (mItem == null)
                throw new IllegalStateException(
                        "Fragment Quote detail created without any Quote");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_quote_detail, container, false);
        ExpandableListView expandableListView = (ExpandableListView) rootView.findViewById(R.id.expandableListView);
        mHeadingTextView = (TextView) rootView.findViewById(R.id.headingTextView);
        mSubheadingTextView = (TextView) rootView.findViewById(R.id.subheadingTextView);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.quoteDetailProgressBar);

        mAdapter = new Adapter(getActivity());
        expandableListView.setAdapter(mAdapter);
//        AndroidObservable.bindFragment(this, NetworkRequestsManager.instance().getObservable(Quote.class)
//                .filter(quote -> quote.id == getArguments().getInt(ARG_QUOTE_ID)))
//                        .subscribe(quote -> {
//                            mItem = quote;
//                            Log.v(TAG, "quote received: " +quote );
////                            mHeadingTextView.setText(String.format("\"%s\"", mItem.searchString));
//                            // initialize adapter only after quote has been fetched
//                            mAdapter.refresh();
//                        });

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    /**
     * Shows the details of {@link Quotation} represented by this {@link QuoteDetailFragment}
     * an expandable list adapter that shows {@link Seller}.nameOfShop as headers
     * Each group has 2 types of children: one showing {@link Seller} details, other showing {@link Quotation}
     * hence even if there are no replies by a seller, a group will have one child view for sure.
     */
    private class Adapter extends BaseExpandableListAdapter {
        private static final int CHILD_TYPE_SELLER = 0;
        private static final int CHILD_TYPE_QUOTATION = 1;
        private List<Seller> mHeaders; // header titles i.e seller names
        private SparseArray<Quotation> mQuotations;
        private SparseArray<Seller> mSellers;
        /**
         * child data in format of header title, child title
         * value `Object` can be of 2 types: {@link Seller} or {@link Quotation}
         */
        private HashMap<Seller, List<Object>> mChildrenMap;
        private final LayoutInflater mInflater;

        public Adapter(Context context) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mHeaders = new ArrayList<>();
            mQuotations = new SparseArray<>();
            mSellers = new SparseArray<>();
            mChildrenMap = new HashMap<>();
        }

        /**
         * main initializer
         */
        private void refresh() {
//            AndroidObservable.bindFragment(QuoteDetailFragment.this, NetworkRequestsManager.instance().getObservable(Seller.class))
////                    .filter(seller -> mItem.sellerIds.contains(seller.id)))
//                            .subscribe(seller -> {
//                                mSellers.put(seller.id, seller);
//                                dataUpdated();
//                            });
//            AndroidObservable.bindFragment(QuoteDetailFragment.this, NetworkRequestsManager.instance().getObservable(Quotation.class))
////                    .filter(quotation -> mItem.sellerIds.contains(quotation.sellerId)))
//                            .subscribe(quotation -> {
//                                Log.d(TAG, "new quotation, id: " + quotation.id);
//                                mQuotations.put(quotation.id, quotation);
//                                dataUpdated();
//                            });
        }

        public void dataUpdated() {
            long start = System.nanoTime();
            mHeaders.clear();
            mChildrenMap.clear();

//            for (Integer id : mItem.sellerIds){
//                Seller seller = mSellers.get(id);
//                if (seller != null) {
//                    mHeaders.add(0, seller);
//                    ArrayList<Object> groupChildren = new ArrayList<>();
//                    for (int i = 0; i < mQuotations.size(); i++) {
//                        Quotation quotation = mQuotations.valueAt(i);
//                        if (quotation.sellerId == id && quotation.quoteId == mItem.id)
//                            groupChildren.add(quotation);
//                    }
//                    // TODO: sort the above
//
////                    put first item, Seller:
//                    groupChildren.add(0, seller);
//                    mChildrenMap.put(seller, groupChildren);
//                }
//            }
            notifyDataSetChanged();
            int numOfSellers = getGroupCount();
            String subheading;
            if (numOfSellers > 0) {
                subheading = String.format("sent to %d retailers", numOfSellers);
                mProgressBar.setVisibility(View.GONE);
            }
            else {
                subheading = "We have received your query and will forward to retailers." +
                        " You will see those sellers below soon.";
                mProgressBar.setVisibility(View.VISIBLE);
            }

            mSubheadingTextView.setText(subheading);

            double time = (System.nanoTime() - start)/ Log.ONE_MILLION;
            Log.v(Log.TIMER_TAG, String.format("Adapter.dataUpdated took %.4fms", time));
        }

        @Override
        public int getChildType(int groupPosition, int childPosition) {
            if (childPosition == 0)
                return CHILD_TYPE_SELLER;
            else
                return CHILD_TYPE_QUOTATION;
        }

        @Override
        public int getChildTypeCount(){
            return 2;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mChildrenMap.get(this.mHeaders.get(groupPosition)).get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getChildView(int groupPosition, final int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            switch (getChildType(groupPosition, childPosition)) {
            case CHILD_TYPE_SELLER:
                final Seller seller = (Seller) getChild(groupPosition, childPosition);
                if (convertView == null)
                    convertView = mInflater.inflate(R.layout.child_list_item_shop, null);

                TextView sellerNameTextView = (TextView) convertView.findViewById(R.id.shopNameTextView);
                TextView addressTextView = (TextView) convertView.findViewById(R.id.addressTextView);
                TextView distanceTextView = (TextView) convertView.findViewById(R.id.distanceTextView);
                ImageButton callImageButton = (ImageButton) convertView.findViewById(R.id.callImageButton);

//                sellerNameTextView.setText(seller.name_of_seller);
//                addressTextView.setText(seller.address);
//                String distanceFromLocation = seller.getPrettyDistanceFromLocation();
//                if (distanceFromLocation != null)
//                    distanceTextView.setText(distanceFromLocation);
//                else
//                    distanceTextView.setVisibility(View.INVISIBLE);

                callImageButton.setOnClickListener(v -> {
//                    Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + seller.phone));
//                    startActivity(callIntent);
                });
                break;

            case CHILD_TYPE_QUOTATION:
                final Quotation quotation = (Quotation) getChild(groupPosition, childPosition);
                if (convertView == null)
                    convertView = mInflater.inflate(R.layout.list_item_quotation, null);

                TextView modelTextView = (TextView) convertView.findViewById(R.id.headingTextView);
                TextView timeElapsedTextView = (TextView) convertView.findViewById(R.id.expiresAtTextView);
                TextView priceTextView = (TextView) convertView.findViewById(R.id.dealHeadingTextView);
                final TextView newTextView = (TextView) convertView.findViewById(R.id.newTextView);
                String timeElapsed = quotation.getPrettyTimeElapsed();

                // TODO: also change color alternating-ly
//                modelTextView.setText(quotation.nameOfProduct);
                timeElapsedTextView.setText(timeElapsed);
                priceTextView.setText("₹ " + quotation.price);

                // TODO:
                convertView.setOnClickListener(v -> {
                    mCallbacks.onQuotationSelected(QuotationDetailFragment.create(quotation,
                            (Seller) getChild(groupPosition, 0), mHeadingTextView.getText().toString()));
                    if (!quotation.isRead()) {
//                            NetworkRequestsManager.instance().setQuotationStatusReadRequest(quotation.id);
                        quotation.setStatusRead();
                        newTextView.setVisibility(View.GONE);
                        notifyDataSetChanged();
                        //TODO: optimize if needed. ref: http://stackoverflow.com/a/9987714/1396264
                    }
                });

                if(quotation.isRead())
                    newTextView.setVisibility(View.GONE);
                else
                    newTextView.setVisibility(View.VISIBLE);
            }
            return convertView;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            Seller seller = getGroup(groupPosition);
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.header_list_item_shop, null);
                // TODO: checkout mInflater.inflate(R.layout.list_item_quotation, parent, false);
            }

            TextView mainTextView = (TextView) convertView.findViewById(R.id.mainTextView);
            TextView responsesTextView = (TextView) convertView.findViewById(R.id.responsesTextView);
            mainTextView.setText(seller.name_of_shop);
            int numResponses = getChildrenCount(groupPosition) - 1; // since one child is seller
            responsesTextView.setText(numResponses + " responses");

            return convertView;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return this.mChildrenMap.get(this.mHeaders.get(groupPosition)).size();
        }

        @Override
        public Seller getGroup(int groupPosition) {
            return this.mHeaders.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return this.mHeaders.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}
