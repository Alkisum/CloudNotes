package com.alkisum.android.notepad.net;

import android.app.ProgressDialog;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.alkisum.android.cloudops.file.json.JsonFile;
import com.alkisum.android.cloudops.net.ConnectDialog;
import com.alkisum.android.cloudops.net.ConnectInfo;
import com.alkisum.android.cloudops.net.owncloud.OcDownloader;
import com.alkisum.android.cloudops.net.owncloud.OcUploader;
import com.alkisum.android.notepad.R;
import com.alkisum.android.notepad.database.Inserter;
import com.alkisum.android.notepad.dialogs.ErrorDialog;
import com.alkisum.android.notepad.files.Json;
import com.alkisum.android.notepad.model.Note;
import com.owncloud.android.lib.resources.files.RemoteFile;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Class implementing CloudOps interfaces.
 *
 * @author Alkisum
 * @version 1.1
 * @since 1.1
 */
public class CloudOpsHelper implements
        ConnectDialog.ConnectDialogListener, OcUploader.UploaderListener,
        OcDownloader.OcDownloaderListener, Inserter.InserterListener {

    /**
     * Log tag.
     */
    private static final String TAG = "CloudOpsHelper";

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
     * CloudOpsHelperListener instance, can be null if the activity does not
     * implement CloudOpsHelperListener.
     */
    private CloudOpsHelperListener callback;

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
     * @param activity Activity to help
     */
    public CloudOpsHelper(final AppCompatActivity activity) {
        this.activity = activity;
        try {
            callback = (CloudOpsHelperListener) activity;
        } catch (ClassCastException e) {
            Log.w(TAG, activity.getClass().getSimpleName()
                    + " does not implement CloudOpsHelperListener");
        }
    }

    /**
     * Triggered by the download action from the activity menu.
     */
    public final void onDownloadAction() {
        DialogFragment connectDialogDownload =
                ConnectDialog.newInstance(DOWNLOAD_OPERATION);
        connectDialogDownload.show(activity.getSupportFragmentManager(),
                ConnectDialog.FRAGMENT_TAG);
        downloader = new OcDownloader(activity);
    }

    /**
     * Triggered by the upload action from the activity menu.
     * @param notes List of notes to upload
     */
    public final void onUploadAction(final List<Note> notes) {
        if (!notes.isEmpty()) {
            DialogFragment connectDialogUpload =
                    ConnectDialog.newInstance(UPLOAD_OPERATION);
            connectDialogUpload.show(activity.getSupportFragmentManager(),
                    ConnectDialog.FRAGMENT_TAG);
            try {
                uploader = new OcUploader(activity,
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
                Toast.makeText(activity,
                        activity.getString(R.string.upload_success_toast),
                        Toast.LENGTH_LONG).show();
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
                Toast.makeText(activity, activity.getString(
                        R.string.download_no_file_toast),
                        Toast.LENGTH_LONG).show();
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
                    Toast.makeText(activity, activity.getString(
                            R.string.download_no_file_toast),
                            Toast.LENGTH_LONG).show();
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
                Toast.makeText(activity, activity.getString(
                        R.string.download_success_toast),
                        Toast.LENGTH_LONG).show();
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
     * Listener for CloudOpsHelper.
     */
    public interface CloudOpsHelperListener {

        /**
         * Called when the activity list must be refreshed.
         */
        void onRefreshList();
    }
}
