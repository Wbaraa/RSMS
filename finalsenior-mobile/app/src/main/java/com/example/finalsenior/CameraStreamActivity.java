// ✅ CameraStreamActivity.java باستخدام VLC SDK

package com.example.finalsenior;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.util.ArrayList;

public class CameraStreamActivity extends AppCompatActivity {

    private LibVLC libVLC;
    private MediaPlayer mediaPlayer;
    private VLCVideoLayout videoLayout;
    private Button btnBack;

    private static final String STREAM_URL = "http://172.21.14.8:8088/stream.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_stream);

        videoLayout = findViewById(R.id.vlc_video_layout);
        btnBack = findViewById(R.id.btnBack);

        // إعداد LibVLC و MediaPlayer
        ArrayList<String> options = new ArrayList<>();
        libVLC = new LibVLC(this, options);
        mediaPlayer = new MediaPlayer(libVLC);

        mediaPlayer.attachViews(videoLayout, null, false, false);
        Media media = new Media(libVLC, STREAM_URL);
        media.setHWDecoderEnabled(true, false);
        media.addOption("--network-caching=150");
        mediaPlayer.setMedia(media);
        media.release();

        mediaPlayer.play();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.detachViews();
            mediaPlayer.release();
            libVLC.release();
        }
    }
}