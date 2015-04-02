package com.instano.retailer.instano.application;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.instano.retailer.instano.R;
import com.instano.retailer.instano.activities.LauncherActivity;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.models.Seller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by ROHIT on 09-Mar-15.
 */
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private static final String TAG = "GcmIntentService";
    private ObjectMapper mJsonObjectMapper;

    public GcmIntentService() {
        super("GcmIntentService");
        mJsonObjectMapper = new ObjectMapper();
        mJsonObjectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
        mJsonObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " +
                        extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // Post notification of received message.
                sendNotification("Received: " + extras.toString());
                Log.v(TAG, "Received: " + extras);
                Log.v(TAG,"TYPE : "+ extras.getString("type"));
                if(extras.getString("type").contains("seller")) {
                    jsonToSeller(extras);
                } else if (extras.getString("type").contains("quote")) {
                    jsonToQuotes(extras);
                } else if(extras.getString("type").contains("quotations")) {
                    jsonToQuotation(extras);
                }
                Log.v(TAG, "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private  void jsonToQuotation(Bundle bundle) {
        String quotation = bundle.getString("quotation");
        Log.v(TAG, "Received quotation: " + quotation);
        try {
            String jsonArrayContent = bundle.getString("quotation");
            JSONArray jsonArray = new JSONArray(jsonArrayContent);
            DataManager.instance().updateQuotes(jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void jsonToQuotes(Bundle bundle) {
        String quotes = bundle.getString("quote");
        Log.v(TAG, "Received Quote : " + quotes);
        try {
            String jsonArrayContent = bundle.getString("quote");
            JSONArray jsonArray = new JSONArray(jsonArrayContent);
            DataManager.instance().updateQuotes(jsonArray);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject explrObject = jsonArray.getJSONObject(i);
                 Log.v(TAG, "Received individual quotes json: " + explrObject);
            }
            Log.v(TAG, "Received quotes json: " + jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.v(TAG, "Quotes object error : " + e);
        }
    }

    private  void  jsonToSeller(Bundle bundle) {
        String seller = bundle.getString("seller");
        Log.v(TAG, "Received Seller: " + seller);
        try {
            mJsonObjectMapper = new ObjectMapper();
//            mJsonObjectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
            mJsonObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            Seller sellerObject = mJsonObjectMapper.readValue(bundle.getString("seller"),Seller.class);
            JSONObject jsonObject = new JSONObject(mJsonObjectMapper.writeValueAsString(sellerObject));
            Log.v(TAG,"Seller object : "+ sellerObject);
            Log.v(TAG,"Seller json : "+ jsonObject);
        } catch (IOException e) {
            e.printStackTrace();
            Log.v(TAG,"Seller object error : "+ e);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.v(TAG,"Seller json error : "+ e);
        }
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        NotificationManager notificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        Log.v(TAG,"sendNotification");
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, LauncherActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.com_facebook_button_like_icon_selected)
                        .setContentTitle("GCM Notification")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
