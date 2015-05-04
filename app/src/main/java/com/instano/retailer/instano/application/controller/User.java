package com.instano.retailer.instano.application.controller;

import android.content.SharedPreferences;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.instano.retailer.instano.application.MyApplication;
import com.instano.retailer.instano.application.network.NetworkRequestsManager;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.model.Buyer;

import rx.Observable;

/**
 * Created by vedant on 5/2/15.
 */
public class User {
    private final static String KEY_BUYER_FACEBOOK_USER_ID = "com.instano.retailer.instano.application.ServicesSingleton.buyer_facebook_user_id";
    private final static String KEY_FIRST_TIME = "com.instano.retailer.instano.application.ServicesSingleton.first_time";
    private static final String TAG = "User";

    private static User sInstance;
    private SharedPreferences mSharedPreferences = MyApplication.instance().getSharedPreferences();

//    public Observable<Outlet> findByProduct(int productId) {
//
//    }

    public void newSignUp(Buyer buyer) {
        Log.d(TAG, "buyer ID: " + buyer);
        SharedPreferences.Editor editor = MyApplication.instance().getSharedPreferences().edit();
        editor.putBoolean(KEY_FIRST_TIME, false); // update first time on first login
        Log.d(TAG, "saving buyer facebook userID: " + buyer.getFacebookUser().getId());
        Tracker appTracker = MyApplication.instance().getTracker(MyApplication.TrackerName.APP_TRACKER);
        appTracker.setClientId(buyer.getFacebookUser().getId());
        appTracker.send(new HitBuilders.AppViewBuilder().build());
        editor.putString(KEY_BUYER_FACEBOOK_USER_ID, buyer.getFacebookUser().getId());
        editor.putBoolean(KEY_FIRST_TIME, false); // update first time on first login
        editor.apply();

        NetworkRequestsManager.instance().newBuyer();
    }


    /**
     * tries to sign in if login details are saved
     * @return true if login details are saved
     */
    public Observable<Buyer> signIn() {
        String facebookUserId = mSharedPreferences.getString(KEY_BUYER_FACEBOOK_USER_ID, null);

        Log.v(TAG, "facebook userId: " + String.valueOf(facebookUserId));

        if (facebookUserId != null) {
            return NetworkRequestsManager.instance().signIn(facebookUserId);
        }
        else
            return null; // TODO: improve
    }

    public void removeFirstTime() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(KEY_FIRST_TIME, false); // update first time on first login
        editor.apply();
    }

    public boolean isFirstTime() {
//        if (BuildConfig.DEBUG)
//            return true;
//        else
        return mSharedPreferences.getBoolean(KEY_FIRST_TIME, true);
    }

    public static User controller() {
        if (sInstance == null)
            sInstance = new User();
        return sInstance;
    }

    private User() {

    }
}
