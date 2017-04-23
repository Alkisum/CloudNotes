package com.alkisum.android.notepad.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Class to build a simple confirmation dialog.
 *
 * @author Alkisum
 * @version 1.0
 * @since 1.0
 */
public final class ConfirmDialog {

    /**
     * ConfirmDialog constructor.
     */
    private ConfirmDialog() {

    }

    /**
     * Build the AlertDialog.
     *
     * @param context         Context in which the dialog should be built
     * @param title           Dialog title
     * @param message         Dialog message
     * @param action          String that will shown as positive button
     * @param onClickListener Task to run when the positive button is clicked
     * @return AlertDialog builder ready to be shown
     */
    public static AlertDialog.Builder build(
            final Context context, final String title, final String message,
            final String action,
            final DialogInterface.OnClickListener onClickListener) {
        return new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(action, onClickListener)
                .setNegativeButton(android.R.string.cancel, null);
    }
}
