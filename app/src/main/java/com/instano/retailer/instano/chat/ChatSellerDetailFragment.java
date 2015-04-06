package com.instano.retailer.instano.chat;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.instano.retailer.instano.R;

/**
 * A fragment representing a single ChatSeller detail screen.
 * This fragment is either contained in a {@link ChatSellerListActivity}
 * on handsets.
 */
public class ChatSellerDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    public static ChatSellerDetailFragment newInstance(int id) {
        Bundle arguments = new Bundle();
        arguments.putInt(ChatSellerDetailFragment.ARG_ITEM_ID, id);
        ChatSellerDetailFragment fragment = new ChatSellerDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ChatSellerDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       // if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
        //}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chatseller_detail, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.listView);
        EditText editText = (EditText) rootView.findViewById(R.id.editText);
        Button button = (Button) rootView.findViewById(R.id.button);

        return rootView;
    }
}
