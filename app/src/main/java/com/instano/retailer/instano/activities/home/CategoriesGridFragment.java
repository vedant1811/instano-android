package com.instano.retailer.instano.activities.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.activities.search.ResultsActivity;
import com.instano.retailer.instano.application.network.NetworkRequestsManager;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.model.Category;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;
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
    private CategoriesAdapter mCategoriesAdapter;

    public CategoriesGridFragment() {
        // Required empty public constructor
    }

    @OnItemClick(R.id.gridView)
    public void gridViewItemClicked(int pos) {
        getActivity().startActivity(new Intent(getActivity(), ResultsActivity.class));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories_grid, container, false);
        ButterKnife.inject(this, view);
        mCategoriesAdapter = new CategoriesAdapter(getActivity());
        mGridView.setAdapter(mCategoriesAdapter);
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

    private class CategoriesAdapter extends ArrayAdapter<Category> {

        /**
         * Constructor
         *
         * @param context  The current context.
         */
        public CategoriesAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_1, android.R.id.text1);
            AndroidObservable.bindFragment(CategoriesGridFragment.this, NetworkRequestsManager.instance().getCategories())
                    .subscribe(this::addAll, e -> Log.fatalError(new RuntimeException(e)));
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
//            ViewHolder viewHolder = new ViewHolder(view);
//            viewHolder.textView.setOnClickListener(v -> {
//                getActivity().startActivity(new Intent(getActivity(), ResultsActivity.class));
//            });
            return view;
        }
    }

//    public class ViewHolder {
//        @InjectView(android.R.id.text1) TextView textView;
//
//        public ViewHolder(View view) {
//            ButterKnife.inject(this, view);
//        }
//
//        @Oncl
//    }
}
