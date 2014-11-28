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
public class SearchConstraintsFragment extends Fragment implements MultiSpinner.MultiSpinnerListener {

    public final static String ARG_SEARCH_STRING = "arg_search_string";
    private final int MIN_OF_RANGE_SEEK_BAR = 1;
    private final int MAX_OF_RANGE_SEEK_BAR = 10;

    private EditText mAdditionalInfoEditText;

    private MultiSpinner mBrandsMultiSpinner;
    private TextView mPriceRangeTextView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SearchConstraintsFragment.
     */
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
    public void onResume() {
        super.onResume();
        onCategorySelected(((SearchTabsActivity) getActivity()).getSelectedCategory());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_constraints, container, false);

        mAdditionalInfoEditText = (EditText) view.findViewById(R.id.additionalInfoEditText);
        mBrandsMultiSpinner = (MultiSpinner) view.findViewById(R.id.brandsMultiSpinner);
        mPriceRangeTextView = (TextView) view.findViewById(R.id.priceRangeTextView);

        // setup brands multi spinner:
        final ProductCategories.Category selectedCategory = ((SearchTabsActivity)getActivity()).getSelectedCategory();
        mBrandsMultiSpinner.setItems(selectedCategory.brands, selectedCategory.getSelected(),
                "Select brands", this);
        if (selectedCategory.name.equals(ProductCategories.UNDEFINED)) {
            mBrandsMultiSpinner.setEnabled(false);
        } else {
            mBrandsMultiSpinner.setEnabled(true);
        }

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

    @Override
    public void onItemsSelected(boolean[] selected) {
        ProductCategories.Category selectedCategory = ((SearchTabsActivity)getActivity()).getSelectedCategory();
        selectedCategory.setSelected(selected);
        ServicesSingleton.getInstance(getActivity()).getSellersArrayAdapter().filter(selectedCategory);
    }

    public void onCategorySelected(ProductCategories.Category selectedCategory) {
        if (mBrandsMultiSpinner == null)
            return;
        mBrandsMultiSpinner.setItems(selectedCategory.brands, selectedCategory.getSelected());
        if (selectedCategory.name.equals(ProductCategories.UNDEFINED)) {
            mBrandsMultiSpinner.setEnabled(false);
        } else {
            mBrandsMultiSpinner.setEnabled(true);
        }
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
