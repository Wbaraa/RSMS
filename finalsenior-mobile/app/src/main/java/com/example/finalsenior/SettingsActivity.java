package com.example.finalsenior;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.SwitchCompat;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.radiobutton.MaterialRadioButton;

import java.util.HashSet;
import java.util.Set;

public class SettingsActivity extends BaseActivity {

    private static final String TAG = "SettingsActivity";

    private MaterialToolbar toolbar;
    private MaterialRadioButton radioEnglish, radioArabic;
    private SwitchCompat switchDarkMode;
    private MaterialCheckBox checkboxSound, checkboxVibration, checkboxSilent;
    private Spinner spinnerMutePeriod;
    private SharedPreferences sharedPreferences;
    private ImageView backButton; // Added back button declaration

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            Log.d(TAG, "onCreate starting");

            // Get shared preferences first
            sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
            Log.d(TAG, "SharedPreferences obtained");

            // Call super.onCreate() first to ensure proper activity initialization
            super.onCreate(savedInstanceState);
            Log.d(TAG, "super.onCreate called");

            // Set content view
            setContentView(R.layout.activity_settings);
            Log.d(TAG, "setContentView completed");

            // Initialize views with error handling
            try {
                initializeViews();
                Log.d(TAG, "Views initialized");
            } catch (Exception e) {
                Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
                Toast.makeText(this, "Error initializing UI components", Toast.LENGTH_SHORT).show();
                // Continue anyway to see if we can partially initialize
            }

