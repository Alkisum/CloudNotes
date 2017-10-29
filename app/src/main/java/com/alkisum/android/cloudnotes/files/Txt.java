package com.alkisum.android.cloudnotes.files;

import com.alkisum.android.cloudlib.file.txt.TxtFile;
import com.alkisum.android.cloudnotes.model.Note;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for TXT files.
 *
 * @author Alkisum
 * @version 2.0
 * @since 2.0
 */
public final class Txt {

    /**
     * Txt constructor.
     */
    private Txt() {

    }

    /**
     * Build a list of TXT files from the given notes.
     *
     * @param notes Selected notes
     * @return List of TXT files
     */
    public static List<TxtFile> buildTxtFilesFromNotes(
            final List<Note> notes) {
        List<TxtFile> txtFiles = new ArrayList<>();
        for (Note note : notes) {
            String fileName = note.getTitle() + TxtFile.FILE_EXT;
            txtFiles.add(new TxtFile(fileName, note.getContent()));
        }
        return txtFiles;
    }
}
