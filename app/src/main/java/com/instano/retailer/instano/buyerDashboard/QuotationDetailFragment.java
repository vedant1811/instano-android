package com.instano.retailer.instano.buyerDashboard;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.model.Quotation;
import com.instano.retailer.instano.utilities.model.Seller;

/**
 * A fragment representing a single Quotation detail screen.
 * This fragment is either contained in a {@link com.instano.retailer.instano.utilities.library.old.QuotationListActivity}
 * in two-pane mode (on tablets) or a {@link QuotationDetailActivity}
 * on handsets.
 */
public class QuotationDetailFragment extends Fragment {

    private static final String TAG = "QuotationDetailFragment";
    
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_QUOTATION_ID = "quotation_id"; // quotation ID
    public static final String ARG_SELLER_NAME = "seller_name";
    public static final String ARG_SHOP_NAME = "shop_name";
    public static final String ARG_NAME_OF_PRODUCT = "name_of_product";
    public static final String ARG_QUOTATION_PRICE = "quotation_price";
    public static final String ARG_QUERY = "query";
    public static final String ARG_ADDITIONAL_INFO = "additional_info";

    private int mQuotationId;
    private  String mSellerName;
    private  String mShopName;
    private  String mProductName;
    private  String mPrice;
    private String mAdditionalInfo;
    private  String mQuery;

    public static QuotationDetailFragment create(Quotation quotation, Seller seller, String query) {
        Log.v(TAG,"quotation id : "+ quotation.id);
        Bundle arguments = new Bundle();
        arguments.putInt(QuotationDetailFragment.ARG_QUOTATION_ID, quotation.id);
        arguments.putInt(QuotationDetailFragment.ARG_QUOTATION_PRICE, quotation.price);
//        arguments.putString(QuotationDetailFragment.ARG_NAME_OF_PRODUCT, quotation.nameOfProduct);
        arguments.putString(QuotationDetailFragment.ARG_ADDITIONAL_INFO, quotation.description);
//        arguments.putString(QuotationDetailFragment.ARG_SELLER_NAME, seller.name_of_seller);
        arguments.putString(QuotationDetailFragment.ARG_SHOP_NAME, seller.name_of_shop);
        arguments.putString(QuotationDetailFragment.ARG_QUERY, query);
        QuotationDetailFragment fragment = new QuotationDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public QuotationDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_QUOTATION_ID)) {
            mQuotationId = getArguments().getInt(ARG_QUOTATION_ID);
            mQuery = getArguments().getString(ARG_QUERY);
            mSellerName = getArguments().getString(ARG_SELLER_NAME);
            mShopName = getArguments().getString(ARG_SHOP_NAME);
            mProductName = getArguments().getString(ARG_NAME_OF_PRODUCT);
            mAdditionalInfo = getArguments().getString(ARG_ADDITIONAL_INFO);
            mPrice = ""+getArguments().getInt(ARG_QUOTATION_PRICE);
        }
        
//        mServicesSingleton = ServicesSingleton.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_quotation_detail, container, false);

//        Quotation quotation = DataManager.instance().getQuotation(mQuotationId);
//        Seller seller = DataManager.instance().getSeller(quotation.sellerId);
//        Quote quote = DataManager.instance().getQuote(quotation.quoteId);

        Log.v(TAG,"onCreateView");
        TextView shopNameTextView = (TextView) rootView.findViewById(R.id.shopNameTextView);
        TextView sellerNameTextView = (TextView) rootView.findViewById(R.id.sellerNameTextView);
        TextView queryTextView = (TextView) rootView.findViewById(R.id.headingTextView);
        TextView modelTextView = (TextView) rootView.findViewById(R.id.modelTextView);
        TextView priceTextView = (TextView) rootView.findViewById(R.id.dealHeadingTextView);
        TextView additionalInfoTextView = (TextView) rootView.findViewById(R.id.additionalInfoTextView);
//        Button acceptDealButton = (Button) rootView.findViewById(R.id.acceptDealButton);

//        shopNameTextView.setText(seller.name_of_shop);
//        sellerNameTextView.setText(seller.name_of_seller);
//        queryTextView.setText(quote.searchString);
//        modelTextView.setText(quotation.nameOfProduct);
//        priceTextView.setText(String.format("₹%,d", quotation.price));
//        if (TextUtils.isEmpty(quotation.description))
//            additionalInfoTextView.setVisibility(View.GONE);
//        else {
//            additionalInfoTextView.setVisibility(View.VISIBLE);
//            additionalInfoTextView.setText("Additional Info:\n" + quotation.description);
//        }

        shopNameTextView.setText(mShopName);
        sellerNameTextView.setText(mSellerName);
        queryTextView.setText(mQuery);
        modelTextView.setText(mProductName);
        priceTextView.setText("Rs. "+mPrice);
        if(mAdditionalInfo.isEmpty())
            additionalInfoTextView.setText("No additional details");
        else
            additionalInfoTextView.setText(mAdditionalInfo);
        return rootView;
    }
}
