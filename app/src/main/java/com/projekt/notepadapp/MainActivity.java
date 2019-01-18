package com.projekt.notepadapp;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.projekt.notepadapp.adapters.NotesAdapter;
import com.projekt.notepadapp.callbacks.MainActionModeCallback;
import com.projekt.notepadapp.callbacks.NoteEventListener;
import com.projekt.notepadapp.db.NotesDB;
import com.projekt.notepadapp.db.NotesDao;
import com.projekt.notepadapp.model.Note;
import com.projekt.notepadapp.utils.NoteUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.projekt.notepadapp.EditeNoteActivity.NOTE_EXTRA_KEY;

public class MainActivity extends AppCompatActivity implements NoteEventListener {


    private RecyclerView recyclerView;
    private ArrayList<Note> notes;
    private NotesAdapter adapter;
    private NotesDao dao;
    private MainActionModeCallback actionModeCallback;
    private int checkedCount = 0;
    private FloatingActionButton fab;
    private FloatingActionButton fab2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        recyclerView = findViewById(R.id.notes_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onAddNewNote();
            }
        });


        fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddNewVoiceNote();
            }
        });


        dao = NotesDB.getInstance(this).notesDao();

    }


    private void loadNotes(){

        this.notes = new ArrayList<>();
        List<Note> list = dao.getNotes();

        this.notes.addAll(list);
        this.adapter = new NotesAdapter(this, notes);


        this.adapter.setListener(this);

        this.recyclerView.setAdapter(adapter);

    }

    private void onAddNewVoiceNote(){
        startActivity(new Intent(this, Main2Activity.class));
    }

    private void onAddNewNote(){
       startActivity(new Intent(this, EditeNoteActivity.class));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume(){
        super.onResume();
        loadNotes();
    }

    @Override
    public void onNoteClick(Note note) {

        Intent edit = new Intent(this, EditeNoteActivity.class);
        edit.putExtra(NOTE_EXTRA_KEY, note.getId());
        startActivity(edit);

    }


    @Override
    public void onNoteLongClick(Note note) {


        note.setChecked(true);
        checkedCount = 1;
        adapter.setMultiCheckMode(true);


        adapter.setListener(new NoteEventListener() {
            @Override
            public void onNoteClick(Note note) {
                note.setChecked(!note.isChecked());
                if(note.isChecked()){
                    checkedCount++;
                }else{
                    checkedCount--;
                }

                if(checkedCount > 1){
                    actionModeCallback.changeShareItemVisible(false);
                }else{
                    actionModeCallback.changeShareItemVisible(true);
                }

                if(checkedCount == 0){
                    actionModeCallback.getAction().finish();
                }

                actionModeCallback.setCount(checkedCount + "/"+ notes.size());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onNoteLongClick(Note note) {

            }
        });


        actionModeCallback = new MainActionModeCallback() {
            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {

                if(menuItem.getItemId() == R.id.action_delete_notes){
                    onDeleteMultiNotes();
                }else if (menuItem.getItemId()==R.id.action_share_notes){

                    Note noteShare = adapter.getCheckedNotes().get(0);

                    try {
                        if (!noteShare.getNotePhoto().equals("")) {
                            shareImage();
                        } else {
                            onShareNotes();
                        }

                    }catch (Exception $e){
                        Toast.makeText(MainActivity.this, "You can share only text from audio note!", Toast.LENGTH_SHORT).show();
                        onShareNotes();
                    }
                }
                actionMode.finish();
                return false;

            }
        };


        startActionMode(actionModeCallback);
        fab.setVisibility(View.GONE);
        fab2.setVisibility(View.GONE);
        actionModeCallback.setCount(checkedCount + "/"+ notes.size());



    }

    private void onShareNotes() {

        Note note = adapter.getCheckedNotes().get(0);
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        String notetext = note.getNoteText()+"\n\n Create on : "+NoteUtils.dateFromLong(note.getNoteDate())+"\n By : "+
                getString(R.string.app_name);
        share.putExtra(Intent.EXTRA_TEXT, notetext);
        startActivity(share);
    }

    private void shareImage() {

        Note note2 = adapter.getCheckedNotes().get(0);
        String noteText = note2.getNoteText();
        Uri path = FileProvider.getUriForFile(this, "com.projekt.notepadapp.fileprovider", new File(note2.getNotePhoto()));

        Intent sharingIntent = new Intent();
        sharingIntent.setAction(Intent.ACTION_SEND);
        sharingIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.app_name));
        sharingIntent.putExtra(Intent.EXTRA_STREAM, path);
        sharingIntent.setType("*/*");
        sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(sharingIntent, "Share"));
    }

    private void onDeleteMultiNotes() {


        List<Note> checkedNotes = adapter.getCheckedNotes();

        if(checkedNotes.size() != 0){
            for(Note note : checkedNotes){
                dao.deleteNote(note);
            }
            loadNotes();
            Toast.makeText(this, checkedNotes.size() + " Note(s) Delete successfully !", Toast.LENGTH_SHORT).show();
        }else Toast.makeText(this, "No Note(s) selected", Toast.LENGTH_SHORT).show();

    }


    @Override
    public void onActionModeFinished(ActionMode mode){
        super.onActionModeFinished(mode);

        adapter.setMultiCheckMode(false);
        adapter.setListener(this);
        fab.setVisibility(View.VISIBLE);
        fab2.setVisibility(View.VISIBLE);

    }


}