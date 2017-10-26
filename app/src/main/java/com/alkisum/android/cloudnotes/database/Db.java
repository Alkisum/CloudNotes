package com.alkisum.android.cloudnotes.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.alkisum.android.cloudnotes.model.DaoMaster;
import com.alkisum.android.cloudnotes.model.DaoSession;

import org.greenrobot.greendao.database.Database;

/**
 * Singleton class handling database.
 *
 * @author Alkisum
 * @version 2.0
 * @since 1.0
 */
public final class Db {

    /**
     * Log tag.
     */
    private static final String TAG = "Db";

    /**
     * Database name.
     */
    private static final String NAME = "notepad.db";

    /**
     * DaoSession instance.
     */
    private DaoSession daoSession;

    /**
     * Database instance.
     */
    private static Db instance = null;

    /**
     * Db constructor.
     */
    private Db() {

    }

    /**
     * @return Database instance
     */
    public static Db getInstance() {
        if (instance == null) {
            instance = new Db();
        }
        return instance;
    }

    /**
     * Initialize database.
     *
     * @param context Context
     * @return Database instance
     */
    public Db init(final Context context) {
        DaoMaster.OpenHelper helper = new DbOpenHelper(
                context.getApplicationContext(), NAME, null);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        return this;
    }

    /**
     * @return DaoSession instance
     */
    public DaoSession getDaoSession() {
        return daoSession;
    }

    /**
     * Class extending SQLiteOpenHelper, used for upgrading database from one
     * version to another.
     */
    private class DbOpenHelper extends DaoMaster.OpenHelper {

        /**
         * DbOpenHelper constructor.
         *
         * @param context Context
         * @param name    Database name
         * @param factory Cursor factory
         */
        DbOpenHelper(final Context context, final String name,
                     final SQLiteDatabase.CursorFactory factory) {
            super(context, name, factory);
        }

        @Override
        public void onUpgrade(final Database db, final int oldVersion,
                              final int newVersion) {
            Log.i(TAG, "Upgrade database from " + oldVersion
                    + " to " + newVersion);
        }
    }
}
