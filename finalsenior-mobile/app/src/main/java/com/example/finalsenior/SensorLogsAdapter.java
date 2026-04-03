package com.example.finalsenior;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SensorLogsAdapter extends RecyclerView.Adapter<SensorLogsAdapter.ViewHolder> {

    private final List<LogsActivity.SensorLogItem> logs;

    public SensorLogsAdapter(List<LogsActivity.SensorLogItem> logs) {
        this.logs = logs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sensor_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LogsActivity.SensorLogItem item = logs.get(position);
        holder.tvSensorType.setText(item.getSensorType());
        holder.tvValue.setText(item.getValue());
        holder.tvTimestamp.setText(item.getTimestamp());
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSensorType, tvValue, tvTimestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSensorType = itemView.findViewById(R.id.tvSensorType);
            tvValue = itemView.findViewById(R.id.tvValue);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}
