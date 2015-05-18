package com.instano.retailer.instano.activities.search;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.activities.BookingDialogFragment;
import com.instano.retailer.instano.activities.SellerDetailActivity;
import com.instano.retailer.instano.application.controller.Quotations;
import com.instano.retailer.instano.application.controller.model.QuotationCard;
import com.instano.retailer.instano.utilities.library.Log;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.observables.AndroidObservable;

/**
 * A fragment representing a list of Items.
 * <p />
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p />
 */
public class SellersListFragment extends ListFragment {
    private static final String TAG = "SellersListFragment";
    private static final String KEY_IS_NOTIFICATION_CANCELLED = "IsNotificationCancelled";

    private boolean mShown = false;
    private int mProductId;
//    private View mHeaderView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SellersListFragment() {
    }

    public void setProduct(int productId) {
        mProductId = productId;
        setShown(false);
        ResultsActivity activity = (ResultsActivity) getActivity();
        if (activity == null)
            return;

//        getListView().addHeaderView(mHeaderView);
        QuotationsAndSellersAdapter adapter = activity.getAdapter();
        adapter.clear();
        setListAdapter(adapter);

        Log.d(TAG, "calling query quotation");
        AndroidObservable.bindFragment(this, Quotations.controller().fetchQuotationsForProduct(productId))
                .subscribe(quotationCard -> {
                        setShown(true);
                        adapter.add(quotationCard);
                }, error -> Log.fatalError(new RuntimeException(error)));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mShown = false;
    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View view = super.onCreateView(inflater, container, savedInstanceState);
//        mHeaderView = inflater.inflate(R.layout.header_notification, null);
//        mHeaderView.findViewById(R.id.cancelButton).setOnClickListener(v -> getListView().removeHeaderView(mHeaderView));
//        return view;
//    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setListShown(mShown);
        setProduct(mProductId);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    private void setShown(boolean shown) {
        mShown = shown;
        if (getView() != null)
            setListShown(mShown);
    }

    public QuotationsAndSellersAdapter getQuotationsAndSellersAdapter(Context context) {
        QuotationsAndSellersAdapter adapter = new QuotationsAndSellersAdapter(context);
        return adapter;
    }


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
            viewHolder.headingTextView.setVisibility(View.VISIBLE);
            viewHolder.headingTextView.setText(item.seller.name_of_shop);
            viewHolder.shopDetailsTextView.setText(item.seller.outlets.get(0).getPrettyDistanceFromLocation());

            if (item.quotation != null)
                viewHolder.subheadingTextView.setText(item.quotation.getPrettyPrice());
            else
                viewHolder.subheadingTextView.setText("Price NA");



            if(true)
                Picasso.with(getContext())
                        .load(item.seller.image).fit().centerInside()
                        .placeholder(R.drawable.no_image_available).fit().centerInside()
                        .into(viewHolder.productImage);
            Log.v(TAG, "dimensions of view : height = " + convertView.getHeight() + " width = " + convertView.getWidth());
    //        else
    //            Picasso.with(getContext())
    //                    .load(deal.product.image)
    //                    .placeholder(R.drawable.img_nature5)
    //                    .error(R.drawable.instano_launcher)
    //                    .into(productImage);

            viewHolder.productImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("seller_id", item.seller.id);
                    bundle.putString("heading", item.seller.name_of_shop);
                    if (item.quotation != null) {
                        bundle.putString("subheading", item.quotation.getPrettyPrice());
                        bundle.putInt("productId", item.quotation.productId);
                        Log.v(TAG, "productId: " + item.quotation.productId);
                    } else {
                        bundle.putString("subheading", "Price NA");
                        bundle.putInt("productId", 0);
                        Log.v(TAG, "productId: 0 Zero");
                        viewHolder.bookItButton.setVisibility(View.INVISIBLE);
                    }
                    Intent intent = new Intent(getContext(), SellerDetailActivity.class);
                    intent.putExtras(bundle);
                    getContext().startActivity(intent);
                }
            });

            viewHolder.bookItButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    android.support.v4.app.FragmentManager fm = getFragmentManager();
                    BookingDialogFragment bookingDialogFragment = BookingDialogFragment.newInstance(item.quotation.getPrettyPrice(),item.seller.name_of_shop);
                    bookingDialogFragment.show(fm,"Booking dialog");
                }
            });

            viewHolder.callButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" +
                            item.seller.outlets.get(0).getPhone()));
                    getContext().startActivity(callIntent);
                }
            });

            viewHolder.msgButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent msgIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" +
                            item.seller.outlets.get(0).getPhone()));
                    msgIntent.putExtra("sms_body", item.seller.name_of_shop + "\n" +
                            (item.quotation != null ? item.quotation.getPrettyPrice() : "Price NA"));
                    getContext().startActivity(msgIntent);
                }
            });


            return convertView;
        }

        public class ViewHolder{
            @InjectView(R.id.dealHeading)
            TextView headingTextView;
            @InjectView(R.id.dealSubheading) TextView subheadingTextView;
            @InjectView(R.id.dealProduct)
            ImageButton productImage;
            @InjectView(R.id.msgButton) ImageButton msgButton;
            @InjectView(R.id.contactButton) ImageButton callButton;
            @InjectView(R.id.bookitButtonStoreFooter)
            Button bookItButton;
            @InjectView(R.id.shopDetails) TextView shopDetailsTextView;


            public ViewHolder(View view) {
                ButterKnife.inject(this, view);

            }
        }
    }
}
