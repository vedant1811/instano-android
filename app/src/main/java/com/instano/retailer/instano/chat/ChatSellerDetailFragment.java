package com.instano.retailer.instano.chat;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.ChatServicesSingleton;
import com.instano.retailer.instano.utilities.library.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;

/**
 * A fragment representing a single ChatSeller detail screen.
 * This fragment is either contained in a {@link ChatSellerListActivity}
 * on handsets.
 */
public class ChatSellerDetailFragment extends Fragment implements ChatManagerListener{
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_SELLER_ID = "item_id";
    private static final String TAG = "ChatSellerDetailFragment";
    ListView listView;
    EditText editText;
    Button button;
    private Chat mChat;
    private ChatMessageAdapter mChatMessageAdapter;
    private AbstractXMPPConnection mConnection;

    public static ChatSellerDetailFragment newInstance(int sellerId) {
        Bundle arguments = new Bundle();
        arguments.putInt(ChatSellerDetailFragment.ARG_SELLER_ID, sellerId);
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
        // if (getArguments().containsKey(ARG_SELLER_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
        //}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chatseller_detail, container, false);
        listView = (ListView) rootView.findViewById(R.id.listView);
        editText = (EditText) rootView.findViewById(R.id.editText);
        button = (Button) rootView.findViewById(R.id.button);
        editText.requestFocus();

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mChatMessageAdapter = new ChatMessageAdapter(getActivity());
        listView.setAdapter(mChatMessageAdapter);
        mConnection = ChatServicesSingleton.instance().getConnection();
        ChatManager.getInstanceFor(mConnection).addChatListener(this);
//        if(mConnection == null) {
//            editText.setError("Not Connected");
//            return;
//        }
        ChatManager chatManager = ChatManager.getInstanceFor(mConnection);

        mChat = chatManager.createChat("admin@instano.in/Spark 2.6.3");
        Log.v(TAG,"chat created");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Message message= new Message();
                    message.setBody(editText.getText().toString());
                    mChat.sendMessage(message);
                    mChatMessageAdapter.add(message);
                    editText.setText(null);
                } catch (SmackException.NotConnectedException e) {
                    editText.setError("Not connected");
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void chatCreated(Chat chat, boolean createdLocally) {
        Log.v(TAG, "chatCreated");
        chat.addMessageListener(new ChatMessageListener() {
            @Override
            public void processMessage(Chat chat, Message message) {
                Log.v(TAG, "Message Received : " + message);
                Log.v(TAG,"From:" + message.getFrom());
                final Message msg = message;
/*                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mChatMessageAdapter.add(msg);
                    }
                });
 */
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mChatMessageAdapter.add(msg);
                        Log.v(TAG, "msg inside handler : " + msg);
                    }
                });
            }
        });
    }

    private class ChatMessageAdapter extends ArrayAdapter<Message> {

        private static final int SENDER_MESSAGE = 0;
        private static final int RECEVIER_MESSAGE = 1;
        private LayoutInflater mInflater;

        public ChatMessageAdapter(Context context) {
            super(context, -1);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                int resource;
                switch (getItemViewType(position)) {
                    case SENDER_MESSAGE:
                        resource = R.layout.list_item_sender_msg;
                        break;
                    case RECEVIER_MESSAGE:
                        resource = R.layout.list_item_receiver_msg;
                        break;
                    default:
                        throw new IllegalArgumentException();
                }
                view = mInflater.inflate(resource, parent, false);
            }

            TextView textView = (TextView) view.findViewById(R.id.message);
            Message message = getItem(position);
            textView.setText(message.getBody());

            return view;
        }

        @Override
        public int getItemViewType(int position) {
            Message message = getItem(position);
//            if (equals(message.getTo())
            if (mConnection.getUser().equals(message.getTo())) {
                return RECEVIER_MESSAGE;
            } else {
                return SENDER_MESSAGE;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }
    }
}
