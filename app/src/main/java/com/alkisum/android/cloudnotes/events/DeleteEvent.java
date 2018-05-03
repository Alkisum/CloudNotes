package com.alkisum.android.cloudnotes.events;

import com.alkisum.android.cloudlib.events.FilteredEvent;
import com.alkisum.android.cloudnotes.model.Note;

import java.util.List;

/**
 * Class defining delete event for EventBus.
 *
 * @author Alkisum
 * @version 2.4
 * @since 2.0
 */
public class DeleteEvent extends FilteredEvent {

    /**
     * Deleted notes.
     */
    private List<Note> deletedNotes;

    /**
     * DeleteEvent constructor.
     *
     * @param subscriberIds Subscriber ids allowed to process the events
     * @param deletedNotes  Deleted notes
     */
    public DeleteEvent(final Integer[] subscriberIds,
                       final List<Note> deletedNotes) {
        super(subscriberIds);
        this.deletedNotes = deletedNotes;
    }

    /**
     * @return Deleted notes
     */
    public final List<Note> getDeletedNotes() {
        return deletedNotes;
    }
}
