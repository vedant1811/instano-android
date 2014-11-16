package com.instano.retailer.instano.search;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.ServicesSingleton;
import com.instano.retailer.instano.utilities.MultiSpinner;
import com.instano.retailer.instano.utilities.ProductCategories;
import com.instano.retailer.instano.utilities.RangeSeekBar;

import java.util.ArrayList;


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

    private EditText mSearchStringEditText;
    private TextView mWithinTextView;
    private SeekBar mWithinSeekBar;
    private EditText mAdditionalInfoEditText;
    private Spinner mProductCategorySpinner;
    private MultiSpinner mBrandsMultiSpinner;
    private TextView mPriceRangeTextView;
    private ArrayAdapter<ProductCategories.Category> mCategoryAdapter;

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
        final ServicesSingleton servicesSingleton = ServicesSingleton.getInstance(getActivity());
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_constraints, container, false);

        mSearchStringEditText = (EditText) view.findViewById(R.id.searchStringEditText);
        mWithinTextView = (TextView) view.findViewById(R.id.withinTextView);
        mWithinSeekBar = (SeekBar) view.findViewById(R.id.withinSeekBar);
        mAdditionalInfoEditText = (EditText) view.findViewById(R.id.additionalInfoEditText);
        mProductCategorySpinner = (Spinner) view.findViewById(R.id.productCategorySpinner);
        mBrandsMultiSpinner = (MultiSpinner) view.findViewById(R.id.brandsMultiSpinner);
        mPriceRangeTextView = (TextView) view.findViewById(R.id.priceRangeTextView);

        mSearchStringEditText.setText(getArguments().getString(ARG_SEARCH_STRING));

        mCategoryAdapter = new ArrayAdapter<ProductCategories.Category>(getActivity(),
                android.R.layout.simple_spinner_item);
        ArrayList<ProductCategories.Category> categories = servicesSingleton.getProductCategories();
        if (categories != null)
            mCategoryAdapter.addAll(categories);
        mCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mProductCategorySpinner.setAdapter(mCategoryAdapter);
        mProductCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final ProductCategories.Category category = mCategoryAdapter.getItem(position);
                if (category.name.equals(ProductCategories.UNDEFINED)) {
                    mBrandsMultiSpinner.setEnabled(false);
                } else {
                    mBrandsMultiSpinner.setEnabled(true);
                }
                mBrandsMultiSpinner.setItems(category.brands, category.getSelected(), "Select brands", new MultiSpinner.MultiSpinnerListener() {
                    @Override
                    public void onItemsSelected(boolean[] selected) {
                        category.setSelected(selected);
                        filter(category);
                    }
                });

                filter(category);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO: do something
            }
        });

        mSearchStringEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                guessCategory();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        guessCategory(); // also triggers a mProductCategorySpinner...onItemSelected

        mWithinSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mWithinTextView.setText(String.format("Within %dkm:", progress + 1));
                filter((progress + 1) * 100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

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
            LinearLayout parent = (LinearLayout) view.findViewById(R.id.parentLayout);

            float scale = getResources().getDisplayMetrics().density;
            int dpAsPixels = (int) (16 * scale + 0.5f); // for 16dp padding
            seekBar.setPadding(dpAsPixels, dpAsPixels/2, dpAsPixels, dpAsPixels);

            parent.addView(seekBar);
        }
        return view;
    }

    private void filter(ProductCategories.Category category) {
        ServicesSingleton.getInstance(getActivity()).getSellersArrayAdapter().filter(category);
    }

    private void filter(int minDist) {
        ServicesSingleton.getInstance(getActivity()).getSellersArrayAdapter().filter(minDist);
    }

    /* package private */ void updateProductCategories(ArrayList<ProductCategories.Category> categories) {
        mCategoryAdapter.clear();
        mCategoryAdapter.addAll(categories);
    }

    // TODO: animate in case category is guessed
    private void guessCategory() {
        long start = System.nanoTime();
        String search = mSearchStringEditText.getText().toString().toLowerCase();
        for (int i = 0; i < mCategoryAdapter.getCount(); i++) {
            if (mCategoryAdapter.getItem(i).matches(search)) {
                mProductCategorySpinner.setSelection(i);
                return;
            }
        }
        mProductCategorySpinner.setSelection(0); // setting to undefined
        double time = (System.nanoTime() - start)/1000;
        Log.v("guessCategory", "time = " + time);
    }

    public String getSearchString() {
        String string = mSearchStringEditText.getText().toString();
        if (string.equals("")) {
            mSearchStringEditText.setError("enter something");
            return null;
        }
        return string;
    }

    public String getBrands() {
        return mAdditionalInfoEditText.getText().toString();
    }

    public String getProductCategory() {
        ProductCategories.Category category = (ProductCategories.Category) mProductCategorySpinner.getSelectedItem();
        if (category != null)
            return category.name;
        else
            return ProductCategories.UNDEFINED;
    }

    public String getPriceRange() {
        return mPriceRangeTextView.getText().toString();
    }

}
