package com.alkisum.android.cloudnotes.files;

import com.alkisum.android.cloudlib.file.json.JsonFile;
import com.alkisum.android.cloudnotes.database.Db;
import com.alkisum.android.cloudnotes.model.Note;
import com.alkisum.android.cloudnotes.utils.Format;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Class containing constant for Json files.
 *
 * @author Alkisum
 * @version 2.0
 * @since 1.1
 */
public final class Json {

    /**
     * JSON file version.
     */
    private static final int JSON_VERSION = 1;

    /**
     * Regex for the Json file.
     */
    private static final String FILE_REGEX =
            "^.*_(\\d{4})-(\\d{2})-(\\d{2})_(\\d{6})"
                    + JsonFile.FILE_EXT + "$";

    /**
     * JSON name for JSON file version number.
     */
    public static final String VERSION = "version";

    /**
     * JSON name for note object.
     */
    public static final String NOTE = "note";

    /**
     * JSON name for note title.
     */
    public static final String NOTE_TITLE = "title";

    /**
     * JSON name for note content.
     */
    public static final String NOTE_CONTENT = "content";

    /**
     * JSON name for note created time.
     */
    public static final String NOTE_CREATED_TIME = "created time";

    /**
     * JSON name for note updated time.
     */
    public static final String NOTE_UPDATED_TIME = "updated time";

    /**
     * Json constructor.
     */
    private Json() {

    }

    /**
     * Build a queue of JSON files from the given notes.
     *
     * @param notes Selected notes
     * @return Queue of JSON files
     * @throws JSONException An error occurred while building the JSON object
     */
    public static Queue<JsonFile> buildJsonFilesFromNotes(
            final List<Note> notes) throws JSONException {
        Queue<JsonFile> jsonFiles = new LinkedList<>();
        for (Note note : notes) {
            String fileName = buildFileName(note);
            JSONObject jsonObject = buildJsonFromNote(note);
            jsonFiles.add(new JsonFile(fileName, jsonObject));
        }
        return jsonFiles;
    }

    /**
     * Build file name (with extension) for JSON file.
     *
     * @param note Note to build the file name from
     * @return File name
     */
    private static String buildFileName(final Note note) {
        return note.getTitle() + "_" + Format.DATE_TIME_JSON.format(
                new Date(note.getCreatedTime())) + JsonFile.FILE_EXT;
    }

    /**
     * Build a JSON object from the given note.
     *
     * @param note Note to build the JSON object from
     * @return JSONObject JSON object built from the note
     * @throws JSONException An error occurred while building the JSON object
     */
    private static JSONObject buildJsonFromNote(final Note note)
            throws JSONException {

        JSONObject jsonNote = new JSONObject();
        jsonNote.put(NOTE_CREATED_TIME, note.getCreatedTime());
        jsonNote.put(NOTE_UPDATED_TIME, note.getUpdatedTime());
        jsonNote.put(NOTE_TITLE, note.getTitle());
        jsonNote.put(NOTE_CONTENT, note.getContent());

        JSONObject jsonBase = new JSONObject();
        jsonBase.put(VERSION, JSON_VERSION);
        jsonBase.put(NOTE, jsonNote);

        return jsonBase;
    }

    /**
     * Check if the file name is valid.
     *
     * @param jsonFile JSON file to check
     * @return true if the file name is valid, false otherwise
     */
    public static boolean isFileNameValid(final JsonFile jsonFile) {
        return jsonFile.getName().matches(FILE_REGEX);
    }

    /**
     * Check if the note is already in the database.
     *
     * @param jsonFile JSON file to check
     * @return true if the note is already in the database, false otherwise
     */
    public static boolean isNoteAlreadyInDb(final JsonFile jsonFile) {
        List<Note> notes = Db.getInstance().getDaoSession().getNoteDao()
                .loadAll();
        for (Note note : notes) {
            if (jsonFile.getName().equals(buildFileName(note))) {
                return true;
            }
        }
        return false;
    }
}
