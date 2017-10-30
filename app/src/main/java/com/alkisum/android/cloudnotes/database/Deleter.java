package com.alkisum.android.cloudnotes.database;

import android.os.AsyncTask;

import com.alkisum.android.cloudnotes.events.DeleteEvent;
import com.alkisum.android.cloudnotes.model.Note;
import com.alkisum.android.cloudnotes.model.NoteDao;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Class deleting the selected notes from the database.
 *
 * @author Alkisum
 * @version 2.0
 * @since 2.0
 */
public class Deleter extends AsyncTask<Void, Void, List<Note>> {

    /**
     * Subscriber ids allowed to process the events.
     */
    private final Integer[] subscriberIds;

    /**
     * Deleter constructor.
     *
     * @param subscriberIds Subscriber ids allowed to process the events
     */
    public Deleter(final Integer[] subscriberIds) {
        this.subscriberIds = subscriberIds;
    }

    @Override
    protected final List<Note> doInBackground(final Void... voids) {
        NoteDao dao = Db.getInstance().getDaoSession().getNoteDao();
        List<Note> notes = Notes.getSelectedNotes();
        dao.deleteInTx(notes);
        return notes;
    }

    @Override
    protected final void onPostExecute(final List<Note> notes) {
        EventBus.getDefault().post(new DeleteEvent(subscriberIds, notes));
    }
}
