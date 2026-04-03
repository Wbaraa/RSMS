#include <WiFi.h>
#include <PubSubClient.h>
#include <DHT.h>
#include <ESP32Servo.h>
#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>
#include <SPI.h>
#include <HTTPClient.h>
#include <MFRC522.h>

// ========================
// WiFi and MQTT Settings
// ========================
const char* ssid = "Guest";
const char* password = "LIU@guest2025";
const char* mqtt_server = "172.21.14.8";
const int mqtt_port = 1883;

// MQTT Topics
const char* topic_led    = "esp32/led";
const char* topic_door   = "esp32/door";
const char* topic_sensor = "esp32/dht11";
const char* topic_fan    = "esp32/fan";

WiFiClient espClient;
PubSubClient client(espClient);

// ========================
// DHT11 Sensor Setup
// ========================
#define DHTPIN 15
#define DHTTYPE DHT11
DHT dht(DHTPIN, DHTTYPE);

// ========================
// RFID Setup
// ========================
#define SS_PIN 5
#define RST_PIN 14
MFRC522 rfid(SS_PIN, RST_PIN);
#define BUZZER_RFID_PIN 2
byte authorizedUID[] = {0x60, 0x9A, 0x64, 0x55};

// ========================
// Sensor Pins
// ========================
#define FIRE_SENSOR_PIN 32
#define WATER_SENSOR_PIN 34
#define MOTION_SENSOR_PIN 35
#define GAS_SENSOR_PIN 33

// ========================
// Actuators
// ========================
#define LED_PIN 12
#define SERVO_PIN 4
#define FAN_PIN 13
Servo doorServo;

// ========================
// RGB LED and Buzzer for Door
// ========================
#define RED_PIN 27
#define GREEN_PIN 26
#define BLUE_PIN 25
#define BUZZER_PIN 2

// ========================
// OLED Setup
// ========================
#define SCREEN_WIDTH 128
#define SCREEN_HEIGHT 64
#define OLED_RESET -1
#define SCREEN_ADDRESS 0x3C
Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, OLED_RESET);

// ========================
// Gas Sensor Threshold
// ========================
int baselineGas = 0;
int thresholdDiff = 5;

// ========================
// Helper Functions
// ========================
void displayMessage(String message) {
  display.clearDisplay();
  display.setTextSize(2);
  display.setTextColor(WHITE);
  int16_t x1, y1;
  uint16_t w, h;
  display.getTextBounds(message, 0, 0, &x1, &y1, &w, &h);
  int x = (SCREEN_WIDTH - w) / 2;
  int y = (SCREEN_HEIGHT - h) / 2;
  display.setCursor(x, y);
  display.println(message);
  display.display();
}

void setRGB(int r, int g, int b) {
  analogWrite(RED_PIN, r);
  analogWrite(GREEN_PIN, g);
  analogWrite(BLUE_PIN, b);
}

void openDoorFromRFID() {
  Serial.println("✅ Authorized RFID card!");
  tone(BUZZER_RFID_PIN, 1000, 200);
  displayMessage("Success");
  setRGB(0, 255, 0);
  doorServo.attach(SERVO_PIN);
  doorServo.write(90); // يبقى مفتوح حتى يُغلق من الموبايل
}

void denyAccess() {
  Serial.println("❌ Unauthorized RFID card!");
  tone(BUZZER_RFID_PIN, 300, 500);
  displayMessage("Failed");
  setRGB(255, 0, 0);
}

void checkWiFi() {
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("⚠ WiFi Disconnected! Attempting to reconnect...");
    WiFi.disconnect();
    WiFi.begin(ssid, password);
    int attempts = 0;
    while (WiFi.status() != WL_CONNECTED && attempts < 10) {
      delay(1000);
      Serial.print(".");
      attempts++;
    }
    if (WiFi.status() == WL_CONNECTED) {
      Serial.println("\n✅ WiFi Reconnected!");
      Serial.print("Device IP: ");
      Serial.println(WiFi.localIP());
    } else {
      Serial.println("\n❌ WiFi Reconnection Failed!");
    }
  }
}

void reconnectMQTT() {
  while (!client.connected()) {
    Serial.println("🔄 Connecting to MQTT...");
    if (client.connect("ESP32_Client")) {
      Serial.println("✅ MQTT Connected!");
      client.subscribe(topic_led);
      client.subscribe(topic_door);
      client.subscribe(topic_fan);
      Serial.println("📩 Subscribed to control topics!");
    } else {
      Serial.print("❌ MQTT Connection Failed. Error Code: ");
      Serial.println(client.state());
      Serial.println("Retrying in 5 seconds...");
      delay(5000);
    }
  }
}

void callback(char* topic, byte* payload, unsigned int length) {
  String command;
  for (unsigned int i = 0; i < length; i++) {
    command += (char)payload[i];
  }

  if (strcmp(topic, topic_led) == 0) {
    digitalWrite(LED_PIN, command == "on" ? HIGH : LOW);

  } else if (strcmp(topic, topic_door) == 0) {
    if (command == "open") {
      doorServo.attach(SERVO_PIN);
      doorServo.write(90);
      displayMessage("Success");
      setRGB(0, 255, 0);
      tone(BUZZER_PIN, 1000, 300);
    } else if (command == "close") {
      doorServo.attach(SERVO_PIN);
      doorServo.write(0);
      displayMessage("Welcome");
      setRGB(0, 0, 0);
    }
  } else if (strcmp(topic, topic_fan) == 0) {
    digitalWrite(FAN_PIN, command == "on" ? HIGH : LOW);
  }
}

