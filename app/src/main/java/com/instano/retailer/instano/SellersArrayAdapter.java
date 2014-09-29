package com.instano.retailer.instano;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * TODO: do more
 * displays a list of sellers sorted by Seller.id
 * Created by vedant on 24/9/14.
 */
public class SellersArrayAdapter extends ArrayAdapter <ServicesSingleton.Seller> {
    public SellersArrayAdapter(Context context) {
        super(context, android.R.layout.simple_list_item_2);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // assign the view we are converting to a local variable
        View view = convertView;

        // first check to see if the view is null. if so, we have to inflate it.
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_shop, parent, false);
        }

        TextView shopNameTextView = (TextView) view.findViewById(R.id.shopNameTextView);
        TextView addressTextView = (TextView) view.findViewById(R.id.addressTextView);
        TextView distanceTextView = (TextView) view.findViewById(R.id.distanceTextView);

        ServicesSingleton.Seller seller = getItem(position);

        shopNameTextView.setText(seller.nameOfShop);
        addressTextView.setText(seller.address);
        String distanceFromLocation = seller.getDistanceFromLocation();
        if (distanceFromLocation != null)
            distanceTextView.setText(distanceFromLocation);
        else
            distanceTextView.setVisibility(View.INVISIBLE);

        return view;
    }

    public ServicesSingleton.Seller getSeller (int sellerId) throws IllegalArgumentException {
        for (int i = 0; i < getCount(); i++) {
            if (getItem(i).id == sellerId)
                return getItem(i);
        }

        throw new IllegalArgumentException("no seller with id " + sellerId);
    }

    @Override
    public long getItemId (int pos){
        return getItem(pos).id;
    }
}
