package com.instano.retailer.instano.utilities.library;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.instano.retailer.instano.BuildConfig;
import com.instano.retailer.instano.application.MyApplication;

/**
 * Prints to log in debug mode and sends (only error messages) to GA when not in debug
 * Created by vedant on 17/12/14.
 */
public class Log {

    public static final String TIMER_TAG = "timer";
    public static final double ONE_MILLION = 1000000.0;
    public static final String ADDRESS_UPDATED = "addressUpdated";

    private static Tracker sAppTracker;

    private Log() {

    }

    private static void send(String category, String tag, String msg) {
        sAppTracker.send(new HitBuilders.EventBuilder()
                .setCategory(category + " Log")
                .setLabel(tag)
                .setAction(msg)
                .build());
    }

    /**
     * be sure to call this from your application
     * @param application
     */
    public static void init(MyApplication application) {
        sAppTracker = application.getTracker(MyApplication.TrackerName.APP_TRACKER);
    }

    /**
     * Send a DEBUG log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static void d(String tag, String msg) {
        if (BuildConfig.DEBUG)
            android.util.Log.d(tag, msg);
//        else
//            send("debug", tag, msg);
    }


    /**
     * Send a DEBUG log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static void d(String tag, String msg, Throwable tr) {
        if (BuildConfig.DEBUG)
            android.util.Log.d(tag, msg, tr);
//        else
//            send("debug", tag, msg + '\n' + android.util.Log.getStackTraceString(tr));
    }

    public static void v(String tag, String msg) {
        if (BuildConfig.DEBUG)
            android.util.Log.v(tag, msg);
//        else // do not send verbose messages
//            send("verbose", tag, msg);
    }

    /**
     * Send an ERROR log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static void e(String tag, String msg) {
        if (BuildConfig.DEBUG)
            android.util.Log.e(tag, msg);
        else
            send("error", tag, msg);
    }

    /**
     * Send a ERROR log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static void e(String tag, String msg, Throwable tr) {
        if (BuildConfig.DEBUG)
            android.util.Log.e(tag, msg, tr);
        else
            send("error", tag, msg + '\n' + android.util.Log.getStackTraceString(tr));
    }

    public static void fatalError(Throwable throwable) {
        if (BuildConfig.DEBUG)
            throwable.printStackTrace();
        // TODO:
//        else
//
    }
}
