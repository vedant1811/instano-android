package com.instano.retailer.instano.activities.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.controller.model.QuotationCard;
import com.instano.retailer.instano.utilities.library.Log;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by vedant on 4/29/15.
 */
public class QuotationsAndSellersAdapter extends ArrayAdapter<QuotationCard> {

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
        ViewHolder viewHolder;
        // first check to see if the view is null. if so, we have to inflate it.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_googlecard, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        else
            viewHolder = (ViewHolder) convertView.getTag();

        QuotationCard item = getItem(position);
        viewHolder.headingTextView.setText(item.seller.name_of_shop);
        if (item.quotation != null)
            viewHolder.subheadingTextView.setText(item.quotation.getPrettyPrice());
        else
            viewHolder.subheadingTextView.setText("Price NA");

        if(true)
            Picasso.with(getContext())
                    .load(item.seller.image).fit().centerInside()
                    .into(viewHolder.productImage);
        Log.v(TAG, "dimensions of view : height = " + convertView.getHeight() + " width = " + convertView.getWidth());
//        else
//            Picasso.with(getContext())
//                    .load(deal.product.image)
//                    .placeholder(R.drawable.img_nature5)
//                    .error(R.drawable.instano_launcher)
//                    .into(productImage);

        return convertView;
    }

    public class ViewHolder{
        @InjectView(R.id.dealHeading) TextView headingTextView;
        @InjectView(R.id.sellerDetails) TextView distanceTextView;
        @InjectView(R.id.dealSubheading) TextView subheadingTextView;
        @InjectView(R.id.dealProduct) ImageButton productImage;

        public ViewHolder(View view) {
            ButterKnife.inject(this,view);
        }
    }
}
