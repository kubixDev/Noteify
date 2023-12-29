package com.kubixdev.noteify.listeners;

import com.kubixdev.noteify.entities.NoteEntity;

// external listener for recyclerview elements (specific notes)
public interface NoteListener {
    void noteClicked(NoteEntity noteEntity, int position);

    void noteLongClicked(NoteEntity noteEntity, int position);
}