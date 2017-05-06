package com.alkisum.android.notepad.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.alkisum.android.notepad.R;
import com.alkisum.android.notepad.adapters.NoteListAdapter;
import com.alkisum.android.notepad.database.Db;
import com.alkisum.android.notepad.database.Notes;
import com.alkisum.android.notepad.dialogs.ConfirmDialog;
import com.alkisum.android.notepad.model.Note;
import com.alkisum.android.notepad.model.NoteDao;
import com.alkisum.android.notepad.net.CloudOpsHelper;

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
        CloudOpsHelper.CloudOpsHelperListener,
        NavigationView.OnNavigationItemSelectedListener {

    /**
     * List adapter for the list view listing the notes.
     */
    private NoteListAdapter listAdapter;

    /**
     * Note DAO instance.
     */
    private final NoteDao dao = Db.getInstance().getDaoSession().getNoteDao();

    /**
     * CloudOpsHelper instance that implements all CloudOps interfaces.
     */
    private CloudOpsHelper cloudOpsHelper;

    /**
     * Drawer toggle.
     */
    private ActionBarDrawerToggle toggle;

    /**
     * Toolbar.
     */
    @BindView(R.id.main_toolbar)
    Toolbar toolbar;

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

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        toolbar = ButterKnife.findById(this, R.id.main_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        setSupportActionBar(toolbar);

        toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.nav_drawer_open, R.string.nav_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = ButterKnife.findById(
                this, R.id.main_nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        listAdapter = new NoteListAdapter(this, loadNotes());
        listView.setAdapter(listAdapter);

        cloudOpsHelper = new CloudOpsHelper(this);
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
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public final boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_download:
                cloudOpsHelper.onDownloadAction();
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
                cloudOpsHelper.onUploadAction(notes);
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
    public final void onRefreshList() {
        refreshList();
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
}
