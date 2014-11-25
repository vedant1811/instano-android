package com.instano.retailer.instano.search;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.ServicesSingleton;
import com.instano.retailer.instano.utilities.MultiSpinner;
import com.instano.retailer.instano.utilities.ProductCategories;
import com.instano.retailer.instano.utilities.RangeSeekBar;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchConstraintsFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class SearchConstraintsFragment extends Fragment {

    public final static String ARG_SEARCH_STRING = "arg_search_string";
    private final int MIN_OF_RANGE_SEEK_BAR = 1;
    private final int MAX_OF_RANGE_SEEK_BAR = 10;

    private TextView mWithinTextView;
//    private SeekBar mWithinSeekBar;
    private EditText mAdditionalInfoEditText;
    private MultiSpinner mBrandsMultiSpinner;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_constraints, container, false);

        mWithinTextView = (TextView) view.findViewById(R.id.withinTextView);
//        mWithinSeekBar = (SeekBar) view.findViewById(R.id.withinSeekBar);
        mAdditionalInfoEditText = (EditText) view.findViewById(R.id.additionalInfoEditText);
        mBrandsMultiSpinner = (MultiSpinner) view.findViewById(R.id.brandsMultiSpinner);
        mPriceRangeTextView = (TextView) view.findViewById(R.id.priceRangeTextView);

//        mWithinSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                mWithinTextView.setText(String.format("Within %dkm:", progress + 1));
//                filter((progress + 1) * 100);
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });

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
                    mPriceRangeTextView.setText(String.format("₹%,d to ₹%,d", minValue, maxValue));
                }
            });
            LinearLayout parent = (LinearLayout) view.findViewById(R.id.priceRangeLinearLayout);

//            float scale = getResources().getDisplayMetrics().density;
//            int dpAsPixels = (int) (132 * scale + 0.5f); // for 16dp padding
//            seekBar.setPadding(0, 0, dpAsPixels, 0);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            parent.addView(seekBar, params);
        }
        return view;
    }

    private void filter(ProductCategories.Category category) {
        ServicesSingleton.getInstance(getActivity()).getSellersArrayAdapter().filter(category);
    }

    private void filter(int minDist) {
        ServicesSingleton.getInstance(getActivity()).getSellersArrayAdapter().filter(minDist);
    }

    public String getBrands() {
        return mAdditionalInfoEditText.getText().toString();
    }

    public String getAdditionalInfo() {
        return mAdditionalInfoEditText.getText().toString();
    }


    public String getPriceRange() {
        return mPriceRangeTextView.getText().toString();
    }

}
