package com.instano.retailer.instano.sellers;

import android.app.ListFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.instano.retailer.instano.utilities.library.Log;

import rx.android.observables.AndroidObservable;

/**
 * A fragment representing a list of Items.
 * <p />
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p />
 */
public class SellersListFragment extends ListFragment implements
        SellersArrayAdapter.ItemInteractionListener {

    private static final String PLEASE_SELECT_LOCATION = "please select location";
    /**
     * The fragment's ListView
     */
    private ListView mListView;

    private RelativeLayout.LayoutParams mDistTextLayoutParams;
    private boolean mShown = false;
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
        SellersArrayAdapter adapter = new SellersArrayAdapter(getActivity());
        mShown = false;

        //TODO: create a new observable when using animations
        //TODO: This is emitted after 10ms delay
        AndroidObservable.bindFragment(this, adapter.getFilteredSellersObservable())
                .subscribe(
                        list -> {
                            mShown = true;
                            setListShown(mShown);
                        },
                        throwable -> Log.fatalError(new RuntimeException(throwable)));
        setListAdapter(adapter);
        adapter.setListener(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

       setListShown(mShown);
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
