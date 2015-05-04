package com.instano.retailer.instano.application;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;

import com.facebook.AppEventsLogger;
import com.instano.retailer.instano.activities.ErrorDialogFragment;
import com.instano.retailer.instano.application.network.NetworkRequestsManager;
import com.instano.retailer.instano.application.network.ResponseError;
import com.instano.retailer.instano.utilities.library.Log;

import rx.Observable;
import rx.Subscription;
import rx.android.observables.AndroidObservable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subscriptions.BooleanSubscription;

/**
 * Created by vedant on 29/12/14.
 */
public abstract class BaseActivity extends Activity {

    private static final String TAG = "BaseActivity";

    protected static final String HOW_DO_YOU_WANT_TO_CONTACT_US = "How do you want to contact us";
    private static final String TEXT_OFFLINE_QUERY = "You can send a query directly by mail";
    private static final String MESSAGE_DIALOG_FRAGMENT = "MessageDialogFragment";


    private Subscription mClickSubscription = BooleanSubscription.create();

    @Override
    protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // To calculate time spent on app
        AppEventsLogger.deactivateApp(this);
    }

    /**
     * binds an observable to this activity.
     * shows a dialog with a retry button if observable throws an error
     * @param observable
     * @param onNext runs as long as @param observable doesn't throw an error
     */
    protected <T> void retryableError(Observable<T> observable, final Action1<? super T> onNext) {
        retryableError(observable, onNext, (throwable) -> false);
    }

    /**
     * binds an observable to this activity.
     * shows a dialog with a retry button if observable throws an error and @param booleanFunc returns false
     * @param observable
     * @param onNext runs as long as @param observable doesn't throw an error
     * @param booleanFunc performs this function on error. return true if error was handled, false if dialog needs to be shown
     */
    protected <T> void retryableError(Observable<T> observable, final Action1<? super T> onNext, Func1<Throwable, Boolean> booleanFunc) {
        AndroidObservable.bindActivity(this, observable)
                .subscribe(
                        next -> {
                            onNext.call(next);
                            cancelDialog();
                        },
                        error -> {
                            // call the function and check whether the error was handled
                            if (booleanFunc.call(error)) {
                                cancelDialog();
                                return; // the callee handled the error, do not do anything
                            }
                            ErrorDialogFragment fragment = showErrorDialog(error);
                            Log.d(TAG, "showProgressBar(false)");
                            fragment.showProgressBar(false);
                            mClickSubscription.unsubscribe();
                            mClickSubscription = fragment.observeTryAgainClicks().subscribe(click -> {
                                Log.d(TAG, "try again click observed");
                                fragment.showProgressBar(true);
                                retryableError(observable.retry(1), onNext, booleanFunc);
                            });
                        });
    }

    private ErrorDialogFragment showErrorDialog(Throwable throwable) {
        Log.d(TAG, "showing error dialog");
        throwable.printStackTrace();
        if (throwable instanceof ResponseError) {
            return contactUs("Trouble connecting", "Please wait we are trying to connect or contact us");
        }
        else if (!NetworkRequestsManager.instance().isOnline())
            return noInternetDialog();
        else
            return contactUs("Server error :(", TEXT_OFFLINE_QUERY);
    }

    protected ErrorDialogFragment noInternetDialog() {
        return contactUs("No internet", TEXT_OFFLINE_QUERY);
    }

    protected ErrorDialogFragment contactUs() {
        return contactUs("Contact us", HOW_DO_YOU_WANT_TO_CONTACT_US);
    }

    protected ErrorDialogFragment contactUs(String heading, String subheading) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(MESSAGE_DIALOG_FRAGMENT);
        if (prev != null) {
            try {
                ErrorDialogFragment dialogFragment = (ErrorDialogFragment) prev;
                if (dialogFragment.isHeadingSameAs(heading)) {
                    Log.d(TAG, "returning old fragment");
                    return dialogFragment; // the same dialog is already showing
                }
            } catch (ClassCastException e) {
                ft.remove(prev);
                Log.fatalError(e);
            }
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = ErrorDialogFragment.newInstance(heading, subheading);
        newFragment.show(ft, MESSAGE_DIALOG_FRAGMENT);
        return (ErrorDialogFragment) newFragment;
    }

    /**
     * idempotent
     */
    protected void cancelDialog() {
        ErrorDialogFragment prev = (ErrorDialogFragment) getFragmentManager().findFragmentByTag(MESSAGE_DIALOG_FRAGMENT);
        if(prev != null) {
            prev.dismiss();
        }
    }

}
