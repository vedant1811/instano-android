package com.instano.retailer.instano.buyerDashboard.quotes;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.DataManager;
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
public class QuoteDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_QUOTE_ID = "quote_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private Quote mItem;

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
        expandableListView.setAdapter(new Adapter(getActivity()));
        return rootView;
    }

    private class Adapter extends BaseExpandableListAdapter {
        private Context mContext;
        private List<String> mListDataHeader; // header titles i.e seller names
        // child data in format of header title, child title
        private HashMap<String, List<Quotation>> mListDataChild;

        public Adapter(Context context) {
            this.mContext = context;
            this.mListDataHeader = new ArrayList<String>();
            this.mListDataChild = new HashMap<String, List<Quotation>>();
            dataUpdated();
        }

        public void dataUpdated() {
            long start = System.nanoTime();
            mListDataHeader.clear();
            mListDataChild.clear();

            for (Integer id : mItem.sellerIds){
                DataManager dataManager = DataManager.instance();
                Seller seller = dataManager.getSeller(id);
                if (seller != null) {
                    mListDataHeader.add(0, seller.nameOfShop);
                    mListDataChild.put(seller.nameOfShop, dataManager.quotationsBySeller(seller.id));
                }
            }
            notifyDataSetChanged();
            double time = (System.nanoTime() - start)/ Log.ONE_MILLION;
            Log.d(Log.TIMER_TAG, String.format("Adapter.dataUpdated took %.4fms", time));
        }

        @Override
        public Quotation getChild(int groupPosition, int childPosititon) {
            return this.mListDataChild.get(this.mListDataHeader.get(groupPosition))
                    .get(childPosititon);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getChildView(int groupPosition, final int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {

            Quotation quotation = getChild(groupPosition, childPosition);
            final String childText = quotation.toChatString();

            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this.mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(android.R.layout.simple_expandable_list_item_1, null);
            }

            TextView txtListChild = (TextView) convertView.findViewById(android.R.id.text1);

            txtListChild.setText(childText);
            return convertView;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return this.mListDataChild.get(this.mListDataHeader.get(groupPosition))
                    .size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return this.mListDataHeader.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return this.mListDataHeader.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            String headerTitle = (String) getGroup(groupPosition);
            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this.mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(android.R.layout.simple_expandable_list_item_2, null);
            }

            TextView lblListHeader = (TextView) convertView
                    .findViewById(android.R.id.text1);
            lblListHeader.setTypeface(null, Typeface.BOLD);
            lblListHeader.setText(headerTitle);

            return convertView;
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
