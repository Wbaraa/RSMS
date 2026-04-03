<?php
$host = "localhost";
$user = "root";
$pass = "";
$db = "project_db";

$conn = new mysqli($host, $user, $pass, $db);
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

$user_id = $_POST['user_id']; // يتم إرساله من JavaScript أو AJAX
$table_name = "device_logs" . $user_id;

// حذف جدول اللوغز الخاص باليوزر
$conn->query("DROP TABLE IF EXISTS `$table_name`");

// حذف اليوزر من جدول users
$stmt = $conn->prepare("DELETE FROM users WHERE id = ?");
$stmt->bind_param("i", $user_id);
$stmt->execute();

$stmt->close();
$conn->close();

echo "User and table $table_name deleted.";
?>
