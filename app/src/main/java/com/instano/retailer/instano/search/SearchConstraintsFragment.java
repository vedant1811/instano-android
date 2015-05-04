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
import com.instano.retailer.instano.application.ServicesSingleton;
import com.instano.retailer.instano.application.controller.User;
import com.instano.retailer.instano.utilities.library.MultiSpinner;
import com.instano.retailer.instano.utilities.library.RangeSeekBar;
import com.instano.retailer.instano.utilities.model.Categories;
import com.instano.retailer.instano.utilities.model.Category;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchConstraintsFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class SearchConstraintsFragment extends Fragment
        implements MultiSpinner.MultiSpinnerListener, View.OnClickListener {

    private static final int SEEK_BAR_MULTIPLIER = 1000;
    private static final int MIN_OF_RANGE_SEEK_BAR = 0;
    private static final int MAX_OF_RANGE_SEEK_BAR = 100;

    private MultiSpinner mBrandsMultiSpinner;
    private TextView mPriceRangeTextView;
    private ViewFlipper mSearchButtonViewFlipper;
    private View[] mOverlayViews;

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
        if (getView() == null) // view hasn't been created as yet
            return;
        sendingQuote(mSending);
        SearchTabsActivity searchTabsActivity = (SearchTabsActivity) getActivity();
        refreshSelectedCategory(searchTabsActivity.getSelectedCategory(), searchTabsActivity.getSearchString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_constraints, container, false);

        mBrandsMultiSpinner = (MultiSpinner) view.findViewById(R.id.brandsMultiSpinner);
        mPriceRangeTextView = (TextView) view.findViewById(R.id.priceRangeTextView);
        mSearchButtonViewFlipper = (ViewFlipper) view.findViewById(R.id.searchButtonViewFlipper);

        mOverlayViews = new View[3];
        mOverlayViews[0] = view.findViewById(R.id.overlay);
        mOverlayViews[1] = view.findViewById(R.id.overlayTextView);
        mOverlayViews[2] = view.findViewById(R.id.overlayTextView2);

        if (User.controller().isFirstTime()) {
            for(View overlayView : mOverlayViews)
                overlayView.setVisibility(View.VISIBLE);
            mOverlayViews[0].setOnClickListener(this);
        }

        // setup brands multi spinner:
        {
            final Category selectedCategory = ((SearchTabsActivity) getActivity()).getSelectedCategory();
            mBrandsMultiSpinner.setItems(selectedCategory.brands, selectedCategory.getSelected(),
                    "Select brands", this);
            if (selectedCategory.name.equals(Categories.UNDEFINED)) {
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
                    minValue *= SEEK_BAR_MULTIPLIER;
                    maxValue *= SEEK_BAR_MULTIPLIER;
                    mPriceRangeTextView.setText(String.format("₹%,d to ₹%,d", minValue, maxValue));
                }
            });
            FrameLayout parent = (FrameLayout) view.findViewById(R.id.priceRangeSeekBarContainer);

            int px24 = ServicesSingleton.instance().dpToPixels(24);
            int px8 = ServicesSingleton.instance().dpToPixels(8);
            seekBar.setPadding(px24, px8, px24, px8);
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
        // TODO: fadeout.
        for(View view : mOverlayViews)
            view.setVisibility(View.GONE);
    }

    @Override
    public void onItemsSelected(boolean[] selected) {
        Category selectedCategory = ((SearchTabsActivity)getActivity()).getSelectedCategory();
        selectedCategory.setSelected(selected, true);
    }

    public void refreshSelectedCategory(Category selectedCategory, String searchString) {
        if (mBrandsMultiSpinner == null)
            return;
        if (selectedCategory.name.equals(Categories.UNDEFINED)) {
            mBrandsMultiSpinner.setEnabled(false);
        }
        else {
            selectedCategory.guessBrands(searchString);
            mBrandsMultiSpinner.setItems(selectedCategory.brands, selectedCategory.getSelected());
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
