<?php
$host = "localhost";
$user = "root";
$pass = "";
$db   = "project_db";

$conn = new mysqli($host, $user, $pass, $db);
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

$full_name = $_POST['full_name'] ?? '';
$email     = $_POST['email'] ?? '';
$password  = $_POST['password'] ?? '';
$address   = $_POST['address'] ?? '';
$number    = $_POST['number'] ?? '';
$sensors   = $_POST['sensors'] ?? '[]';

if (!$full_name || !$email || !$password || !$address || !$number) {
    http_response_code(400);
    echo "Missing required fields.";
    exit;
}

// إضافة المستخدم في جدول users
$stmt = $conn->prepare("INSERT INTO users (full_name, email, password, address, number) VALUES (?, ?, ?, ?, ?)");
$stmt->bind_param("sssss", $full_name, $email, $password, $address, $number);
$stmt->execute();
$user_id = $conn->insert_id;
$stmt->close();

// إنشاء جدول device_logs_{user_id} حسب الحساسات المختارة
$sensorsArray = json_decode($sensors, true);

$columns = [
    "id INT AUTO_INCREMENT PRIMARY KEY",
    "record_time DATETIME DEFAULT CURRENT_TIMESTAMP"
];

foreach ($sensorsArray as $sensor) {
    switch (strtolower($sensor)) {
        case 'temperature': $columns[] = "temperature VARCHAR(10)"; break;
        case 'humidity': $columns[] = "humidity VARCHAR(10)"; break;
        case 'gas': $columns[] = "gas_status VARCHAR(50)"; break;
        case 'fire': $columns[] = "fire_status VARCHAR(50)"; break;
        case 'motion': $columns[] = "motion_status VARCHAR(50)"; break;
        case 'water': $columns[] = "water_leak_status VARCHAR(50)"; break;
        case 'door': $columns[] = "door_state VARCHAR(10)"; break;
        case 'led': $columns[] = "led_state VARCHAR(10)"; break;
        case 'fan': $columns[] = "fan_state VARCHAR(10)"; break;
    }
}

$tableName = "device_logs_" . $user_id;
$columnsSQL = implode(", ", $columns);

$createTableSQL = "CREATE TABLE `$tableName` ($columnsSQL) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

if ($conn->query($createTableSQL) === TRUE) {
    echo "User and $tableName created successfully.";
} else {
    echo "Error creating table: " . $conn->error;
}

$conn->close();
?>
