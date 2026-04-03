package com.example.finalsenior;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class ForgotPasswordActivity extends BaseActivity {

    private static final String TAG = "ForgotPasswordActivity";

    private EditText emailInput;
    private Button requestPasswordButton;
    private ImageView backButton;
    private TextView titleText, subtitleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_forgot_password);

            // Initialize views
            initializeViews();

            // Setup listeners
            setupListeners();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing screen", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        emailInput = findViewById(R.id.email_input);
        requestPasswordButton = findViewById(R.id.request_password_button);
        backButton = findViewById(R.id.backButton);
        titleText = findViewById(R.id.title_text);
        subtitleText = findViewById(R.id.subtitle_text);
    }

    @Override
    protected void refreshThemeStyling() {
        // Theme styling is now handled by XML attributes
        // No manual styling needed here
    }

    private void setupListeners() {
        // Back button click listener
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // This closes the current activity and returns to the previous one
            }
        });

        // Request password button click listener
        requestPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    sendPasswordResetEmail();
                }
            }
        });
    }

    private boolean validateInput() {
        String email = emailInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Please enter a valid email");
            return false;
        }

        return true;
    }

    private void sendPasswordResetEmail() {
        String email = emailInput.getText().toString().trim();

        requestPasswordButton.setEnabled(false);
        requestPasswordButton.setText("Sending...");

        String url = "http://172.21.14.8/senior-php/forgot_password.php"; // استبدل بالـ IP تبعك إذا تغيّر

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    requestPasswordButton.setEnabled(true);
                    requestPasswordButton.setText("Request Password");

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        Toast.makeText(ForgotPasswordActivity.this,
                                jsonObject.getString("message"),
                                Toast.LENGTH_LONG).show();

                        if (jsonObject.getString("status").equals("success")) {
                            finish(); // رجوع إلى صفحة تسجيل الدخول
                        }

                    } catch (JSONException e) {
                        Toast.makeText(this, "Response parsing error", Toast.LENGTH_SHORT).show();
                    }

                }, error -> {
            requestPasswordButton.setEnabled(true);
            requestPasswordButton.setText("Request Password");
            Toast.makeText(this, "Network or server error", Toast.LENGTH_SHORT).show();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

}