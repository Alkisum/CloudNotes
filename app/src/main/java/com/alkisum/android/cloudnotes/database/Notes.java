package com.alkisum.android.cloudnotes.database;

import com.alkisum.android.cloudnotes.model.Note;
import com.alkisum.android.cloudnotes.model.NoteDao;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for Notes operations.
 *
 * @author Alkisum
 * @version 2.0
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

    /**
     * Get note by the given title.
     *
     * @param title Title to get the note from
     * @return Note having the same title as the given one
     */
    static Note getNoteByTitle(final String title) {
        NoteDao dao = Db.getInstance().getDaoSession().getNoteDao();
        for (Note note : dao.loadAll()) {
            if (note.getTitle().equals(title)) {
                return note;
            }
        }
        return null;
    }

    /**
     * If necessary, add and increment a number after the note's title to make
     * it unique.
     *
     * @param note Note to make its title unique
     */
    public static void setUniqueTitle(final Note note) {
        Note duplicateNote = getNoteByTitle(note.getTitle());
        if (duplicateNote == null
                || duplicateNote.getId().equals(note.getId())) {
            // the title us already unique if there is no other note with this
            // title or if the note with the title is the same note the one
            // being checked
            return;
        }

        // get current title
        String title = note.getTitle();

        // get string already added to title if exists
        String addOn = getAddOn(title);

        // get original title (without add-on)
        String originalTitle = title;
        int idx = 2;
        if (addOn != null) {
            originalTitle = title.substring(0, title.lastIndexOf(addOn));
            idx = getAddOnIndex(addOn);
        }

        // define title format to make it unique
        String format = originalTitle.trim() + " (%d)";

        // use temporary variable to set the new title
        // (avoid note title being checked with itself)
        String newTitle = note.getTitle();

        // increment add-on index until the title is unique
        for (; getNoteByTitle(newTitle) != null; idx++) {
            newTitle = String.format(format, idx);
        }

        // set unique title to note
        note.setTitle(newTitle);
    }

    /**
     * Get the add-on string "(*)" already added to given title.
     *
     * @param title Title to get the add-on from
     * @return Add-on string, null if it does not exist
     */
    private static String getAddOn(final String title) {
        String addOn = null;
        Pattern pattern = Pattern.compile("\\(\\d+\\)$");
        Matcher matcher = pattern.matcher(title);
        if (matcher.find()) {
            addOn = matcher.group(0);
        }
        return addOn;
    }

    /**
     * Get index form add-on.
     *
     * @param addOn Add-on string to get the index from
     * @return Add-on index
     */
    private static Integer getAddOnIndex(final String addOn) {
        String str = addOn.split("[()]")[1];
        return Integer.valueOf(str);
    }
}
