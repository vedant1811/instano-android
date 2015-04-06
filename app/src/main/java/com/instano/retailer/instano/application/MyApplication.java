package com.instano.retailer.instano.application;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.instano.retailer.instano.BuildConfig;
import com.instano.retailer.instano.R;
import com.instano.retailer.instano.application.network.NetworkRequestsManager;
import com.instano.retailer.instano.utilities.library.Log;

import java.util.HashMap;

import rx.plugins.DebugHook;
import rx.plugins.DebugNotification;
import rx.plugins.DebugNotificationListener;
import rx.plugins.RxJavaPlugins;

public class MyApplication extends Application
{
    private HashMap<TrackerName, Tracker> mTrackers;

    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     * Implementations should be as quick as possible (for example using
     * lazy initialization of state) since the time spent in this function
     * directly impacts the performance of starting the first activity,
     * service, or receiver in a process.
     * If you override this method, be sure to call super.onCreate().
     */
    @Override
    public void onCreate() {
        super.onCreate();
        mTrackers = new HashMap<>();

        if (BuildConfig.DEBUG) {
            RxJavaPlugins.getInstance().registerObservableExecutionHook(new DebugHook<>(new DebugNotificationListener<Object>() {
                private static final String TAG = "RxJava";

                public Object onNext(DebugNotification n) {
                    Log.v(TAG, Thread.currentThread() + " onNext on "+n);
                    return super.onNext(n);
                }


                public Object start(DebugNotification n) {
                    Log.v(TAG, Thread.currentThread() + " start on "+n);
                    return super.start(n);
                }


                public void complete(Object context) {
                    super.complete(context);
                    Log.v(TAG, Thread.currentThread() + " onComplete on "+context);
                }

                public void error(Object context, Throwable e) {
                    super.error(context, e);
                    Log.v(TAG, Thread.currentThread() + " error on "+context);
                }
            }));
        }
        // Initialize the singletons so their instances
        // are bound to the application process.
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        Log.init(this);
        NetworkRequestsManager.init(this);
        ServicesSingleton.init(this);
    }

    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
//            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(PROPERTY_ID)
//                    : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.global_tracker)
//                    : analytics.newTracker(R.xml.ecommerce_tracker);
            Tracker tracker = analytics.newTracker(R.xml.app_tracker_config);
            tracker.enableAdvertisingIdCollection(true);
            mTrackers.put(trackerId, tracker);

        }
        return mTrackers.get(trackerId);
    }

    /**
     * Enum used to identify the tracker that needs to be used for tracking.
     *
     * A single tracker is usually enough for most purposes. In case you do need multiple trackers,
     * storing them all in Application object helps ensure that they are created only once per
     * application instance.
     */
    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
//        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
//        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }

}
