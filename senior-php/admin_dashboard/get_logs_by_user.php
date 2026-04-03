<?php
$host = "localhost";
$user = "root";
$pass = "";
$db   = "project_db";

$conn = new mysqli($host, $user, $pass, $db);
if ($conn->connect_error) {
    die(json_encode(["error" => "Connection failed"]));
}

$user_id = isset($_GET['user_id']) ? intval($_GET['user_id']) : 0;
$table = $user_id === 1 ? "device_logs" : "device_logs_$user_id";

$logs = [];
$columns = [];

// جلب الأعمدة
$colsQuery = $conn->query("SHOW COLUMNS FROM `$table`");
while ($col = $colsQuery->fetch_assoc()) {
    $columns[] = $col['Field'];
}

// جلب البيانات
$result = $conn->query("SELECT * FROM `$table` ORDER BY id DESC LIMIT 100");
while ($row = $result->fetch_assoc()) {
    $logs[] = $row;
}

echo json_encode([
    "columns" => $columns,
    "logs" => $logs
]);

$conn->close();
?>
