<?php
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "project_db";

$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// التقاط user_id من التطبيق
$user_id = isset($_GET['user_id']) ? intval($_GET['user_id']) : 0;

// تحديد اسم الجدول حسب المستخدم
$table = ($user_id === 1) ? "device_logs" : "device_logs_$user_id";

// تنفيذ الاستعلام
$sql = "SELECT * FROM `$table` ORDER BY record_time DESC LIMIT 50";
$result = $conn->query($sql);

$logs = array();

if ($result && $result->num_rows > 0) {
    while($row = $result->fetch_assoc()) {
        $logs[] = array(
            "timestamp"         => $row["record_time"],
            "temperature"       => $row["temperature"],
            "humidity"          => $row["humidity"],
            "gas_status"        => $row["gas_status"],
            "fire_status"       => $row["fire_status"],
            "motion_status"     => $row["motion_status"],
            "water_leak_status" => $row["water_leak_status"],
            "door_state"        => $row["door_state"],
            "led_state"         => $row["led_state"],
            "fan_state"         => $row["fan_state"]
        );
    }
}

header('Content-Type: application/json');
echo json_encode($logs);

$conn->close();
?>
