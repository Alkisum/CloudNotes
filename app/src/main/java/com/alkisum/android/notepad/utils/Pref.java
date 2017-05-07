package com.alkisum.android.notepad.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.alkisum.android.cloudops.utils.CloudPref;

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
            editor.putString(THEME, Theme.LIGHT);
        }
        if (!sharedPref.contains(CloudPref.SAVE_OWNCLOUD_INFO)) {
            editor.putBoolean(CloudPref.SAVE_OWNCLOUD_INFO, true);
        }
        editor.apply();
    }
}
