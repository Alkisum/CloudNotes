package com.alkisum.android.cloudnotes.net;

import android.content.Context;
import android.content.Intent;

import com.alkisum.android.cloudlib.events.TxtFileWriterEvent;
import com.alkisum.android.cloudlib.events.UploadEvent;
import com.alkisum.android.cloudlib.file.txt.TxtFileWriter;
import com.alkisum.android.cloudlib.net.ConnectInfo;
import com.alkisum.android.cloudlib.net.nextcloud.NcUploader;
import com.alkisum.android.cloudnotes.files.Txt;
import com.alkisum.android.cloudnotes.model.Note;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Class starting upload operation and subscribing to upload events.
 *
 * @author Alkisum
 * @version 2.0
 * @since 2.0
 */
public class Uploader {

    /**
     * Subscriber id to use when receiving event.
     */
    private static final int SUBSCRIBER_ID = 637;

    /**
     * OcUploader instance to start.
     */
    private final NcUploader ncUploader;

    /**
     * Uploader constructor.
     *
     * @param context      Context
     * @param connectInfo  Connection information
     * @param intent       Intent for notification
     * @param notes        List of note to upload
     * @param subscriberId Subscriber id allowed to process the events
     */
    public Uploader(final Context context, final ConnectInfo connectInfo,
                    final Intent intent, final List<Note> notes,
                    final int subscriberId) {
        EventBus.getDefault().register(this);

        ncUploader = new NcUploader(context, intent,
                "CloudRunUploader", "CloudRun upload",
                new Integer[]{SUBSCRIBER_ID, subscriberId});
        ncUploader.init(
                connectInfo.getAddress(),
                connectInfo.getPath(),
                connectInfo.getUsername(),
                connectInfo.getPassword());

        // Write content into temporary TXT files
        new TxtFileWriter(context.getCacheDir(),
                Txt.buildTxtFilesFromNotes(notes),
                new Integer[]{SUBSCRIBER_ID, subscriberId}).execute();
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
                ncUploader.start(event.getCloudFiles());
                break;
            case TxtFileWriterEvent.ERROR:
                EventBus.getDefault().unregister(this);
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
            case UploadEvent.OK:
                EventBus.getDefault().unregister(this);
                break;
            case UploadEvent.ERROR:
                EventBus.getDefault().unregister(this);
                break;
            default:
                break;
        }
    }
}
