package com.alkisum.android.notepad.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.alkisum.android.notepad.R;

/**
 * Utility class for the application theme.
 *
 * @author Alkisum
 * @version 1.1
 * @since 1.1
 */
public final class Theme {

    /**
     * String constant defining the light theme.
     */
    static final String LIGHT = "light";

    /**
     * String constant defining the dark theme.
     */
    private static final String DARK = "dark";

    /**
     * Theme constructor.
     */
    private Theme() {

    }

    /**
     * Set the current theme to the given activity.
     *
     * @param activity Activity to set the theme to
     */
    public static void setCurrentTheme(final Activity activity) {
        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(activity);
        String theme = sharedPref.getString(Pref.THEME, LIGHT);
        switch (theme) {
            case LIGHT:
                activity.setTheme(R.style.MaterialLight);
                break;
            case DARK:
                activity.setTheme(R.style.MaterialDark);
                break;
            default:
                break;
        }
    }

    /**
     * Reload the given activity to apply the new theme.
     *
     * @param activity Activity to reload
     */
    public static void reload(final Activity activity) {
        activity.finish();
        Intent intent = new Intent(activity, activity.getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }

    /**
     * Get summary of current theme.
     *
     * @param context Context
     * @return Summary of current theme
     */
    public static String getSummary(final Context context) {
        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        String theme = sharedPref.getString(Pref.THEME, LIGHT);
        String[] themes = context.getResources().getStringArray(R.array.themes);
        switch (theme) {
            case LIGHT:
                return themes[0];
            case DARK:
                return themes[1];
            default:
                return "";
        }
    }
}
