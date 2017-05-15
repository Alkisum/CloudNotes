package com.alkisum.android.notepad.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.util.TypedValue;

import com.alkisum.android.notepad.R;
import com.alkisum.android.notepad.utils.Pref;

/**
 * Utility class for the application theme.
 *
 * @author Alkisum
 * @version 1.1
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
}
