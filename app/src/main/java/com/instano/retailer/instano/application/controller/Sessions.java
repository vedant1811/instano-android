package com.instano.retailer.instano.application.controller;

import android.content.Context;

import com.facebook.Session;
import com.instano.retailer.instano.activities.home.HomeActivity;
import com.instano.retailer.instano.activities.signUp.SignUpActivity;
import com.instano.retailer.instano.application.network.NetworkRequestsManager;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.model.Buyer;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by Rohit on 5/2/15.
 */
public class Sessions {
    private final String TAG = getClass().getSimpleName();

    private static Sessions sInstance;

//    public Observable<Outlet> findByProduct(int productId) {
//
//    }

    public Observable<Class> doFacebookSignIn(Context context) {
        PublishSubject<Class> subject = PublishSubject.create();
        Observable<Class> observable = subject.doOnSubscribe(() -> {
            Session session = Session.getActiveSession();
            Log.d(TAG, "getActiveSession(): " + session);
            if (session == null) {
                session = Session.openActiveSessionFromCache(context);
                Log.d(TAG, "openActiveSessionFromCache(): " + session);
            }
            if (session == null) {
                subject.onNext(SignUpActivity.class);
                Log.d(TAG, "subject.onNext(SignUpActivity.class)");
            }
            else {
                Observable<Buyer> buyerObservable = signIn();
                Log.d(TAG, "signing in");
                buyerObservable.subscribe(buyer -> {
                            NetworkRequestsManager.instance().newBuyer();
                            subject.onNext(HomeActivity.class);
                        },
                        subject::onError); // just pass the error along
            }
        });
        return observable;
    }

    public void newSignUp(Buyer buyer) {
        Log.d(TAG, "buyer ID: " + buyer);
        Preferences.controller().saveBuyer(buyer);
        NetworkRequestsManager.instance().newBuyer();
    }

    /**
     * tries to sign in if login details are saved
     * @return Buyer observable if login details are saved, null otherwise
     */
    public Observable<Buyer> signIn() {
        String facebookUserId = Preferences.controller().getFacebookUserId();

        Log.v(TAG, "facebook userId: " + facebookUserId);

        if (facebookUserId != null) {
            return NetworkRequestsManager.instance().signIn(facebookUserId);
        }
        else
            return null; // TODO: improve
    }

    public void removeFirstTime() {
        Preferences.controller().removeFirstTime();
    }

    public boolean isFirstTime() {
//        if (BuildConfig.DEBUG)
//            return true;
//        else
        return Preferences.controller().isFirstTime();
    }

    public static Sessions controller() {
        if (sInstance == null)
            sInstance = new Sessions();
        return sInstance;
    }

    private Sessions() {
    }
}
