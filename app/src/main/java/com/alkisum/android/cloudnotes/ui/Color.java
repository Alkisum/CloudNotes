package com.alkisum.android.cloudnotes.ui;

import android.content.Context;
import android.support.v4.content.ContextCompat;

/**
 * Class defining a color used for the app views.
 *
 * @author Alkisum
 * @version 1.1
 * @since 1.1
 */
public class Color {

    /**
     * Color key.
     */
    private final String key;

    /**
     * Color name.
     */
    private final String name;

    /**
     * Color hex code.
     */
    private final int code;

    /**
     * Light theme attached to the color (accent).
     */
    private final int lightThemeStyle;

    /**
     * Dark theme attached to the color (accent).
     */
    private final int darkThemeStyle;

    /**
     * AppBar layout id attached to the color (primary).
     */
    private final int appBarLayout;

    /**
     * Color constructor.
     *
     * @param key             Color key
     * @param name            Color name
     * @param code            Color hex code
     * @param lightThemeStyle Light theme attached to the color (accent)
     * @param darkThemeStyle  Dark theme attached to the color (accent)
     * @param appBarLayout    AppBar layout id attached to the color (primary)
     */
    Color(final String key, final String name, final int code,
          final int lightThemeStyle, final int darkThemeStyle,
          final int appBarLayout) {
        this.key = key;
        this.name = name;
        this.code = code;
        this.lightThemeStyle = lightThemeStyle;
        this.darkThemeStyle = darkThemeStyle;
        this.appBarLayout = appBarLayout;
    }

    /**
     * @return Color key
     */
    public final String getKey() {
        return key;
    }

    /**
     * @return Color name
     */
    public final String getName() {
        return name;
    }

    /**
     * @return Color hex code
     */
    public int getCode() {
        return code;
    }

    /**
     * @param context Context
     * @return Color from hex code
     */
    public final int getColor(final Context context) {
        return ContextCompat.getColor(context, code);
    }

    /**
     * @return Light theme attached to the color (accent)
     */
    final int getLightThemeStyle() {
        return lightThemeStyle;
    }

    /**
     * @return Dark theme attached to the color (accent)
     */
    final int getDarkThemeStyle() {
        return darkThemeStyle;
    }

    /**
     * @return AppBar layout id attached to the color (primary)
     */
    final int getAppBarLayout() {
        return appBarLayout;
    }
}
