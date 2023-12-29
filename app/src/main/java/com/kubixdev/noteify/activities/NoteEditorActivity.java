package com.kubixdev.noteify.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kubixdev.noteify.R;
import com.kubixdev.noteify.database.NotepadDatabase;
import com.kubixdev.noteify.entities.NoteEntity;

public class NoteEditorActivity extends AppCompatActivity {

    private EditText inputTitle;
    private EditText inputNote;
    private NoteEntity noteAlreadyExists;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);

        inputTitle = findViewById(R.id.noteTitle);
        inputNote = findViewById(R.id.noteBody);


        ImageView buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                closeKeyboard();
            }
        });

        TextView saveNoteButton = findViewById(R.id.buttonSave);
        saveNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // calls the note saving method when pressed
                saveNote();
                closeKeyboard();
            }
        });


        // by default the note is not updated, hence the value is false
        if (getIntent().getBooleanExtra("isNoteUpdated", false)) {
            noteAlreadyExists = (NoteEntity)getIntent().getSerializableExtra("note");

            // calls the note updating method
            setNoteUpdate();
        }
    }


    //////////////////////////
    // CLOSE KEYBOARD METHOD
    //////////////////////////

    // to prevent the keyboard from opening after switching back to main activity
    public void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    ///////////////////////
    // NOTE UPDATE METHOD
    ///////////////////////
    private void setNoteUpdate() {
        // inserts a new title and text into a note that already exists (noteAlreadyExists is a NoteEntity)
        inputTitle.setText(noteAlreadyExists.getNoteTitle());
        inputNote.setText(noteAlreadyExists.getNoteBody());
    }


    ///////////////////////
    // NOTE SAVING METHOD
    ///////////////////////
    private void saveNote() {
        if (inputTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        else if (inputNote.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Note cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // creates a NoteEntity
        final NoteEntity noteEntity = new NoteEntity();
        noteEntity.setNoteTitle(inputTitle.getText().toString());
        noteEntity.setNoteBody(inputNote.getText().toString());


        // if the note already exists, it means that it is modified and a new ID is set
        if (noteAlreadyExists != null) {
            noteEntity.setNoteID(noteAlreadyExists.getNoteID());
        }


        ////////////////
        // NOTE SAVING
        ////////////////
        @SuppressLint("StaticFieldLeak")
        class SaveNoteClass extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                // asynctask creates a note in the database
                NotepadDatabase.getNoteDatabase(getApplicationContext()).notepadDao().createNote(noteEntity);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                // an intent is created, and its result is passed to call a specific request code (adding or modifying a note)
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }

        new SaveNoteClass().execute();
    }
}