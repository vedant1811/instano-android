package com.instano.retailer.instano.deals;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.DataManager;
import com.instano.retailer.instano.application.ServicesSingleton;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.models.Deal;
import com.instano.retailer.instano.utilities.models.Seller;

import java.util.HashSet;

/**
 * A list fragment representing a list of Deals. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link DealDetailFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class DealListFragment extends ListFragment implements DataManager.DealsListener {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(int id);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(int id) {
        }
    };

    @Override
    public void dealsUpdated() {
        DealsAdapter adapter = (DealsAdapter) getListAdapter();
        adapter.clear();
        HashSet<Deal> validDeals = new HashSet<Deal>();
        // add only valid deals:
        for(Deal deal : DataManager.instance().getDeals()) {
            Seller seller = DataManager.instance().getSeller(deal.sellerId);
            // can happen if sever feeds wrong data
            if (seller != null && System.currentTimeMillis() < deal.expiresAt) {
                validDeals.add(deal);
            }
            else
                Log.e("dealsUpdated", "no seller for id: " + deal.sellerId);
        }
        adapter.addAll(validDeals);
    }

    @Override
    public void sellersUpdated() {
        DealsAdapter adapter = (DealsAdapter) getListAdapter();
        adapter.notifyDataSetChanged();
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DealListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setListAdapter(new DealsAdapter(getActivity()));
        // call after setting the adapter so that the adapter is not null
        dealsUpdated();
        DataManager.instance().registerListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DataManager.instance().unregisterListener(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        Deal deal = (Deal) getListAdapter().getItem(position);
        mCallbacks.onItemSelected(deal.id);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    private class DealsAdapter extends ArrayAdapter<Deal> {
        private LayoutInflater mInflater;

        public DealsAdapter(Context context) {
            super(context, -1);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null)
                view = mInflater.inflate(R.layout.list_item_deal, parent, false);

            Deal deal = getItem(position);
            Seller seller = DataManager.instance().getSeller(deal.sellerId);

//            if (seller == null || System.currentTimeMillis() >= deal.expiresAt) {
//                throw new IllegalStateException("Invalid deal should have not entered the adapter");
//            }

            TextView headingTextView = (TextView) view.findViewById(R.id.headingTextView);
            TextView subheadingTextView = (TextView) view.findViewById(R.id.subheadingTextView);
            TextView distanceTextView = (TextView) view.findViewById(R.id.distanceTextView);
            TextView expiresAtTextView = (TextView) view.findViewById(R.id.expiresAtTextView);

            headingTextView.setText(deal.heading);
            subheadingTextView.setText(deal.subheading);
            if (seller != null)
                distanceTextView.setText(seller.getPrettyDistanceFromLocation());
            else {
                Log.e("DealsAdapter", "no seller for id: " + deal.sellerId);
                distanceTextView.setVisibility(View.GONE);
            }
            expiresAtTextView.setText("expires " + ServicesSingleton.instance().getPrettyTimeElapsed(deal.expiresAt));

            // to behave as a button i.e. have a "pressed" state
            view.setBackgroundResource(R.drawable.selector_list_item);

            return view;
        }

    }
}
