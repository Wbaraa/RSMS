package com.example.finalsenior;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlarmActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (NotificationUtils.ACTION_DISMISS.equals(action)) {
            // هنا تعمل أي لوجيك لـ “Off”
            Toast.makeText(context, "Alarm dismissed", Toast.LENGTH_SHORT).show();
            // لو بتفتح Activity لإنهاء الإنذار:
            Intent finish = new Intent(context, AlarmActivity.class)
                    .putExtra("finish_now", true)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(finish);
        }

        // شلينا هذا الجزء تماماً:
        // else if (NotificationUtils.ACTION_SNOOZE.equals(action)) { … }
    }
}
