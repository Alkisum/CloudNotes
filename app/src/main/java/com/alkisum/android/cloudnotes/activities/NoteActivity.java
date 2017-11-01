package com.alkisum.android.cloudnotes.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alkisum.android.cloudlib.events.TxtFileWriterEvent;
import com.alkisum.android.cloudlib.events.UploadEvent;
import com.alkisum.android.cloudlib.net.ConnectDialog;
import com.alkisum.android.cloudlib.net.ConnectInfo;
import com.alkisum.android.cloudnotes.R;
import com.alkisum.android.cloudnotes.database.Db;
import com.alkisum.android.cloudnotes.database.Deleter;
import com.alkisum.android.cloudnotes.database.Notes;
import com.alkisum.android.cloudnotes.dialogs.ErrorDialog;
import com.alkisum.android.cloudnotes.events.DeleteEvent;
import com.alkisum.android.cloudnotes.model.Note;
import com.alkisum.android.cloudnotes.model.NoteDao;
import com.alkisum.android.cloudnotes.net.Uploader;
import com.alkisum.android.cloudnotes.ui.AppBar;
import com.alkisum.android.cloudnotes.ui.ThemePref;
import com.alkisum.android.cloudnotes.utils.KeyboardUtil;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Activity showing a note and enabling the user to make actions on it.
 *
 * @author Alkisum
 * @version 2.0
 * @since 1.0
 */
