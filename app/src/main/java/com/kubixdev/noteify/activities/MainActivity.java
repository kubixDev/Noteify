package com.kubixdev.noteify.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kubixdev.noteify.R;
import com.kubixdev.noteify.adapters.NoteAdapter;
import com.kubixdev.noteify.database.NotepadDatabase;
import com.kubixdev.noteify.entities.NoteEntity;
import com.kubixdev.noteify.listeners.NoteListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NoteListener {

    /////////////////////
    // REQUEST CODES
    /////////////////////
    public static final int addNoteCode = 1;
    public static final int updateNoteCode = 2;
    public static final int displayNotesCode = 3;

    private RecyclerView noteRecycler;
    private List<NoteEntity> noteList;
    private NoteAdapter noteAdapter;
    private int clickedNotePosition = -1;
    public int longClickedNotePosition = -2;
    public int noteCounter;

    private AlertDialog deleteNoteDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton addNoteButton = findViewById(R.id.buttonAddNote);
        addNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getApplicationContext(), NoteEditorActivity.class), addNoteCode);
            }
        });

        // setting the recycler layout, in this case a vertical linearlayout
        noteRecycler = findViewById(R.id.noteRecycler);
        noteRecycler.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        );

        noteList = new ArrayList<>();
        noteAdapter = new NoteAdapter(noteList, this);
        noteRecycler.setAdapter(noteAdapter);

        // the request code is is responsible for displaying all notes at the start
        getNote(displayNotesCode);


        // functionality of the search bar
        EditText searchBar = findViewById(R.id.searchBar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // when the text changes, the timer resets (so that it doesn't search immediately)
                noteAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // uses the note search only when the list is not empty
                if (noteList.size() != 0) {
                    noteAdapter.searchNotes(s.toString());
                }
            }
        });
    }


    /////////////////////////////
    // NOTE CLICKED METHOD
    /////////////////////////////
    @Override
    public void noteClicked(NoteEntity noteEntity, int position) {
        clickedNotePosition = position;
        Intent intent = new Intent(getApplicationContext(), NoteEditorActivity.class);
        intent.putExtra("isNoteUpdated", true);
        intent.putExtra("note", noteEntity);
        startActivityForResult(intent, updateNoteCode);
    }


    /////////////////////////////
    // NOTE LONG CLICKED METHOD
    /////////////////////////////
    @Override
    public void noteLongClicked(NoteEntity noteEntity, int position) {
        longClickedNotePosition = position;
        deleteDialog(noteEntity, longClickedNotePosition);
        getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
    }


    ////////////////////////////////
    // SHOW DELETION DIALOG METHOD
    ////////////////////////////////
    private void deleteDialog(NoteEntity noteEntity, int position) {
        if (deleteNoteDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View view = LayoutInflater.from(this).inflate(R.layout.delete_layout, (ViewGroup) findViewById(R.id.layoutDeleteNoteBox));

            builder.setView(view);
            deleteNoteDialog = builder.create();

            if (deleteNoteDialog.getWindow() != null) {
                deleteNoteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            // blocks other ways to exit alertdialog except for buttons
            deleteNoteDialog.setCancelable(false);
            deleteNoteDialog.setCanceledOnTouchOutside(false);

            view.findViewById(R.id.buttonDelete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AsyncTask.execute(() -> NotepadDatabase.getNoteDatabase(getApplicationContext()).notepadDao().deleteNote(noteEntity));
                    // removes a specific note from the list
                    noteList.remove(position);

                    // reduces the note counter
                    noteCounter--;

                    // sets the note label
                    noteCounterSetLabel();

                    // informs about the change in the database
                    noteAdapter.notifyDataSetChanged();

                    // reloads the recycler and terminates the dialog box
                    deleteNoteDialog.dismiss();

                    // reloads MainActivity
                    reloadActivity();
                }
            });

            view.findViewById(R.id.buttonCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteNoteDialog.dismiss();
                    reloadActivity();
                }
            });
        }

        deleteNoteDialog.show();
        deleteNoteDialog.getWindow().setGravity(Gravity.BOTTOM);
    }


    ////////////////////////
    // DELETE NOTE METHOD
    ////////////////////////
    private void deleteNote(NoteEntity noteEntity, int position) {

        @SuppressLint("StaticFieldLeak")
        class DeleteNoteClass extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                NotepadDatabase.getNoteDatabase(getApplicationContext()).notepadDao().deleteNote(noteEntity);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                noteList.remove(position);
                noteCounter--;
                noteCounterSetLabel();
                noteAdapter.notifyDataSetChanged();
            }
        }

        new DeleteNoteClass().execute();
    }


    ////////////////////////////
    // RELOAD ACTIVITY METHOD
    ////////////////////////////
    public void reloadActivity() {
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }


    //////////////////////////////
    // SET COUNTER LABEL METHOD
    //////////////////////////////
    public void noteCounterSetLabel() {
        TextView noteCountView = findViewById(R.id.noteCount);
        noteCountView.setText(noteCounter + " notes");
    }


    /////////////////////
    // GET NOTE METHOD
    /////////////////////
    private void getNote(final int request) {

        @SuppressLint("StaticFieldLeak")
        class SaveNoteClass extends AsyncTask<Void, Void, List<NoteEntity>> {

            // returns notes from room database
            @Override
            protected List<NoteEntity> doInBackground(Void... voids) {
                return NotepadDatabase.getNoteDatabase(getApplicationContext()).notepadDao().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<NoteEntity> noteEntities) {
                super.onPostExecute(noteEntities);

                switch (request) {
                    case displayNotesCode: {
                        noteList.addAll(noteEntities);
                        noteAdapter.notifyDataSetChanged();
                        noteCounter = noteList.size();

                        noteCounterSetLabel();
                        break;
                    }

                    case addNoteCode: {
                        noteList.add(0, noteEntities.get(0));
                        noteAdapter.notifyItemInserted(0);

                        // automatically scrolls the notes recyclerview to the beginning
                        noteRecycler.getLayoutManager().scrollToPosition(0);

                        noteCounter++;
                        noteCounterSetLabel();
                        reloadActivity();
                        break;
                    }

                    case updateNoteCode: {
                        // when a note is modified, it deletes the previous one and inserts a new one in the same position
                        noteList.remove(clickedNotePosition);
                        noteList.add(clickedNotePosition, noteEntities.get(clickedNotePosition));
                        noteAdapter.notifyItemChanged(clickedNotePosition);
                        reloadActivity();
                        break;
                    }
                }
            }
        }

        new SaveNoteClass().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // when a new note has been created, a request code for adding a note is passed to the onActivityResult method
        // in addition, the result code of the previous activity (NoteEditor) must equal RESULT_OK
        // if the application were to crash, the code would be RESULT_CANCELED
        if (requestCode == addNoteCode && resultCode == RESULT_OK) {
            getNote(addNoteCode);
        }

        // if the note already exists, NoteEditorActivity receives a request code to update a specific note
        else if (requestCode == updateNoteCode && resultCode == RESULT_OK) {
            if (data != null) {
                getNote(updateNoteCode);
            }
        }
    }
}