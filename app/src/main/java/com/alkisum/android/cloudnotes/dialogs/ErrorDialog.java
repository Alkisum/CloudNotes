package com.alkisum.android.cloudnotes.dialogs;

import android.app.AlertDialog;
import android.content.Context;

/**
 * Class to show a simple error dialog.
 *
 * @author Alkisum
 * @version 2.0
 * @since 1.1
 */
public final class ErrorDialog {

    /**
     * ErrorDialog constructor.
     */
    private ErrorDialog() {

    }

    /**
     * Build and show the AlertDialog.
     *
     * @param context         Context in which the dialog should be built
     * @param title           Dialog title
     * @param message         Dialog message
     */
    public static void show(final Context context, final String title,
                            final String message) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null).show();
    }
}
