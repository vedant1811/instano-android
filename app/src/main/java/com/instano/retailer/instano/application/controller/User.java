package com.instano.retailer.instano.application.controller;

import com.instano.retailer.instano.application.network.NetworkRequestsManager;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.model.Buyer;

import rx.Observable;

/**
 * Created by Rohit on 5/2/15.
 */
public class User {
    private static final String TAG = "User";

    private static User sInstance;

//    public Observable<Outlet> findByProduct(int productId) {
//
//    }

    public void newSignUp(Buyer buyer) {
        Log.d(TAG, "buyer ID: " + buyer);
        SharedPreferencesController.controller().saveBuyer(buyer);
        NetworkRequestsManager.instance().newBuyer();
    }


    /**
     * tries to sign in if login details are saved
     * @return true if login details are saved
     */
    public Observable<Buyer> signIn() {
        String facebookUserId = SharedPreferencesController.controller().getFacebookUserId();

        Log.v(TAG, "facebook userId: " + facebookUserId);

        if (facebookUserId != null) {
            return NetworkRequestsManager.instance().signIn(facebookUserId);
        }
        else
            return null; // TODO: improve
    }

    public void removeFirstTime() {
        SharedPreferencesController.controller().removeFirstTime();
    }

    public boolean isFirstTime() {
//        if (BuildConfig.DEBUG)
//            return true;
//        else
        return SharedPreferencesController.controller().isFirstTime();
    }

    public static User controller() {
        if (sInstance == null)
            sInstance = new User();
        return sInstance;
    }

    private User() {

    }
}
