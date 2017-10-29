package com.alkisum.android.cloudnotes.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alkisum.android.cloudlib.events.DownloadEvent;
import com.alkisum.android.cloudlib.events.TxtFileReaderEvent;
import com.alkisum.android.cloudlib.events.TxtFileWriterEvent;
import com.alkisum.android.cloudlib.events.UploadEvent;
import com.alkisum.android.cloudlib.net.ConnectDialog;
import com.alkisum.android.cloudlib.net.ConnectInfo;
import com.alkisum.android.cloudnotes.R;
import com.alkisum.android.cloudnotes.adapters.NoteListAdapter;
import com.alkisum.android.cloudnotes.database.Db;
import com.alkisum.android.cloudnotes.database.Notes;
import com.alkisum.android.cloudnotes.dialogs.ErrorDialog;
import com.alkisum.android.cloudnotes.events.InsertEvent;
import com.alkisum.android.cloudnotes.model.Note;
import com.alkisum.android.cloudnotes.model.NoteDao;
import com.alkisum.android.cloudnotes.net.Downloader;
import com.alkisum.android.cloudnotes.net.Uploader;
import com.alkisum.android.cloudnotes.ui.AppBar;
import com.alkisum.android.cloudnotes.ui.ThemePref;
import com.alkisum.android.cloudnotes.utils.Pref;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;

/**
 * Main activity listing the notes stored in the database.
 *
 * @author Alkisum
 * @version 2.0
 * @since 1.0
 */