void setup() {
  Serial.begin(115200);
  pinMode(LED_PIN, OUTPUT);
  pinMode(FAN_PIN, OUTPUT);
  pinMode(FIRE_SENSOR_PIN, INPUT);
  pinMode(MOTION_SENSOR_PIN, INPUT);
  pinMode(GAS_SENSOR_PIN, INPUT);
  pinMode(RED_PIN, OUTPUT);
  pinMode(GREEN_PIN, OUTPUT);
  pinMode(BLUE_PIN, OUTPUT);
  pinMode(BUZZER_PIN, OUTPUT);
  pinMode(BUZZER_RFID_PIN, OUTPUT);

  dht.begin();
  doorServo.attach(SERVO_PIN);
  doorServo.write(0);

  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.print(".");
  }
  Serial.println("\n✅ WiFi Connected!");
  Serial.print("Device IP: ");
  Serial.println(WiFi.localIP());

  client.setServer(mqtt_server, mqtt_port);
  client.setCallback(callback);
  delay(3000);
  baselineGas = analogRead(GAS_SENSOR_PIN);

  Wire.begin(22, 21);
  if (!display.begin(SSD1306_SWITCHCAPVCC, SCREEN_ADDRESS)) {
    Serial.println(F("❌ OLED not found! Check connections."));
    while (true);
  }
  displayMessage("Welcome");

  SPI.begin(18, 19, 23, 5);
  rfid.PCD_Init();
  Serial.println("🔍 Waiting for RFID card...");

  setRGB(0, 0, 0);
}

void loop() {
  if (WiFi.status() != WL_CONNECTED) {
    checkWiFi();
    return;
  }
  if (!client.connected()) {
    reconnectMQTT();
  }
  client.loop();

  float temperature = dht.readTemperature();
  float humidity = dht.readHumidity();
  if (isnan(temperature) || isnan(humidity)) {
    Serial.println("❌ Failed to read from DHT sensor!");
  }

  int fireStatus = digitalRead(FIRE_SENSOR_PIN);
  int waterAnalog = analogRead(WATER_SENSOR_PIN);
  int motionStatus = digitalRead(MOTION_SENSOR_PIN);
  int gasValue = analogRead(GAS_SENSOR_PIN);

  String payload = "{";
  payload += "\"temperature\":" + String(temperature) + ",";
  payload += "\"humidity\":" + String(humidity) + ",";
  payload += "\"fire_status\":\"" + String(fireStatus == LOW ? "Detected" : "Safe") + "\",";
  payload += "\"water_status\":\"" + String(waterAnalog > 10 ? "Detected" : "Safe") + "\",";
  payload += "\"motion_status\":\"" + String(motionStatus == HIGH ? "Detected" : "Safe") + "\",";
  payload += "\"gas_status\":\"" + String(gasValue > baselineGas + thresholdDiff ? "Detected" : "Safe") + "\",";
  payload += "\"door_state\":\"" + String(doorServo.read() > 45 ? "open" : "close") + "\",";
  payload += "\"led_state\":\"" + String(digitalRead(LED_PIN) == HIGH ? "on" : "off") + "\",";
  payload += "\"fan_state\":\"" + String(digitalRead(FAN_PIN) == HIGH ? "on" : "off") + "\"";
  payload += "}";

  if (client.publish(topic_sensor, payload.c_str())) {
    Serial.print("📤 Published: ");
    Serial.println(payload);
  } else {
    Serial.println("❌ Failed to publish sensor data");
  }

  if (rfid.PICC_IsNewCardPresent() && rfid.PICC_ReadCardSerial()) {
    Serial.print("📛 UID: ");
    bool isAuthorized = true;
    for (byte i = 0; i < 4; i++) {
      Serial.print(rfid.uid.uidByte[i] < 0x10 ? " 0" : " ");
      Serial.print(rfid.uid.uidByte[i], HEX);
      if (rfid.uid.uidByte[i] != authorizedUID[i]) {
        isAuthorized = false;
      }
    }
    Serial.println();

    if (isAuthorized) {
      openDoorFromRFID();
    } else {
      denyAccess();
    }
    rfid.PICC_HaltA();
    rfid.PCD_StopCrypto1();
  }

  HTTPClient http;
  http.begin("http://172.21.14.8/senior-php/sensor_data.php");
  http.addHeader("Content-Type", "application/json");

  String jsonData = "{";
  jsonData += "\"temperature\":" + String(temperature) + ",";
  jsonData += "\"humidity\":" + String(humidity) + ",";
  jsonData += "\"gas_status\":\"" + String(gasValue > baselineGas + thresholdDiff ? "Gas Detected" : "Air Clean") + "\",";
  jsonData += "\"fire_status\":\"" + String(fireStatus == LOW ? "Fire" : "No Fire") + "\",";
  jsonData += "\"motion_status\":\"" + String(motionStatus == HIGH ? "Motion Detected" : "No Motion") + "\",";
  jsonData += "\"water_leak_status\":\"" + String(waterAnalog > 10 ? "Water Leak" : "Dry") + "\",";
  jsonData += "\"door_state\":\"" + String(doorServo.read() > 45 ? "open" : "close") + "\",";
  jsonData += "\"led_state\":\"" + String(digitalRead(LED_PIN) == HIGH ? "on" : "off") + "\",";
  jsonData += "\"fan_state\":\"" + String(digitalRead(FAN_PIN) == HIGH ? "on" : "off") + "\"";
  jsonData += "}";

  int httpResponseCode = http.POST(jsonData);
  if (httpResponseCode > 0) {
    Serial.print("✅ Data sent to PHP. Response: ");
    Serial.println(http.getString());
  } else {
    Serial.print("❌ Failed to send data. HTTP error: ");
    Serial.println(httpResponseCode);
  }
  http.end();

  delay(1000);
}