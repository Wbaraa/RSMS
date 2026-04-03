package com.example.finalsenior;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.Log;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.Locale;

/**
 * Application class for initializing app-wide settings.
 * This ensures themes and language are applied consistently across all activities.
 */
public class RoomServerMonitorApplication extends Application {

    private static final String TAG = "RSMS";

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize theme and language settings
        initializeAppSettings();
    }

    /**
     * Initialize application-wide settings from SharedPreferences
     * Enhanced to properly apply theme and language at application startup
     */
    private void initializeAppSettings() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);

        // Set default language if not already set
        if (!sharedPreferences.contains("language")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("language", "english"); // Default to English
            editor.apply();
        }

        // Apply the saved language
        String language = sharedPreferences.getString("language", "english");
        applyLanguageToApplication(language);

        // Set default theme to light mode if not already set
        if (!sharedPreferences.contains("dark_mode")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("dark_mode", false); // Default to light mode
            editor.apply();
        }

        // Apply theme using ThemeLanguageUtils
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        ThemeLanguageUtils.applyAppTheme(isDarkMode);
    }

    /**
     * Apply language settings to application context
     */
    private void applyLanguageToApplication(String languageCode) {
        try {
            // Create the correct locale
            Locale locale;
            if (languageCode.equals("arabic")) {
                locale = new Locale("ar");
            } else {
                locale = new Locale("en");
            }

            // Set default locale
            Locale.setDefault(locale);

            // Update configuration
            Configuration config = getResources().getConfiguration();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                config.setLocale(locale);
                // Set layout direction based on language
                config.setLayoutDirection(locale);
            } else {
                config.locale = locale;
            }

            getResources().updateConfiguration(config, getResources().getDisplayMetrics());

            Log.d(TAG, "Language applied to application: " + languageCode);
        } catch (Exception e) {
            Log.e(TAG, "Error applying language to application: " + e.getMessage(), e);
        }
    }
}