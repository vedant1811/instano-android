package com.instano.retailer.instano.deals;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.DataManager;
import com.instano.retailer.instano.utilities.models.Deal;
import com.instano.retailer.instano.utilities.models.Seller;

/**
 * A fragment representing a single Deal detail screen.
 * This fragment is either contained in a {@link DealListActivity}
 * in two-pane mode (on tablets) or a {@link DealDetailActivity}
 * on handsets.
 */
public class DealDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private Deal mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */

    public static DealDetailFragment create(int id) {
        Bundle arguments = new Bundle();
        arguments.putInt(DealDetailFragment.ARG_ITEM_ID, id);
        DealDetailFragment fragment = new DealDetailFragment();
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            int id = getArguments().getInt(ARG_ITEM_ID);
            mItem = DataManager.instance().getDeal(id);
            if (mItem == null)
                throw new IllegalStateException(
                        "Fragment Deal detail created without any Deal. Deal id: " + id);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_deal_detail, container, false);

        final Seller seller = DataManager.instance().getSeller(mItem.sellerId);

        TextView shopNameTextView = (TextView) view.findViewById(R.id.shopNameTextView);
        TextView sellerNameTextView = (TextView) view.findViewById(R.id.sellerNameTextView);
        TextView addressTextView = (TextView) view.findViewById(R.id.addressTextView);
        TextView distanceTextView = (TextView) view.findViewById(R.id.distanceTextView);
        ImageButton callImageButton = (ImageButton) view.findViewById(R.id.callImageButton);
        TextView dealHeadingTextView = (TextView) view.findViewById(R.id.dealHeadingTextView);
        TextView dealSubheadingTextView = (TextView) view.findViewById(R.id.dealSubheadingTextView);
        TextView expiresAtTextView = (TextView) view.findViewById(R.id.expiresAtTextView);

        dealHeadingTextView.setText(mItem.heading);
        dealSubheadingTextView.setText(mItem.subheading);
        expiresAtTextView.setText(mItem.expiresAt());

        // TODO: maybe handle more gracefully
        if (seller == null)
            return view;

        shopNameTextView.setText(seller.nameOfShop);
        sellerNameTextView.setText(seller.nameOfSeller);
        addressTextView.setText(seller.address);
        distanceTextView.setText(seller.getPrettyDistanceFromLocation());
        callImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + seller.phone));
                startActivity(callIntent);
            }
        });

        return view;
    }
}
