package com.alkisum.android.cloudnotes.database;

import android.os.AsyncTask;

import com.alkisum.android.cloudlib.file.txt.TxtFile;
import com.alkisum.android.cloudnotes.events.InsertEvent;
import com.alkisum.android.cloudnotes.model.Note;
import com.alkisum.android.cloudnotes.model.NoteDao;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Task to insert notes in database from TXT files.
 *
 * @author Alkisum
 * @version 2.0
 * @since 1.1
 */
public class Inserter extends AsyncTask<Void, Void, Void> {

    /**
     * List of TXT files to read.
     */
    private final List<TxtFile> txtFiles;

    /**
     * Note dao.
     */
    private final NoteDao noteDao;

    /**
     * Inserter constructor.
     *
     * @param txtFiles List of TXT files to read
     */
    public Inserter(final List<TxtFile> txtFiles) {
        this.txtFiles = txtFiles;
        noteDao = Db.getInstance().getDaoSession().getNoteDao();
    }

    @Override
    protected final Void doInBackground(final Void... params) {
        for (TxtFile txtFile : txtFiles) {
            Note note = Notes.getNoteByTitle(txtFile.getBaseName());
            if (note != null) {
                noteDao.update(updateNoteFromTxtFile(note, txtFile));
            } else {
                noteDao.insert(buildNewNoteFromTxtFile(txtFile));
            }
        }
        return null;
    }

    @Override
    protected final void onPostExecute(final Void param) {
        EventBus.getDefault().post(new InsertEvent(InsertEvent.OK));
    }

    /**
     * Get note data from TXT file and insert notes into database.
     *
     * @param txtFile TXT file to get the data from
     * @return Note built from TXT file
     */
    private Note buildNewNoteFromTxtFile(final TxtFile txtFile) {
        return new Note(null,
                txtFile.getBaseName(),
                txtFile.getContent(),
                txtFile.getModifiedTime());
    }

    /**
     * Update the given note with the given TXT file.
     *
     * @param note    Note to update
     * @param txtFile TXT file to use to update the note
     * @return Updated note
     */
    private Note updateNoteFromTxtFile(final Note note, final TxtFile txtFile) {
        note.setContent(txtFile.getContent());
        note.setUpdatedTime(txtFile.getModifiedTime());
        return note;
    }
}
