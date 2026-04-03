<?php
session_start(); // تأكد من بدء الـ session
$host = "localhost";
$user = "root";
$pass = "";
$db   = "project_db";

$conn = new mysqli($host, $user, $pass, $db);
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// تحقق من إذا المستخدم مشرف
$is_admin = isset($_SESSION['role']) && $_SESSION['role'] === 'admin';

$result = $conn->query("SELECT * FROM users");
$users = [];

while ($row = $result->fetch_assoc()) {
    $users[] = [
        "id"       => $row["id"],
        "full_name"=> $row["full_name"],
        "email"    => $row["email"],
        "password" => $row["password"], 
        "address"  => $row["address"],
        "number"   => $row["number"]
    ];
}

header('Content-Type: application/json');
echo json_encode($users);
$conn->close();
file_put_contents("debug.txt", json_encode($users));

