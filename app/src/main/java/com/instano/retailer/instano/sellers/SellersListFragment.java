package com.instano.retailer.instano.sellers;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.instano.retailer.instano.R;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sellers, container, false);

//        setEmptyText("No sellers nearby. Try relaxing location/categories/brands constraints");

        SellersActivity activity = (SellersActivity) getActivity();
        SellersArrayAdapter adapter = activity.getAdapter();

        // Set the adapter
        mListView = (ListView) view.findViewById(R.id.listView);
        mListView.setAdapter(adapter);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        adapter.setListener(this);

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
    public void callButtonClicked(String number) {
        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
        startActivity(callIntent);
    }
}
