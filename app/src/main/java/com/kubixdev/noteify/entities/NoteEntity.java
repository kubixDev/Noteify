package com.kubixdev.noteify.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "notesTable")
public class NoteEntity implements Serializable {

    // klucz podstawowy bazy danych - id notatki
    @PrimaryKey(autoGenerate = true)
    private int noteID;

    // druga kolumna - tytul notatki
    @ColumnInfo(name = "noteTitle")
    private String noteTitle;

    // trzecia kolumna - tresc notatki
    @ColumnInfo(name = "noteBody")
    private String noteBody;


    //////////////////////////////
    // SETTERY, GETTERY, TOSTRING
    //////////////////////////////
    public int getNoteID() {
        return noteID;
    }

    public void setNoteID(int noteID) {
        this.noteID = noteID;
    }

    public String getNoteTitle() {
        return noteTitle;
    }

    public void setNoteTitle(String noteTitle) {
        this.noteTitle = noteTitle;
    }

    public String getNoteBody() {
        return noteBody;
    }

    public void setNoteBody(String noteBody) {
        this.noteBody = noteBody;
    }

    @Override
    public String toString() {
        return "NoteEntity{" +
                "noteID=" + noteID +
                ", noteTitle='" + noteTitle + '\'' +
                ", noteBody='" + noteBody + '\'' +
                '}';
    }
}