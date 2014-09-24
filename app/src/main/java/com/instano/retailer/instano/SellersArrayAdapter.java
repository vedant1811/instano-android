package com.instano.retailer.instano;

import android.content.Context;
import android.widget.ArrayAdapter;

/**
 * TODO: do more
 * displays a list of sellers sorted by Seller.id
 * Created by vedant on 24/9/14.
 */
public class SellersArrayAdapter extends ArrayAdapter <ServicesSingleton.Seller> {
    public SellersArrayAdapter(Context context) {
        super(context, android.R.layout.simple_list_item_2);
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
