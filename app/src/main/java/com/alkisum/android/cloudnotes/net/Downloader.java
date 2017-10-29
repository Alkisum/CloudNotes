package com.alkisum.android.cloudnotes.net;

import android.content.Context;
import android.content.Intent;

import com.alkisum.android.cloudlib.events.DownloadEvent;
import com.alkisum.android.cloudlib.events.TxtFileReaderEvent;
import com.alkisum.android.cloudlib.file.txt.TxtFile;
import com.alkisum.android.cloudlib.file.txt.TxtFileReader;
import com.alkisum.android.cloudlib.net.ConnectInfo;
import com.alkisum.android.cloudlib.net.nextcloud.NcDownloader;
import com.alkisum.android.cloudnotes.database.Inserter;
import com.alkisum.android.cloudnotes.events.InsertEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Class starting download operation and subscribing to download events.
 *
 * @author Alkisum
 * @version 2.0
 * @since 2.0
 */
public class Downloader {

    /**
     * Subscriber id to use when receiving event.
     */
    private static final int SUBSCRIBER_ID = 462;

    /**
     * Subscriber id allowed to process the events.
     */
    private final Integer subscriberId;

    /**
     * Downloader constructor.
     *
     * @param context      Context
     * @param connectInfo  Connection information
     * @param intent       Intent for notification
     * @param subscriberId Subscriber id allowed to process the events
     */
    public Downloader(final Context context, final ConnectInfo connectInfo,
                      final Intent intent, final Integer subscriberId) {
        EventBus.getDefault().register(this);

        this.subscriberId = subscriberId;
        NcDownloader ncDownloader = new NcDownloader(context, intent,
                "CloudNotesDownloader", "CloudNotes download",
                new Integer[]{SUBSCRIBER_ID, subscriberId},
                new String[]{TxtFile.FILE_EXT});

        ncDownloader.init(
                connectInfo.getAddress(),
                connectInfo.getPath(),
                connectInfo.getUsername(),
                connectInfo.getPassword());
        ncDownloader.start();
    }

    /**
     * Triggered on download event.
     *
     * @param event Download event
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public final void onDownloadEvent(final DownloadEvent event) {
        if (!event.isSubscriberAllowed(SUBSCRIBER_ID)) {
            return;
        }
        switch (event.getResult()) {
            case DownloadEvent.OK:
                new TxtFileReader(event.getFiles(),
                        new Integer[]{SUBSCRIBER_ID, subscriberId}).execute();
                break;
            case DownloadEvent.NO_FILE:
                EventBus.getDefault().unregister(this);
                break;
            case DownloadEvent.ERROR:
                EventBus.getDefault().unregister(this);
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
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public final void onTxtFileReaderEvent(final TxtFileReaderEvent event) {
        if (!event.isSubscriberAllowed(SUBSCRIBER_ID)) {
            return;
        }
        switch (event.getResult()) {
            case TxtFileReaderEvent.OK:
                new Inserter(event.getTxtFiles()).execute();
                break;
            case TxtFileReaderEvent.ERROR:
                EventBus.getDefault().unregister(this);
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
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public final void onInsertEvent(final InsertEvent event) {
        switch (event.getResult()) {
            case InsertEvent.OK:
                EventBus.getDefault().unregister(this);
                break;
            case InsertEvent.ERROR:
                EventBus.getDefault().unregister(this);
                break;
            default:
                break;
        }
    }
}
