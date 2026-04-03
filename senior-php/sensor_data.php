<?php
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "project_db";

$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

$data = json_decode(file_get_contents("php://input"), true);

$temperature       = $data['temperature'];
$humidity          = $data['humidity'];
$gas_status        = $data['gas_status'];
$fire_status       = $data['fire_status'];           // ✅ لازم يكون fire_status مش fire
$motion_status     = $data['motion_status'];         // ✅
$water_leak_status = $data['water_leak_status'];     // ✅
$door_state        = $data['door_state'];
$led_state         = $data['led_state'];
$fan_state         = $data['fan_state'];

$sql = "INSERT INTO device_logs (temperature, humidity, gas_status, fire_status, motion_status, water_leak_status, door_state, led_state, fan_state)
        VALUES ('$temperature', '$humidity', '$gas_status', '$fire_status', '$motion_status', '$water_leak_status', '$door_state', '$led_state', '$fan_state')";

if ($conn->query($sql) === TRUE) {
    echo "✅ Data inserted successfully";
} else {
    echo "❌ Error: " . $conn->error;
}

$conn->close();
?>