public class NoteActivity extends AppCompatActivity implements
        ConnectDialog.ConnectDialogListener {

    /**
     * Argument for the note id.
     */
    static final String ARG_NOTE_ID = "arg_note_id";

    /**
     * Argument for the JSON representation of the note, sent to the
     * MainActivity when the note has been deleted.
     */
    static final String ARG_NOTE_JSON = "arg_note_json";

    /**
     * Subscriber id to use when receiving event.
     */
    private static final int SUBSCRIBER_ID = 354;

    /**
     * Operation id for upload.
     */
    private static final int UPLOAD_OPERATION = 1;

    /**
     * Flag set to true if the edit mode is on, false otherwise.
     */
    private boolean editMode;

    /**
     * Note instance. Can be null if the activity is used to create a new note.
     */
    private Note note;

    /**
     * Note DAO.
     */
    private final NoteDao dao = Db.getInstance().getDaoSession().getNoteDao();

    /**
     * TextView containing the note title.
     */
    @BindView(R.id.note_text_title)
    TextView titleTextView;

    /**
     * TextView containing the note content.
     */
    @BindView(R.id.note_text_content)
    TextView contentTextView;

    /**
     * EditText containing the note title.
     */
    @BindView(R.id.note_edit_title)
    EditText titleEditText;

    /**
     * EditText containing the note content.
     */
    @BindView(R.id.note_edit_content)
    EditText contentEditText;

    /**
     * Activity root layout.
     */
    @BindView(R.id.note_layout_root)
    LinearLayout rootLayout;

    /**
     * Progress bar to show the progress of operations.
     */
    @BindView(R.id.note_progressbar)
    ProgressBar progressBar;

    /**
     * Helper to move the content edit text up when the keyboard is shown.
     */
    private KeyboardUtil keyboardUtil;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ThemePref.applyTheme(this);

        setContentView(R.layout.activity_note);
        ButterKnife.bind(this);

        // Toolbar
        Toolbar toolbar = AppBar.inflate(this, R.id.note_stub_app_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Get intent, extras, action and MIME type
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String action = intent.getAction();
        String type = intent.getType();

        note = null;
        if (extras != null) {
            // Started from MainActivity
            long id = extras.getLong(ARG_NOTE_ID);
            note = Db.getInstance().getDaoSession().getNoteDao().load(id);
        }

        if (note != null) {
            // Read mode
            setEditMode(false);
        } else {
            // Edit mode
            setToolbarTitle(R.string.note_toolbar_title_create);
            setEditMode(true);
        }

        if (Intent.ACTION_SEND.equals(action)
                && type != null && "text/plain".equals(type)) {
            // Started from other app
            String extraSubject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
            if (extraSubject != null) {
                titleEditText.setText(extraSubject);
            }
            String extraText = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (extraText != null) {
                contentEditText.setText(extraText);
            }
        }

        // Enable KeyboardUtil only when the windowTranslucentStatus is enabled
        if (!ThemePref.isLightStatusBarEnabled(this)) {
            keyboardUtil = new KeyboardUtil(this, rootLayout);
            keyboardUtil.enable();
        }
    }

    @Override
    public final void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public final void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected final void onDestroy() {
        super.onDestroy();
        if (!ThemePref.isLightStatusBarEnabled(this)) {
            keyboardUtil.disable();
        }
    }

    @Override
    public final boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public final boolean onPrepareOptionsMenu(final Menu menu) {
        menu.findItem(R.id.action_save).setVisible(editMode);
        menu.findItem(R.id.action_edit).setVisible(!editMode);
        menu.findItem(R.id.action_delete).setVisible(!editMode);
        menu.findItem(R.id.action_share).setVisible(!editMode);
        menu.findItem(R.id.action_upload).setVisible(!editMode);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public final boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (editMode && note != null) {
                    setToolbarTitle(R.string.app_name);
                    setEditMode(false);
                } else {
                    hideKeyboard();
                    super.onBackPressed();
                }
                return true;
            case R.id.action_save:
                saveNote();
                return true;
            case R.id.action_edit:
                setToolbarTitle(R.string.note_toolbar_title_edit);
                setEditMode(true);
                return true;
            case R.id.action_delete:
                note.setSelected(true);
                if (!Notes.getSelectedNotes().isEmpty()) {
                    deleteNote();
                }
                return true;
            case R.id.action_share:
                share();
                break;
            case R.id.action_upload:
                ConnectDialog connectDialogUpload =
                        ConnectDialog.newInstance(UPLOAD_OPERATION);
                connectDialogUpload.setCallback(this);
                connectDialogUpload.show(getSupportFragmentManager(),
                        ConnectDialog.FRAGMENT_TAG);
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Set the edit mode and change the GUI according to the new mode.
     *
     * @param editMode true if the mode to set is the edit mode, false otherwise
     */
    private void setEditMode(final boolean editMode) {
        this.editMode = editMode;
        String title = "";
        String content = "";
        if (note != null) {
            title = note.getTitle();
            content = note.getContent();
        }
        titleTextView.setText(title);
        contentTextView.setText(content);
        titleEditText.setText(title);
        contentEditText.setText(content);
        if (editMode) {
            titleTextView.setVisibility(View.GONE);
            contentTextView.setVisibility(View.GONE);
            titleEditText.setVisibility(View.VISIBLE);
            contentEditText.setVisibility(View.VISIBLE);
            setToolbarIcon(R.drawable.ic_close_black_24dp);
            showKeyboard();
        } else {
            hideKeyboard();
            titleEditText.setVisibility(View.GONE);
            contentEditText.setVisibility(View.GONE);
            titleTextView.setVisibility(View.VISIBLE);
            contentTextView.setVisibility(View.VISIBLE);
            setToolbarIcon(R.drawable.ic_arrow_back_black_24dp);
        }
        invalidateOptionsMenu();
    }

    /**
     * Save the note being edited. Insert the note if it does not already exist,
     * or update the note.
     */
    private void saveNote() {
        long time = System.currentTimeMillis();
        if (note == null) {
            note = new Note(null,
                    titleEditText.getText().toString(),
                    contentEditText.getText().toString(),
                    time);
            Notes.setUniqueTitle(note);
            dao.insert(note);
        } else {
            note.setTitle(titleEditText.getText().toString());
            note.setContent(contentEditText.getText().toString());
            note.setUpdatedTime(time);
            Notes.setUniqueTitle(note);
            dao.update(note);
        }
        setToolbarTitle(R.string.app_name);
        setEditMode(false);
    }

    /**
     * Delete the note.
     */
    private void deleteNote() {
        new Deleter(new Integer[]{SUBSCRIBER_ID}).execute();
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public final void onSubmit(final int operation,
                               final ConnectInfo connectInfo) {
        if (operation == UPLOAD_OPERATION) {
            Intent intent = new Intent(this, NoteActivity.class);
            intent.putExtra(ARG_NOTE_ID, note.getId());
            List<Note> notes = new ArrayList<>();
            notes.add(note);
            new Uploader(getApplicationContext(), connectInfo, intent, notes,
                    SUBSCRIBER_ID);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setIndeterminate(true);
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Share the note using the Intent object.
     */
    private void share() {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, note.getTitle());
        intent.putExtra(android.content.Intent.EXTRA_TEXT, note.getContent());
        startActivity(Intent.createChooser(intent,
                getString(R.string.action_share)));
    }

    @Override
    public final void onBackPressed() {
        if (editMode && note != null) {
            setToolbarTitle(R.string.app_name);
            setEditMode(false);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Set the toolbar title with the given string.
     *
     * @param resId Resource ID of title string to set
     */
    private void setToolbarTitle(final int resId) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(resId);
        }
    }

    /**
     * Set the toolbar icon with the given drawable.
     *
     * @param resId Resource ID of a drawable to use for the up indicator
     */
    private void setToolbarIcon(final int resId) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(resId);
        }
    }

    /**
     * Show the keyboard.
     */
    private void showKeyboard() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                titleEditText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(titleEditText,
                            InputMethodManager.SHOW_FORCED);
                }
            }
        }, 100);
    }

    /**
     * Hide the keyboard.
     */
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    /**
     * Triggered on TXT file writer event.
     *
     * @param event TXT file writer event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onTxtFileWriterEvent(final TxtFileWriterEvent event) {
        if (!event.isSubscriberAllowed(SUBSCRIBER_ID)) {
            return;
        }
        switch (event.getResult()) {
            case TxtFileWriterEvent.OK:
                progressBar.setVisibility(View.VISIBLE);
                break;
            case TxtFileWriterEvent.ERROR:
                ErrorDialog.show(this,
                        getString(R.string.upload_writing_failure_title),
                        event.getException().getMessage());
                progressBar.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    /**
     * Triggered on upload event.
     *
     * @param event Upload event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onUploadEvent(final UploadEvent event) {
        if (!event.isSubscriberAllowed(SUBSCRIBER_ID)) {
            return;
        }
        switch (event.getResult()) {
            case UploadEvent.UPLOADING:
                progressBar.setVisibility(View.VISIBLE);
                break;
            case UploadEvent.OK:
                Snackbar.make(findViewById(R.id.note_layout_main),
                        R.string.upload_success_snackbar,
                        Snackbar.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
                break;
            case UploadEvent.ERROR:
                ErrorDialog.show(this, getString(
                        R.string.upload_failure_title), event.getMessage());
                progressBar.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    /**
     * Triggered on delete event.
     *
     * @param event Delete event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onDeleteEvent(final DeleteEvent event) {
        if (!event.isSubscriberAllowed(SUBSCRIBER_ID)) {
            return;
        }

        // Notify MainActivity that a note has been deleted
        Intent intent = new Intent();
        intent.putExtra(ARG_NOTE_JSON, new Gson().toJson(note));
        setResult(RESULT_OK, intent);
        finish();
    }
}
