package com.instano.retailer.instano.activities.home;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.activities.BookingDialogFragment;
import com.instano.retailer.instano.activities.SellerDetailActivity;
import com.instano.retailer.instano.application.controller.Sellers;
import com.instano.retailer.instano.application.network.NetworkRequestsManager;
import com.instano.retailer.instano.deals.DealDetailFragment;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.model.Deal;
import com.instano.retailer.instano.utilities.model.Seller;
import com.squareup.picasso.Picasso;

import rx.Observable;
import com.instano.retailer.instano.utilities.model.Quotation;
import com.instano.retailer.instano.utilities.model.Seller;
import com.squareup.picasso.Picasso;

import java.util.zip.Inflater;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.android.observables.AndroidObservable;

/**
 * A list fragment representing a list of Deals. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link DealDetailFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class DealListFragment extends ListFragment{

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private static final String TAG = "DealListFragment";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;
    private boolean mShown = false;

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
    public DealListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        mShown = false;
        DealsAdapter dealsAdapter = new DealsAdapter(getActivity());
        setListAdapter(dealsAdapter);
        AndroidObservable.bindFragment(this, NetworkRequestsManager.instance().getDeals())
                .subscribe((deal) -> {
                    setShown(true);
                    Log.d(TAG, "new deal:" + deal.id);
                    dealsAdapter.add(deal);
//                    adapter.sort((lhs, rhs) -> lhs.compareTo(rhs));
                }, throwable -> Log.fatalError(new RuntimeException(
                        "error response in subscribe to getObservable(Quote.class)",
                        throwable)));
        setListAdapter(dealsAdapter);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setListShown(mShown);
        // Restore the previously serialized activated item position.
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION))
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
    }

    private void setShown(boolean shown) {
        mShown = shown;
        if (getView() != null)
            setListShown(mShown);
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
        Log.v(TAG,"clicked ::::::::::::::::::::::::::");
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

    public class DealsAdapter extends ArrayAdapter<Deal> {
        private LayoutInflater mInflater;

        public DealsAdapter(Context context) {
            super(context, -1);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_googlecard, parent, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            }

            else
                viewHolder = (ViewHolder) convertView.getTag();

            Deal deal = getItem(position);
            Log.v(TAG,"heading : "+deal.heading+" subheading : "+deal.subheading);
            viewHolder.headingTextView.setText(deal.heading);

            if (TextUtils.isEmpty(deal.subheading))
                viewHolder.subheadingTextView.setVisibility(View.GONE);
            else
                viewHolder.subheadingTextView.setText(deal.subheading);



            if (deal.product != null) {
                Picasso.with(getContext())
                        .load(deal.product.image).fit().centerInside()
                        .placeholder(R.drawable.no_image_available).fit().centerInside()
                        .error(R.drawable.instano_launcher)
                        .into(viewHolder.productImage);
            }

            viewHolder.bookitButton.setOnClickListener(v1 -> {
                FragmentManager fm = getFragmentManager();
                BookingDialogFragment bookingDialogFragment = BookingDialogFragment.newInstance(deal,deal.product.image);
                bookingDialogFragment.show(fm, "Book it Dialog");
            });

            viewHolder.productImage.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putInt("seller_id", deal.sellerId);
                bundle.putString("heading", deal.heading);
                bundle.putString("subheading", deal.subheading);
                Intent intent = new Intent(getActivity(), SellerDetailActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                //TODO: Send Deal details and fetch seller in SellerDeatailActivity through the sellerId in Deal
            });

            viewHolder.msgButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Sellers.controller().getSeller(deal.sellerId).subscribe(seller -> {
                                Intent msgIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" +
                                        seller.outlets.get(0).getPhone()));
                                msgIntent.putExtra("sms_body",deal.heading + "\n" + deal.subheading);
                                startActivity(msgIntent);
                            },
                            error -> Log.fatalError(new RuntimeException(error)));

                }
            });

            viewHolder.contactButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Sellers.controller().getSeller(deal.sellerId).subscribe(seller -> {
                                Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" +
                                        seller.outlets.get(0).getPhone()));
                                startActivity(callIntent);
                    },
                    error -> Log.fatalError(new RuntimeException(error)));

                }
            });

            return convertView;
        }
    }

    public class ViewHolder{
        @InjectView(R.id.dealProduct) ImageButton productImage ;
        @InjectView(R.id.dealHeading) TextView headingTextView;
        @InjectView(R.id.dealSubheading) TextView subheadingTextView;
        @InjectView(R.id.msgButton) ImageButton msgButton;
        @InjectView(R.id.contactButton)ImageButton contactButton;
        @InjectView(R.id.bookitButton)Button bookitButton;

        public ViewHolder(View view){
            ButterKnife.inject(this, view);
        }

    }
}

