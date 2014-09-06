package com.instano.retailer.instano;

import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchingFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class SearchingFragment extends Fragment {

    private FetchQuotations mFetchQuotations;
    /**
     * Factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param searchString the string that will be appended to the textView
     * @return A new instance of fragment SearchingFragment.
     */
    public static SearchingFragment newInstance(String searchString) {
        SearchingFragment fragment = new SearchingFragment();
        Bundle args = new Bundle();
        args.putString(PurchaseActivity.SEARCH_STRING, searchString);
        fragment.setArguments(args);
        return fragment;
    }
    public SearchingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_searching, container, false);
        // append the searchString
        String searchString = getArguments().getString(PurchaseActivity.SEARCH_STRING);
        TextView searchingFor_TextView = (TextView) rootView.findViewById(R.id.searchingForTextView);
        searchingFor_TextView.append(searchString);

        Log.d("SearchingFrament.onCreateView", "creating quotations");

        mFetchQuotations = new FetchQuotations(getActivity());

        mFetchQuotations.runQuery(searchString);

        return rootView;
    }
}
