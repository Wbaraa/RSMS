<?php
$host = "localhost";
$user = "root";
$pass = "";
$db   = "project_db";

$conn = new mysqli($host, $user, $pass, $db);
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

$user_id = $_POST['user_id'] ?? 0;
if ($user_id != 1) {
    // حذف المستخدم
    $stmt = $conn->prepare("DELETE FROM users WHERE id = ?");
    $stmt->bind_param("i", $user_id);
    $stmt->execute();
    $stmt->close();

    // حذف الجدول المرتبط
    $table = "device_logs_" . intval($user_id);
    $conn->query("DROP TABLE IF EXISTS `$table`");
}

$conn->close();
?>
