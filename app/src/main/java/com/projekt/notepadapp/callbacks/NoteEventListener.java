package com.projekt.notepadapp.callbacks;

import com.projekt.notepadapp.model.Note;

public interface NoteEventListener {


    void onNoteClick(Note note);


    void onNoteLongClick(Note note);

}
