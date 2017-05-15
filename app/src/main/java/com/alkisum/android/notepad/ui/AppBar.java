package com.alkisum.android.notepad.ui;

import android.app.Activity;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;
import android.view.ViewStub;

import butterknife.ButterKnife;

/**
 * Utility class to inflate the AppBarLayout according to the current theme.
 *
 * @author Alkisum
 * @version 1.1
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
        ViewStub stub = ButterKnife.findById(activity, stubId);
        stub.setLayoutResource(ColorPref.getAppBarLayout(activity));
        AppBarLayout appBarLayout = (AppBarLayout) stub.inflate();
        Toolbar toolbar = (Toolbar) appBarLayout.getChildAt(0);
        toolbar.setBackgroundColor(ColorPref.getPrimaryColor(activity));
        return toolbar;
    }
}
