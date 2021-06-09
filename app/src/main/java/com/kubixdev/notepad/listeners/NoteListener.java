package com.kubixdev.notepad.listeners;

import com.kubixdev.notepad.entities.NoteEntity;

// zewnetrzny listener do elementow recyclerview (konkretnych notatek)
public interface NoteListener {
    void noteClicked(NoteEntity noteEntity, int position);

    void noteLongClicked(NoteEntity noteEntity, int position);
}
