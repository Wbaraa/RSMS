package com.example.finalsenior;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import java.util.Locale;

public class ThemeLanguageUtils {

    private static final String TAG = "ThemeLanguageUtils";

    /**
     * Apply dark or light theme to the application
     * This uses Android's native day/night system for more consistent application
     *
     * @param isDarkMode true for dark mode, false for light mode
     */
    public static void applyAppTheme(boolean isDarkMode) {
        try {
            if (isDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            Log.d(TAG, "Theme applied: " + (isDarkMode ? "Dark" : "Light"));
        } catch (Exception e) {
            Log.e(TAG, "Error applying theme: " + e.getMessage(), e);
        }
    }

    /**
     * Check if dark mode is currently enabled
     *
     * @param context The context to check settings
     * @return true if dark mode is enabled, false otherwise
     */
    public static boolean isDarkModeEnabled(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("dark_mode", false);
    }

    /**
     * Update the layout direction based on the current language
     * @param context The context to update
     */
    public static void updateLayoutDirection(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        String language = sharedPreferences.getString("language", "english");

        // Set layout direction based on language
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Configuration config = context.getResources().getConfiguration();
            if (language.equals("arabic")) {
                config.setLayoutDirection(new Locale("ar"));
            } else {
                config.setLayoutDirection(new Locale("en"));
            }
        }
    }

    /**
     * Apply selected language to the application
     * Improved implementation to handle configurations properly across API levels
     *
     * @param context      Activity context
     * @param languageCode language code ("english" or "arabic")
     */
    public static void applyAppLanguage(Context context, String languageCode) {
        try {
            Locale locale;
            if (languageCode.equals("arabic")) {
                locale = new Locale("ar");
            } else {
                locale = new Locale("en");
            }

            // Set default locale for consistent behavior across the app
            Locale.setDefault(locale);

            // Apply to current context
            Resources resources = context.getResources();
            Configuration configuration = new Configuration(resources.getConfiguration());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                configuration.setLocale(locale);
                configuration.setLayoutDirection(locale);
            } else {
                configuration.locale = locale;
            }

            resources.updateConfiguration(configuration, resources.getDisplayMetrics());

            // Apply to application context as well to ensure app-wide changes
            if (context != context.getApplicationContext()) {
                Resources appResources = context.getApplicationContext().getResources();
                Configuration appConfig = new Configuration(appResources.getConfiguration());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    appConfig.setLocale(locale);
                    appConfig.setLayoutDirection(locale);
                } else {
                    appConfig.locale = locale;
                }

                appResources.updateConfiguration(appConfig, appResources.getDisplayMetrics());
            }

            // Save the language preference
            SharedPreferences sharedPreferences = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("language", languageCode);
            editor.apply();

            Log.d(TAG, "Language successfully changed to: " + languageCode);
        } catch (Exception e) {
            Log.e(TAG, "Error applying language: " + e.getMessage(), e);
        }
    }

    /**
     * Get the current app language
     *
     * @param context The context to get settings from
     * @return The current language code ("english" or "arabic")
     */
    public static String getCurrentLanguage(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        return sharedPreferences.getString("language", "english");
    }

    /**
     * Check if the current language is RTL
     *
     * @param context The context to check settings
     * @return true if the current language is RTL (Arabic), false otherwise
     */
    public static boolean isRtlLanguage(Context context) {
        String language = getCurrentLanguage(context);
        return language.equals("arabic");
    }

    /**
     * Force configuration changes to propagate through the system
     * Call this from activities when needed
     *
     * @param activity Current activity
     */
    public static void refreshActivity(Activity activity) {
        if (activity != null) {
            activity.recreate();
        }
    }

    /**
     * Create a properly configured context with the correct locale
     * Useful for creating new contexts with the current language
     *
     * @param context Base context
     * @return Context with proper locale configuration
     */
    public static Context createConfiguredContext(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        String language = sharedPreferences.getString("language", "english");

        Locale locale;
        if (language.equals("arabic")) {
            locale = new Locale("ar");
        } else {
            locale = new Locale("en");
        }

        Locale.setDefault(locale);

        Configuration config = new Configuration(context.getResources().getConfiguration());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
            config.setLayoutDirection(locale);
        } else {
            config.locale = locale;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.createConfigurationContext(config);
        } else {
            Resources resources = context.getResources();
            resources.updateConfiguration(config, resources.getDisplayMetrics());
            return context;
        }
    }
}