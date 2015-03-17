package com.instano.retailer.instano.sellers;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import com.instano.retailer.instano.utilities.library.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.instano.retailer.instano.R;

import java.util.HashSet;

/**
 * A fragment representing a list of Items.
 * <p />
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p />
 */
public class SellersListFragment extends Fragment implements
        SellersArrayAdapter.ItemInteractionListener {

    private static final String PLEASE_SELECT_LOCATION = "please select location";
    /**
     * The fragment's ListView
     */
    private ListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private SellersArrayAdapter mAdapter;

    private RelativeLayout.LayoutParams mDistTextLayoutParams;
//    private Animation mAnimationFadeOut;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SellersListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        mAdapter = ServicesSingleton.getInstance(getActivity()).getSellersArrayAdapter();
//        mAdapter.filter();

//        ServicesSingleton.getInstance(getActivity()).registerCallback(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sellers, container, false);

//        setEmptyText("No sellers nearby. Try relaxing location/categories/brands constraints");

        mAdapter = new SellersArrayAdapter(getActivity());

        // Set the adapter
        mListView = (ListView) view.findViewById(R.id.listView);
        mListView.setAdapter(mAdapter);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        mAdapter.setListener(this);

        return view;
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    // TODO: fix this method
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyText instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    @Override
    public void itemCheckedStateChanged(int selected) {
    }

    @Override
    public void callButtonClicked(String number) {
        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
        startActivity(callIntent);
    }

    public HashSet<Integer> getSellerIds() {
        return mAdapter.getSelectedSellerIds();
    }
}
