package com.instano.retailer.instano.utilities;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.instano.retailer.instano.NetworkRequestsManager;
import com.instano.retailer.instano.R;

import java.util.HashMap;

public class MyApplication extends Application
{
    private HashMap<TrackerName, Tracker> mTrackers;

    @Override
    public void onCreate()
    {
        super.onCreate();
        mTrackers = new HashMap<TrackerName, Tracker>();

        // Initialize the singletons so their instances
        // are bound to the application process.
        NetworkRequestsManager.init(this);
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
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
