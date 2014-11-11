package com.instano.retailer.instano;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.instano.retailer.instano.utilities.ProductCategories;
import com.instano.retailer.instano.utilities.Seller;

import java.util.ArrayList;

/**
 * TODO: do more
 * displays a list of sellers sorted by Seller.id
 * Created by vedant on 24/9/14.
 */
public class SellersArrayAdapter extends ArrayAdapter <Seller> {

    private ArrayList<Seller> mFilteredList;
    private DistanceFilter mFilter;
    private SparseBooleanArray mCheckedItems;

    private ItemInteractionListener mListener;

    public SellersArrayAdapter(Context context) {
        super(context, android.R.layout.simple_list_item_2);
        mFilteredList = new ArrayList<Seller>();
        mFilter = new DistanceFilter();
        mCheckedItems = new SparseBooleanArray();
    }

    public void setListener (ItemInteractionListener listener) {
        this.mListener = listener;
    }

    public ArrayList<Integer> getSelectedSellerIds() {
        ArrayList<Integer> ids = new ArrayList<Integer>();
        for (Seller seller : mFilteredList)
            if (mCheckedItems.get(seller.id))
                ids.add(seller.id);
        return ids;
    }

    @Override
    public int getCount() {
        return mFilteredList.size();
    }

    @Override
    public Seller getItem (int index) {
        return mFilteredList.get(index);
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final View view;

        // first check to see if the view is null. if so, we have to inflate it.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_shop, parent, false);
        }
        else
            view = convertView;

        TextView shopNameTextView = (TextView) view.findViewById(R.id.shopNameTextView);
        TextView addressTextView = (TextView) view.findViewById(R.id.addressTextView);
        TextView distanceTextView = (TextView) view.findViewById(R.id.distanceTextView);
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
        ImageButton callImageButton = (ImageButton) view.findViewById(R.id.callImageButton);

        final Seller seller = getItem(position);

        checkBox.setOnCheckedChangeListener (new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (mListener != null)
                    mListener.itemCheckedStateChanged(position, isChecked);
                mCheckedItems.put(seller.id, isChecked);
            }
        });
        // initially setting all to checked
        mCheckedItems.put(seller.id, true);
        if (mListener != null)
            mListener.itemCheckedStateChanged(position, true);

        shopNameTextView.setText(seller.nameOfShop);
        addressTextView.setText(seller.address);
        String distanceFromLocation = seller.getPrettyDistanceFromLocation();
        if (distanceFromLocation != null)
            distanceTextView.setText(distanceFromLocation);
        else
            distanceTextView.setVisibility(View.INVISIBLE);

        callImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null)
                    mListener.callButtonClicked(seller.phone);
            }
        });

        return view;
    }

    public Seller getSeller (int sellerId) throws IllegalArgumentException {
        for (int i = 0; i < super.getCount(); i++) {
            if (super.getItem(i).id == sellerId)
                return super.getItem(i);
        }

        throw new IllegalArgumentException("no seller with id " + sellerId);
    }

    @Override
    public long getItemId (int pos){
        return getItem(pos).id;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public void filer() {
        mFilter.runOldFilter();
    }

    public interface ItemInteractionListener {
        /**
         *
         * @param pos position whose checkedState has changed, or -1 if listItems changed
         * @param checkedState
         */
        public void itemCheckedStateChanged(int pos, boolean checkedState);
        public void callButtonClicked(String number);
    }

    private ArrayList<Seller> getUnderlyingArray() {
        ArrayList<Seller> sellers = new ArrayList<Seller>();
        for (int i = 0; i < super.getCount(); i++) {
            sellers.add(super.getItem(i));
        }
        return sellers;
    }

    private class DistanceFilter extends Filter {

        private CharSequence constraint = "1000," + ProductCategories.UNDEFINED;

        void runOldFilter() {
            filter(constraint);
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            this.constraint = constraint;
            ArrayList<Seller> filteredList;
            ArrayList<Seller> underlyingArray = getUnderlyingArray();

            try {
                String[] constraints = String.valueOf(this.constraint).split(",");

                filteredList = new ArrayList<Seller>();

                int minDist = Integer.parseInt(constraints[0]);
                String productCategory = constraints[1];
                for (Seller seller : underlyingArray) {
//                    if (seller.getDistanceFromLocation() <= minDist && // match product category as well:
                            if(seller.productCategories.contains(productCategory))
                        filteredList.add(seller);
                }
            } catch (NumberFormatException e) {
                // add all in that case
                filteredList = underlyingArray;
            }

            FilterResults filterResults = new FilterResults();
            filterResults.count = filteredList.size();
            filterResults.values = filteredList;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFilteredList = (ArrayList<Seller>) results.values;
            if (results.count == 0)
                notifyDataSetInvalidated();
            else
                notifyDataSetChanged();
            if (mListener != null)
                mListener.itemCheckedStateChanged(-1, false);
        }
    }
}
