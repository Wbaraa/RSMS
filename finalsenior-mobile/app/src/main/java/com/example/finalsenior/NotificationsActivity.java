package com.example.finalsenior;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationsAdapter adapter;
    private List<NotificationItem> notificationList;
    private TextView noNotifications;
    private ImageView backButton;
    private AppCompatButton markAllButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        recyclerView = findViewById(R.id.notificationsRecyclerView);
        noNotifications = findViewById(R.id.emptyNotificationsText);
        backButton = findViewById(R.id.backButton);
        markAllButton = findViewById(R.id.markAllReadButton);

        backButton.setOnClickListener(v -> onBackPressed());
        markAllButton.setOnClickListener(v -> {
            clearNotifications();
        });

        notificationList = loadNotificationsFromPrefs();
        adapter = new NotificationsAdapter(this, notificationList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        noNotifications.setVisibility(notificationList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private List<NotificationItem> loadNotificationsFromPrefs() {
        SharedPreferences prefs = getSharedPreferences("AlertPrefs", MODE_PRIVATE);
        List<NotificationItem> list = new ArrayList<>();

        int count = prefs.getInt("alert_count", 0);
        for (int i = 0; i < count; i++) {
            String message = prefs.getString("alert_message_" + i, "");
            String timestamp = prefs.getString("alert_time_" + i, "");
            String type = prefs.getString("alert_type_" + i, "");
            list.add(new NotificationItem(message, timestamp, type));
        }
        return list;
    }

    private void clearNotifications() {
        SharedPreferences prefs = getSharedPreferences("AlertPrefs", MODE_PRIVATE);
        prefs.edit().clear().apply();
        notificationList.clear();
        adapter.notifyDataSetChanged();
        noNotifications.setVisibility(View.VISIBLE);
    }

    public static class NotificationItem {
        private final String message;
        private final String timestamp;
        private final String type;

        public NotificationItem(String message, String timestamp, String type) {
            this.message = message;
            this.timestamp = timestamp;
            this.type = type;
        }

        public String getMessage() { return message; }
        public String getTimestamp() { return timestamp; }
        public String getType() { return type; }
    }
}
