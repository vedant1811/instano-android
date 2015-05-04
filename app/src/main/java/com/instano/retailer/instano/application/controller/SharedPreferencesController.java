package com.instano.retailer.instano.application.controller;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.instano.retailer.instano.application.MyApplication;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.model.Buyer;

/**
 * Created by Rohit on 5/2/15.
 */
public class SharedPreferencesController {
    private static final String TAG = "SharedPreferencesController";
    private static SharedPreferencesController sInstance;
    private SharedPreferences mSharedPreferences ;

    private final static String KEY_BUYER_FACEBOOK_USER_ID = "buyer_facebook_user_id";
    private final static String KEY_FIRST_TIME = "first_time";
    private final static String KEY_BUYER_NAME = "buyer_name";

//    public Observable<Outlet> findByProduct(int productId) {
//
//    }

    public static SharedPreferencesController controller() {
        if (sInstance == null)
            sInstance = new SharedPreferencesController();
        return sInstance;
    }

    public void saveBuyer(Buyer buyer) {
        Log.d(TAG, "saving buyer facebook userID: " + buyer.getFacebookUser().getId());
        Tracker appTracker = MyApplication.instance().getTracker(MyApplication.TrackerName.APP_TRACKER);
        appTracker.setClientId(buyer.getFacebookUser().getId());
        appTracker.send(new HitBuilders.AppViewBuilder().build());

        SharedPreferences.Editor editor = MyApplication.instance().getSharedPreferences().edit();
        editor.putString(KEY_BUYER_FACEBOOK_USER_ID, buyer.getFacebookUser().getId());
        editor.putBoolean(KEY_FIRST_TIME, false); // update first time on first login
        editor.putString(KEY_BUYER_NAME, buyer.getFacebookUser().getName());
        editor.apply();
    }

    @Nullable
    public String getFacebookUserId() {
        return mSharedPreferences.getString(KEY_BUYER_FACEBOOK_USER_ID, null);
    }

    @Nullable
    public String getBuyerName() {
        return mSharedPreferences.getString(KEY_BUYER_NAME, null);
    }

    public void removeFirstTime() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(KEY_FIRST_TIME, false); // update first time on first login
        editor.apply();
    }

    public boolean isFirstTime() {
        return mSharedPreferences.getBoolean(KEY_FIRST_TIME, true);
    }

    private SharedPreferencesController() {
        mSharedPreferences = MyApplication.instance().getSharedPreferences();
    }
}
