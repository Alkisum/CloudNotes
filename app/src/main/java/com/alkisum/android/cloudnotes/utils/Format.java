package com.alkisum.android.cloudnotes.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Utility class to format values.
 *
 * @author Alkisum
 * @version 2.7
 * @since 1.1
 */
public final class Format {

    /**
     * @return Format for build date
     */
    public static SimpleDateFormat getDateBuild() {
        return new SimpleDateFormat("MMM. dd, yyyy", Locale.getDefault());
    }

    /**
     * Format constructor.
     */
    private Format() {

    }
}
