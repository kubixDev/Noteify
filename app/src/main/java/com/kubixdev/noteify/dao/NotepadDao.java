package com.kubixdev.noteify.dao;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.kubixdev.noteify.entities.NoteEntity;

import java.util.List;

@androidx.room.Dao
public interface NotepadDao {

    // query sorts notes descending by ID
    // notesTable is defined in NoteEntity
    @Query("SELECT * FROM notesTable ORDER BY noteID DESC")
    List<NoteEntity> getAllNotes();

    // if the note already exists (and is modified), it will be replaced by a new note
    // ID for it is set in NoteEditorActivity
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void createNote(NoteEntity noteEntity);

    @Delete
    void deleteNote(NoteEntity noteEntity);
}