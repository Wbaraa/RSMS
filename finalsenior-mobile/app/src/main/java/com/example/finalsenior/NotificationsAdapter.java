package com.example.finalsenior;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {

    private Context context;
    private List<NotificationsActivity.NotificationItem> notificationsList;

    public NotificationsAdapter(Context context, List<NotificationsActivity.NotificationItem> notificationsList) {
        this.context = context;
        this.notificationsList = notificationsList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationsActivity.NotificationItem notification = notificationsList.get(position);

        // Set message
        holder.notificationMessage.setText(notification.getMessage());

        // Format and set time
        holder.notificationTime.setText(formatTimeForDisplay(notification.getTimestamp()));

        // Apply alert coloring based on notification type if needed
        applyAlertColoring(holder.itemView, notification.getType(), notification.getMessage());
    }

    private void applyAlertColoring(View itemView, String type, String message) {
        if (type.equalsIgnoreCase("fire")) {
            itemView.setBackgroundColor(context.getResources().getColor(R.color.alert_color));
        } else if (type.equalsIgnoreCase("gas")) {
            itemView.setBackgroundColor(context.getResources().getColor(R.color.warning_color));
        } else if (type.equalsIgnoreCase("water")) {
            itemView.setBackgroundColor(context.getResources().getColor(R.color.cold_color));
        } else if (type.equalsIgnoreCase("temperature") && message.contains("High temperature")) {
            itemView.setBackgroundColor(context.getResources().getColor(R.color.alert_color));
        } else if (type.equalsIgnoreCase("humidity") && message.contains("High humidity")) {
            itemView.setBackgroundColor(context.getResources().getColor(R.color.warning_color));
        } else if (type.equalsIgnoreCase("motion")) {
            itemView.setBackgroundColor(context.getResources().getColor(R.color.purple_light));
        } else {
            itemView.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        }
    }

    private String formatTimeForDisplay(String timestamp) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(timestamp);

            Calendar now = Calendar.getInstance();
            Calendar notificationTime = Calendar.getInstance();
            notificationTime.setTime(date);

            if (isSameDay(now, notificationTime)) {
                return "Today, " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date);
            } else if (isYesterday(now, notificationTime)) {
                return "Yesterday, " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date);
            } else {
                return new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(date);
            }

        } catch (ParseException e) {
            return timestamp;
        }
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isYesterday(Calendar today, Calendar other) {
        Calendar yesterday = (Calendar) today.clone();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        return isSameDay(yesterday, other);
    }

    public void updateNotifications(List<NotificationsActivity.NotificationItem> newNotifications) {
        this.notificationsList = newNotifications;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView notificationMessage;
        TextView notificationTime;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationMessage = itemView.findViewById(R.id.tvNotificationMessage);
            notificationTime = itemView.findViewById(R.id.tvNotificationTime);
        }
    }
}
