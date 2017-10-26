package com.alkisum.android.cloudnotes.database;

import com.alkisum.android.cloudnotes.model.Note;
import com.alkisum.android.cloudnotes.model.NoteDao;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for Notes operations.
 *
 * @author Alkisum
 * @version 1.1
 * @since 1.1
 */
public final class Notes {

    /**
     * Notes constructor.
     */
    private Notes() {

    }

    /**
     * Load all the notes from the database and return only the selected ones.
     *
     * @return List of selected notes.
     */
    public static List<Note> getSelectedNotes() {
        NoteDao dao = Db.getInstance().getDaoSession().getNoteDao();
        List<Note> selectedNotes = new ArrayList<>();
        for (Note note : dao.loadAll()) {
            if (note.isSelected()) {
                selectedNotes.add(note);
            }
        }
        return selectedNotes;
    }
}
