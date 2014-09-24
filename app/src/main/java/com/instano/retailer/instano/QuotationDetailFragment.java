package com.instano.retailer.instano;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A fragment representing a single Quotation detail screen.
 * This fragment is either contained in a {@link QuotationListActivity}
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
        
        mServicesSingleton = ServicesSingleton.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_quotation_detail, container, false);

        ServicesSingleton.Quotation quotation = mServicesSingleton.getQuotationArrayAdapter().getQuotation(mQuotationId);

        TextView textView = (TextView) rootView.findViewById(R.id.chatTextView);
        TextView header = (TextView) rootView.findViewById(R.id.headerTextView);

        String productInfo = quotation.toChatString();

        String title;
        try {
            ServicesSingleton.Seller seller = mServicesSingleton.getSellersArrayAdapter().getSeller(quotation.sellerId);
            productInfo += "\n\nSELLER INFO:" + seller.phone + "\n" + seller.email;
            title = seller.nameOfSeller + " from \"" + seller.nameOfShop +"\"";
        } catch (IllegalArgumentException e) {
            title = "INVALID SELLER";
            e.printStackTrace();
        }

        textView.setText(productInfo);
        header.setText(title);

        return rootView;
    }
}
