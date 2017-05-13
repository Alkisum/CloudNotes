package com.alkisum.android.notepad.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.alkisum.android.cloudops.utils.CloudPref;
import com.alkisum.android.notepad.ui.ColorPref;
import com.alkisum.android.notepad.ui.ThemePref;

/**
 * Class defining constants for SharedPreferences.
 *
 * @author Alkisum
 * @version 1.1
 * @since 1.0
 */
public final class Pref {

    /**
     * Preference key for the theme.
     */
    public static final String THEME = "theme";

    /**
     * Preference key for the primary color.
     */
    public static final String PRIMARY_COLOR = "primaryColor";

    /**
     * Preference key for the accent color.
     */
    public static final String ACCENT_COLOR = "accentColor";

    /**
     * Preference key for about entry in Settings.
     */
    public static final String ABOUT = "about";

    /**
     * Preference key for build version entry in About.
     */
    public static final String BUILD_VERSION = "buildVersion";

    /**
     * Preference key for build date entry in About.
     */
    public static final String BUILD_DATE = "buildDate";

    /**
     * Preference key for github entry in About.
     */
    public static final String GITHUB = "github";

    /**
     * Pref constructor.
     */
    private Pref() {

    }

    /**
     * Initialize the preferences with their default values.
     *
     * @param context Context
     */
    public static void init(final Context context) {
        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        if (!sharedPref.contains(THEME)) {
            editor.putString(THEME, ThemePref.DEFAULT_THEME);
        }
        if (!sharedPref.contains(PRIMARY_COLOR)) {
            editor.putString(PRIMARY_COLOR, ColorPref.DEFAULT_PRIMARY_COLOR);
        }
        if (!sharedPref.contains(ACCENT_COLOR)) {
            editor.putString(ACCENT_COLOR, ColorPref.DEFAULT_ACCENT_COLOR);
        }
        if (!sharedPref.contains(CloudPref.SAVE_OWNCLOUD_INFO)) {
            editor.putBoolean(CloudPref.SAVE_OWNCLOUD_INFO, true);
        }
        editor.apply();
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
}
