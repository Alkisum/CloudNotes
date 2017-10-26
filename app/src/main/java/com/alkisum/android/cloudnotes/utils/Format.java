package com.alkisum.android.cloudnotes.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Utility class to format values.
 *
 * @author Alkisum
 * @version 1.1
 * @since 1.1
 */
public final class Format {

    /**
     * Format for JSON file name.
     */
    public static final SimpleDateFormat DATE_TIME_JSON =
            new SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.getDefault());

    /**
     * Format for build date.
     */
    public static final SimpleDateFormat DATE_BUILD =
            new SimpleDateFormat("MMM. dd, yyyy", Locale.getDefault());

    /**
     * Format constructor.
     */
    private Format() {

    }
}
