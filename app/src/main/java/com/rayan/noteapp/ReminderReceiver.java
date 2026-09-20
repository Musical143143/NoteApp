package com.rayan.noteapp;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class ReminderReceiver
        extends BroadcastReceiver {

    private static final String CHANNEL =
            "note_reminders";

    @Override
    public void onReceive(
            Context context,
            Intent intent
    ) {

        NotificationManager manager =
                (NotificationManager)
                        context.getSystemService(
                                Context.NOTIFICATION_SERVICE
                        );

        if (Build.VERSION.SDK_INT >= 26) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL,
                            "Note reminders",
                            NotificationManager
                                    .IMPORTANCE_HIGH
                    );

            manager.createNotificationChannel(
                    channel
            );
        }

        android.app.Notification.Builder builder;

        if (Build.VERSION.SDK_INT >= 26) {

            builder =
                    new android.app.Notification.Builder(
                            context,
                            CHANNEL
                    );

        } else {

            builder =
                    new android.app.Notification.Builder(
                            context
                    );
        }

        builder.setSmallIcon(
                android.R.drawable.ic_menu_edit
        );

        builder.setContentTitle(
                "Notes reminder"
        );

        builder.setContentText(
                "You have a note reminder."
        );

        builder.setAutoCancel(true);

        manager.notify(
                (int)
                        System.currentTimeMillis(),
                builder.build()
        );
    }

    public static void schedule(
            Context context,
            long noteId,
            long time
    ) {

        AlarmManager alarm =
                (AlarmManager)
                        context.getSystemService(
                                Context.ALARM_SERVICE
                        );

        Intent intent =
                new Intent(
                        context,
                        ReminderReceiver.class
                );

        intent.putExtra(
                "noteId",
                noteId
        );

        PendingIntent pending =
                PendingIntent.getBroadcast(
                        context,
                        (int) noteId,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );

        if (Build.VERSION.SDK_INT >= 23) {

            alarm.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    time,
                    pending
            );

        } else {

            alarm.set(
                    AlarmManager.RTC_WAKEUP,
                    time,
                    pending
            );
        }
    }
}
