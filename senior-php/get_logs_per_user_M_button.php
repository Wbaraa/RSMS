<?php
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "project_db";

$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

echo $_GET['user_id'];
// التقاط user_id من التطبيق
$user_id = isset($_GET['user_id']) ? intval($_GET['user_id']) : 0;

// حماية إذا user_id مش موجود أو = 0
if ($user_id <= 0) {
    die("<h2 style='color:red; text-align:center;'>❌ Invalid or missing user ID.</h2>");
}


// تحديد اسم الجدول حسب المستخدم
$table = ($user_id === 1) ? "device_logs" : "device_logs_$user_id";

$checkTable = $conn->query("SHOW TABLES LIKE '$table'");
if ($checkTable->num_rows === 0) {
    die("<h2 style='color:red; text-align:center;'>❌ Table '$table' does not exist in the database.</h2>");
}


// تحديد التاريخ الحالي أو التاريخ الذي حدده المستخدم
$current_date = date('Y-m-d');
$selected_date = isset($_GET['date']) ? $_GET['date'] : $current_date;

$where_clause = "WHERE DATE(record_time) = '$selected_date'";
$sql = "SELECT * FROM `$table` $where_clause ORDER BY record_time DESC";
$result = $conn->query($sql);
?>

<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>📊 Device Logs - Filter by Date</title>
  <style>
    body {
      font-family: Arial;
      background: #f5f5f5;
      padding: 20px;
    }
    h2 {
      text-align: center;
    }
    .filter {
      text-align: right;
      margin: 10px 50px;
      background-color: white;
      padding: 10px;
      border-radius: 6px;
      box-shadow: 0 0 6px rgba(0, 0, 0, 0.2);
    }
    table {
      border-collapse: collapse;
      margin: auto;
      width: 95%;
      background: white;
    }
    th, td {
      padding: 10px;
      border: 1px solid #ccc;
      text-align: center;
    }
    input[type="date"] {
      padding: 5px;
    }
    button {
      padding: 5px 10px;
      background: #7a5af8;
      color: white;
      border: none;
      border-radius: 5px;
      cursor: pointer;
    }
    button:hover {
      background: #593de1;
    }
  </style>
</head>
<body>

<div class="filter">
  <form method="GET">
  <input type="hidden" name="user_id" value="<?= htmlspecialchars($user_id) ?>">
  <label for="date">📅 Filter Day:</label>
  <input type="date" name="date" id="date" value="<?= htmlspecialchars($selected_date) ?>">
  <button type="submit">Filter</button>
  <a href="view_logs.php"><button type="button">Reset</button></a>
</form>

</div>

<h2>📊 Device Logs <?= $selected_date ? "for <u>$selected_date</u>" : "" ?></h2>

<table>
  <tr>
    <th>ID</th>
    <th>Time</th>
    <th>Temperature</th>
    <th>Humidity</th>
    <th>Gas</th>
    <th>Fire</th>
    <th>Motion</th>
    <th>Water</th>
    <th>Door</th>
    <th>LED</th>
    <th>Fan</th>
  </tr>

<?php
if ($result->num_rows > 0) {
    while($row = $result->fetch_assoc()) {
        echo "<tr>";
        echo "<td>{$row["id"]}</td>";
        echo "<td>{$row["record_time"]}</td>";
        echo "<td>{$row["temperature"]}</td>";
        echo "<td>{$row["humidity"]}</td>";
        echo "<td>" . ($row["gas_status"] == "Gas Detected" ? "🔥 Gas Detected" : "✅ Air Clean") . "</td>";
        echo "<td>" . ($row["fire_status"] == "Fire" ? "🔥 Fire" : "✅ Safe") . "</td>";
        echo "<td>" . ($row["motion_status"] == "Motion Detected" ? "🛆 Motion" : "✅ No Motion") . "</td>";
        echo "<td>" . ($row["water_leak_status"] == "Water Leak" ? "💧 Leak" : "✅ Dry") . "</td>";
        echo "<td>" . ($row["door_state"] == "open" ? "🔓 Open" : "🔒 Close") . "</td>";
        echo "<td>" . ($row["led_state"] == "on" ? "💡 On" : "❌ Off") . "</td>";
        echo "<td>" . ($row["fan_state"] == "on" ? "🌀 On" : "❌ Off") . "</td>";
        echo "</tr>";
    }
} else {
    echo "<tr><td colspan='11'>No data available for this day.</td></tr>";
}
?>

</table>
</body>
</html>
