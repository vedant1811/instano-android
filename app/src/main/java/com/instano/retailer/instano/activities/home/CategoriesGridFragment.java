package com.instano.retailer.instano.activities.home;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.network.NetworkRequestsManager;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.model.Category;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.observables.AndroidObservable;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CategoriesGridFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class CategoriesGridFragment extends Fragment {
    public static final String TAG = "CategoriesGridFragment";

    @InjectView(R.id.gridView) GridView mGridView;

    private OnFragmentInteractionListener mListener;
    private ArrayAdapter<Category> mCategoryAdapter;

    public CategoriesGridFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories_grid, container, false);
        ButterKnife.inject(this, view);
        mCategoryAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                android.R.id.text1
        );
        mGridView.setAdapter(mCategoryAdapter);
        AndroidObservable.bindFragment(this, NetworkRequestsManager.instance().getCategories())
                .subscribe(mCategoryAdapter::addAll, e -> Log.fatalError(new RuntimeException(e)));
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
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
        public void onFragmentInteraction(Uri uri);
    }

}
