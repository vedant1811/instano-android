package com.instano.retailer.instano.activities.search;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.utilities.models.Quotation;
import com.instano.retailer.instano.utilities.models.Seller;
import com.squareup.picasso.Picasso;

/**
 * Created by vedant on 4/29/15.
 */
public class QuotationsAndSellersAdapter extends ArrayAdapter<Pair<Seller, Quotation>> {

    private static final String TAG = "QuotationsAndSellersAdapter";

    /**
     * Constructor
     *
     * @param context  The current context.
     */
    public QuotationsAndSellersAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View view;

        // first check to see if the view is null. if so, we have to inflate it.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_googlecard, parent, false);
        }
        else
            view = convertView;

        Pair<Seller, Quotation> item = getItem(position);

        TextView headingTextView = (TextView) view.findViewById(R.id.dealHeading);
        TextView subheadingTextView = (TextView) view.findViewById(R.id.dealSubheading);
        TextView distanceTextView = (TextView) view.findViewById(R.id.dealDistance);
        ImageButton productImage = (ImageButton) view.findViewById(R.id.dealProduct);
        headingTextView.setText(item.first.name_of_shop);
        subheadingTextView.setText(String.valueOf(item.second.price));

        if(true)
            Picasso.with(getContext())
                    .load(R.drawable.img_nature5)
                    .into(productImage);
//        else
//            Picasso.with(getContext())
//                    .load(deal.product.image)
//                    .placeholder(R.drawable.img_nature5)
//                    .error(R.drawable.instano_launcher)
//                    .into(productImage);

        return view;
    }
}
