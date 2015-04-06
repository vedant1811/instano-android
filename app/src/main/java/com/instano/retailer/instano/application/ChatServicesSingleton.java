package com.instano.retailer.instano.application;


import android.os.AsyncTask;

import com.instano.retailer.instano.utilities.library.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.io.IOException;

/**
 * Created by Rohit on 5/4/15.
 */
public class ChatServicesSingleton  {

    private static final String HOST = "192.168.0.137";
    private static final int PORT = 5222;
    private static final String SERVICE = "instano.in";
    private final MyApplication mApplication;
    private static ChatServicesSingleton sInstance;
    private AbstractXMPPConnection mConnection;

    private static final String TAG = "ChatServicesSingleton";


    /*package*/ static void init(MyApplication application) {
        sInstance = new ChatServicesSingleton(application);
    }

    public static ChatServicesSingleton instance() {
        if (sInstance == null)
            throw new IllegalStateException("ChatServicesSingleton.Init() never called");
        return sInstance;
    }

    private ChatServicesSingleton(MyApplication application) {
        mApplication = application;
    }

    public void connectToChat() {

        new AsyncTask<Void,Void,Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                XMPPTCPConnectionConfiguration connConfig = XMPPTCPConnectionConfiguration
                    .builder()
                    .setHost(HOST)
                    .setPort(PORT)
                    .setServiceName(SERVICE)
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                    .setDebuggerEnabled(true)
                    .build();
                AbstractXMPPConnection connection = new XMPPTCPConnection(connConfig);

                try {
                    connection.connect();
                    if (connection.isConnected())
                        mConnection = connection;
                    else mConnection = null;
                    Log.v(TAG, "isConnected  :" + connection.isConnected());
//                    connection.login("user2","user2");
                    Log.v(TAG, "isAuthenticated  :" + connection.isAuthenticated());
                } catch (SmackException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XMPPException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();


    }

    public AbstractXMPPConnection getConnection() {
        return mConnection;
    }


}
