package com.instano.retailer.instano.search;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.ServicesSingleton;
import com.instano.retailer.instano.utilities.library.MultiSpinner;
import com.instano.retailer.instano.utilities.library.RangeSeekBar;
import com.instano.retailer.instano.utilities.models.ProductCategories;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchConstraintsFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class SearchConstraintsFragment extends Fragment
        implements MultiSpinner.MultiSpinnerListener, View.OnClickListener {

    private final int MIN_OF_RANGE_SEEK_BAR = 1;
    private final int MAX_OF_RANGE_SEEK_BAR = 10;

    private MultiSpinner mBrandsMultiSpinner;
    private TextView mPriceRangeTextView;
    private ViewFlipper mSearchButtonViewFlipper;
    private View[] overlayViews;

    private boolean mSending;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SearchConstraintsFragment.
     */
    public static SearchConstraintsFragment newInstance() {
        return new SearchConstraintsFragment();
    }
    public SearchConstraintsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        onCategorySelected(((SearchTabsActivity) getActivity()).getSelectedCategory());
        sendingQuote(mSending);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_constraints, container, false);

        mBrandsMultiSpinner = (MultiSpinner) view.findViewById(R.id.brandsMultiSpinner);
        mPriceRangeTextView = (TextView) view.findViewById(R.id.priceRangeTextView);
        mSearchButtonViewFlipper = (ViewFlipper) view.findViewById(R.id.searchButtonViewFlipper);

        overlayViews = new View[3];
        overlayViews[0] = view.findViewById(R.id.overlay);
        overlayViews[1] = view.findViewById(R.id.overlayTextView);
        overlayViews[2] = view.findViewById(R.id.overlayTextView2);

        overlayViews[0].setOnClickListener(this);

        // setup brands multi spinner:
        {
            final ProductCategories.Category selectedCategory = ((SearchTabsActivity) getActivity()).getSelectedCategory();
            mBrandsMultiSpinner.setItems(selectedCategory.brands, selectedCategory.getSelected(),
                    "Select brands", this);
            if (selectedCategory.name.equals(ProductCategories.UNDEFINED)) {
                mBrandsMultiSpinner.setEnabled(false);
            } else {
                mBrandsMultiSpinner.setEnabled(true);
            }
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
            FrameLayout parent = (FrameLayout) view.findViewById(R.id.priceRangeSeekBarContainer);

//            float scale = getResources().getDisplayMetrics().density;
//            int dpAsPixels = (int) (132 * scale + 0.5f); // for 16dp padding
//            seekBar.setPadding(0, 0, dpAsPixels, 0);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            parent.addView(seekBar, params);
        }
        return view;
    }

    /**
     * Overlay clicked
     */
    @Override
    public void onClick(View v) {
        // TODO: fadeout
        for(View view : overlayViews)
            view.setVisibility(View.GONE);
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

    public String getPriceRange() {
        return mPriceRangeTextView.getText().toString();
    }

    public void sendingQuote(boolean isSending) {
        mSending = isSending;
        if (isSending)
            mSearchButtonViewFlipper.setDisplayedChild(1); // progress bar
        else
            mSearchButtonViewFlipper.setDisplayedChild(0); // button
    }
}
