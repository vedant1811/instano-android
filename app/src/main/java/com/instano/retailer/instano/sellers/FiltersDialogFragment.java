package com.instano.retailer.instano.sellers;


import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.library.Spinner;
import com.instano.retailer.instano.utilities.models.Category;

/**
 * A simple {@link Fragment} subclass.
 */
public class FiltersDialogFragment extends DialogFragment {

    private static final String KEY_CATEGORY = "category";
    private ArrayAdapter<Category> mCategoryAdapter;
    private Spinner mProductCategorySpinner;

    public static FiltersDialogFragment newInstance(Category category) {
        Bundle arguments = new Bundle();
        arguments.putString(KEY_CATEGORY, category.toString());
        FiltersDialogFragment fragment = new FiltersDialogFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    public FiltersDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getDialog().setTitle("Choose a category");

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_filters_dialog, container, false);
        mProductCategorySpinner = (Spinner) view.findViewById(R.id.categoriesSpinner);

        mCategoryAdapter = new ArrayAdapter<Category>(getActivity(),
                android.R.layout.simple_spinner_item);
//        List<Category> categories = DataManager.instance().getProductCategories(true);
//        if (categories != null)
//            mCategoryAdapter.addAll(categories);
        mCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mProductCategorySpinner.setAdapter(mCategoryAdapter);
        mProductCategorySpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id, boolean userSelected) {
                if (userSelected)
                    ((SellersActivity) getActivity()).onCategorySelected(mCategoryAdapter.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        String categoryName = getArguments().getString(KEY_CATEGORY).toLowerCase();
        Log.d(getClass().getSimpleName(), "setting category: " + categoryName);
        for (int i = 0; i < mCategoryAdapter.getCount(); i++)
            if (mCategoryAdapter.getItem(i).matches(categoryName)) {
                mProductCategorySpinner.programmaticallySetPosition(i, true);
                Log.d(getClass().getSimpleName(), "setting position: " + i);
                break;
            }
    }
}
