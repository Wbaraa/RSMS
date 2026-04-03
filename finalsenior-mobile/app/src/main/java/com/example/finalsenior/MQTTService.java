package com.example.finalsenior;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.util.Set;

public class MQTTService extends Service {

    private static final String CHANNEL_ID = "SensorAlerts";
    private static final String BROKER_URL = "tcp://172.21.14.8:1883";
    private static final String TOPIC = "esp32/dht11";

    private final String subscribeTopicLED = "esp32/led";
    private final String publishTopicLED = "esp32/led";
    private final String subscribeTopicDoor = "esp32/door";
    private final String publishTopicDoor = "esp32/door";
    private final String subscribeTopicFan = "esp32/fan";
    private final String publishTopicFan = "esp32/fan";

    private MqttClient client;
    private MqttConnectOptions mqttConnectOptions;

    public class LocalBinder extends Binder {
        public MQTTService getService() {
            return MQTTService.this;
        }
    }

    private final IBinder binder = new LocalBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(1, getBaseNotification("Listening for sensor alerts..."));
        connectToBroker();
    }

    private void connectToBroker() {
        try {
            client = new MqttClient(BROKER_URL, MqttClient.generateClientId(), null);
            mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setAutomaticReconnect(true);
            mqttConnectOptions.setCleanSession(false);

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.e("MQTT", "Connection lost", cause);
                    reconnect();
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    handleSensorData(message.toString());
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {}
            });

            client.connect(mqttConnectOptions);
            subscribeToTopics();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reconnect() {
        try {
            if (client != null && !client.isConnected()) {
                client.connect(mqttConnectOptions);
                subscribeToTopics();
                Log.d("MQTT", "✅ Reconnected to MQTT and re-subscribed.");
            }
        } catch (MqttException e) {
            Log.e("MQTT", "❌ MQTT reconnect failed", e);
        }
    }

    private void subscribeToTopics() throws MqttException {
        client.subscribe("esp32/dht11");
        client.subscribe("esp32/led");
        client.subscribe("esp32/fan");
        client.subscribe("esp32/door");
    }

    public void handleSensorData(String data) {
        Log.d("DEBUG_HANDLER", "handleSensorData triggered with: " + data);

        try {
            JSONObject json = new JSONObject(data);

            String fire = json.optString("fire_status", "").toLowerCase();
            String gas = json.optString("gas_status", "").toLowerCase();
            String water = json.optString("water_status", "").toLowerCase();
            String motion = json.optString("motion_status", "").toLowerCase();
            double temp = json.optDouble("temperature", 0);

            Log.d("DEBUG_TEMP", "Received temperature: " + temp);

            if (fire.contains("detected")) {
                saveNotification("Dangerous fire detected.", "fire");
                NotificationUtils.showAlert(getApplicationContext(), "🔥 Fire Detected!", "Dangerous fire detected.", NotificationCompat.PRIORITY_MAX);
            }

            if (gas.contains("detected")) {
                saveNotification("Gas levels are high.", "gas");
                NotificationUtils.showAlert(getApplicationContext(), "🟡 Gas Leak!", "Gas levels are high.", NotificationCompat.PRIORITY_HIGH);
            }

            if (water.contains("detected") || water.contains("leak")) {
                saveNotification("Water detected in server room.", "water");
                NotificationUtils.showAlert(getApplicationContext(), "💧 Water Leak!", "Water detected in server room.", NotificationCompat.PRIORITY_HIGH);
            }

            if (motion.contains("detected")) {
                saveNotification("Movement detected in the room.", "motion");
                NotificationUtils.showAlert(getApplicationContext(), "🚶 Motion Alert", "Movement detected in the room.", NotificationCompat.PRIORITY_DEFAULT);
            }

            if (temp > 30) {
                saveNotification("Temperature exceeded 30°C", "temperature");
                NotificationUtils.showAlert(getApplicationContext(), "🌡️ High Temp", "Temperature exceeded 30°C", NotificationCompat.PRIORITY_MAX);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveNotification(String message, String type) {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("AlertPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        int count = prefs.getInt("alert_count", 0);
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date());

        editor.putString("alert_message_" + count, message);
        editor.putString("alert_time_" + count, timestamp);
        editor.putString("alert_type_" + count, type);
        editor.putInt("alert_count", count + 1);
        editor.apply();

        Log.d("NOTIF_SAVE", "Saved: " + message + " - " + type);
    }

    private android.app.Notification getBaseNotification(String content) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Sensor Monitoring Active")
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Sensor Alerts";
            String description = "Critical sensor notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void publishLED(String payload) {
        publishMessage(publishTopicLED, payload);
    }

    public void publishFan(String payload) {
        publishMessage(publishTopicFan, payload);
    }

    public void publishMotor(String payload) {
        publishMessage(publishTopicDoor, payload);
    }

    public void setCallback(MqttCallback callback) {
        if (client != null) {
            client.setCallback(callback);
        }
    }

    public void disconnect() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void publishMessage(String topic, String payload) {
        try {
            if (client != null && client.isConnected()) {
                client.publish(topic, new MqttMessage(payload.getBytes()));
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
