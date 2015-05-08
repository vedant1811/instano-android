package com.instano.retailer.instano.application.controller;

import android.app.Activity;
import android.content.Context;

import com.facebook.Request;
import com.facebook.Session;
import com.facebook.SessionState;
import com.instano.retailer.instano.activities.home.HomeActivity;
import com.instano.retailer.instano.activities.signUp.SignUpActivity;
import com.instano.retailer.instano.application.network.NetworkRequestsManager;
import com.instano.retailer.instano.application.network.ResponseError;
import com.instano.retailer.instano.utilities.library.Log;
import com.instano.retailer.instano.utilities.model.Buyer;
import com.instano.retailer.instano.utilities.models.FacebookUser;

import java.util.Arrays;

import rx.Observable;
import rx.Subscriber;
import rx.android.observables.Assertions;

/**
 * Created by Rohit on 5/2/15.
 */
public class Sessions {
    private final String TAG = getClass().getSimpleName();

    private static Sessions sInstance;
    public static final String[] USER_DATA_FIELDS = new String[]{"user_likes", "user_status", "user_birthday", "email", "public_profile", "user_friends"};

//    public Observable<Outlet> findByProduct(int productId) {
//
//    }

    public Observable<Class> doFacebookSignUp(Activity activity) {
        Observable<Class> observable = Observable.create(subscriber -> {
            Session.openActiveSession(
                    activity, true, Arrays.asList(USER_DATA_FIELDS), new Session.StatusCallback() {
                        private int maxTries = 3;
                        @Override
                        public void call(Session session, SessionState state, Exception exception) {
                            // this callback may be called multiple times
                            if (!state.isOpened()) {
                                Log.d(TAG, "Logged out");
                                if (--maxTries == 0)
                                    subscriber.onError(new RuntimeException("Logged out 3 times"));
                            } else {
                                Log.v(TAG, "Logged in");
                                Sessions.this.meRequest(subscriber, Session.getActiveSession());
                            }
                        }
                    });
        });
        return observable;
    }

    /**
     * creates a me request
     * @param subscriber onNext/onError is called on this
     * @param session to be sent to newMeRequest
     */
    private void meRequest(Subscriber<? super Class> subscriber, Session session) {
        Log.v(TAG, "creating a meRequest, session.isOpened:" + session.isOpened());
        Assertions.assertUiThread();
        Request.newMeRequest(session, (user, response) -> {
            Log.d(TAG, "newMeResponse");
            if (user == null)
                subscriber.onError(new RuntimeException("user is null"));
            else {
                Log.v(TAG, "Response : " + response);

                FacebookUser facebookUser = new FacebookUser();
                facebookUser.setId(user.getId());
                facebookUser.setName(user.getName());
                facebookUser.setEmail(user.getProperty("email").toString());
                facebookUser.setVerified(user.getProperty("verified").toString());
                facebookUser.setUserUpdatedAt(user.getProperty("updated_time").toString());
                facebookUser.setGender(user.getProperty("gender").toString());
                Buyer newBuyer = new Buyer();
                newBuyer.setFacebookUser(facebookUser);

                // TODO:
//                        new Request(Session.getActiveSession(),
//                                "/me/friends",
//                                null,
//                                HttpMethod.GET,
//                                new Request.Callback() {
//                                    public void onCompleted(Response response) {
//                                        /* handle the result */
//                                        Log.v(TAG, "response for friends : " + response);
//                                    }
//                                }
//                        ).executeAsync();


                subscriber.onNext(HomeActivity.class);
                NetworkRequestsManager.instance().registerBuyer(newBuyer).subscribe(
                        buyer -> {
                            Log.d(TAG, "unsubscribed: " + subscriber.isUnsubscribed());
                            // TODO: remove these and return an observable instead from this method
                            newSignUp(newBuyer);
                            subscriber.onNext(HomeActivity.class);
                        },
                        throwable -> {
                            removeFirstTime();
                            subscriber.onError(new RuntimeException(throwable));
                        });
            }
        }).executeAsync(); // Request.newMeRequest
    }

    public Observable<Class> doFacebookSignIn(Context context) {
        Observable<Class> observable = Observable.create(subscriber -> {
            Session session = Session.getActiveSession();
            Log.d(TAG, "getActiveSession(): " + session);
            if (session == null) {
                session = Session.openActiveSessionFromCache(context);
                Log.d(TAG, "openActiveSessionFromCache(): " + session);
            }
            if (session == null) {
                subscriber.onNext(SignUpActivity.class);
                Log.d(TAG, "subject.onNext(SignUpActivity.class)");
            } else {
                Observable<Buyer> buyerObservable = signIn();
                Log.d(TAG, "signing in");
                if (buyerObservable == null)
                    meRequest(subscriber, session);
                else
                    buyerObservable.subscribe(buyer -> {
                                NetworkRequestsManager.instance().newBuyer();
                                subscriber.onNext(HomeActivity.class);
                            },
                            (error) -> {
                                if (ResponseError.Type.INCORRECT_FACEBOOK_ID.is(error))
                                    subscriber.onNext(SignUpActivity.class);
                                else // just pass the error along
                                    subscriber.onError(error);
                            });
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
