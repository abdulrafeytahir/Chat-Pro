package com.wartech.chatpro.sync;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.wartech.chatpro.ChatActivity;
import com.wartech.chatpro.R;


public class NotificationUtils {

    private static final int CHAT_NOTIFICATION_ID = 4214;
    private static final int CHAT_PENDING_INTENT_ID = 5196;
    private static String mPhoneNumber;

    public static void clearAllNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public static void chatReminder(Context context, String name, String phoneNumber, String message) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.logo)
                        .setLargeIcon(largeIcon(context))
                        .setContentTitle(name)
                        .setContentText(message)
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setContentIntent(contentIntent(context))
                        .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mBuilder.setPriority(Notification.PRIORITY_HIGH);
        }
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(CHAT_NOTIFICATION_ID, mBuilder.build());

        mPhoneNumber = phoneNumber;

    }

    private static PendingIntent contentIntent(Context context) {
        Intent startActivityIntent = new Intent(context, ChatActivity.class);
        startActivityIntent.putExtra("notification phone number", mPhoneNumber);

        return PendingIntent.getActivity(
                context,
                CHAT_PENDING_INTENT_ID,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Bitmap largeIcon(Context context) {
        Resources res = context.getResources();
        Bitmap largeIcon = BitmapFactory.decodeResource(res, R.mipmap.logo);
        return largeIcon;
    }
}
