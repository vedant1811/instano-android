package com.instano.retailer.instano.application;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.instano.retailer.instano.R;
import com.instano.retailer.instano.activities.SearchableActivity;
import com.instano.retailer.instano.activities.search.ResultsActivity;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.model.Quotation;
import com.instano.retailer.instano.utilities.model.Seller;

import java.io.IOException;

/**
 * Created by ROHIT on 09-Mar-15.
 */
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private static final String TAG = "GcmIntentService";

    public GcmIntentService() {
        super("GcmIntentService");
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
                String type = extras.getString("type");
                Log.v(TAG, "TYPE : " + type);
                if (type != null) {
                    try {
                        ObjectMapper mapper = ServicesSingleton.instance().getDefaultObjectMapper();
                        switch (type) {
                            case "seller":
                                Seller seller = mapper.readValue(extras.getString("seller"), Seller.class);
//                                NetworkRequestsManager.instance().newObject(seller);
                                break;
                            case "quotation":
                                Quotation quotation = mapper.readValue(extras.getString("quotation"), Quotation.class);
//                                NetworkRequestsManager.instance().newObject(quotation);
                                newQuotationsNotification(quotation, extras.getString("product_name"));
                                break;
                        }
                    } catch (IOException e) {
                        Log.fatalError(e);
                    }
                }
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    public void newQuotationsNotification(Quotation quotation, String productName) {
        Log.d(TAG, "new quotations received");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.instano_launcher)
                .setContentTitle("New Quotations")
                .setContentText("Click to view your new quotations")
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL);

        // TODO: make into a quote received and show the details of the new quote
        Intent intent = new Intent(this, ResultsActivity.class);
        intent.putExtra(SearchableActivity.KEY_PRODUCT, productName);
        intent.putExtra(SearchableActivity.KEY_PRODUCT_ID, quotation.productId);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(resultPendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(001, builder.build());
    }

    /**
     * create a notification. Used for debugging only
     */
    private void sendNotification(String msg) {
//        if (!BuildConfig.DEBUG)
//            return;
//        NotificationManager notificationManager = (NotificationManager)
//                this.getSystemService(Context.NOTIFICATION_SERVICE);
//        Log.v(TAG,"sendNotification");
//        PendingIntent contentIntent = PendingIntent.getActivity(
//                this,
//                0,
//                new Intent(this, LauncherActivity.class),
//                0
//        );
//
//        NotificationCompat.Builder builder =
//                new NotificationCompat.Builder(this)
//                        .setSmallIcon(R.drawable.com_facebook_button_like_icon_selected)
//                        .setContentTitle("GCM Notification")
//                        .setStyle(new NotificationCompat.BigTextStyle()
//                                .bigText(msg))
//                        .setContentText(msg);
//
//        builder.setContentIntent(contentIntent);
//        // preserve all notifications
//        notificationManager.notify(new Random().nextInt(), builder.build());
    }
}
