package com.instano.retailer.instano;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by vedant on 23/9/14.
 */
public class QuotationsArrayAdapter extends ArrayAdapter<ServicesSingleton.Quotation> {

    private QuotationListFragment.Callbacks mCallbacks;
    public QuotationsArrayAdapter(Context context) {
        super(context, R.layout.list_item_quotation);
        mCallbacks = null;
    }

    public void registerCallback (QuotationListFragment.Callbacks callbacks) {
        mCallbacks = callbacks;
    }

    public boolean insertIfNeeded(ServicesSingleton.Quotation quotation) {

        if (getQuotation(quotation.id) != null)
            return false;

        try {
            ServicesSingleton.getInstance(null).getSellersArrayAdapter().getSeller(quotation.sellerId);
            insert(quotation, 0);
            return true;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // assign the view we are converting to a local variable
        View view = convertView;

        // first check to see if the view is null. if so, we have to inflate it.
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_quotation, parent, false);
        }

        TextView modelTextView = (TextView) view.findViewById(R.id.modelTextView);
        TextView queryInfoTextView = (TextView) view.findViewById(R.id.queryInfoTextView);
        TextView priceTextView = (TextView) view.findViewById(R.id.priceTextView);
        TextView descriptionTextView = (TextView) view.findViewById(R.id.descrptionTextView);
        TextView shopTextView = (TextView) view.findViewById(R.id.shopTextView);
        TextView distanceTextView = (TextView) view.findViewById(R.id.distanceTextView);

        ServicesSingleton servicesSingleton = ServicesSingleton.getInstance(null);

        ServicesSingleton.Quotation quotation = getItem(position);
        String timeElapsed = quotation.getPrettyTimeElapsed();

        modelTextView.setText(quotation.nameOfProduct);
        // TODO: queryInfoTextView.setText(String.format("%s for query \"%s\"",timeElapsed, );
        queryInfoTextView.setText(String.format("%s",timeElapsed));
        priceTextView.setText("₹" + quotation.price);
        descriptionTextView.setText(quotation.description);

        String nameOfShop;

        // TODO: better handle error
        try {
            ServicesSingleton.Seller seller = servicesSingleton.getSellersArrayAdapter()
                    .getSeller(quotation.sellerId);
            nameOfShop = seller.nameOfShop;
            String distance = seller.getPrettyDistanceFromLocation();
            if (distance != null)
                distanceTextView.setText(distance);
            else
                distanceTextView.setVisibility(View.INVISIBLE);
        } catch (IllegalArgumentException e) {
            nameOfShop = "INVALID SHOP";
            distanceTextView.setVisibility(View.INVISIBLE);
        }

        shopTextView.setText(nameOfShop);

        // TODO: fix hack
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCallbacks != null)
                    mCallbacks.onItemSelected(position);
            }
        });

        view.setFocusable(false);

        return view;
    }

    public ServicesSingleton.Quotation getQuotation (int quotationId) {
        for (int i = 0; i < getCount(); i++) {
            if ( getItem(i).id == quotationId )
                return getItem(i);
        }
        return null;
    }

    @Override
    public long getItemId (int pos){
        return getItem(pos).id;
    }
}
