package com.alkisum.android.cloudnotes.database;

import android.os.AsyncTask;

import com.alkisum.android.cloudnotes.files.Json;
import com.alkisum.android.cloudnotes.model.Note;
import com.alkisum.android.cloudnotes.model.NoteDao;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Task to insert notes in database from JSON objects.
 *
 * @author Alkisum
 * @version 2.0
 * @since 1.1
 */
public class Inserter extends AsyncTask<Void, Void, Void> {

    /**
     * Listener to get notification when the task finishes.
     */
    private final InserterListener callback;

    /**
     * List of JSON objects to read.
     */
    private final List<JSONObject> jsonObjects;

    /**
     * Note dao.
     */
    private final NoteDao noteDao;

    /**
     * Exception that can be set when an exception is caught during the task.
     */
    private Exception exception;

    /**
     * Inserter constructor.
     *
     * @param callback    Listener of the task
     * @param jsonObjects List of JSON objects to read
     */
    public Inserter(final InserterListener callback,
                    final List<JSONObject> jsonObjects) {
        this.callback = callback;
        this.jsonObjects = jsonObjects;
        noteDao = Db.getInstance().getDaoSession().getNoteDao();
    }

    @Override
    protected final Void doInBackground(final Void... params) {
        try {
            for (JSONObject jsonObject : jsonObjects) {
                buildNoteFromJson(jsonObject);
            }
        } catch (JSONException e) {
            exception = e;
        }
        return null;
    }

    @Override
    protected final void onPostExecute(final Void param) {
        if (exception == null) {
            callback.onDataInserted();
        } else {
            callback.onInsertDataFailed(exception);
        }
    }

    /**
     * Get note data from JSON and insert notes into database.
     *
     * @param jsonBase JSONObject the structure is based on
     * @throws JSONException An error occurred while parsing the JSON object
     */
    private void buildNoteFromJson(final JSONObject jsonBase)
            throws JSONException {
        int version = jsonBase.getInt(Json.VERSION);
        switch (version) {
            case 1:
                fromVersion1(jsonBase);
                break;
            default:
                break;
        }
    }

    /**
     * Build note from Json file version 1.
     *
     * @param jsonBase JSONObject the structure is based on
     * @throws JSONException An error occurred while parsing the JSON object
     */
    private void fromVersion1(final JSONObject jsonBase) throws JSONException {
        JSONObject jsonNote = jsonBase.getJSONObject(Json.NOTE);
        Note note = new Note(null,
                jsonNote.getString(Json.NOTE_TITLE),
                jsonNote.getString(Json.NOTE_CONTENT),
                jsonNote.getLong(Json.NOTE_CREATED_TIME),
                jsonNote.getLong(Json.NOTE_UPDATED_TIME));
        noteDao.insert(note);
    }

    /**
     * Listener for the Inserter.
     */
    public interface InserterListener {

        /**
         * Called when the JSON files are read and the note's data inserted
         * into the database.
         */
        void onDataInserted();

        /**
         * Called when an exception has been caught during the task.
         *
         * @param exception Exception caught
         */
        void onInsertDataFailed(Exception exception);
    }
}
