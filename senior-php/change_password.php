<?php
$email = isset($_GET['email']) ? $_GET['email'] : '';
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Change Your Password</title>
    <style>
        body {
            font-family: Arial;
            background: #f0f0f0;
            padding: 30px;
        }
        .container {
            max-width: 400px;
            margin: auto;
            background: white;
            padding: 25px 30px;
            border-radius: 10px;
            box-shadow: 0 0 15px rgba(0,0,0,0.1);
        }
        h2 {
            text-align: center;
        }
        .input-group {
            position: relative;
            margin-bottom: 20px;
        }
        .input-group input {
            width: 100%;
            padding: 10px 40px 10px 12px;
            border: 1px solid #ccc;
            border-radius: 6px;
        }
        .input-group i {
            position: absolute;
            top: 50%;
            right: 12px;
            transform: translateY(-50%);
            cursor: pointer;
            color: #888;
        }
        button {
            width: 100%;
            padding: 10px;
            background-color: #4CAF50;
            color: white;
            border: none;
            border-radius: 6px;
            font-size: 16px;
        }
    </style>
</head>
<body>

<div class="container">
    <h2>Change Your Password</h2>
    <form action="update_password.php" method="post">
        <input type="hidden" name="email" value="<?php echo htmlspecialchars($email); ?>">

        <div class="input-group">
            <label>🔒 Current Password</label>
            <input type="password" id="currentPass" name="current_password" placeholder="Enter current password" required>
            <!-- لا يوجد أيقونة 👁️ هنا -->
        </div>

        <div class="input-group">
            <label>🔑 New Password</label>
            <input type="password" id="newPass" name="new_password" placeholder="Enter new password" required>
            <i onclick="togglePassword('newPass', this)">👁️</i>
        </div>

        <div class="input-group">
            <label>🔑 Confirm New Password</label>
            <input type="password" id="confirmPass" name="confirm_password" placeholder="Confirm new password" required>
            <i onclick="togglePassword('confirmPass', this)">👁️</i>
        </div>

        <button type="submit">Update Password</button>
    </form>
</div>

<script>
function togglePassword(inputId, icon) {
    const input = document.getElementById(inputId);
    if (input.type === "password") {
        input.type = "text";
        icon.textContent = "🙈";
    } else {
        input.type = "password";
        icon.textContent = "👁️";
    }
}
</script>

</body>
</html>
