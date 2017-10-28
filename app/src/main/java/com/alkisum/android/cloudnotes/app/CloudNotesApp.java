package com.alkisum.android.cloudnotes.app;

import android.app.Application;

import com.alkisum.android.cloudnotes.database.Db;
import com.alkisum.android.cloudnotes.utils.Pref;

/**
 * Application class.
 *
 * @author Alkisum
 * @version 2.0
 * @since 1.0
 */
public class CloudNotesApp extends Application {

    @Override
    public final void onCreate() {
        super.onCreate();

        Db.getInstance().init(this);

        Pref.init(this);
    }
}
