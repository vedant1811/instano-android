package com.instano.retailer.instano.buyerDashboard.quotes;

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
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.models.Quotation;
import com.instano.retailer.instano.utilities.models.Quote;

import java.util.List;

/**
 * A list fragment representing a list of Quotes. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link QuoteDetailFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class QuoteListFragment extends ListFragment implements DataManager.QuotesListener {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private static final String TAG = "QuotesListFragment";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    @Override
    public void quotesUpdated() {
        long start = System.nanoTime();

        QuotesAdapter adapter = (QuotesAdapter) getListAdapter();
        adapter.clear();
        adapter.addAll(DataManager.instance().getQuotes());

        double time = (System.nanoTime() - start)/ Log.ONE_MILLION;
        Log.d(Log.TIMER_TAG, String.format("QuotesAdapter.dataUpdated took %.4fms", time));
    }

    @Override
    public void quotationsUpdated() {
        QuotesAdapter adapter = (QuotesAdapter) getListAdapter();
        adapter.notifyDataSetChanged();
    }

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

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public QuoteListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        QuotesAdapter adapter = new QuotesAdapter(getActivity());
        setListAdapter(adapter);
        // call after setting the adapter so that the adapter is not null
        quotesUpdated();
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

        setEmptyText("Your searches appear here. Seems you have not searched anything yet" +
                "\n\nuse the menu (top-right) to contact us if this is an error");
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
        Quote quote = (Quote) getListAdapter().getItem(position);
        mCallbacks.onItemSelected(quote.id);
        Log.d(TAG, "Quote clicked, id: " + quote.id);
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

    private class QuotesAdapter extends ArrayAdapter<Quote> {
        private LayoutInflater mInflater;

        /**
         * Constructor
         *
         * @param context  The current context.
         */
        public QuotesAdapter(Context context) {
            super(context, -1);

            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null)
                view = mInflater.inflate(R.layout.list_item_quote, parent, false);

            TextView primaryTextView = (TextView) view.findViewById(R.id.mainTextView);
            TextView timeTextView = (TextView) view.findViewById(R.id.timeTextView);
            TextView responsesTextView = (TextView) view.findViewById(R.id.responsesTextView);
            TextView sentToTextView = (TextView) view.findViewById(R.id.sentToTextView);

            Quote quote = getItem(position);

            primaryTextView.setText(quote.searchString);
            timeTextView.setText(quote.getPrettyTimeElapsed());

            int numResponses = 0;
            List<Quotation> quotations = DataManager.instance().getQuotations();
            for (Quotation quotation : quotations)
                if (quotation.quoteId == quote.id)
                    numResponses++;
            responsesTextView.setText(numResponses + " responses");
            sentToTextView.setText(String.format("sent to %d retailers", quote.sellerIds.size()));

            // to behave as a button i.e. have a "pressed" state
            view.setBackgroundResource(R.drawable.selector_list_item);

            return view;
        }
    }
}
