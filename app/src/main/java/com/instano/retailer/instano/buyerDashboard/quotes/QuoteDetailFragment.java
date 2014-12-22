package com.instano.retailer.instano.buyerDashboard.quotes;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.DataManager;
import com.instano.retailer.instano.application.NetworkRequestsManager;
import com.instano.retailer.instano.buyerDashboard.QuotationDetailActivity;
import com.instano.retailer.instano.buyerDashboard.QuotationDetailFragment;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.models.Quotation;
import com.instano.retailer.instano.utilities.models.Quote;
import com.instano.retailer.instano.utilities.models.Seller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A fragment representing a single Quote detail screen.
 * This fragment is either contained in a {@link QuoteListActivity}
 * in two-pane mode (on tablets) or a {@link QuoteDetailActivity}
 * on handsets.
 */
public class QuoteDetailFragment extends Fragment implements DataManager.Listener{
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_QUOTE_ID = "quote_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private Quote mItem;
    private Adapter mAdapter;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_QUOTE_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            int id = getArguments().getInt(ARG_QUOTE_ID);
            mItem = DataManager.instance().getQuote(id);
            if (mItem == null)
                throw new IllegalStateException(
                        "Fragment Quote detail created without any Quote. Quote id: " + id);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_quote_detail, container, false);
        ExpandableListView expandableListView = (ExpandableListView) rootView.findViewById(R.id.expandableListView);
        mAdapter = new Adapter(getActivity());
        DataManager.instance().registerListener(this);
        expandableListView.setAdapter(mAdapter);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        DataManager.instance().unregisterListener(this);
    }

    @Override
    public void quotesUpdated() {
        mAdapter.dataUpdated();
    }

    @Override
    public void quotationsUpdated() {
        mAdapter.dataUpdated();
    }

    @Override
    public void sellersUpdated() {

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
        /**
         * child data in format of header title, child title
         * value `Object` can be of 2 types: {@link Seller} or {@link Quotation}
         */
        private HashMap<Seller, List<Object>> mChildrenMap;
        private final LayoutInflater mInflater;

        public Adapter(Context context) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mHeaders = new ArrayList<Seller>();
            mChildrenMap = new HashMap<Seller, List<Object>>();
            dataUpdated();
        }

        public void dataUpdated() {
            long start = System.nanoTime();
            mHeaders.clear();
            mChildrenMap.clear();

            for (Integer id : mItem.sellerIds){
                DataManager dataManager = DataManager.instance();
                Seller seller = dataManager.getSeller(id);
                if (seller != null) {
                    mHeaders.add(0, seller);
                    ArrayList<Object> groupChildren = dataManager.quotationsBySeller(seller.id);
                    // put first item, Seller:
                    groupChildren.add(0, seller);
                    mChildrenMap.put(seller, groupChildren);
                }
            }
            notifyDataSetChanged();
            double time = (System.nanoTime() - start)/ Log.ONE_MILLION;
            Log.d(Log.TIMER_TAG, String.format("Adapter.dataUpdated took %.4fms", time));
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

                TextView sellerNameTextView = (TextView) convertView.findViewById(R.id.sellerNameTextView);
                TextView addressTextView = (TextView) convertView.findViewById(R.id.addressTextView);
                TextView distanceTextView = (TextView) convertView.findViewById(R.id.distanceTextView);
                ImageButton callImageButton = (ImageButton) convertView.findViewById(R.id.callImageButton);

                sellerNameTextView.setText(seller.nameOfSeller);
                addressTextView.setText(seller.address);
                String distanceFromLocation = seller.getPrettyDistanceFromLocation();
                if (distanceFromLocation != null)
                    distanceTextView.setText(distanceFromLocation);
                else
                    distanceTextView.setVisibility(View.INVISIBLE);

                callImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + seller.phone));
                        startActivity(callIntent);
                    }
                });
                break;

            case CHILD_TYPE_QUOTATION:
                final Quotation quotation = (Quotation) getChild(groupPosition, childPosition);
                if (convertView == null)
                    convertView = mInflater.inflate(R.layout.list_item_quotation, null);

                TextView modelTextView = (TextView) convertView.findViewById(R.id.queryTextView);
                TextView timeElapsedTextView = (TextView) convertView.findViewById(R.id.priceTextView);
                TextView priceTextView = (TextView) convertView.findViewById(R.id.timeElapsedTextView);
                final TextView newTextView = (TextView) convertView.findViewById(R.id.newTextView);
                String timeElapsed = quotation.getPrettyTimeElapsed();

                // TODO: also change color alternating-ly
                modelTextView.setText(quotation.nameOfProduct);
                timeElapsedTextView.setText(timeElapsed);
                priceTextView.setText("₹ " + quotation.price);

                // TODO:
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent detailIntent = new Intent(getActivity(), QuotationDetailActivity.class);
                        detailIntent.putExtra(QuotationDetailFragment.ARG_QUOTATION_ID, quotation.id);
                        startActivity(detailIntent);
                        if (!quotation.isRead()) {
                            NetworkRequestsManager.instance().setQuotationStatusReadRequest(quotation.id);
                            quotation.setStatusRead();
                            newTextView.setVisibility(View.GONE);
                            notifyDataSetChanged();
                            //TODO: optimize if needed. ref: http://stackoverflow.com/a/9987714/1396264
                        }
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
            mainTextView.setText(seller.nameOfShop);
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