public class MainActivity extends AppCompatActivity implements
        ConnectDialog.ConnectDialogListener,
        NavigationView.OnNavigationItemSelectedListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * Subscriber id to use when receiving event.
     */
    private static final int SUBSCRIBER_ID = 481;

    /**
     * Request code for result from NoteActivity.
     */
    private static final int NOTE_DELETED_REQUEST = 649;

    /**
     * Operation id for download.
     */
    private static final int DOWNLOAD_OPERATION = 1;

    /**
     * Operation id for upload.
     */
    private static final int UPLOAD_OPERATION = 2;

    /**
     * List adapter for the list view listing the notes.
     */
    private NoteListAdapter listAdapter;

    /**
     * Note DAO instance.
     */
    private final NoteDao dao = Db.getInstance().getDaoSession().getNoteDao();

    /**
     * Drawer toggle.
     */
    private ActionBarDrawerToggle toggle;

    /**
     * Shared preferences.
     */
    private SharedPreferences sharedPref;

    /**
     * Flag set to true if the theme has been changed from the Settings
     * activity, false otherwise.
     */
    private boolean themeChanged;

    /**
     * Toolbar.
     */
    private Toolbar toolbar;

    /**
     * List view listing the notes.
     */
    @BindView(R.id.main_list)
    ListView listView;

    /**
     * Button to add a note.
     */
    @BindView(R.id.main_fab)
    FloatingActionButton fab;

    /**
     * TextView shown when no note is available.
     */
    @BindView(R.id.main_no_note)
    TextView noNoteTextView;

    /**
     * Navigation drawer layout.
     */
    @BindView(R.id.main_drawer_layout)
    DrawerLayout drawer;

    /**
     * Progress bar to show the progress of operations.
     */
    @BindView(R.id.main_progressbar)
    ProgressBar progressBar;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ThemePref.applyTheme(this);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.registerOnSharedPreferenceChangeListener(this);

        // Toolbar
        toolbar = AppBar.inflate(this, R.id.main_stub_app_bar);
        toolbar.setNavigationIcon(R.drawable.ic_close_black_24dp);
        setSupportActionBar(toolbar);

        toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.nav_drawer_open, R.string.nav_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.main_nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(ThemePref.getNavIconTint(this));
        navigationView.setItemTextColor(ThemePref.getNavTextColor(this));

        listAdapter = new NoteListAdapter(this, loadNotes());
        listView.setAdapter(listAdapter);

        // Register onCreate to receive events even when NoteActivity is open
        EventBus.getDefault().register(this);
    }

    @Override
    protected final void onStart() {
        super.onStart();
        if (themeChanged) {
            themeChanged = false;
            Pref.reload(this);
            return;
        }
        refreshList();
    }

    @Override
    public final void onDestroy() {
        super.onDestroy();
        sharedPref.unregisterOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public final void onSharedPreferenceChanged(
            final SharedPreferences sharedPreferences, final String key) {
        switch (key) {
            case Pref.THEME:
                themeChanged = true;
                break;
            case Pref.PRIMARY_COLOR:
                themeChanged = true;
                break;
            case Pref.ACCENT_COLOR:
                themeChanged = true;
                break;
            case Pref.LIGHT_STATUS_BAR:
                themeChanged = true;
                break;
            default:
                break;
        }
    }

    /**
     * Reload the list of notes and notify the list adapter.
     */
    private void refreshList() {
        List<Note> notes = loadNotes();
        if (notes.isEmpty()) {
            listView.setVisibility(View.GONE);
            noNoteTextView.setVisibility(View.VISIBLE);
        } else {
            noNoteTextView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }
        listAdapter.setNotes(loadNotes());
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public final boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public final boolean onPrepareOptionsMenu(final Menu menu) {
        menu.findItem(R.id.action_download).setVisible(
                !listAdapter.isEditMode());
        menu.findItem(R.id.action_delete).setVisible(listAdapter.isEditMode());
        menu.findItem(R.id.action_upload).setVisible(listAdapter.isEditMode());
        menu.findItem(R.id.action_select_all).setVisible(
                listAdapter.isEditMode());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public final boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_download:
                ConnectDialog connectDialogDownload =
                        ConnectDialog.newInstance(DOWNLOAD_OPERATION);
                connectDialogDownload.setCallback(this);
                connectDialogDownload.show(getSupportFragmentManager(),
                        ConnectDialog.FRAGMENT_TAG);
                return true;
            case R.id.action_delete:
                final List<Note> selectedNotes = Notes.getSelectedNotes();
                if (!selectedNotes.isEmpty()) {
                    deleteNotes(selectedNotes);
                }
                return true;
            case R.id.action_upload:
                if (!Notes.getSelectedNotes().isEmpty()) {
                    ConnectDialog connectDialogUpload =
                            ConnectDialog.newInstance(UPLOAD_OPERATION);
                    connectDialogUpload.setCallback(this);
                    connectDialogUpload.show(getSupportFragmentManager(),
                            ConnectDialog.FRAGMENT_TAG);
                }
                return true;
            case R.id.action_select_all:
                selectAll();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when the floating button is clicked.
     */
    @OnClick(R.id.main_fab)
    public final void onFabClicked() {
        Intent intent = new Intent(this, NoteActivity.class);
        startActivityForResult(intent, NOTE_DELETED_REQUEST);
    }

    /**
     * Called when an item on the list view is clicked.
     *
     * @param position The position of the view in the list
     * @param id       The row id of the item that was clicked
     */
    @OnItemClick(R.id.main_list)
    public final void onNoteClicked(final int position, final long id) {
        if (listAdapter.isEditMode()) {
            listAdapter.setNoteSelected(position);
            listAdapter.notifyDataSetChanged();
        } else {
            Intent intent = new Intent(this, NoteActivity.class);
            intent.putExtra(NoteActivity.ARG_NOTE_ID, id);
            startActivityForResult(intent, NOTE_DELETED_REQUEST);
        }
    }

    /**
     * Called when an item on the list view is long clicked.
     *
     * @param position The position of the view in the list
     * @return true if the callback consumed the long click, false otherwise
     */
    @OnItemLongClick(R.id.main_list)
    public final boolean onNoteLongClicked(final int position) {
        setEditMode(true);
        listAdapter.setNoteSelected(position);
        listAdapter.notifyDataSetChanged();
        return true;
    }

    /**
     * Delete selected notes.
     *
     * @param selectedNotes List of selected notes to delete
     */
    private void deleteNotes(final List<Note> selectedNotes) {
        for (Note note : selectedNotes) {
            dao.delete(note);
        }
        setEditMode(false);
        listAdapter.setNotes(loadNotes());
        listAdapter.notifyDataSetChanged();
        Snackbar.make(fab, R.string.delete_notes_snackbar, Snackbar.LENGTH_LONG)
                .setAction(R.string.action_undo, new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        dao.insertInTx(selectedNotes);
                        listAdapter.setNotes(loadNotes());
                        listAdapter.notifyDataSetChanged();
                    }
                }).show();
    }

    /**
     * Set the edit mode and change the GUI according to the new mode.
     *
     * @param editMode true if the mode to set is the edit mode, false otherwise
     */
    private void setEditMode(final boolean editMode) {
        if (editMode) {
            fab.setVisibility(View.GONE);
            toggle.setDrawerIndicatorEnabled(false);
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    setEditMode(false);
                }
            });
        } else {
            fab.setVisibility(View.VISIBLE);
            toggle.setDrawerIndicatorEnabled(true);
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    drawer.openDrawer(GravityCompat.START);
                }
            });
        }
        listAdapter.setEditMode(editMode);
        listAdapter.notifyDataSetChanged();
        invalidateOptionsMenu();
    }

    /**
     * Select all notes listed in the list.
     */
    private void selectAll() {
        listAdapter.selectAll();
        listAdapter.notifyDataSetChanged();
    }

    /**
     * Load all the notes and order them in the anti-chronological order.
     *
     * @return List of notes
     */
    private List<Note> loadNotes() {
        return dao.queryBuilder().orderDesc(NoteDao.Properties.UpdatedTime)
                .list();
    }

    @Override
    public final void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (listAdapter.isEditMode()) {
                setEditMode(false);
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public final boolean onNavigationItemSelected(
            @NonNull final MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected final void onActivityResult(final int requestCode,
                                          final int resultCode,
                                          final Intent data) {
        if (requestCode == NOTE_DELETED_REQUEST) {
            if (resultCode == RESULT_OK) {
                String json = data.getStringExtra(NoteActivity.ARG_NOTE_JSON);
                final Note note = new Gson().fromJson(json, Note.class);
                Snackbar.make(fab, R.string.delete_note_snackbar,
                        Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_undo,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(final View v) {
                                        dao.insert(note);
                                        listAdapter.setNotes(loadNotes());
                                        listAdapter.notifyDataSetChanged();
                                    }
                                }).show();
            }
        }
    }

    @Override
    public final void onSubmit(final int operation,
                               final ConnectInfo connectInfo) {
        if (operation == DOWNLOAD_OPERATION) {
            new Downloader(getApplicationContext(), connectInfo,
                    new Intent(this, MainActivity.class), SUBSCRIBER_ID);
        } else if (operation == UPLOAD_OPERATION) {
            new Uploader(getApplicationContext(), connectInfo,
                    new Intent(this, MainActivity.class),
                    Notes.getSelectedNotes(), SUBSCRIBER_ID);
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
     * Triggered on download event.
     *
     * @param event Download event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onDownloadEvent(final DownloadEvent event) {
        if (!event.isSubscriberAllowed(SUBSCRIBER_ID)) {
            return;
        }
        switch (event.getResult()) {
            case DownloadEvent.DOWNLOADING:
                progressBar.setVisibility(View.VISIBLE);
                break;
            case DownloadEvent.OK:
                progressBar.setVisibility(View.VISIBLE);
                break;
            case DownloadEvent.NO_FILE:
                Snackbar.make(fab, R.string.download_no_file_snackbar,
                        Snackbar.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
                break;
            case DownloadEvent.ERROR:
                ErrorDialog.show(this,
                        getString(R.string.download_failure_title),
                        event.getMessage());
                progressBar.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    /**
     * Triggered on TXT file reader event.
     *
     * @param event TXT file reader event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onTxtFileReaderEvent(final TxtFileReaderEvent event) {
        if (!event.isSubscriberAllowed(SUBSCRIBER_ID)) {
            return;
        }
        switch (event.getResult()) {
            case TxtFileReaderEvent.OK:
                progressBar.setVisibility(View.VISIBLE);
                break;
            case TxtFileReaderEvent.ERROR:
                ErrorDialog.show(this,
                        getString(R.string.download_reading_failure_title),
                        event.getException().getMessage());
                progressBar.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    /**
     * Triggered on inserter event.
     *
     * @param event Inserter event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onInsertEvent(final InsertEvent event) {
        switch (event.getResult()) {
            case InsertEvent.OK:
                refreshList();
                Snackbar.make(fab, R.string.download_success_snackbar,
                        Snackbar.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
                break;
            case InsertEvent.ERROR:
                ErrorDialog.show(this,
                        getString(R.string.download_insert_failure_title),
                        event.getException().getMessage());
                progressBar.setVisibility(View.GONE);
                break;
            default:
                break;
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
                Snackbar.make(fab, R.string.upload_success_snackbar,
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
}
