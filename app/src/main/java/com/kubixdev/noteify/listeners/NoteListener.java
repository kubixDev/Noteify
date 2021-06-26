package com.kubixdev.noteify.listeners;

import com.kubixdev.noteify.entities.NoteEntity;

// zewnetrzny listener do elementow recyclerview (konkretnych notatek)
public interface NoteListener {
    void noteClicked(NoteEntity noteEntity, int position);

    void noteLongClicked(NoteEntity noteEntity, int position);
}
