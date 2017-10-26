package com.alkisum.android.cloudnotes.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Class to show a simple confirmation dialog.
 *
 * @author Alkisum
 * @version 2.0
 * @since 1.0
 */
public final class ConfirmDialog {

    /**
     * ConfirmDialog constructor.
     */
    private ConfirmDialog() {

    }

    /**
     * Build and show the AlertDialog.
     *
     * @param context         Context in which the dialog should be built
     * @param title           Dialog title
     * @param message         Dialog message
     * @param action          String that will shown as positive button
     * @param onClickListener Task to run when the positive button is clicked
     */
    public static void show(
            final Context context, final String title, final String message,
            final String action,
            final DialogInterface.OnClickListener onClickListener) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(action, onClickListener)
                .setNegativeButton(android.R.string.cancel, null).show();
    }
}
