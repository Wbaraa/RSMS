<?php
// استخدام الكلاسات من PHPMailer
use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;


// استيراد ملفات PHPMailer (تأكد من المجلد PHPMailer-master موجود بنفس المجلد)
require __DIR__ . '/PHPMailer-master/src/PHPMailer.php';
require __DIR__ . '/PHPMailer-master/src/SMTP.php';
require __DIR__ . '/PHPMailer-master/src/Exception.php';

header('Content-Type: application/json');

// إعدادات قاعدة البيانات
$host = "localhost";
$user = "root";
$pass = "";
$db   = "project_db";

// الاتصال بقاعدة البيانات
$conn = new mysqli($host, $user, $pass, $db);
if ($conn->connect_error) {
    echo json_encode(["status" => "error", "message" => "Connection failed"]);
    exit;
}

// استلام الإيميل من الطلب
$email = isset($_POST['email']) ? trim($_POST['email']) : '';
file_put_contents("debug_log.txt", "📩 Email received: $email\n", FILE_APPEND);
if (empty($email)) {
    echo json_encode(["status" => "error", "message" => "Email is required"]);
    exit;
}

// البحث عن الإيميل في جدول users
$stmt = $conn->prepare("SELECT password FROM users WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();

// إذا تم العثور على الإيميل
if ($result->num_rows === 1) {
    $row = $result->fetch_assoc();
    $password = $row['password'];

    // إنشاء كائن البريد
    $mail = new PHPMailer(true);
    try {
        // إعدادات Gmail SMTP
        $mail->isSMTP();
        $mail->Host       = 'smtp.gmail.com';
        $mail->SMTPAuth   = true;
        $mail->Username   = 'qamarnajeeb2@gmail.com';       // بريد Gmail المستخدم للإرسال
        $mail->Password   = 'unvonwermcfqvamh';             // App Password من Gmail (بدون مسافات)
        $mail->SMTPSecure = 'tls';
        $mail->Port       = 587;

        // إعدادات البريد المُرسل
        $mail->setFrom('qamarnajeeb2@gmail.com', 'Room Monitor Support');
        $mail->addAddress($email);  // بريد المستخدم الذي نسي كلمة السر

        $mail->Subject = 'Password Recovery - Room Server Monitor';
        $mail->Body    = "Hello,\n\nYour password is: $password\n\nKeep it safe.\n\nRSMS";

        // إرسال الرسالة
        $mail->send();

        echo json_encode(["status" => "success", "message" => "Password sent to your email"]);
    } catch (Exception $e) {
        echo json_encode([
            "status" => "error",
            "message" => "Failed to send email: {$mail->ErrorInfo}"
        ]);
    }

} else {
    echo json_encode(["status" => "error", "message" => "Email not found"]);
}

$conn->close();
?>
