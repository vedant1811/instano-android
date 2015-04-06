package com.instano.retailer.instano.chat;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.ChatServicesSingleton;
import com.instano.retailer.instano.utilities.GlobalMenuActivity;
import com.instano.retailer.instano.utilities.library.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.iqregister.AccountManager;

import java.io.IOException;

/**
 * An activity representing a list of ChatSellers. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
  representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ChatSellerListFragment} and the item details
 * (if present) is a {@link ChatSellerDetailFragment}.
 * <p/>
 * This activity also implements the required
 * to listen for item selections.
 */
public class ChatSellerListActivity extends GlobalMenuActivity
        implements ChatSellerListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    String TAG = "ChatSellerListActivity";
    private boolean mTwoPane;
    private AbstractXMPPConnection mConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatseller_list);

        if (findViewById(R.id.chatseller_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((ChatSellerListFragment) getFragmentManager()
                    .findFragmentById(R.id.fragment_chatseller_list))
                    .setActivateOnItemClick(true);
        }
        else {
            Fragment newFragment = new ChatSellerListFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, newFragment)
                    .commit();
        }

        mConnection = ChatServicesSingleton.instance().getConnection();
        if(mConnection != null && mConnection.isConnected()) {

            AccountManager manager = AccountManager.getInstance(mConnection);
            try {
                mConnection.login("user2", "user2");
                Chat chat;
                ChatManager chatManager = ChatManager.getInstanceFor(mConnection);
                chat = chatManager.createChat("admin@instano.in",new ChatMessageListener() {
                    @Override
                    public void processMessage(Chat chat, Message message) {
                        Log.v(TAG,"Message Rreceived : "+message);
                    }
                });
                chat.sendMessage("Hello2");
            } catch (SmackException.NoResponseException e) {
                e.printStackTrace();
            } catch (XMPPException.XMPPErrorException e) {
                e.printStackTrace();
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SmackException e) {
                e.printStackTrace();
            } catch (XMPPException e) {
                e.printStackTrace();
            }
            Log.v(TAG, "user : " +mConnection.getUser());
            Log.v(TAG, "isAuthenticated : " + mConnection.isAuthenticated());
        }



     // TODO: If exposing deep links into your app, handle intents here.
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mConnection.disconnect();
    }

    @Override
    public void onItemSelected(int id) {
        ChatSellerDetailFragment fragment = ChatSellerDetailFragment.newInstance(id);
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            getFragmentManager().beginTransaction()
                    .replace(R.id.chatseller_detail_container, fragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}
