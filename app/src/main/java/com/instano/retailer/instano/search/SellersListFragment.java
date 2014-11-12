package com.instano.retailer.instano.search;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.SellersArrayAdapter;
import com.instano.retailer.instano.ServicesSingleton;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p />
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p />
 */
public class SellersListFragment extends Fragment implements SellersArrayAdapter.ItemInteractionListener {

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

        return view;
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

    // TODO: implement
    @Override
    public void itemCheckedStateChanged(int pos, boolean checkedState) {
//        if (pos > -1)
//            mListView.setItemChecked(pos, checkedState);
//
//        int checkedItemCount = mListView.getCheckedItemCount();
//        switch (checkedItemCount) {
//            case 0:
//                mHeaderButton.setText("Send to zero Sellers");
//                mHeaderButton.setEnabled(false);
//                break;
//            case 1:
//                mHeaderButton.setText("Send to 1 seller");
//                mHeaderButton.setEnabled(true);
//                break;
//            default:
//                mHeaderButton.setText(String.format("Send to %d sellers", checkedItemCount));
//                mHeaderButton.setEnabled(true);
//        }
    }

    @Override
    public void callButtonClicked(String number) {
        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
        startActivity(callIntent);
    }

    public ArrayList<Integer> getSellerIds() {
        return mAdapter.getSelectedSellerIds();
    }
}
