package com.instano.retailer.instano;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchConstraintsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SearchConstraintsFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class SearchConstraintsFragment extends Fragment {

    public final static String ARG_SEARCH_STRING = "arg_search_string";
    private final int MIN_OF_RANGE_SEEK_BAR = 1;
    private final int MAX_OF_RANGE_SEEK_BAR = 10;

    private EditText mSearchStringEditText;
    private OnFragmentInteractionListener mListener;
    private EditText mAdditionalInfoEditText;
    private Spinner mProductCategorySpinner;
    private TextView mPriceRangeTextView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SearchConstraintsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchConstraintsFragment newInstance(String searchString) {
        SearchConstraintsFragment fragment = new SearchConstraintsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SEARCH_STRING, searchString);
        fragment.setArguments(args);
        return fragment;
    }
    public SearchConstraintsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ServicesSingleton servicesSingleton = ServicesSingleton.getInstance(getActivity());
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_constraints, container, false);

        mSearchStringEditText = (EditText) view.findViewById(R.id.searchStringEditText);
        mAdditionalInfoEditText = (EditText) view.findViewById(R.id.additionalInfoEditText);
        mProductCategorySpinner = (Spinner) view.findViewById(R.id.productCategorySpinner);
        mPriceRangeTextView = (TextView) view.findViewById(R.id.priceRangeTextView);

        mSearchStringEditText.setText(getArguments().getString(ARG_SEARCH_STRING));


        mProductCategorySpinner = (Spinner) view.findViewById(R.id.productCategorySpinner);

        ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.addAll(servicesSingleton.getProductCategories());

        mProductCategorySpinner.setAdapter(adapter);

        // setup range seek bar:
        {
            RangeSeekBar<Integer> seekBar = new RangeSeekBar<Integer>(
                    MIN_OF_RANGE_SEEK_BAR, MAX_OF_RANGE_SEEK_BAR, getActivity());
            seekBar.setNotifyWhileDragging(true);
            seekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
                @Override
                public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
                    minValue *= 5000;
                    maxValue *= 5000;
                    mPriceRangeTextView.setText(String.format("₹ %,d to ₹ %,d", minValue, maxValue));
                }
            });
            RelativeLayout parent = (RelativeLayout) view.findViewById(R.id.parentRelativeLayout);

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.BELOW, mPriceRangeTextView.getId());

            float scale = getResources().getDisplayMetrics().density;
            int dpAsPixels = (int) (8 * scale + 0.5f); // for 8dp padding
            seekBar.setPadding(dpAsPixels, dpAsPixels/2, dpAsPixels, dpAsPixels);

            parent.addView(seekBar, layoutParams);
        }
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onSearchConstraintsFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onSearchConstraintsFragmentInteraction(Uri uri);
    }

}
