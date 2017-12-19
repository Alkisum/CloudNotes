package com.alkisum.android.cloudnotes.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.alkisum.android.cloudnotes.R;
import com.alkisum.android.cloudnotes.utils.Pref;

/**
 * Utility class for the application theme.
 *
 * @author Alkisum
 * @version 2.3
 * @since 1.1
 */
public final class ThemePref {

    /**
     * String constant defining the light theme.
     */
    private static final String LIGHT = "light";

    /**
     * String constant defining the dark theme.
     */
    private static final String DARK = "dark";

    /**
     * Default theme.
     */
    public static final String DEFAULT_THEME = LIGHT;

    /**
     * Default value for light status bar preference.
     */
    public static final boolean DEFAULT_LIGHT_STATUS_BAR = false;

    /**
     * Default value for light navigation bar preference.
     */
    public static final boolean DEFAULT_LIGHT_NAVIGATION_BAR = false;

    /**
     * ThemePref constructor.
     */
    private ThemePref() {

    }

    /**
     * Set the current theme to the given activity.
     *
     * @param activity Activity to set the theme to
     */
    public static void applyTheme(final Activity activity) {
        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(activity);
        String theme = sharedPref.getString(Pref.THEME, DEFAULT_THEME);
        String accent = sharedPref.getString(Pref.ACCENT_COLOR,
                ColorPref.DEFAULT_ACCENT_COLOR);
        switch (theme) {
            case LIGHT:
                activity.setTheme(ColorPref.getColors().get(accent)
                        .getLightThemeStyle());
                break;
            case DARK:
                activity.setTheme(ColorPref.getColors().get(accent)
                        .getDarkThemeStyle());
                break;
            default:
                break;
        }

        // Apply status and navigation bar theme
        Window w = activity.getWindow();
        boolean lightStatusBar = isLightStatusBarEnabled(activity);
        boolean lightNavigationBar = isLightNavigationBarEnabled(activity);
        if (!lightStatusBar && !lightNavigationBar) {
            // Disable light status bar and light navigation bar
            w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else if (lightStatusBar && lightNavigationBar
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Enable light status bar and light navigation bar
            w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            w.addFlags(WindowManager.LayoutParams
                    .FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            w.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                            | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            w.setStatusBarColor(ContextCompat.getColor(
                    activity, R.color.lightStatusBarColor));
            w.setNavigationBarColor(ContextCompat.getColor(
                    activity, R.color.lightNavigationBarColor));
        } else if (lightStatusBar
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Enable light status bar only
            w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            w.addFlags(WindowManager.LayoutParams
                    .FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            w.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            w.setStatusBarColor(ContextCompat.getColor(
                    activity, R.color.lightStatusBarColor));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Enable light navigation bar only
            w.addFlags(WindowManager.LayoutParams
                    .FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            w.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            w.setNavigationBarColor(ContextCompat.getColor(
                    activity, R.color.lightNavigationBarColor));
            // Disable light status bar
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**
     * Make global changes on views after inflation.
     *
     * @param activity Activity to change the views on
     */
    public static void applyViews(final Activity activity) {
        View separator = activity.findViewById(R.id.light_nav_bar_separator);
        if (isLightNavigationBarEnabled(activity)) {
            separator.setVisibility(View.VISIBLE);
        } else {
            separator.setVisibility(View.GONE);
        }
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
        String theme = sharedPref.getString(Pref.THEME, DEFAULT_THEME);
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

    /**
     * Get the primary text color from the attributes.
     *
     * @param context Context
     * @return Primary text color
     */
    private static int getPrimaryTextColor(final Context context) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.textColorPrimary, typedValue, true);
        @ColorInt int color = typedValue.data;
        return color;
    }

    /**
     * Get the secondary text color from the attributes.
     *
     * @param context Context
     * @return Secondary text color
     */
    private static int getSecondaryTextColor(final Context context) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.textColorSecondary, typedValue, true);
        @ColorInt int color = typedValue.data;
        return color;
    }

    /**
     * Create ColorStateList for the navigation item icon tint.
     *
     * @param context Context
     * @return navigation item icon tint ColorStateList
     */
    public static ColorStateList getNavIconTint(final Context context) {
        return new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                },
                new int[]{
                        ColorPref.getAccentColor(context),
                        ThemePref.getSecondaryTextColor(context)
                }
        );
    }

    /**
     * Create ColorStateList for the navigation item text color.
     *
     * @param context Context
     * @return navigation item text color ColorStateList
     */
    public static ColorStateList getNavTextColor(final Context context) {
        return new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                },
                new int[]{
                        ColorPref.getAccentColor(context),
                        ThemePref.getPrimaryTextColor(context)
                }
        );
    }

    /**
     * Create ColorStateList for the note list checkbox.
     *
     * @param context Context
     * @return note list checkbox ColorStateList
     */
    public static ColorStateList getNoteCheckBoxTint(final Context context) {
        return new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_enabled}
                },
                new int[]{
                        ColorPref.getAccentColor(context)
                }
        );
    }

    /**
     * Check if the light status bar is enabled or not.
     *
     * @param context Context
     * @return true if the light status bar is enabled, false otherwise
     */
    public static boolean isLightStatusBarEnabled(final Context context) {
        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sharedPref.getBoolean(Pref.LIGHT_STATUS_BAR,
                DEFAULT_LIGHT_STATUS_BAR);
    }

    /**
     * Check if the light navigation bar is enabled or not.
     *
     * @param context Context
     * @return true if the light navigation bar is enabled, false otherwise
     */
    private static boolean isLightNavigationBarEnabled(final Context context) {
        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sharedPref.getBoolean(Pref.LIGHT_NAVIGATION_BAR,
                DEFAULT_LIGHT_NAVIGATION_BAR);
    }
}
