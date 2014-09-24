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

    public QuotationsArrayAdapter(Context context) {
        super(context, R.layout.list_item_quotation);
    }

    public void insertAtStart (ServicesSingleton.Quotation quotation) {
        insert(quotation, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // assign the view we are converting to a local variable
        View view = convertView;

        // first check to see if the view is null. if so, we have to inflate it.
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_quotation, parent, false);
        }

        TextView modelTextView = (TextView) view.findViewById(R.id.modelTextView);
        TextView priceTextView = (TextView) view.findViewById(R.id.priceTextView);
        TextView descriptionTextView = (TextView) view.findViewById(R.id.descrptionTextView);
        TextView shopTextView = (TextView) view.findViewById(R.id.shopTextView);

        ServicesSingleton.Quotation quotation = getItem(position);

        modelTextView.setText(quotation.nameOfProduct);
        priceTextView.setText("Rs. " + quotation.price); // TODO change to rupee symbol
        descriptionTextView.setText(quotation.description);

        String nameOfShop;


        // TODO: better handle error
        try {
            nameOfShop = ServicesSingleton.getInstance(null).getSellersArrayAdapter()
                                    .getSeller(quotation.sellerId).nameOfShop;
        } catch (IllegalArgumentException e) {
            nameOfShop = "INVALID SHOP";
        }

        shopTextView.setText(nameOfShop);

        return view;
    }

    public ServicesSingleton.Quotation getQuotation (int quotationId) {
        for (int i = 0; i < getCount(); i++) {
            if ( getItem(i).id == quotationId )
                return getItem(i);
        }
        throw new IllegalArgumentException("no such quotation present");
    }

    @Override
    public long getItemId (int pos){
        return getItem(pos).id;
    }
}
