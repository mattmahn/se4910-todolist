package com.mahnke.todolist;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    public AlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(this.getClass().getName(), "Received a notification");

        Intent i = new Intent(context, TodosOverviewActivity.class);

        Notification notification = new Notification.Builder(context)
                .setAutoCancel(true)
                .setContentTitle("Task due")
                .setContentText(intent.getStringExtra(TodoDatabaseHelper.COL_SUMMARY))
                .setContentIntent(PendingIntent.getActivity(
                        context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT))
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(R.string.app_name, notification);
    }
}