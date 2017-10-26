package com.alkisum.android.cloudnotes.utils;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * Class implementing a hack to change the layout padding on bottom if the
 * keyboard is shown to allow long lists with editTextViews.
 *
 * @author Alkisum
 * @version 2.0
 * @since 1.1
 */
public class KeyboardUtil {

    /**
     * Decor view.
     */
    private final View decorView;

    /**
     * Content view.
     */
    private final View contentView;

    /**
     * KeyboardUtil constructor.
     *
     * @param activity    Activity
     * @param contentView Content view
     */
    public KeyboardUtil(final Activity activity, final View contentView) {
        this.decorView = activity.getWindow().getDecorView();
        this.contentView = contentView;
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(
                onGlobalLayoutListener);
    }

    /**
     * Enable helper.
     */
    public final void enable() {
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(
                onGlobalLayoutListener);
    }

    /**
     * Disable helper.
     */
    public final void disable() {
        decorView.getViewTreeObserver().removeOnGlobalLayoutListener(
                onGlobalLayoutListener);
    }


    /**
     * A small helper to allow showing the editText focus.
     */
    private final ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener
            = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            Rect r = new Rect();
            decorView.getWindowVisibleDisplayFrame(r);
            int height = decorView.getContext().getResources()
                    .getDisplayMetrics().heightPixels;
            int diff = height - r.bottom;
            if (diff != 0) {
                if (contentView.getPaddingBottom() != diff) {
                    contentView.setPadding(0, 0, 0, diff);
                }
            } else {
                if (contentView.getPaddingBottom() != 0) {
                    contentView.setPadding(0, 0, 0, 0);
                }
            }
        }
    };
}
