package com.kubixdev.noteify.database;


import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.kubixdev.noteify.dao.NotepadDao;
import com.kubixdev.noteify.entities.NoteEntity;


@androidx.room.Database(entities = NoteEntity.class, version = 1, exportSchema = false)
public abstract class NotepadDatabase extends RoomDatabase {
    private static NotepadDatabase notepadDatabase;

    public static synchronized NotepadDatabase getNoteDatabase(Context context) {

        // jezeli nie ma bazy danych to ja tworzy
        if (notepadDatabase == null) {
            notepadDatabase = Room.databaseBuilder(context, NotepadDatabase.class, "noteDatabase").build();
        }

        // w kazdym wypadku zwroci gotowa baze
        return notepadDatabase;
    }

    public abstract NotepadDao notepadDao();
}