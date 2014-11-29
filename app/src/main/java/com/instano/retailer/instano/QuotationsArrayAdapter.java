package com.instano.retailer.instano;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.instano.retailer.instano.buyerDashboard.QuotationListFragment;
import com.instano.retailer.instano.utilities.Quotation;
import com.instano.retailer.instano.utilities.Quote;
import com.instano.retailer.instano.utilities.Seller;

import java.util.ArrayList;

/**
 * Created by vedant on 23/9/14.
 */
public class QuotationsArrayAdapter extends BaseAdapter {

    private ArrayList<QuotationsGroup> mGroupsOfQuotations;
    private QuotationListFragment.Callbacks mCallbacks;

    private ArrayList<Object> mObjects;
    private LayoutInflater mInflater;

    public QuotationsArrayAdapter(Context context) {
        mCallbacks = null;
        mGroupsOfQuotations = new ArrayList<QuotationsGroup>();
        mObjects = new ArrayList<Object>();
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void registerCallback (QuotationListFragment.Callbacks callbacks) {
        mCallbacks = callbacks;
    }

    public boolean insertIfNeeded(Quotation quotation) {

        QuotationsGroup group = getGroup(quotation.quoteId);
        Seller seller = ServicesSingleton.getInstance(null).getSellersArrayAdapter().getSeller(quotation.sellerId);

        if (group == null || seller == null)
            return false; // group is not yet created. will be created next time

        if (!group.quotations.contains(quotation)) {
            group.quotations.add(0, quotation);
            // TODO: sort if needed
            newData();
            return true;
        } else
            return false;
    }

    public void insertAtStart(Quote quote) {

        for (QuotationsGroup group : mGroupsOfQuotations) {
            if (group.quote.equals(quote))
                return;
        }
        // no such quotation group exists, so add a new one:
        QuotationsGroup group = new QuotationsGroup(quote);
        mGroupsOfQuotations.add(0, group);
        // we will add quotations when they arrive (or even if they have arrived, they will be fetched again)
        newData();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // assign the view we are converting to a local variable
        View view = convertView;

        Object object = getItem(position);
        // first check to see if the view is null. if so, we have to inflate it.
        if (object instanceof Quotation) {
            if (view == null)
                view = mInflater.inflate(R.layout.list_item_quotation, parent, false);
            getQuotationView((Quotation) object, view);
        } else {
            if (view == null)
                view = mInflater.inflate(R.layout.list_item_separator, parent, false);
            getSeparatorView((Quote) object, view);
        }

        return view;
    }

    private void getSeparatorView(Quote quote, View view) {
        TextView primaryTextView = (TextView) view.findViewById(R.id.primaryTextView);
        TextView secondaryTextView = (TextView) view.findViewById(R.id.secondaryTextView);
        primaryTextView.setText(quote.searchString + ":");
        secondaryTextView.setText(quote.getPrettyTimeElapsed());
    }

    private void getQuotationView(final Quotation quotation, View view) {
        TextView modelTextView = (TextView) view.findViewById(R.id.modelTextView);
        TextView timeElapsedTextView = (TextView) view.findViewById(R.id.timeElapsedTextView);
        TextView priceTextView = (TextView) view.findViewById(R.id.priceTextView);
        TextView descriptionTextView = (TextView) view.findViewById(R.id.descrptionTextView);
        TextView shopTextView = (TextView) view.findViewById(R.id.shopTextView);
        TextView distanceTextView = (TextView) view.findViewById(R.id.distanceTextView);

        ServicesSingleton servicesSingleton = ServicesSingleton.getInstance(null);
        String timeElapsed = quotation.getPrettyTimeElapsed();

        modelTextView.setText(quotation.nameOfProduct);
        timeElapsedTextView.setText(timeElapsed);
        priceTextView.setText("₹" + quotation.price);
        descriptionTextView.setText(quotation.description);

        String nameOfShop;

        Seller seller = servicesSingleton.getSellersArrayAdapter().getSeller(quotation.sellerId);
        // TODO: better handle error
        if (seller != null) {
            nameOfShop = seller.nameOfShop;
            String distance = seller.getPrettyDistanceFromLocation();
            if (distance != null)
                distanceTextView.setText(distance);
            else
                distanceTextView.setVisibility(View.INVISIBLE);
        } else {
            nameOfShop = "INVALID SHOP";
            distanceTextView.setVisibility(View.INVISIBLE);
        }

        shopTextView.setText(nameOfShop);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCallbacks != null)
                    mCallbacks.onItemSelected(quotation.id);
            }
        });

        view.setFocusable(false);
    }

    @Nullable
    public QuotationsGroup getGroup (int quoteId) {
        for (QuotationsGroup group : mGroupsOfQuotations) {
            if (group.quote.id == quoteId)
                return group;
        }
        return null;
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        return mObjects.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position) {
        return mObjects.get(position);
    }

    @Override
    public long getItemId (int pos){
        return pos;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position) instanceof Quote)
            return 1; // separator
        else
            return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    private void newData() {
        mObjects.clear();
        for (QuotationsGroup group : mGroupsOfQuotations) {
            mObjects.add(group.quote);
            for (Quotation quotation : group.quotations)
                mObjects.add(quotation);
        }

        if (mObjects.size() > 0)
            notifyDataSetChanged();
        else
            notifyDataSetInvalidated();
    }

    public void clear() {
        mGroupsOfQuotations.clear();
        newData();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    public Quotation getQuotation(int quotationId) {
        for (QuotationsGroup group : mGroupsOfQuotations)
            for (Quotation quotation : group.quotations)
                if (quotation.id == quotationId)
                    return quotation;
        return null;
    }

    private class QuotationsGroup {
        private Quote quote;
        private ArrayList<Quotation> quotations;

        private QuotationsGroup(Quote quote) {
            this.quote = quote;
            quotations = new ArrayList<Quotation>();
        }
    }
}
