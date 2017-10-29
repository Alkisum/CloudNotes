package com.alkisum.android.cloudnotes.net;

import android.content.Context;
import android.content.Intent;

import com.alkisum.android.cloudlib.events.DownloadEvent;
import com.alkisum.android.cloudlib.events.JsonFileReaderEvent;
import com.alkisum.android.cloudlib.file.json.JsonFile;
import com.alkisum.android.cloudlib.file.json.JsonFileReader;
import com.alkisum.android.cloudlib.net.ConnectInfo;
import com.alkisum.android.cloudlib.net.nextcloud.NcDownloader;
import com.alkisum.android.cloudnotes.database.Inserter;
import com.alkisum.android.cloudnotes.events.InsertEvent;
import com.alkisum.android.cloudnotes.files.Json;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
                new Integer[]{SUBSCRIBER_ID, subscriberId});

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
                new JsonFileReader(event.getFiles(),
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
     * Triggered on JSON file reader event.
     *
     * @param event JSON file reader event
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public final void onJsonFileReaderEvent(final JsonFileReaderEvent event) {
        if (!event.isSubscriberAllowed(SUBSCRIBER_ID)) {
            return;
        }
        switch (event.getResult()) {
            case JsonFileReaderEvent.OK:
                List<JSONObject> jsonObjects = new ArrayList<>();
                for (JsonFile jsonFile : event.getJsonFiles()) {
                    if (Json.isFileNameValid(jsonFile)
                            && !Json.isNoteAlreadyInDb(jsonFile)) {
                        jsonObjects.add(jsonFile.getJsonObject());
                    }
                }
                new Inserter(jsonObjects).execute();
                break;
            case JsonFileReaderEvent.ERROR:
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
