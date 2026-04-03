package com.example.finalsenior;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

public class introActivity extends BaseActivity {

    private ImageView backgroundImage;
    private TextView titleText, subtitleText;
    private Button getStartedButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        // Initialize UI components
        initializeViews();

        // Set up click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        backgroundImage = findViewById(R.id.background_image);
        titleText = findViewById(R.id.title_text);
        subtitleText = findViewById(R.id.subtitle_text);
        getStartedButton = findViewById(R.id.get_started_button);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Theme styling is now handled by the theme attributes in XML
        // This call ensures any dynamic changes are applied
        refreshThemeStyling();
    }

    @Override
    protected void refreshThemeStyling() {
        // Most styling is now handled automatically through theme attributes
        // This is only needed for any additional dynamic styling
        boolean isDarkMode = ThemeLanguageUtils.isDarkModeEnabled(this);

        // If there's anything specific that still needs programmatic updates
        // For example, if you need to change something that can't be done via themes:
        if (isDarkMode) {
            // Any dark mode specific code if needed
        } else {
            // Any light mode specific code if needed
        }
    }

    private void setupClickListeners() {
        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to login screen
                Intent loginIntent = new Intent(introActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
        });
    }
}