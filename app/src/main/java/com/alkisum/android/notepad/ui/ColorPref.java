package com.alkisum.android.notepad.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

import com.alkisum.android.notepad.R;
import com.alkisum.android.notepad.utils.Pref;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility class for color preferences.
 *
 * @author Alkisum
 * @version 1.1
 * @since 1.1
 */
public final class ColorPref {

    /**
     * Default primary color key.
     */
    public static final String DEFAULT_PRIMARY_COLOR = "indigo";

    /**
     * Default accent color key.
     */
    public static final String DEFAULT_ACCENT_COLOR = "pink";

    /**
     * ColorPref constructor.
     */
    private ColorPref() {

    }

    /**
     * Map of available colors.
     */
    private static final Map<String, Color> COLORS;

    static {
        Map<String, Color> map = new LinkedHashMap<>();
        map.put("red", new Color("red", "Red",
                R.color.red,
                R.style.pref_accent_light_red,
                R.style.pref_accent_dark_red,
                R.layout.layout_app_bar_dark));
        map.put("pink", new Color("pink", "Pink",
                R.color.pink,
                R.style.pref_accent_light_pink,
                R.style.pref_accent_dark_pink,
                R.layout.layout_app_bar_dark));
        map.put("purple", new Color("purple", "Purple",
                R.color.purple,
                R.style.pref_accent_light_purple,
                R.style.pref_accent_dark_purple,
                R.layout.layout_app_bar_dark));
        map.put("deep_purple", new Color("deep_purple", "Deep Purple",
                R.color.deep_purple,
                R.style.pref_accent_light_deep_purple,
                R.style.pref_accent_dark_deep_purple,
                R.layout.layout_app_bar_dark));
        map.put("indigo", new Color("indigo", "Indigo",
                R.color.indigo,
                R.style.pref_accent_light_indigo,
                R.style.pref_accent_dark_indigo,
                R.layout.layout_app_bar_dark));
        map.put("blue", new Color("blue", "Blue",
                R.color.blue,
                R.style.pref_accent_light_blue,
                R.style.pref_accent_dark_blue,
                R.layout.layout_app_bar_dark));
        map.put("light_blue", new Color("light_blue", "Light Blue",
                R.color.light_blue,
                R.style.pref_accent_light_light_blue,
                R.style.pref_accent_dark_light_blue,
                R.layout.layout_app_bar_dark));
        map.put("cyan", new Color("cyan", "Cyan",
                R.color.cyan,
                R.style.pref_accent_light_cyan,
                R.style.pref_accent_dark_cyan,
                R.layout.layout_app_bar_dark));
        map.put("teal", new Color("teal", "Teal",
                R.color.teal,
                R.style.pref_accent_light_teal,
                R.style.pref_accent_dark_teal,
                R.layout.layout_app_bar_dark));
        map.put("green", new Color("green", "Green",
                R.color.green,
                R.style.pref_accent_light_green,
                R.style.pref_accent_dark_green,
                R.layout.layout_app_bar_dark));
        map.put("light_green", new Color("light_green", "Light Green",
                R.color.light_green,
                R.style.pref_accent_light_light_green,
                R.style.pref_accent_dark_light_green,
                R.layout.layout_app_bar_dark));
        map.put("lime", new Color("lime", "Lime",
                R.color.lime,
                R.style.pref_accent_light_lime,
                R.style.pref_accent_dark_lime,
                R.layout.layout_app_bar_dark));
        map.put("yellow", new Color("yellow", "Yellow",
                R.color.yellow,
                R.style.pref_accent_light_yellow,
                R.style.pref_accent_dark_yellow,
                R.layout.layout_app_bar_dark));
        map.put("amber", new Color("amber", "Amber",
                R.color.amber,
                R.style.pref_accent_light_amber,
                R.style.pref_accent_dark_amber,
                R.layout.layout_app_bar_dark));
        map.put("orange", new Color("orange", "Orange",
                R.color.orange,
                R.style.pref_accent_light_orange,
                R.style.pref_accent_dark_orange,
                R.layout.layout_app_bar_dark));
        map.put("deep_orange", new Color("deep_orange", "Deep Orange",
                R.color.deep_orange,
                R.style.pref_accent_light_deep_orange,
                R.style.pref_accent_dark_deep_orange,
                R.layout.layout_app_bar_dark));
        map.put("brown", new Color("brown", "Brown",
                R.color.brown,
                R.style.pref_accent_light_brown,
                R.style.pref_accent_dark_brown,
                R.layout.layout_app_bar_dark));
        map.put("grey", new Color("grey", "Grey",
                R.color.grey,
                R.style.pref_accent_light_grey,
                R.style.pref_accent_dark_grey,
                R.layout.layout_app_bar_dark));
        map.put("blue_grey", new Color("blue_grey", "Blue Grey",
                R.color.blue_grey,
                R.style.pref_accent_light_blue_grey,
                R.style.pref_accent_dark_blue_grey,
                R.layout.layout_app_bar_dark));
        map.put("black", new Color("black", "Black",
                R.color.black,
                R.style.pref_accent_light_black,
                R.style.pref_accent_dark_black,
                R.layout.layout_app_bar_dark));
        map.put("white", new Color("white", "White",
                R.color.white,
                R.style.pref_accent_light_white,
                R.style.pref_accent_dark_white,
                R.layout.layout_app_bar_light));
        COLORS = Collections.unmodifiableMap(map);
    }

