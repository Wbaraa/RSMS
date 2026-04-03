package com.example.finalsenior;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;

public class HelpActivity extends BaseActivity {

    private MaterialToolbar toolbar;
    private TextView tvHelpContent;
    private Button btnContactSupport;
    private ImageView backButton; // Added back button declaration

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        // Initialize UI components
        initializeViews();

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false); // Changed to false since we're using custom back button
            getSupportActionBar().setTitle(getString(R.string.help_title));
        }

        // Setup back button
        backButton.setOnClickListener(v -> onBackPressed());

        // Setup content
        setupHelpContent();

        // Setup button click listener
        btnContactSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                composeEmail();
            }
        });
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tvHelpContent = findViewById(R.id.tvHelpContent);
        btnContactSupport = findViewById(R.id.btnContactSupport);
        backButton = findViewById(R.id.backButton); // Initialize back button
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Theme is now handled through XML attributes
    }

    private void setupHelpContent() {
        StringBuilder helpText = new StringBuilder();

        // Welcome message
        helpText.append(getString(R.string.help_welcome)).append("\n\n");

        // Introduction section
        helpText.append(getString(R.string.help_intro_title)).append("\n");
        helpText.append(getString(R.string.help_intro_content)).append("\n\n");

        // Home Screen section
        helpText.append(getString(R.string.help_home_title)).append("\n");
        helpText.append(getString(R.string.help_home_monitoring)).append("\n");
        helpText.append(getString(R.string.help_home_controls)).append("\n\n");

        // Sensor Logs section
        helpText.append(getString(R.string.help_logs_title)).append("\n");
        helpText.append(getString(R.string.help_logs_content)).append("\n\n");

        // Notifications section
        helpText.append(getString(R.string.help_notifications_title)).append("\n");
        helpText.append(getString(R.string.help_notifications_content)).append("\n\n");

        // Settings section
        helpText.append(getString(R.string.help_settings_title)).append("\n");
        helpText.append(getString(R.string.help_settings_content)).append("\n\n");

        // Troubleshooting section
        helpText.append(getString(R.string.help_troubleshooting_title)).append("\n");
        helpText.append(getString(R.string.help_troubleshooting_sensor)).append("\n\n");
        helpText.append(getString(R.string.help_troubleshooting_device)).append("\n\n");
        helpText.append(getString(R.string.help_troubleshooting_alert)).append("\n\n");

        // Final note
        helpText.append(getString(R.string.help_contact_note));

        tvHelpContent.setText(helpText.toString());
    }

    private void composeEmail() {
        // استرجاع البيانات من SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userName = prefs.getString("name", "Unknown User");
        String userEmail = prefs.getString("email", "unknown@example.com");

        // إعداد الإيميل
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // فتح تطبيقات الإيميل فقط
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"qamar.najeeb2@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Room Server Monitor Support Request");

        String message = "Hello,\n\n" +
                "I am reaching out to report an issue I am experiencing with the application.\n\n" +
                "User Information:\n" +
                "- Name: " + userName + "\n" +
                "- Account: " + userEmail + "\n" +
                "- Device Model: " + android.os.Build.MODEL + "\n" +
                "- Android Version: " + android.os.Build.VERSION.RELEASE  +"\n" +  "\n";

        intent.putExtra(Intent.EXTRA_TEXT, message);

        try {
            intent.setPackage("com.google.android.gm"); // محاولة فتح Gmail مباشرة
            startActivity(intent);
        } catch (Exception e) {
            startActivity(Intent.createChooser(intent, "Choose Email App"));
        }
    }


}