package com.example.finalsenior;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Vibrator;
import android.os.VibrationEffect;


import androidx.core.app.NotificationCompat;

import java.util.Set;

public class NotificationUtils {
    public static final String CHANNEL_ID_ALARM   = "SensorAlertsAlarm_v2";
    public static final String CHANNEL_ID_VIBRATE = "SensorAlertsVibrate";
    public static final String CHANNEL_ID_MESSAGE = "SensorAlertsMessage";

    public static final int ALARM_NOTIFICATION_ID = 1001;
    public static final String ACTION_DISMISS = "com.example.finalsenior.ACTION_DISMISS";

    /**
     * @param context  السياق
     * @param title    عنوان الإشعار
     * @param content  محتوى الإشعار
     * @param priority NotificationCompat.PRIORITY_*
     */
    public static void showAlert(Context context, String title, String content, int priority) {
        SharedPreferences prefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        Set<String> styles = prefs.getStringSet("notification_styles", null);

        boolean playSound   = styles != null && styles.contains("sound");
        boolean vibrate     = styles != null && styles.contains("vibration");
        boolean showMessage = styles != null && styles.contains("message");

        // ====== 1) Alarm Mode (sound مع optional vibration + action Off فقط) ======
        if (playSound) {
            NotificationManager nm = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);

            // 1. تهيئة قناة التنبيه (Android O+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // إذا القناة موجودة نعدلها، وإلا ننشئها
                NotificationChannel ch = nm.getNotificationChannel(CHANNEL_ID_ALARM);
                if (ch == null) {
                    ch = new NotificationChannel(
                            CHANNEL_ID_ALARM,
                            "Alarm Alerts",
                            NotificationManager.IMPORTANCE_HIGH
                    );
                }
                ch.setDescription("High-priority alarm alerts");
                ch.enableVibration(true); // force ON
                ch.setVibrationPattern(new long[]{0, 500, 500, 500});
                ch.setSound(
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                        ch.getAudioAttributes()
                );
                ch.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                nm.createNotificationChannel(ch);
            }

            // action “Off” only
            Intent off = new Intent(context, AlarmActionReceiver.class)
                    .setAction(ACTION_DISMISS);
            PendingIntent offPI = PendingIntent.getBroadcast(
                    context, 1, off,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // 2. بناء الإشعار كإشعار عادي مع صوت عالي واهتزاز
            NotificationCompat.Builder b = new NotificationCompat.Builder(context, CHANNEL_ID_ALARM)
                    .setSmallIcon(R.drawable.ic_notifications)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setAutoCancel(true)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(content));

            if (vibrate) {
                // استخدام نمط اهتزاز مخصص يكفل التوافق
                b.setVibrate(new long[]{0, 500, 500, 500});
                // تفعيل الاهتزاز يدويًا لضمانه
                Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, 500, 500, 500}, -1));
                    } else {
                        vibrator.vibrate(new long[]{0, 500, 500, 500}, -1);
                    }
                }
            }
            nm.notify(ALARM_NOTIFICATION_ID, b.build());
            return;
        }

        // ====== 2) لا اهتزاز ولا رسالة => ما نعرض شيء ======
        if (!vibrate && !showMessage) {
            return;
        }

        // ====== 3) إشعارات عادية (اهتزاز فقط أو رسالة فقط) ======
        notifyBasic(context, title, content, vibrate, showMessage, priority);
    }

    private static void notifyBasic(
            Context context,
            String title,
            String content,
            boolean vibrate,
            boolean showMessage,
            int priority
    ) {
        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        boolean isVibeOnly = vibrate && !showMessage;
        String channelId   = isVibeOnly ? CHANNEL_ID_VIBRATE : CHANNEL_ID_MESSAGE;

        // 1. حذف وإعادة إنشاء القناة (Android O+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (nm.getNotificationChannel(channelId) != null) {
                nm.deleteNotificationChannel(channelId);
            }
            NotificationChannel ch = new NotificationChannel(
                    channelId,
                    isVibeOnly ? "Vibrate Alerts" : "Message Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            ch.enableVibration(true);
            ch.setVibrationPattern(new long[]{0, 500, 500, 500});
            ch.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            nm.createNotificationChannel(ch);
        }

        // 2. بناء الإشعار
        NotificationCompat.Builder b = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(priority)
                .setAutoCancel(true);

        if (vibrate) {
            b.setVibrate(new long[]{0, 500, 500, 500});

            // تفعيل الاهتزاز يدويًا لضمانه
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, 500, 500, 500}, -1));
                } else {
                    vibrator.vibrate(new long[]{0, 500, 500, 500}, -1);
                }
            }

        }
        if (showMessage && !vibrate) {
            b.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }
        if (showMessage) {
            b.setStyle(new NotificationCompat.BigTextStyle().bigText(content));
        }

        nm.notify((int) System.currentTimeMillis(), b.build());
    }

}