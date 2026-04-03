package com.example.finalsenior;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LogsActivity extends BaseActivity {
    private MaterialToolbar toolbar;
    private RecyclerView recyclerViewLogs;
    private TextView tvNoLogs;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView backButton;
    private Spinner spinnerSensorFilter;

    private List<SensorLogItem> allLogs = new ArrayList<>();
    private SensorLogsAdapter logsAdapter;

    private Integer user_id ;
    private static final String LOGS_URL = "http://172.21.14.8/senior-php/get_logs_per_user_M.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userIdString = sharedPreferences.getString("user_id", "0"); // Default to "0" if not found
        user_id = Integer.parseInt(userIdString);
        Log.d("LOGSACTIVITY","user_id is:"+user_id);

        initializeViews();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle("");

        backButton.setOnClickListener(v -> onBackPressed());

        recyclerViewLogs.setLayoutManager(new LinearLayoutManager(this));
        logsAdapter = new SensorLogsAdapter(new ArrayList<>());
        recyclerViewLogs.setAdapter(logsAdapter);

        swipeRefreshLayout.setOnRefreshListener(this::fetchLogsFromDatabase);
        fetchLogsFromDatabase();

        // إعداد السبينر
        spinnerSensorFilter = findViewById(R.id.spinnerSensorFilter);
        String[] sensorTypes = {"All", "Temperature", "Humidity", "Gas", "Fire", "Motion", "Water", "Door", "LED", "Fan"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_dropdown_item, sensorTypes);
        spinnerSensorFilter.setAdapter(adapter);

        spinnerSensorFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterLogs(sensorTypes[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        Button openLogsButton = findViewById(R.id.buttonOpenLogsPage);
        openLogsButton.setOnClickListener(v -> {
            Log.d("LOGSACTIVITY-BUTTON","user_id is:"+user_id);
            String url = "http://172.21.14.8/senior-php/get_logs_per_user_M_button.php?user_id="+user_id; // localhost داخل الـ Emulator
            Log.d("LOGSACTIVITY","BUTTON URL IS :"+url);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerViewLogs = findViewById(R.id.recyclerViewLogs);
        tvNoLogs = findViewById(R.id.tvNoLogs);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        backButton = findViewById(R.id.backButton);
    }

    private void fetchLogsFromDatabase() {
        swipeRefreshLayout.setRefreshing(true);
        RequestQueue queue = Volley.newRequestQueue(this);

        // Append user_id to the URL
        Log.d("LOGSACTIVITY-USER_LOGS","user_id is:"+user_id);
        String urlWithUserId = LOGS_URL + "?user_id=" + user_id;

        StringRequest request = new StringRequest(Request.Method.GET, urlWithUserId,
                response -> {
                    try {
                        JSONArray array = new JSONArray(response);
                        allLogs.clear();

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            allLogs.add(new SensorLogItem("Temperature", obj.getString("timestamp"), obj.getString("temperature") + " °C"));
                            allLogs.add(new SensorLogItem("Humidity", obj.getString("timestamp"), obj.getString("humidity") + " %"));
                            allLogs.add(new SensorLogItem("Gas", obj.getString("timestamp"), obj.getString("gas_status")));
                            allLogs.add(new SensorLogItem("Fire", obj.getString("timestamp"), obj.getString("fire_status")));
                            allLogs.add(new SensorLogItem("Motion", obj.getString("timestamp"), obj.getString("motion_status")));
                            allLogs.add(new SensorLogItem("Water", obj.getString("timestamp"), obj.getString("water_leak_status")));
                            allLogs.add(new SensorLogItem("Door", obj.getString("timestamp"), obj.getString("door_state")));
                            allLogs.add(new SensorLogItem("LED", obj.getString("timestamp"), obj.getString("led_state")));
                            allLogs.add(new SensorLogItem("Fan", obj.getString("timestamp"), obj.getString("fan_state")));
                        }

                        Log.d("LOGS_RESPONSE", "Logs loaded: " + allLogs.size());
                        filterLogs(spinnerSensorFilter.getSelectedItem().toString());

                    } catch (Exception e) {
                        tvNoLogs.setText("Error parsing logs.");
                        tvNoLogs.setVisibility(View.VISIBLE);
                        recyclerViewLogs.setVisibility(View.GONE);
                        Log.e("LOGS", "Parse error", e);
                    }
                    swipeRefreshLayout.setRefreshing(false);
                },
                error -> {
                    Toast.makeText(this, "Failed to fetch logs", Toast.LENGTH_SHORT).show();
                    tvNoLogs.setText("Failed to connect.");
                    tvNoLogs.setVisibility(View.VISIBLE);
                    recyclerViewLogs.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    Log.e("LOGS", "Volley error", error);
                });

        queue.add(request);
    }


    private void filterLogs(String type) {
        List<SensorLogItem> filtered = new ArrayList<>();
        if (type.equalsIgnoreCase("All")) {
            filtered = allLogs;
        } else {
            for (SensorLogItem log : allLogs) {
                if (log.getSensorType().equalsIgnoreCase(type)) {
                    filtered.add(log);
                }
            }
        }

        logsAdapter = new SensorLogsAdapter(filtered);
        recyclerViewLogs.setAdapter(logsAdapter);
        logsAdapter.notifyDataSetChanged();

        tvNoLogs.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerViewLogs.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
    }

    public static class SensorLogItem {
        private final String sensorType;
        private final String timestamp;
        private final String value;

        public SensorLogItem(String sensorType, String timestamp, String value) {
            this.sensorType = sensorType;
            this.timestamp = timestamp;
            this.value = value;
        }

        public String getSensorType() { return sensorType; }
        public String getTimestamp() { return timestamp; }
        public String getValue() { return value; }
    }
}
