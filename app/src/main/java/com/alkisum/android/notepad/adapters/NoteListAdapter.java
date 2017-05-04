package com.alkisum.android.notepad.adapters;

import android.content.Context;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.alkisum.android.notepad.R;
import com.alkisum.android.notepad.model.Note;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * List adapter for the list view listing the notes.
 *
 * @author Alkisum
 * @version 1.0
 * @since 1.0
 */
public class NoteListAdapter extends BaseAdapter {

    /**
     * Note's time date format.
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
            "MMM dd, YYY", Locale.getDefault());

    /**
     * Context.
     */
    private final Context context;

    /**
     * List of notes.
     */
    private final List<Note> notes;

    /**
     * Flag set to true if the edit mode is on, false otherwise.
     */
    private boolean editMode;

    /**
     * NoteListAdapter constructor.
     *
     * @param context Context
     * @param notes   True if the edit mode is on, false otherwise
     */
    public NoteListAdapter(final Context context, final List<Note> notes) {
        this.context = context;
        this.notes = notes;
    }

    /**
     * Replace the list of notes with the given one.
     *
     * @param notes List of notes
     */
    public final void setNotes(final List<Note> notes) {
        this.notes.clear();
        this.notes.addAll(notes);
    }

    /**
     * @return true if the edit mode is on, false otherwise
     */
    public final boolean isEditMode() {
        return editMode;
    }

    /**
     * Set the edit mode and deselect all notes.
     *
     * @param editMode True if the edit mode is on, false otherwise
     */
    public final void setEditMode(final boolean editMode) {
        this.editMode = editMode;
        for (Note note : notes) {
            note.setSelected(false);
        }
    }

    /**
     * Change the selection state of the note stored at the given position in
     * the list.
     *
     * @param position Position of the note in the list
     */
    public final void setNoteSelected(final int position) {
        Note note = notes.get(position);
        note.setSelected(!note.isSelected());
    }

    /**
     * Select all the notes stored in the list.
     */
    public final void selectAll() {
        for (Note note : notes) {
            note.setSelected(true);
        }
    }

    /**
     * @return List of notes
     */
    public final List<Note> getNotes() {
        return notes;
    }

    @Override
    public final int getCount() {
        return notes.size();
    }

    @Override
    public final Note getItem(final int position) {
        return notes.get(position);
    }

    @Override
    public final long getItemId(final int position) {
        return notes.get(position).getId();
    }

    @Override
    public final View getView(final int position, final View convertView,
                              final ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        View view = convertView;
        if (view == null || view.getTag() == null) {
            view = inflater.inflate(R.layout.list_item_note, parent, false);
            final ViewHolder holder = new ViewHolder(view);
            view.setTag(holder);
        }
        final Note note = getItem(position);
        final ViewHolder holder = (ViewHolder) view.getTag();

        holder.title.setText(note.getTitle());
        holder.content.setText(note.getContent());
        holder.time.setText(String.format("%s %s",
                context.getString(R.string.note_last_update),
                DATE_FORMAT.format(note.getUpdatedTime())));
        holder.checkBox.setChecked(note.isSelected());

        if (editMode) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    note.setSelected(holder.checkBox.isChecked());
                }
            });
        } else {
            holder.checkBox.setVisibility(View.GONE);
        }

        return view;
    }

    /**
     * ViewHolder for note list adapter.
     */
    static class ViewHolder {

        /**
         * Note title.
         */
        @BindView(R.id.note_list_title)
        TextView title;

        /**
         * Note content.
         */
        @BindView(R.id.note_list_content)
        TextView content;

        /**
         * Note time.
         */
        @BindView(R.id.note_list_time)
        TextView time;

        /**
         * Note selected state.
         */
        @BindView(R.id.note_list_checkbox)
        AppCompatCheckBox checkBox;

        /**
         * ViewHolder constructor.
         *
         * @param view View to bind with ButterKnife
         */
        ViewHolder(final View view) {
            ButterKnife.bind(this, view);
        }
    }
}