            // Setup toolbar with error handling
            try {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false); // Changed to false to use custom back button
                    getSupportActionBar().setTitle(R.string.nav_settings);
                }
                //toolbar.setNavigationOnClickListener(v -> onBackPressed()); // Removed as we'll use our custom back button
                Log.d(TAG, "Toolbar setup completed");
            } catch (Exception e) {
                Log.e(TAG, "Error setting up toolbar: " + e.getMessage(), e);
                // Continue execution
            }

            // Load settings with error handling
            try {
                loadSettings();
                Log.d(TAG, "Settings loaded");
            } catch (Exception e) {
                Log.e(TAG, "Error loading settings: " + e.getMessage(), e);
                // Continue execution
            }

            // Setup listeners with error handling
            try {
                setupListeners();
                Log.d(TAG, "Listeners setup completed");
            } catch (Exception e) {
                Log.e(TAG, "Error setting up listeners: " + e.getMessage(), e);
                // Continue execution
            }

            // Setup spinner with error handling
            try {
                setupMuteNotificationsSpinner();
                Log.d(TAG, "Spinner setup completed");
            } catch (Exception e) {
                Log.e(TAG, "Error setting up spinner: " + e.getMessage(), e);
                // Continue execution
            }

            Log.d(TAG, "onCreate completed successfully");

        } catch (Exception e) {
            Log.e(TAG, "Fatal error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing settings screen", Toast.LENGTH_LONG).show();
            // Finish the activity gracefully to avoid crash
            finish();
        }
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        if (toolbar == null) {
            Log.e(TAG, "Toolbar not found in layout");
        }

        // Initialize back button
        backButton = findViewById(R.id.backButton);
        if (backButton == null) {
            Log.e(TAG, "backButton not found in layout");
        }

        radioEnglish = findViewById(R.id.radioEnglish);
        if (radioEnglish == null) {
            Log.e(TAG, "radioEnglish not found in layout");
        }

        radioArabic = findViewById(R.id.radioArabic);
        if (radioArabic == null) {
            Log.e(TAG, "radioArabic not found in layout");
        }

        switchDarkMode = findViewById(R.id.switchDarkMode);
        if (switchDarkMode == null) {
            Log.e(TAG, "switchDarkMode not found in layout");
        }

        checkboxSound = findViewById(R.id.checkboxSound);
        if (checkboxSound == null) {
            Log.e(TAG, "checkboxSound not found in layout");
        }

        checkboxVibration = findViewById(R.id.checkboxVibration);
        if (checkboxVibration == null) {
            Log.e(TAG, "checkboxVibration not found in layout");
        }

        checkboxSilent = findViewById(R.id.checkboxMessage);
        if (checkboxSilent == null) {
            Log.e(TAG, "checkboxSilent not found in layout");
        }
    }

    private void loadSettings() {
        // Make sure SharedPreferences is initialized
        if (sharedPreferences == null) {
            sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
        }

        // Load language setting
        try {
            String language = sharedPreferences.getString("language", "english");
            if (radioEnglish != null && radioArabic != null) {
                if (language.equals("english")) {
                    radioEnglish.setChecked(true);
                } else {
                    radioArabic.setChecked(true);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading language setting: " + e.getMessage(), e);
        }

        // Load theme setting
        try {
            boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
            if (switchDarkMode != null) {
                switchDarkMode.setChecked(isDarkMode);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading theme setting: " + e.getMessage(), e);
        }

        // Load notification styles (now multiple can be selected)
        try {
            Set<String> notificationStyles = sharedPreferences.getStringSet("notification_styles", new HashSet<>());

            // If no styles are saved yet, set sound as default
            if (notificationStyles.isEmpty()) {
                notificationStyles = new HashSet<>();
                notificationStyles.add("sound");
            }

            // Set checkboxes based on saved preferences
            if (checkboxSound != null) {
                checkboxSound.setChecked(notificationStyles.contains("sound"));
            }

            if (checkboxVibration != null) {
                checkboxVibration.setChecked(notificationStyles.contains("vibration"));
            }

            if (checkboxSilent != null) {
                checkboxSilent.setChecked(notificationStyles.contains("message"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading notification styles: " + e.getMessage(), e);

            // Fallback to legacy format if stringset isn't available
            try {
                String notificationStyle = sharedPreferences.getString("notification_style", "sound");
                if (checkboxSound != null && checkboxVibration != null && checkboxSilent != null) {
                    // Reset all checkboxes first
                    checkboxSound.setChecked(false);
                    checkboxVibration.setChecked(false);
                    checkboxSilent.setChecked(false);

                    // Set only the previously selected option
                    switch (notificationStyle) {
                        case "sound":
                            checkboxSound.setChecked(true);
                            break;
                        case "vibration":
                            checkboxVibration.setChecked(true);
                            break;
                        case "silent":
                            checkboxSilent.setChecked(true);
                            break;
                    }
                }
            } catch (Exception ex) {
                Log.e(TAG, "Error in fallback notification style loading: " + ex.getMessage(), ex);
            }
        }
    }

    private void setupMuteNotificationsSpinner() {
        if (spinnerMutePeriod == null) {
            Log.e(TAG, "Cannot setup spinner - spinnerMutePeriod is null");
            return;
        }

        try {
            // Create the adapter using our custom layouts
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                    this, R.array.mute_times, R.layout.spinner_item);
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            spinnerMutePeriod.setAdapter(adapter);

            // Load saved mute setting
            int mutePeriodIndex = sharedPreferences.getInt("mute_period_index", 0);
            spinnerMutePeriod.setSelection(mutePeriodIndex);

            spinnerMutePeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // Force black text color for the selected item
                    if (view instanceof TextView) {
                        ((TextView) view).setTextColor(getResources().getColor(R.color.black, getTheme()));
                    }
                    saveMutePeriod(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Do nothing
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up mute notifications spinner: " + e.getMessage(), e);
        }
    }

    private void setupListeners() {
        // Setup back button listener
        if (backButton != null) {
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish(); // Close the activity and return to the previous one
                }
            });
        }

        // Language selection with null checks
        if (radioEnglish != null) {
            radioEnglish.setOnClickListener(v -> saveLanguageSetting("english"));
        }

        if (radioArabic != null) {
            radioArabic.setOnClickListener(v -> saveLanguageSetting("arabic"));
        }

        // Theme selection with null check
        if (switchDarkMode != null) {
            switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                saveThemeSetting(isChecked);
            });
        }

        // Notification style with null checks - now using checkboxes
        if (checkboxSound != null) {
            checkboxSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
                saveNotificationStyles();
            });
        }

        if (checkboxVibration != null) {
            checkboxVibration.setOnCheckedChangeListener((buttonView, isChecked) -> {
                saveNotificationStyles();
            });
        }

        if (checkboxSilent != null) {
            checkboxSilent.setOnCheckedChangeListener((buttonView, isChecked) -> {
                saveNotificationStyles();
            });
        }
    }

    private void saveLanguageSetting(String language) {
        try {
            // Save the language preference
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("language", language);
            editor.apply();

            // Apply language change immediately
            ThemeLanguageUtils.applyAppLanguage(getApplicationContext(), language);

            // Show confirmation toast
            String message = language.equals("english") ?
                    getString(R.string.language_changed, "English") :
                    getString(R.string.language_changed, "العربية");
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

            // Restart the entire application to ensure all activities use the new language
            Intent intent = new Intent(this, introActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            // Properly finish current activity
            finish();

            // Force complete app process restart - this is crucial for language to apply everywhere
            // android.os.Process.killProcess(android.os.Process.myPid());
            // System.exit(0);

        } catch (Exception e) {
            Log.e(TAG, "Error saving language setting: " + e.getMessage(), e);
            Toast.makeText(this, "Error changing language", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveThemeSetting(boolean isDarkMode) {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("dark_mode", isDarkMode);
            editor.apply();

            // Apply theme change with error handling
            try {
                ThemeLanguageUtils.applyAppTheme(isDarkMode);
            } catch (Exception e) {
                Log.e(TAG, "Error applying theme: " + e.getMessage(), e);
            }

            // Show confirmation toast
            String message = isDarkMode ? getString(R.string.dark_mode_enabled) : getString(R.string.light_mode_enabled);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error saving theme setting: " + e.getMessage(), e);
            Toast.makeText(this, "Error changing theme", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveNotificationStyles() {
        try {
            Set<String> selectedStyles = new HashSet<>();

            if (checkboxSound != null && checkboxSound.isChecked()) {
                selectedStyles.add("sound");
            }

            if (checkboxVibration != null && checkboxVibration.isChecked()) {
                selectedStyles.add("vibration");
            }

            if (checkboxSilent != null && checkboxSilent.isChecked()) {
                selectedStyles.add("message"); // ✅ تعديل الاسم هنا
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putStringSet("notification_styles", selectedStyles);

            if (!selectedStyles.isEmpty()) {
                String firstStyle = selectedStyles.iterator().next();
                editor.putString("notification_style", firstStyle);
            }

            editor.apply();
            applyNotificationSettings(selectedStyles);

            Toast.makeText(this, getString(R.string.notification_styles_changed), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error saving notification styles: " + e.getMessage(), e);
            Toast.makeText(this, "Error changing notification styles", Toast.LENGTH_SHORT).show();
        }
    }


    private void applyNotificationSettings(Set<String> styles) {
        // This would be implemented in a real app to configure the device's notification settings
        // For this demo, we'll just log the changes
        Log.d(TAG, "Applying notification styles: " + styles.toString());

        // In a real implementation, you would use NotificationManager and NotificationChannel
        // to configure notification behavior based on the selected styles
    }

    private void saveMutePeriod(int index) {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("mute_period_index", index);
            editor.apply();

            // Apply mute period
            applyMutePeriod(index);
        } catch (Exception e) {
            Log.e(TAG, "Error saving mute period: " + e.getMessage(), e);
        }
    }

    private void applyMutePeriod(int index) {
        // Get the current time
        long currentTime = System.currentTimeMillis();
        long muteUntil = currentTime;

        // Calculate mute duration based on index
        switch (index) {
            case 0: // Off - no muting
                muteUntil = 0;
                break;
            case 1: // 15 minutes
                muteUntil = currentTime + (15 * 60 * 1000);
                break;
            case 2: // 1 hour
                muteUntil = currentTime + (60 * 60 * 1000);
                break;
            case 3: // 8 hours
                muteUntil = currentTime + (8 * 60 * 60 * 1000);
                break;
            case 4: // 24 hours
                muteUntil = currentTime + (24 * 60 * 60 * 1000);
                break;
        }

        // Save mute end time
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("notifications_muted_until", muteUntil);
        editor.apply();

        Log.d(TAG, "Notifications muted until: " + new java.util.Date(muteUntil));
    }

    @Override
    public void recreate() {
        try {
            // Clear activity animation when recreating
            finish();
            overridePendingTransition(0, 0);
            startActivity(getIntent());
            overridePendingTransition(0, 0);
        } catch (Exception e) {
            Log.e(TAG, "Error recreating activity: " + e.getMessage(), e);
            // Fall back to standard recreation if custom method fails
            super.recreate();
        }
    }

    @Override
    protected void refreshThemeStyling() {
        // Theme styling is now handled by XML attributes
        // No manual styling needed here
    }
}