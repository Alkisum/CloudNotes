package com.alkisum.android.cloudnotes.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alkisum.android.cloudnotes.R;
import com.alkisum.android.cloudnotes.database.Db;
import com.alkisum.android.cloudnotes.model.Note;
import com.alkisum.android.cloudnotes.model.NoteDao;
import com.alkisum.android.cloudnotes.net.CloudLibHelper;
import com.alkisum.android.cloudnotes.ui.AppBar;
import com.alkisum.android.cloudnotes.ui.ThemePref;
import com.alkisum.android.cloudnotes.utils.KeyboardUtil;
import com.google.gson.Gson;

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
public class NoteActivity extends AppCompatActivity {

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
     * CloudLibHelper instance that implements all CloudOps interfaces.
     */
    private CloudLibHelper cloudLibHelper;

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

        note = null;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            long id = bundle.getLong(ARG_NOTE_ID);
            note = Db.getInstance().getDaoSession().getNoteDao().load(id);
        }

        // Enable KeyboardUtil only when the windowTranslucentStatus is enabled
        if (!ThemePref.isLightStatusBarEnabled(this)) {
            keyboardUtil = new KeyboardUtil(this, rootLayout);
            keyboardUtil.enable();
        }

        if (note != null) {
            // Read mode
            setEditMode(false);
        } else {
            // Edit mode
            setToolbarTitle(R.string.note_toolbar_title_create);
            setEditMode(true);
        }

        cloudLibHelper = new CloudLibHelper(this);
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
                deleteNote();
                return true;
            case R.id.action_share:
                share();
                break;
            case R.id.action_upload:
                List<Note> notes = new ArrayList<>();
                notes.add(note);
                cloudLibHelper.onUploadAction(notes);
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
                    time,
                    time);
            dao.insert(note);
        } else {
            note.setTitle(titleEditText.getText().toString());
            note.setContent(contentEditText.getText().toString());
            note.setUpdatedTime(time);
            dao.update(note);
        }
        setToolbarTitle(R.string.app_name);
        setEditMode(false);
    }

    /**
     * Delete the note.
     */
    private void deleteNote() {
        if (note != null) {
            dao.delete(note);
            // Notify MainActivity that a note has been deleted
            Intent intent = new Intent();
            intent.putExtra(ARG_NOTE_JSON, new Gson().toJson(note));
            setResult(RESULT_OK, intent);
        }
        finish();
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
                imm.showSoftInput(titleEditText,
                        InputMethodManager.SHOW_FORCED);
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
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
