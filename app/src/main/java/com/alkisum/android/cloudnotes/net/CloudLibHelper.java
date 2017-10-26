package com.alkisum.android.cloudnotes.net;

import android.app.ProgressDialog;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.alkisum.android.cloudlib.file.json.JsonFile;
import com.alkisum.android.cloudlib.net.ConnectDialog;
import com.alkisum.android.cloudlib.net.ConnectInfo;
import com.alkisum.android.cloudnotes.R;
import com.alkisum.android.cloudnotes.activities.MainActivity;
import com.alkisum.android.cloudnotes.activities.NoteActivity;
import com.alkisum.android.cloudnotes.database.Inserter;
import com.alkisum.android.cloudnotes.dialogs.ErrorDialog;
import com.alkisum.android.cloudnotes.files.Json;
import com.alkisum.android.cloudnotes.model.Note;
import com.owncloud.android.lib.resources.files.RemoteFile;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Class implementing CloudLib interfaces.
 *
 * @author Alkisum
 * @version 2.0
 * @since 1.1
 */
public class CloudLibHelper implements
        ConnectDialog.ConnectDialogListener, Inserter.InserterListener {

    /**
     * Log tag.
     */
    private static final String TAG = "CloudLibHelper";

    /**
     * Operation id for download.
     */
    private static final int DOWNLOAD_OPERATION = 1;

    /**
     * Operation id for upload.
     */
    private static final int UPLOAD_OPERATION = 2;

    /**
     * Activity to help.
     */
    private final AppCompatActivity activity;

    /**
     * CloudLibHelperListener instance, can be null if the activity does not
     * implement CloudLibHelperListener.
     */
    private CloudLibHelperListener callback;

    /**
     * Progress dialog to show the progress of operations.
     */
    private ProgressDialog progressDialog;

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
     * CloudOps constructor.
     *
     * @param activity Activity to help
     */
    public CloudLibHelper(final AppCompatActivity activity) {
        this.activity = activity;
        try {
            callback = (CloudLibHelperListener) activity;
        } catch (ClassCastException e) {
            Log.w(TAG, activity.getClass().getSimpleName()
                    + " does not implement CloudLibHelperListener");
        }
    }

    /**
     * Triggered by the download action from the activity menu.
     */
    public final void onDownloadAction() {
        ConnectDialog connectDialog =
                ConnectDialog.newInstance(DOWNLOAD_OPERATION);
        connectDialog.setCallback(this);
        connectDialog.show(activity.getSupportFragmentManager(),
                ConnectDialog.FRAGMENT_TAG);
        downloader = new OcDownloader(activity, this);
    }

    /**
     * Triggered by the upload action from the activity menu.
     *
     * @param notes List of notes to upload
     */
    public final void onUploadAction(final List<Note> notes) {
        if (!notes.isEmpty()) {
            ConnectDialog connectDialog =
                    ConnectDialog.newInstance(UPLOAD_OPERATION);
            connectDialog.setCallback(this);
            connectDialog.show(activity.getSupportFragmentManager(),
                    ConnectDialog.FRAGMENT_TAG);
            try {
                uploader = new OcUploader(activity, this,
                        Json.buildJsonFilesFromNotes(notes));
            } catch (JSONException e) {
                ErrorDialog.show(activity,
                        activity.getString(R.string.upload_failure_title),
                        e.getMessage());
            }
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

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog = new ProgressDialog(activity);
                progressDialog.setIndeterminate(true);
                progressDialog.setProgressStyle(
                        ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setProgressNumberFormat(null);
                progressDialog.setMessage(activity.getString(
                        R.string.operation_progress_init_msg));
                progressDialog.show();
            }
        });
    }

    @Override
    public final void onWritingFileFailed(final Exception e) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                ErrorDialog.show(activity, activity.getString(
                        R.string.upload_writing_failure_title),
                        e.getMessage());
            }
        });
    }

    @Override
    public final void onUploadStart(final JsonFile jsonFile) {
        activity.runOnUiThread(new Runnable() {
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
        activity.runOnUiThread(new Runnable() {
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
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                View view = activity.findViewById(android.R.id.content);
                if (activity instanceof MainActivity) {
                    view = activity.findViewById(R.id.main_list);
                } else if (activity instanceof NoteActivity) {
                    view = activity.findViewById(R.id.note_layout_main);
                }
                Snackbar.make(view,
                        activity.getString(R.string.upload_success_feedback),
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public final void onUploadFailed(final String message) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                ErrorDialog.show(activity, activity.getString(
                        R.string.upload_failure_title), message);
            }
        });
    }

    @Override
    public final void onDownloadStart(final RemoteFile file) {
        activity.runOnUiThread(new Runnable() {
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
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                Snackbar.make(activity.findViewById(R.id.main_fab),
                        activity.getString(R.string.download_no_file_feedback),
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public final void onDownloading(final int percentage) {
        activity.runOnUiThread(new Runnable() {
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
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    progressDialog.setMessage(
                            activity.getString(R.string.download_reading_msg));
                    progressDialog.setProgressPercentFormat(null);
                    progressDialog.setIndeterminate(true);
                }
            }
        });
    }

    @Override
    public final void onDownloadFailed(final String message) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                ErrorDialog.show(activity, activity.getString(
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
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    Snackbar.make(activity.findViewById(R.id.main_fab),
                            activity.getString(
                                    R.string.download_no_file_feedback),
                            Snackbar.LENGTH_LONG).show();
                }
            });
        } else {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (progressDialog != null) {
                        progressDialog.setMessage(activity.getString(
                                R.string.download_inserting_msg));
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
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                ErrorDialog.show(activity, activity.getString(
                        R.string.download_reading_failure_title),
                        e.getMessage());
            }
        });
    }

    @Override
    public final void onDataInserted() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                if (callback != null) {
                    callback.onRefreshList();
                }
                Snackbar.make(activity.findViewById(R.id.main_fab),
                        activity.getString(R.string.download_success_feedback),
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public final void onInsertDataFailed(final Exception exception) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                ErrorDialog.show(activity, activity.getString(
                        R.string.download_insert_failure_title),
                        exception.getMessage());
            }
        });
    }

    /**
     * Listener for CloudLibHelper.
     */
    public interface CloudLibHelperListener {

        /**
         * Called when the activity list must be refreshed.
         */
        void onRefreshList();
    }
}
