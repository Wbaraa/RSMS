<?php
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "project_db";

$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

$days = isset($_GET['days']) ? $_GET['days'] : 'all';
if ($days === 'all') {
    $sql = "SELECT * FROM device_logs ORDER BY record_time ASC";
} else {
    $sql = "SELECT * FROM device_logs WHERE record_time >= NOW() - INTERVAL $days DAY ORDER BY record_time ASC";
}

$data = [];
$result = $conn->query($sql);
while ($row = $result->fetch_assoc()) {
    $data[] = $row;
}
$conn->close();
?>

<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Sensor Charts - Aggregated by Date</title>
  <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
  <style>
    body {
      font-family: Arial;
      padding: 20px;
      background: #f5f5f5;
    }
    h2 {
      text-align: center;
      margin-bottom: 20px;
    }
    .chart-container {
      width: 100%;
      max-width: 900px;
      margin: 40px auto;
      background: white;
      padding: 20px;
      border-radius: 10px;
      box-shadow: 0 0 10px rgba(0,0,0,0.1);
    }
    .filter-container {
      text-align: center;
      margin-top: 20px;
    }
  </style>
</head>
<body>

<h2>📊 Sensor Aggregated Data Dashboard</h2>

<div class="filter-container">
  <label for="days">📅 Choose the number of days:</label>
  <select id="days" onchange="reloadWithDays()">
    <option value="1" <?= $days == '1' ? 'selected' : '' ?>>Last day</option>
    <option value="3" <?= $days == '3' ? 'selected' : '' ?>>Last 3 days</option>
    <option value="7" <?= $days == '7' ? 'selected' : '' ?>>Last 7 days</option>
    <option value="30" <?= $days == '30' ? 'selected' : '' ?>>Last 30 days</option>
    <option value="all" <?= $days == 'all' ? 'selected' : '' ?>>All</option>
  </select>
</div>

<div class="chart-container"><canvas id="tempHumidityChart"></canvas></div>
<div class="chart-container"><canvas id="gasChart"></canvas></div>
<div class="chart-container"><canvas id="fireChart"></canvas></div>
<div class="chart-container"><canvas id="motionChart"></canvas></div>
<div class="chart-container"><canvas id="waterChart"></canvas></div>
<div class="chart-container"><canvas id="doorChart"></canvas></div>
<div class="chart-container"><canvas id="ledChart"></canvas></div>
<div class="chart-container"><canvas id="fanChart"></canvas></div>

<script>
  function reloadWithDays() {
    const days = document.getElementById('days').value;
    window.location.href = `sensor_charts.php?days=${days}`;
  }

  const data = <?php echo json_encode($data); ?>;
  const labels = data.map(row => row.record_time);
  const temp = data.map(row => parseFloat(row.temperature));
  const humidity = data.map(row => parseFloat(row.humidity));

  // Line Chart: Temperature & Humidity
  new Chart(document.getElementById('tempHumidityChart'), {
    type: 'line',
    data: {
      labels,
      datasets: [
        {
          label: '🌡️ Temperature (°C)',
          data: temp,
          borderColor: 'red',
          fill: false
        },
        {
          label: '💧 Humidity (%)',
          data: humidity,
          borderColor: 'blue',
          fill: false
        }
      ]
    },
    options: {
      responsive: true,
      plugins: {
        title: { display: true, text: 'Temperature & Humidity Over Time' }
      }
    }
  });

  const groupByDate = (key, match) => {
    const counts = {};
    data.forEach(row => {
      const date = row.record_time.split(' ')[0];
      if (row[key].toLowerCase() === match.toLowerCase()) {
        counts[date] = (counts[date] || 0) + 1;
      }
    });
    return counts;
  };

  const makeAggregatedChart = (canvasId, label, key, match, color) => {
    const grouped = groupByDate(key, match);
    const labels = Object.keys(grouped);
    const values = Object.values(grouped);

    new Chart(document.getElementById(canvasId), {
      type: 'bar',
      data: {
        labels,
        datasets: [{
          label: `${label} (per day)` ,
          data: values,
          backgroundColor: color
        }]
      },
      options: {
        responsive: true,
        plugins: {
          title: { display: true, text: `${label} per Day` }
        },
        scales: {
          y: {
            beginAtZero: true
          }
        }
      }
    });
  };

  makeAggregatedChart('gasChart', '🔥 Gas Detected', 'gas_status', 'Gas Detected', 'orange');
  makeAggregatedChart('fireChart', '🔥 Fire', 'fire_status', 'Fire', 'red');
  makeAggregatedChart('motionChart', '🛆 Motion', 'motion_status', 'Motion Detected', 'purple');
  makeAggregatedChart('waterChart', '💧 Leak', 'water_leak_status', 'Water Leak', 'blue');
  makeAggregatedChart('doorChart', '🚪 Door Open', 'door_state', 'open', 'green');
  makeAggregatedChart('ledChart', '💡 LED On', 'led_state', 'on', 'gold');
  makeAggregatedChart('fanChart', '🌀 Fan On', 'fan_state', 'on', 'teal');
</script>

</body>
</html>