package com.instano.retailer.instano.buyerDashboard.quotes;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.DataManager;
import com.instano.retailer.instano.utilities.models.Quote;

/**
 * A fragment representing a single Quote detail screen.
 * This fragment is either contained in a {@link QuoteListActivity}
 * in two-pane mode (on tablets) or a {@link QuoteDetailActivity}
 * on handsets.
 */
public class QuoteDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_QUOTE_ID = "quote_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private Quote mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public QuoteDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_QUOTE_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = DataManager.instance().getQuote(getArguments().getInt(ARG_QUOTE_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_quote_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.quote_detail)).setText(mItem.searchString);
        }

        return rootView;
    }
}
