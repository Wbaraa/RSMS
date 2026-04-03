package com.example.finalsenior;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class AlarmActivity extends Activity {

    private MediaPlayer mediaPlayer;
    private SeekBar slider;
    private TextView slideText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ إنهاء الـ Activity فوراً إذا تم فتحها من زر "Off" بالإشعار
        boolean shouldFinish = getIntent().getBooleanExtra("finish_now", false);
        if (shouldFinish) {
            finish();
            return;
        }

        setContentView(R.layout.activity_alarm);

        // 1) إظهار الشاشة فوق القفل وتشغيل الشاشة
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        );

        // 2) ضبط صوت الإنذار على أعلى مستوى
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (am != null) {
            int max = am.getStreamMaxVolume(AudioManager.STREAM_ALARM);
            am.setStreamVolume(AudioManager.STREAM_ALARM, max, 0);
        }

        // 3) تشغيل صوت الإنذار
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_sound);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        // 4) تشغيل الاهتزاز
        Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vib != null && vib.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createWaveform(new long[]{0, 500, 500}, 0));
            } else {
                vib.vibrate(new long[]{0, 500, 500}, 0);
            }
        }

        // 5) التحكم بإيقاف الإنذار عبر السلايدر
        slider = findViewById(R.id.sliderSeek);
        slideText = findViewById(R.id.slideText);

        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean u) {
                if (p >= 90) {
                    stopAlarm();
                }
            }

            @Override public void onStartTrackingTouch(SeekBar s) { }

            @Override public void onStopTrackingTouch(SeekBar s) { }
        });
    }

    private void stopAlarm() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        Toast.makeText(this, "Alarm dismissed", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAlarm();
    }
}
