package com.alkisum.android.cloudnotes.ui;

import android.app.Activity;
import android.view.ViewStub;

import com.alkisum.android.cloudnotes.R;
import com.google.android.material.appbar.AppBarLayout;

import androidx.appcompat.widget.Toolbar;

/**
 * Utility class to inflate the AppBarLayout according to the current theme.
 *
 * @author Alkisum
 * @version 2.7
 * @since 1.1
 */
public final class AppBar {

    /**
     * AppBar constructor.
     */
    private AppBar() {

    }

    /**
     * Inflate AppBar layout and the toolbar according to the primary color
     * AppBar theme.
     *
     * @param activity Activity
     * @param stubId   ViewStub id
     * @return Inflated toolbar
     */
    public static Toolbar inflate(final Activity activity, final int stubId) {
        ViewStub stub = activity.findViewById(stubId);
        stub.setLayoutResource(ColorPref.getAppBarLayout(activity));
        AppBarLayout appBarLayout = (AppBarLayout) stub.inflate();
        Toolbar toolbar = (Toolbar) appBarLayout.getChildAt(0);
        toolbar.setBackgroundColor(ColorPref.getPrimaryColor(activity));
        if (!ThemePref.isLightStatusBarEnabled(activity)) {
            int top = activity.getResources().getDimensionPixelOffset(
                    R.dimen.status_bar_height);
            toolbar.setPadding(0, top, 0, 0);
        }
        return toolbar;
    }
}
