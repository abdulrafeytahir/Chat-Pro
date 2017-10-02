package com.wartech.chatpro.sync;

import android.content.Context;

import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;


public class ReminderUtilities {

    private static final String TAG = "Chat Notification";

    private static boolean sInitialized;

    private static Context mContext;

    private static FirebaseJobDispatcher dispatcher;

    synchronized public static void scheduleChatReminder(final Context context) {
//        if (sInitialized)
//            return;

        mContext = context;
        scheduleJob();
    }

    private static void scheduleJob() {
        Driver driver = new GooglePlayDriver(mContext);
        dispatcher = new FirebaseJobDispatcher(driver);

        Job constraintReminderJob = dispatcher.newJobBuilder()
                .setService(ChatReminderFirebaseJobService.class)
                .setTag(TAG)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(false)
                .setReplaceCurrent(true)
                .build();

        dispatcher.schedule(constraintReminderJob);

        sInitialized = true;

    }

    synchronized public static void haltJob() {
        dispatcher.cancel(TAG);
    }

}
