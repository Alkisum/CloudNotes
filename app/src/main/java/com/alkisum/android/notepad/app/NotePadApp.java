package com.alkisum.android.notepad.app;

import android.app.Application;

import com.alkisum.android.notepad.database.Db;
import com.alkisum.android.notepad.utils.Pref;

/**
 * Application class.
 *
 * @author Alkisum
 * @version 1.1
 * @since 1.0
 */
public class NotePadApp extends Application {

    @Override
    public final void onCreate() {
        super.onCreate();

        Db.getInstance().init(this);

        Pref.init(this);
    }
}
