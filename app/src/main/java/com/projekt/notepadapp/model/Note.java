package com.projekt.notepadapp.model;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "notes")
public class Note {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "text")
    private String noteText;

    @ColumnInfo(name = "date")
    private long noteDate;

    @ColumnInfo(name = "photo")
    private String notePhoto;

    @ColumnInfo(name = "audio")
    private String noteAudio;

    @Ignore
    private boolean checked = false;

    public Note(){}

    public Note(String noteText, long noteDate, String notePhoto, String noteAudio){
        this.noteText = noteText;
        this.noteDate = noteDate;
        this.notePhoto = notePhoto;
        this.noteAudio = noteAudio;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    public long getNoteDate() {
        return noteDate;
    }

    public void setNoteDate(long noteDate) {
        this.noteDate = noteDate;
    }


    public void setChecked(boolean checked){
        this.checked = checked;
    }

    public boolean isChecked() {
        return checked;
    }


    public String getNotePhoto() {
        return notePhoto;
    }

    public void setNotePhoto(String notePhoto) {
        this.notePhoto = notePhoto;
    }

    public String getNoteAudio() {
        return noteAudio;
    }

    public void setNoteAudio(String noteAudio) {
        this.noteAudio = noteAudio;
    }

    @Override
    public String toString() {
        return "Note{" +
                "id=" + id +
                ", noteDate=" + noteDate +
                '}';
    }
}
