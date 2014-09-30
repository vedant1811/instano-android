package com.instano.retailer.instano;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

/**
 * A fragment representing a list of Items.
 * <p />
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p />
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class SellersListFragment extends Fragment implements AbsListView.OnItemClickListener,
        SellersArrayAdapter.ItemCheckedStateChangedListener {

    private OnFragmentInteractionListener mListener;

    /**
     * The fragment's ListView
     */
    private ListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private SellersArrayAdapter mAdapter;
    private View mHeader;
    private Button mHeaderButton;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SellersListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = ServicesSingleton.getInstance(getActivity()).getSellersArrayAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sellers, container, false);

        mHeader = inflater.inflate(R.layout.header_search, null, false);
        mHeaderButton = (Button) mHeader.findViewById(R.id.searchButton);

        // Set the adapter
        mListView = (ListView) view.findViewById(R.id.listView);
        mListView.addHeaderView(mHeader);
        mListView.setAdapter(mAdapter);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        mAdapter.setListener(this);
        mAdapter.getFilter().filter("1000"); // so that items are displayed

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        return view;
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


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onSellersListFragmentInteraction((int) id);
        }
    }

    public void searchButtonClicked(View view) {

    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyText instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    @Override
    public void itemStateChanged(int pos, boolean checkedState) {
        if (pos > -1)
            mListView.setItemChecked(pos, checkedState);

        int checkedItemCount = mListView.getCheckedItemCount();
        switch (checkedItemCount) {
            case 0:
                mHeaderButton.setText("Send to zero Sellers");
                mHeaderButton.setEnabled(false);
                break;
            case 1:
                mHeaderButton.setText("Send to 1 seller");
                mHeaderButton.setEnabled(true);
                break;
            default:
                mHeaderButton.setText(String.format("Send to %d sellers", checkedItemCount));
                mHeaderButton.setEnabled(true);
        }
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
        public void onSellersListFragmentInteraction(int sellerId);
    }
}
