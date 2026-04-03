package com.example.finalsenior;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

public class homeActivity extends BaseActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;
    private ImageView ivProfile;
    private TextView navUserName, navUserEmail;
    private TextView welcomeTextView, subtitleText;

    private SwitchCompat switchLight, switchFan, switchDoor, switchCamera;
    private TextView txtLightStatus, txtFanStatus, txtDoorStatus, txtCameraStatus;

    private TextView txtTemperatureValue, txtHumidityValue, txtFireValue, txtGasValue, txtWaterValue, txtMotionValue;

    private MQTTService mqttService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initializeViews();
        setupToolbarAndDrawer();
        ivProfile.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        setupNavigationListener();
        setupMQTTService();
        loadUserData();
        setupDeviceControlListeners();
    }

    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        ivProfile = findViewById(R.id.ivProfile);

        switchLight = findViewById(R.id.switchLight);
        switchFan = findViewById(R.id.switchFan);
        switchDoor = findViewById(R.id.switchDoor);
        switchCamera = findViewById(R.id.switchCamera);

        txtTemperatureValue = findViewById(R.id.txtTemperatureValue);
        txtHumidityValue = findViewById(R.id.txtHumidityValue);
        txtFireValue = findViewById(R.id.txtFireValue);
        txtGasValue = findViewById(R.id.txtGasValue);
        txtWaterValue = findViewById(R.id.txtWaterValue);
        txtMotionValue = findViewById(R.id.txtMotionValue);

        txtLightStatus = findViewById(R.id.txtLightStatus);
        txtFanStatus = findViewById(R.id.txtFanStatus);
        txtDoorStatus = findViewById(R.id.txtDoorStatus);


        welcomeTextView = findViewById(R.id.textView);
        subtitleText = findViewById(R.id.textView2);

        View headerView = navigationView.getHeaderView(0);
        navUserName = headerView.findViewById(R.id.nav_name);
        navUserEmail = headerView.findViewById(R.id.nav_email);
    }

    private void setupToolbarAndDrawer() {
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_open, R.string.navigation_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void setupNavigationListener() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // Already in home
            } else if (id == R.id.nav_logs) {
                startActivity(new Intent(this, LogsActivity.class));
            } else if (id == R.id.nav_notifications) {
                startActivity(new Intent(this, NotificationsActivity.class));
            } else if (id == R.id.nav_help) {
                startActivity(new Intent(this, HelpActivity.class));
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
            } else if (id == R.id.nav_logout) {
                logoutUser();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MQTTService.LocalBinder binder = (MQTTService.LocalBinder) service;
            mqttService = binder.getService();
            // ✅ أضف هذا السطر لضمان إعادة الاتصال إذا رجع المستخدم للتطبيق
            mqttService.reconnect();  // 🔧 ضروري بعد الرجعة من الخلفية
            mqttService.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.e("MQTT", "Connection lost", cause);
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    String payload = new String(message.getPayload());
                    Log.d("MQTT", "Topic: " + topic + ", Message: " + payload);


                    switch (topic) {
                        case "esp32/dht11":
                            updateSensorData(payload);                    // يعرض البيانات على الشاشة
                            mqttService.handleSensorData(payload);        // 🔥 يفحص الحالات الخطيرة ويظهر الإشعارات
                            break;
                        case "esp32/led":
                            updateSwitchState(switchLight, payload.equalsIgnoreCase("on"));
                            break;
                        case "esp32/fan":
                            updateSwitchState(switchFan, payload.equalsIgnoreCase("on"));
                            break;
                        case "esp32/door":
                            updateSwitchState(switchDoor, payload.equalsIgnoreCase("open"));
                            break;
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {}
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mqttService = null;
        }
    };

    private void setupMQTTService() {
        Intent serviceIntent = new Intent(this, MQTTService.class);
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    private void updateSensorData(String json) {
        try {
            JSONObject obj = new JSONObject(json);

            double temperature = obj.getDouble("temperature");
            double humidity = obj.getDouble("humidity");

            Log.d("MQTT_SENSOR_DATA", "Received JSON: " + json);

            txtTemperatureValue.setText(temperature + " °C");
            txtHumidityValue.setText(humidity + " %");
            txtFireValue.setText(obj.getString("fire_status"));
            txtGasValue.setText(obj.getString("gas_status"));
            txtWaterValue.setText(obj.getString("water_status"));
            txtMotionValue.setText(obj.getString("motion_status"));

            updateSwitchState(switchLight, obj.getString("led_state").equalsIgnoreCase("on"));
            updateSwitchState(switchFan, obj.getString("fan_state").equalsIgnoreCase("on"));
            updateSwitchState(switchDoor, obj.getString("door_state").equalsIgnoreCase("open"));

        } catch (Exception e) {
            Log.e("JSON", "Error parsing sensor data", e);
        }
    }

    private void updateSwitchState(SwitchCompat sw, boolean isOn) {
        runOnUiThread(() -> {
            sw.setOnCheckedChangeListener(null);
            sw.setChecked(isOn);

            // ✅ تحديث النص حسب السويتش
            if (sw == switchLight && txtLightStatus != null)
                txtLightStatus.setText(isOn ? "ON" : "OFF");
            else if (sw == switchFan && txtFanStatus != null)
                txtFanStatus.setText(isOn ? "ON" : "OFF");
            else if (sw == switchDoor && txtDoorStatus != null)
                txtDoorStatus.setText(isOn ? "OPEN" : "CLOSE");

            sw.setOnCheckedChangeListener(deviceControlListener(sw));
        });
    }


    private SwitchCompat.OnCheckedChangeListener deviceControlListener(SwitchCompat sw) {
        return (buttonView, isChecked) -> {
            if (sw == switchLight) {
                mqttService.publishLED(isChecked ? "on" : "off");
            } else if (sw == switchFan) {
                mqttService.publishFan(isChecked ? "on" : "off");
            } else if (sw == switchDoor) {
                mqttService.publishMotor(isChecked ? "open" : "close");
            } else if (sw == switchCamera && isChecked) {
               /* startActivity(new Intent(this, CameraStreamActivity.class));
                sw.setChecked(false);*/
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://172.21.14.8:8088/stream.html"));
                startActivity(browserIntent);
                sw.setChecked(false);

            }
        };
    }

    private void setupDeviceControlListeners() {
        switchLight.setOnCheckedChangeListener(deviceControlListener(switchLight));
        switchFan.setOnCheckedChangeListener(deviceControlListener(switchFan));
        switchDoor.setOnCheckedChangeListener(deviceControlListener(switchDoor));
        switchCamera.setOnCheckedChangeListener(deviceControlListener(switchCamera));
    }

    private void loadUserData() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userEmail = sharedPreferences.getString("email", "user@example.com");
        String userName = sharedPreferences.getString("name", "User");

        welcomeTextView.setText(userName);
        navUserName.setText(userName);
        navUserEmail.setText(userEmail);
    }

    private void logoutUser() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        prefs.edit().clear().apply();

        if (mqttService != null) mqttService.disconnect();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mqttService != null) {
            unbindService(serviceConnection);
            mqttService = null;
        }
    }
}