    /**
     * @return Map of available colors
     */
    public static Map<String, Color> getColors() {
        return COLORS;
    }

    /**
     * Get primary color from the shared preferences.
     *
     * @param context Context
     * @return Primary color
     */
    static int getPrimaryColor(final Context context) {
        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        String id = sharedPref.getString(Pref.PRIMARY_COLOR,
                DEFAULT_PRIMARY_COLOR);
        return ContextCompat.getColor(context, COLORS.get(id).getCode());
    }

    /**
     * Save the given primary color to the shared preferences.
     *
     * @param context Context
     * @param primary Primary color
     */
    public static void setPrimaryColor(final Context context,
                                       final Color primary) {
        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Pref.PRIMARY_COLOR, primary.getKey());
        editor.apply();
    }

    /**
     * Get accent color from the shared preferences.
     *
     * @param context Context
     * @return Accent color
     */
    public static int getAccentColor(final Context context) {
        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        String id = sharedPref.getString(Pref.ACCENT_COLOR,
                DEFAULT_ACCENT_COLOR);
        return ContextCompat.getColor(context, COLORS.get(id).getCode());
    }

    /**
     * Save the given accent color to the shared preferences.
     *
     * @param context Context
     * @param accent  Accent color
     */
    public static void setAccentColor(final Context context,
                                      final Color accent) {
        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Pref.ACCENT_COLOR, accent.getKey());
        editor.apply();
    }

    /**
     * Save the default primary color to the shared preferences.
     *
     * @param context Context
     */
    public static void applyDefaultPrimary(final Context context) {
        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Pref.PRIMARY_COLOR, DEFAULT_PRIMARY_COLOR);
        editor.apply();
    }

    /**
     * Save the default accent color to the shared preferences.
     *
     * @param context Context
     */
    public static void applyDefaultAccent(final Context context) {
        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Pref.ACCENT_COLOR, DEFAULT_ACCENT_COLOR);
        editor.apply();
    }

    /**
     * Get the primary color from the saved preferences and return its name.
     *
     * @param context Context
     * @return Primary color name
     */
    public static String getPrimarySummary(final Context context) {
        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        String key = sharedPref.getString(Pref.PRIMARY_COLOR,
                DEFAULT_PRIMARY_COLOR);
        return COLORS.get(key).getName();

    }

    /**
     * Get the accent color from the saved preferences and return its name.
     *
     * @param context Context
     * @return Accent color name
     */
    public static String getAccentSummary(final Context context) {
        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        String key = sharedPref.getString(Pref.ACCENT_COLOR,
                DEFAULT_ACCENT_COLOR);
        return COLORS.get(key).getName();
    }

    /**
     * Get the primary color from the saved preferences and return its AppBar
     * layout.
     *
     * @param context Context
     * @return AppBar layout
     */
    static int getAppBarLayout(final Context context) {
        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        String key = sharedPref.getString(Pref.PRIMARY_COLOR,
                DEFAULT_PRIMARY_COLOR);
        return COLORS.get(key).getAppBarLayout();

    }
}
