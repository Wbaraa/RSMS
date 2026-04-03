<?php
$host = "localhost";
$user = "root";
$pass = "";
$db   = "project_db";

$conn = new mysqli($host, $user, $pass, $db);
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// استقبال البيانات من الفورم
$email            = $_POST['email'];
$current_password = $_POST['current_password'];
$new_password     = $_POST['new_password'];
$confirm_password = $_POST['confirm_password'];

// التحقق من تطابق كلمتي السر الجديدتين
if ($new_password !== $confirm_password) {
    die("❌ كلمة المرور الجديدة غير متطابقة.");
}

// التحقق من وجود المستخدم وكلمة المرور الحالية
$stmt = $conn->prepare("SELECT password FROM users WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 1) {
    $row = $result->fetch_assoc();
    $stored_password = $row['password'];

    if ($current_password === $stored_password) {
        // تحديث كلمة المرور
        $update = $conn->prepare("UPDATE users SET password = ? WHERE email = ?");
        $update->bind_param("ss", $new_password, $email);
        if ($update->execute()) {
            echo "<h2 style='color: green;'>✅ تم تغيير كلمة المرور بنجاح.</h2>";
            echo "<a href='http://localhost/dashboardadmin.php'>العودة إلى لوحة التحكم</a>";
        } else {
            echo "<h2 style='color: red;'>❌ حدث خطأ أثناء التحديث.</h2>";
        }
    } else {
        echo "<h2 style='color: red;'>❌ كلمة المرور الحالية غير صحيحة.</h2>";
    }
} else {
    echo "<h2 style='color: red;'>❌ المستخدم غير موجود.</h2>";
}

$conn->close();
?>
