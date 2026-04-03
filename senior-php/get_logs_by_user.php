<?php
$host = "localhost";
$user = "root";
$pass = "";
$db   = "project_db";

$conn = new mysqli($host, $user, $pass, $db);
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

$users = [];
$result = $conn->query("SELECT * FROM users");

while ($row = $result->fetch_assoc()) {
    $users[] = $row;
}

echo json_encode($users);
$conn->close();
?>
