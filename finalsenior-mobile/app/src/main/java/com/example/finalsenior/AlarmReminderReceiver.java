package com.example.finalsenior;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

public class AlarmReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String title   = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");

        NotificationUtils.showAlert(
                context,
                title   != null ? title   : "Alarm",
                content != null ? content : "Reminder",
                NotificationCompat.PRIORITY_MAX
        );
    }
}
