package com.kubixdev.noteify.dao;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.kubixdev.noteify.entities.NoteEntity;

import java.util.List;

@androidx.room.Dao
public interface NotepadDao {

    // kwerenda sortuje notatki malejaco po ID
    // tabela notesTable okreslona jest w NoteEntity
    @Query("SELECT * FROM notesTable ORDER BY noteID DESC")
    List<NoteEntity> getAllNotes();

    // https://developer.android.com/reference/android/arch/persistence/room/OnConflictStrategy
    // jezeli notatka juz istnieje (a jest modyfikowana), to zostanie zastapiona przez nowa notatke
    // w NoteEditorActivity ustawiane jest dla niej ID
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void createNote(NoteEntity noteEntity);

    @Delete
    void deleteNote(NoteEntity noteEntity);
}