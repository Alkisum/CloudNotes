package com.alkisum.android.cloudnotes.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Utility class to format values.
 *
 * @author Alkisum
 * @version 2.0
 * @since 1.1
 */
public final class Format {

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
