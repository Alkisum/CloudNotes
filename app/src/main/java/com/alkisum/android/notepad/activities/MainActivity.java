package com.alkisum.android.notepad.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alkisum.android.cloudops.file.json.JsonFile;
import com.alkisum.android.cloudops.net.ConnectDialog;
import com.alkisum.android.cloudops.net.ConnectInfo;
import com.alkisum.android.cloudops.net.owncloud.OcDownloader;
import com.alkisum.android.cloudops.net.owncloud.OcUploader;
import com.alkisum.android.notepad.R;
import com.alkisum.android.notepad.adapters.NoteListAdapter;
import com.alkisum.android.notepad.database.Db;
import com.alkisum.android.notepad.database.Inserter;
import com.alkisum.android.notepad.database.Notes;
import com.alkisum.android.notepad.dialogs.ConfirmDialog;
import com.alkisum.android.notepad.dialogs.ErrorDialog;
import com.alkisum.android.notepad.files.Json;
import com.alkisum.android.notepad.model.Note;
import com.alkisum.android.notepad.model.NoteDao;
import com.owncloud.android.lib.resources.files.RemoteFile;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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
 * @version 1.1
 * @since 1.0
 */
public class MainActivity extends AppCompatActivity implements
        ConnectDialog.ConnectDialogListener, OcUploader.UploaderListener,
        OcDownloader.OcDownloaderListener, Inserter.InserterListener {

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
     * Progress dialog to show the progress of operations.
     */
    private ProgressDialog progressDialog;

    /**
     * Note DAO instance.
     */
    private NoteDao dao = Db.getInstance().getDaoSession().getNoteDao();

    /**
     * OcDownloader instance created when the user presses on the Download item
     * from the option menu, and initialized when the connect dialog is submit.
     */
    private OcDownloader downloader;

    /**
     * OcUploader instance created when the user presses on the Upload item from
     * the option menu, and initialized when the connect dialog is submit.
     */
    private OcUploader uploader;

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

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Toolbar toolbar = ButterKnife.findById(this, R.id.main_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(
                    R.drawable.ic_close_white_24dp);
        }

        listAdapter = new NoteListAdapter(this, loadNotes());
        listView.setAdapter(listAdapter);
    }

    @Override
    protected final void onStart() {
        super.onStart();
        refreshList();
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
        menu.findItem(R.id.action_about).setVisible(!listAdapter.isEditMode());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public final boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setEditMode(false);
                return true;
            case R.id.action_download:
                DialogFragment connectDialogDownload =
                        ConnectDialog.newInstance(DOWNLOAD_OPERATION);
                connectDialogDownload.show(getSupportFragmentManager(),
                        ConnectDialog.FRAGMENT_TAG);
                downloader = new OcDownloader(this);
                return true;
            case R.id.action_delete:
                ConfirmDialog.show(this,
                        getString(R.string.delete_notes_title),
                        getString(R.string.delete_notes_message),
                        getString(R.string.action_delete),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                                final int which) {
                                deleteNotes();
                            }
                        });
                return true;
            case R.id.action_upload:
                List<Note> notes = Notes.getSelectedNotes();
                if (!notes.isEmpty()) {
                    DialogFragment connectDialogUpload =
                            ConnectDialog.newInstance(UPLOAD_OPERATION);
                    connectDialogUpload.show(getSupportFragmentManager(),
                            ConnectDialog.FRAGMENT_TAG);
                    try {
                        uploader = new OcUploader(this,
                                Json.buildJsonFilesFromNotes(notes));
                    } catch (JSONException e) {
                        ErrorDialog.show(this,
                                getString(R.string.upload_failure_title),
                                e.getMessage());
                    }
                }
                return true;
            case R.id.action_select_all:
                selectAll();
                return true;
            case R.id.action_about:
                startAboutActivity();
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
        startActivity(intent);
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
            startActivity(intent);
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
     */
    private void deleteNotes() {
        for (Note note : listAdapter.getNotes()) {
            if (note.isSelected()) {
                dao.delete(note);
            }
        }
        setEditMode(false);
        listAdapter.setNotes(loadNotes());
        listAdapter.notifyDataSetChanged();
    }

    /**
     * Set the edit mode and change the GUI according to the new mode.
     *
     * @param editMode true if the mode to set is the edit mode, false otherwise
     */
    private void setEditMode(final boolean editMode) {
        if (editMode) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            fab.setVisibility(View.GONE);
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
            fab.setVisibility(View.VISIBLE);
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
        return dao.queryBuilder().orderDesc(NoteDao.Properties.Time).list();
    }

    /**
     * Start the AboutActivity.
     */
    private void startAboutActivity() {
        startActivity(new Intent(this, AboutActivity.class));
    }

    @Override
    public final void onBackPressed() {
        if (listAdapter.isEditMode()) {
            setEditMode(false);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Start the download operation.
     *
     * @param connectInfo Connection information given by user
     */
    private void startDownload(final ConnectInfo connectInfo) {
        if (downloader == null) {
            return;
        }
        downloader.init(
                connectInfo.getAddress(),
                connectInfo.getPath(),
                connectInfo.getUsername(),
                connectInfo.getPassword()).start();
    }

    /**
     * Start the upload operation.
     *
     * @param connectInfo Connection information given by user
     */
    private void startUpload(final ConnectInfo connectInfo) {
        if (uploader == null) {
            return;
        }
        uploader.init(
                connectInfo.getAddress(),
                connectInfo.getPath(),
                connectInfo.getUsername(),
                connectInfo.getPassword()).start();
    }

    @Override
    public final void onSubmit(final int operation,
                               final ConnectInfo connectInfo) {
        if (operation == DOWNLOAD_OPERATION) {
            startDownload(connectInfo);
        } else if (operation == UPLOAD_OPERATION) {
            startUpload(connectInfo);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setIndeterminate(true);
                progressDialog.setProgressStyle(
                        ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setProgressNumberFormat(null);
                progressDialog.setMessage(getString(
                        R.string.operation_progress_init_msg));
                progressDialog.show();
            }
        });
    }

    @Override
    public final void onWritingFileFailed(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                ErrorDialog.show(MainActivity.this,
                        getString(R.string.upload_writing_failure_title),
                        e.getMessage());
            }
        });
    }

    @Override
    public final void onUploadStart(final JsonFile jsonFile) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    progressDialog.setMessage("Uploading "
                            + jsonFile.getName() + " ...");
                    progressDialog.setIndeterminate(false);
                }
            }
        });
    }

    @Override
    public final void onUploading(final int percentage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    progressDialog.setProgress(percentage);
                }
            }
        });
    }

    @Override
    public final void onAllUploadComplete() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                Toast.makeText(MainActivity.this,
                        getString(R.string.upload_success_toast),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public final void onUploadFailed(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                ErrorDialog.show(MainActivity.this, getString(
                        R.string.upload_failure_title), message);
            }
        });
    }

    @Override
    public final void onDownloadStart(final RemoteFile file) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    progressDialog.setMessage("Downloading "
                            + file.getRemotePath() + " ...");
                    progressDialog.setIndeterminate(false);
                }
            }
        });
    }

    @Override
    public final void onNoFileToDownload() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                Toast.makeText(MainActivity.this, getString(R.string.
                        download_no_file_toast), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public final void onDownloading(final int percentage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    progressDialog.setProgress(percentage);
                }
            }
        });
    }

    @Override
    public final void onAllDownloadComplete() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    progressDialog.setMessage(
                            getString(R.string.download_reading_msg));
                    progressDialog.setProgressPercentFormat(null);
                    progressDialog.setIndeterminate(true);
                }
            }
        });
    }

    @Override
    public final void onDownloadFailed(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                ErrorDialog.show(MainActivity.this, getString(
                        R.string.download_failure_title), message);
            }
        });
    }

    @Override
    public final void onJsonFilesRead(final List<JsonFile> jsonFiles) {
        List<JSONObject> jsonObjects = new ArrayList<>();
        for (JsonFile jsonFile : jsonFiles) {
            if (Json.isFileNameValid(jsonFile)
                    && !Json.isNoteAlreadyInDb(jsonFile)) {
                jsonObjects.add(jsonFile.getJsonObject());
            }
        }

        if (jsonObjects.isEmpty()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    Toast.makeText(MainActivity.this, getString(R.string.
                                    download_no_file_toast),
                            Toast.LENGTH_LONG).show();
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (progressDialog != null) {
                        progressDialog.setMessage(
                                getString(R.string.download_inserting_msg));
                        progressDialog.setProgressPercentFormat(null);
                        progressDialog.setIndeterminate(true);
                    }
                }
            });
            new Inserter(this, jsonObjects).execute();
        }
    }

    @Override
    public final void onReadingFileFailed(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                ErrorDialog.show(MainActivity.this,
                        getString(R.string.download_reading_failure_title),
                        e.getMessage());
            }
        });
    }

    @Override
    public final void onDataInserted() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                refreshList();
                Toast.makeText(MainActivity.this, getString(R.string.
                        download_success_toast), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public final void onInsertDataFailed(final Exception exception) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                ErrorDialog.show(MainActivity.this,
                        getString(R.string.download_insert_failure_title),
                        exception.getMessage());
            }
        });
    }
}
