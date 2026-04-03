package com.example.finalsenior;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

/**
 * Base activity class that handles common functionality across all activities
 * such as applying the correct theme and language settings.
 * Enhanced with improved locale handling.
 */
public class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Apply saved theme and language settings before creating the activity
        applyUserSettings();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        // Load language settings
        SharedPreferences sharedPreferences = newBase.getSharedPreferences("AppSettings", MODE_PRIVATE);
        String language = sharedPreferences.getString("language", "english");

        // Create configuration with the correct locale
        Locale locale;
        if (language.equals("arabic")) {
            locale = new Locale("ar");
        } else {
            locale = new Locale("en");
        }

        // Set default locale for consistent behavior
        Locale.setDefault(locale);

        // Apply locale to configuration
        Context context = updateBaseContextLocale(newBase, locale);

        super.attachBaseContext(context);
    }

    /**
     * Apply saved user settings for theme and language
     */
    protected void applyUserSettings() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);

        // Apply theme
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        ThemeLanguageUtils.applyAppTheme(isDarkMode);
    }

    /**
     * Update context with the correct locale based on API level
     * @param context Current context
     * @param locale Locale to apply
     * @return Context with updated locale
     */
    private Context updateBaseContextLocale(Context context, Locale locale) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // For API 24 and above
            Configuration config = context.getResources().getConfiguration();
            config.setLocale(locale);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                config.setLayoutDirection(locale);
            }
            return context.createConfigurationContext(config);
        } else {
            // For older API versions
            Resources resources = context.getResources();
            Configuration config = resources.getConfiguration();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                config.setLocale(locale);
                config.setLayoutDirection(locale);
            } else {
                config.locale = locale;
            }
            resources.updateConfiguration(config, resources.getDisplayMetrics());
            return context;
        }
    }

    /**
     * Apply theme styling to views after they've been created
     * This can be overridden in subclasses to refresh theme-specific styling
     */
    protected void refreshThemeStyling() {
        // Override in subclasses to apply theme-specific styling
        // (Now handled through XML theme attributes)
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh theme settings in case they've changed
        applyUserSettings();
        // Let subclasses apply their theme styling if needed
        refreshThemeStyling();
    }
}