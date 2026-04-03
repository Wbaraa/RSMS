<?php
use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;

require __DIR__ . '/PHPMailer-master/src/Exception.php';
require __DIR__ . '/PHPMailer-master/src/PHPMailer.php';
require __DIR__ . '/PHPMailer-master/src/SMTP.php';

header('Content-Type: application/json');

// إعدادات قاعدة البيانات
$host = "localhost";
$user = "root";
$pass = "";
$db   = "project_db";

$conn = new mysqli($host, $user, $pass, $db);
if ($conn->connect_error) {
    die(json_encode(["status" => "error", "message" => "Connection failed: " . $conn->connect_error]));
}

$email = isset($_POST['email']) ? $_POST['email'] : null;
$password = isset($_POST['password']) ? $_POST['password'] : null;

if ($email && $password) {
    $stmt = $conn->prepare("SELECT id, full_name, email, password FROM users WHERE email=?");
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        $row = $result->fetch_assoc();
        $storedPassword = $row['password'];

        if ($password === $storedPassword) {
            // إرسال Login Notification عبر Gmail SMTP
            $mail = new PHPMailer(true);
            try {
                $mail->isSMTP();
                $mail->Host = 'smtp.gmail.com';
                $mail->SMTPAuth = true;
                $mail->Username = 'qamarnajeeb2@gmail.com';      // الإيميل يلي رح يبعت منه
                $mail->Password = 'unvonwermcfqvamh';            // App Password
                $mail->SMTPSecure = 'tls';
                $mail->Port = 587;

                $mail->setFrom('qamarnajeeb2@gmail.com', 'Room Monitor Login Alert');
                $mail->addAddress($email);  // الإيميل يلي عمل login

                $mail->Subject = 'Login Alert - Room Server Monitor';
                $mail->Body    = "Hello " . $row['full_name'] . ",\n\nA login to your account was made successfully.\n\nIN : " . date("Y-m-d H:i:s") . "\n\nRSMS.";

                $mail->send();
            } catch (Exception $e) {
                // مش ضروري توقف العملية لو فشل الإرسال
                error_log("Email failed: " . $mail->ErrorInfo);
            }

            echo json_encode([
                "status" => "success",
                "message" => "Login successful",
                "full_name" => $row['full_name'],
                "email" => $row['email'],
                "user_id"=> $row['id'],
            ]);
        } else {
            echo json_encode(["status" => "failure", "message" => "Invalid credentials"]);
        }
    } else {
        echo json_encode(["status" => "failure", "message" => "User not found"]);
    }

    $stmt->close();
} else {
    echo json_encode(["status" => "error", "message" => "Missing email or password"]);
}

$conn->close();
?>
