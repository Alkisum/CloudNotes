package com.alkisum.android.cloudnotes.database;

import android.os.AsyncTask;

import com.alkisum.android.cloudnotes.events.RestoreEvent;
import com.alkisum.android.cloudnotes.model.Note;
import com.alkisum.android.cloudnotes.model.NoteDao;

import org.greenrobot.eventbus.EventBus;

/**
 * Class restoring the deleted notes in the database.
 *
 * @author Alkisum
 * @version 2.0
 * @since 2.0
 */
public class Restorer extends AsyncTask<Note, Void, Void> {

    /**
     * Restorer constructor.
     */
    public Restorer() {
    }

    @Override
    protected final Void doInBackground(final Note... notes) {
        NoteDao dao = Db.getInstance().getDaoSession().getNoteDao();
        dao.insertInTx(notes);
        return null;
    }

    @Override
    protected final void onPostExecute(final Void param) {
        EventBus.getDefault().post(new RestoreEvent());
    }
}
