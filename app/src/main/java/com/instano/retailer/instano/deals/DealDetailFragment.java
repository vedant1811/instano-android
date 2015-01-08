package com.instano.retailer.instano.deals;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.DataManager;
import com.instano.retailer.instano.utilities.models.Deal;

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
        View rootView = inflater.inflate(R.layout.fragment_deal_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.deal_detail)).setText(mItem.heading);
        }

        return rootView;
    }
}
