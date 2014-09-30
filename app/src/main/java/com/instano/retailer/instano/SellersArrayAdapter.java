package com.instano.retailer.instano;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * TODO: do more
 * displays a list of sellers sorted by Seller.id
 * Created by vedant on 24/9/14.
 */
public class SellersArrayAdapter extends ArrayAdapter <ServicesSingleton.Seller> {
    private ArrayList<ServicesSingleton.Seller> mFilteredList;
    private Filter mFilter;

    private ItemCheckedStateChangedListener mListener;

    public SellersArrayAdapter(Context context) {
        super(context, android.R.layout.simple_list_item_2);
        mFilteredList = new ArrayList<ServicesSingleton.Seller>();
        mFilter = new DistanceFilter();
    }

    public void setListener (ItemCheckedStateChangedListener listener) {
        this.mListener = listener;
    }

    @Override
    public int getCount() {
        return mFilteredList.size();
    }

    @Override
    public ServicesSingleton.Seller getItem (int index) {
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

        checkBox.setOnCheckedChangeListener (new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (mListener != null)
                    mListener.itemStateChanged(position, isChecked);
            }
        });
        // initially setting all to checked
        mListener.itemStateChanged(position, true);

        ServicesSingleton.Seller seller = getItem(position);

        shopNameTextView.setText(seller.nameOfShop);
        addressTextView.setText(seller.address);
        String distanceFromLocation = seller.getPrettyDistanceFromLocation();
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

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public interface ItemCheckedStateChangedListener {
        /**
         *
         * @param pos position whose checkedState has changed, or -1 if listItems changed
         * @param checkedState
         */
        public void itemStateChanged(int pos, boolean checkedState);
    }

    private ServicesSingleton.Seller[] getUnderlyingArray() {
        ServicesSingleton.Seller[] sellers = new ServicesSingleton.Seller[super.getCount()];
        for (int i = 0; i < super.getCount(); i++) {
            sellers[i] = super.getItem(i);
        }
        return sellers;
    }

    private class DistanceFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            int minDist = Integer.parseInt(String.valueOf(constraint));

            ArrayList<ServicesSingleton.Seller> filteredList = new ArrayList<ServicesSingleton.Seller>();

            for (ServicesSingleton.Seller item : getUnderlyingArray())
                if (item.getDistanceFromLocation() <= minDist)
                    filteredList.add(item);

            FilterResults filterResults = new FilterResults();
            filterResults.count = filteredList.size();
            filterResults.values = filteredList;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFilteredList = (ArrayList<ServicesSingleton.Seller>) results.values;
            if (results.count == 0)
                notifyDataSetInvalidated();
            else
                notifyDataSetChanged();
            mListener.itemStateChanged(-1, false);
        }
    }
}
