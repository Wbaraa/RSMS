package com.example.finalsenior;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginButton;
    private TextView forgotPasswordText;
    private ProgressDialog progressDialog;
    private static final String URL = "http://172.21.14.8/senior-php/login.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        forgotPasswordText = findViewById(R.id.forgot_password);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        if (sharedPreferences.getBoolean("loggedIn", false)) {
            startActivity(new Intent(LoginActivity.this, homeActivity.class));
            finish();
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        if (forgotPasswordText != null) {
            forgotPasswordText.setOnClickListener(v -> handleForgotPassword());
        }
    }

    private void loginUser() {
        final String  email = emailInput.getText().toString().trim();
        final String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter your email and password.", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d("LOGIN_DEBUG", "إرسل البيانات: email=" + email + ", password=" + password);

        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        Log.d("LOGIN_DEBUG", "استجابة السيرفر: " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            String message = jsonObject.getString("message");

                            if (status.equals("success")) {
                                String fullName = jsonObject.getString("full_name");
                                String email = jsonObject.getString("email");
                                String user_id = jsonObject.getString("user_id");
                                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean("loggedIn", true);
                                editor.putString("name", fullName);     // ✅ حفظ الاسم
                                editor.putString("email", email);      // ✅ حفظ الإيميل
                                editor.putString("user_id", user_id);
                                Log.d("LOGINACTIVITY","user_id is:"+user_id);
                                editor.apply();

                                Toast.makeText(LoginActivity.this, "You have been successfully logged in.", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, homeActivity.class));
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(LoginActivity.this, "Error in data analysis", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Log.d("LOGIN_DEBUG", "خطأ في الاتصال: " + error.toString());
                        Toast.makeText(LoginActivity.this, "Failed to connect to the server", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
                Log.d("LOGIN_DEBUG", "إرسال البيانات: " + params.toString());
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(stringRequest);
    }

    private void handleForgotPassword() {
        Intent forgotPasswordIntent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
        startActivity(forgotPasswordIntent);
    }
}
