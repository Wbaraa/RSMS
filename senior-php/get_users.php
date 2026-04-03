<?php
$host = "localhost";
$user = "root";
$pass = "";
$db   = "project_db";

$conn = new mysqli($host, $user, $pass, $db);
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

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
