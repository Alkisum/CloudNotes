package com.alkisum.android.notepad.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.alkisum.android.notepad.R;
import com.alkisum.android.notepad.adapters.NoteListAdapter;
import com.alkisum.android.notepad.database.Db;
import com.alkisum.android.notepad.dialogs.ConfirmDialog;
import com.alkisum.android.notepad.model.Note;
import com.alkisum.android.notepad.model.NoteDao;

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
 * @version 1.0
 * @since 1.0
 */
public class MainActivity extends AppCompatActivity {

    /**
     * List adapter for the list view listing the notes.
     */
    private NoteListAdapter listAdapter;

    /**
     * Note DAO instance.
     */
    private NoteDao dao = Db.getInstance().getDaoSession().getNoteDao();

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
        menu.findItem(R.id.action_delete).setVisible(listAdapter.isEditMode());
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
            case R.id.action_delete:
                ConfirmDialog.build(this,
                        getString(R.string.delete_notes_title),
                        getString(R.string.delete_notes_message),
                        getString(R.string.action_delete),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                                final int which) {
                                deleteNotes();
                            }
                        }).show();
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
}
