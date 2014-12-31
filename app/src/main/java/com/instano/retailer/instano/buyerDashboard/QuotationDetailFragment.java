package com.instano.retailer.instano.buyerDashboard;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.DataManager;
import com.instano.retailer.instano.application.ServicesSingleton;
import com.instano.retailer.instano.utilities.models.Quotation;
import com.instano.retailer.instano.utilities.models.Quote;
import com.instano.retailer.instano.utilities.models.Seller;

/**
 * A fragment representing a single Quotation detail screen.
 * This fragment is either contained in a {@link com.instano.retailer.instano.utilities.library.old.QuotationListActivity}
 * in two-pane mode (on tablets) or a {@link QuotationDetailActivity}
 * on handsets.
 */
public class QuotationDetailFragment extends Fragment {
    
    ServicesSingleton mServicesSingleton;
    
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_QUOTATION_ID = "com.instano.retailer.instano.item_id"; // quotation ID

    private int mQuotationId;

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
        }
        
//        mServicesSingleton = ServicesSingleton.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_quotation_detail, container, false);

        Quotation quotation = DataManager.instance().getQuotation(mQuotationId);
        Seller seller = DataManager.instance().getSeller(quotation.sellerId);
        Quote quote = DataManager.instance().getQuote(quotation.quoteId);

        TextView shopNameTextView = (TextView) rootView.findViewById(R.id.shopNameTextView);
        TextView sellerNameTextView = (TextView) rootView.findViewById(R.id.sellerNameTextView);
        TextView queryTextView = (TextView) rootView.findViewById(R.id.queryTextView);
        TextView modelTextView = (TextView) rootView.findViewById(R.id.modelTextView);
        TextView priceTextView = (TextView) rootView.findViewById(R.id.priceTextView);
        TextView additionalInfoTextView = (TextView) rootView.findViewById(R.id.additionalInfoTextView);
//        Button acceptDealButton = (Button) rootView.findViewById(R.id.acceptDealButton);

        shopNameTextView.setText(seller.nameOfShop);
        sellerNameTextView.setText(seller.nameOfSeller);
        queryTextView.setText(quote.searchString);
        modelTextView.setText(quotation.nameOfProduct);
        priceTextView.setText(String.format("₹%,d", quotation.price));
        if (TextUtils.isEmpty(quotation.description))
            additionalInfoTextView.setVisibility(View.GONE);
        else {
            additionalInfoTextView.setVisibility(View.VISIBLE);
            additionalInfoTextView.setText("Additional Info:\n" + quotation.description);
        }

        return rootView;
    }
}
