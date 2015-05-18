package com.instano.retailer.instano.activities;

import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.network.NetworkRequestsManager;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.model.Deal;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by Dheeraj on 12-May-15.
 */
public class BookingDialogFragment extends DialogFragment {

    public static final String ITEM_DETAILS = "Item_Details";
    public static final String ITEM_IMAGE = "Item Image";
    private static final String TAG = "BookingDialogFragment";
    private CharSequence mDetails;
    private String imageUrl;

    @InjectView(R.id.item_details) TextView details;
    @InjectView(R.id.bookButton)Button book;
    @InjectView(R.id.cancelButton) Button cancel;
    @InjectView(R.id.productImage)ImageView image;

    public BookingDialogFragment(){

    }

    public static BookingDialogFragment newInstance(String details,String dealImage){
        String productImage = dealImage ;
        Bundle arguments = new Bundle();
        arguments.putString(ITEM_DETAILS, details);
        arguments.putString(ITEM_IMAGE, productImage);
        BookingDialogFragment fragment = new BookingDialogFragment();
        fragment.setArguments(arguments);

        return fragment;
    }
    //TODO get Product details
    public static BookingDialogFragment newInstance(int productId){

        Bundle arguments = new Bundle();
        NetworkRequestsManager.instance().getProduct(productId)
                .subscribe(product -> {
                    Log.v(TAG, "productID : "+ productId);
                    String details = product.name;
                    String productImage = product.image;

                    arguments.putString(ITEM_DETAILS, details);
                    arguments.putString(ITEM_IMAGE, productImage);
                }, throwable -> Log.fatalError(new RuntimeException(throwable)));
        BookingDialogFragment fragment = new BookingDialogFragment();
        fragment.setArguments(arguments);

        return fragment;
    }

   @Override
   public void onCreate(Bundle savedInstanceStates){
        super.onCreate(savedInstanceStates);
        mDetails = getArguments().getString(ITEM_DETAILS);
        imageUrl = getArguments().getString(ITEM_IMAGE);
   }

    @OnClick(R.id.bookButton)
    public void bookClicked(){

    }

    @OnClick(R.id.cancelButton)
    public void cancelClicked() {
        this.dismiss();
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View rootview = inflater.inflate(R.layout.fragment_booking_dialog,container,false);
        ButterKnife.inject(this, rootview);
        details.setText(mDetails);
        Picasso.with(getActivity())
                .load(imageUrl).fit().centerInside()
                .into(image);
        return rootview;
    }
}
