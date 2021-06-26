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
    private int clickedNotePosition = -1;        // odpowiada za obecnie kliknieta notatke
    public int longClickedNotePosition = -2;     // odpowiada za obecnie dlugo kliknieta notatke
    public int noteCounter;                      // odpowiada za licznik notatek

    private AlertDialog deleteNoteDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // przycisk dodawania notatki
        FloatingActionButton addNoteButton = findViewById(R.id.buttonAddNote);
        addNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getApplicationContext(), NoteEditorActivity.class), addNoteCode);
            }
        });


        // ustawienie layoutu recyclera, w tym przypadku pionowy linearlayout
        noteRecycler = findViewById(R.id.noteRecycler);
        noteRecycler.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        );


        noteList = new ArrayList<>();
        noteAdapter = new NoteAdapter(noteList, this);
        noteRecycler.setAdapter(noteAdapter);


        // metoda getNote wywolywana jest na starcie aplikacji (onCreate)
        // przekazany jest request code, ktory odpowiada za wyswietlenie wszystkich notatek na poczatku
        getNote(displayNotesCode);


        // odpowiada za dzialanie paska wyszukiwania
        EditText searchBar = findViewById(R.id.searchBar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // jak zmieni sie tekst, to timer sie resetuje (aby nie szukalo od razu)
                noteAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // uzywa metody szukania notatki tylko gdy lista nie jest pusta
                if (noteList.size() != 0) {
                    noteAdapter.searchNotes(s.toString());
                }
            }
        });
    }


    /////////////////////////////
    // METODA KLIKNIECIA NOTATKI
    /////////////////////////////
    @Override
    public void noteClicked(NoteEntity noteEntity, int position) {
        clickedNotePosition = position;
        Intent intent = new Intent(getApplicationContext(), NoteEditorActivity.class);
        intent.putExtra("isNoteUpdated", true);
        intent.putExtra("note", noteEntity);
        startActivityForResult(intent, updateNoteCode);
    }


    //////////////////////////////////////
    // METODA DLUGIEGO KLIKNIECIA NOTATKI
    //////////////////////////////////////
    @Override
    public void noteLongClicked(NoteEntity noteEntity, int position) {
        longClickedNotePosition = position;
//        Toast.makeText(this, String.valueOf(position) ,Toast.LENGTH_SHORT).show();


//        dziala, przerobic na okno dialogowe
//        AsyncTask.execute(() -> NotepadDatabase.getNoteDatabase(getApplicationContext()).notepadDao().deleteNote(noteEntity));
//        noteCounter--;
//        noteCounterSetLabel();
//        noteAdapter.notifyDataSetChanged();

        deleteDialog(noteEntity, longClickedNotePosition);
        getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
    }


    //////////////////////////////////////
    // METODA POKAZANIA DIALOGU USUNIECIA
    //////////////////////////////////////
    private void deleteDialog(NoteEntity noteEntity, int position) {
        if (deleteNoteDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View view = LayoutInflater.from(this).inflate(R.layout.delete_layout, (ViewGroup) findViewById(R.id.layoutDeleteNoteBox));

            builder.setView(view);
            deleteNoteDialog = builder.create();

            if (deleteNoteDialog.getWindow() != null) {
                deleteNoteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }


            // blokuje inne mozliwosci wyjscia z alertdialog oprocz przyciskow
            deleteNoteDialog.setCancelable(false);
            deleteNoteDialog.setCanceledOnTouchOutside(false);


            view.findViewById(R.id.buttonDelete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // zwykly asynctask powodowal bledy, lambda dziala
                    // problem z usuwaniem wielu notatek z bazy danych, mozna usunac tylko jedna notatke na raz, reszta usuwa sie tylko wizualnie
                    // todo tymczasowo rozwiazane poprzez reload MainActivity
                    AsyncTask.execute(() -> NotepadDatabase.getNoteDatabase(getApplicationContext()).notepadDao().deleteNote(noteEntity));


                    // usuwa konkretna notatke z listy, zmniejsza licznik, ustawia label notatek, informuje o zmianie w database
                    noteList.remove(position);
                    noteCounter--;
                    noteCounterSetLabel();
                    noteAdapter.notifyDataSetChanged();


                    // przeladowuje recyclera i konczy okno dialogowe
                    deleteNoteDialog.dismiss();


                    // reloaduje MainActivity, tymczasowe
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

        // pokazuje alertdialog
        deleteNoteDialog.show();

        // ustawia go u dolu dla wygody
        // https://www.geeksforgeeks.org/how-to-change-the-position-of-alertdialog-in-android/
        deleteNoteDialog.getWindow().setGravity(Gravity.BOTTOM);
    }


    /////////////////////
    // METODA DELETENOTE
    /////////////////////
    // todo do poprawienia, sa bledy w przeciwienstwie do lambdy
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
    // METODA RELOAD ACTIVITY
    ////////////////////////////
    public void reloadActivity() {
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }


    ////////////////////////////
    // METODA NOTECOUNTER LABEL
    ////////////////////////////
    public void noteCounterSetLabel() {
        TextView noteCountView = findViewById(R.id.noteCount);
        noteCountView.setText(noteCounter + " notes");
    }


    /////////////////////
    // METODA GETNOTE
    /////////////////////
    private void getNote(final int request) {

        @SuppressLint("StaticFieldLeak")
        // https://developer.android.com/reference/android/os/AsyncTask
        class SaveNoteClass extends AsyncTask<Void, Void, List<NoteEntity>> {

            // zwraca notatki z room database
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

                        // informuje o zmianie notatki na konkretnej pozycji zeby nie uzywac notifyDataSetChanged, ktory informuje o zmianie calosci
                        noteAdapter.notifyItemInserted(0);

                        // automatycznie scrolluje recyclerview notatek na poczatek
                        noteRecycler.getLayoutManager().scrollToPosition(0);

                        noteCounter++;
                        noteCounterSetLabel();
                        reloadActivity();
                        break;
                    }

                    case updateNoteCode: {

                        // gdy notatka jest modyfikowana, usuwa poprzednia i wstawia nowa na to samo miejsce
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
    // https://developer.android.com/training/basics/intents/result
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // jezeli zostala utworzona nowa notatka, to do metody onActivityResult przekazuje sie request code dodania pojedynczej notatki
        // przy okzaji kod wyniku poprzedniego activity (NoteEditor) musi rownac sie RESULT_OK
        // gdy aplikacja by sie zcrashowala, to kodem bylby RESULT_CANCELED
        if (requestCode == addNoteCode && resultCode == RESULT_OK) {
            getNote(addNoteCode);
        }

        // jezeli notatka juz istniala, to jako wynik NoteEditorActivity dostaje request code aktualizacji konkretnej notatki
        else if (requestCode == updateNoteCode && resultCode == RESULT_OK) {

            // getNote wywolywana jest tylko, gdy dane onActivityResult nie sa puste
            // data element is the intent that activity returned as response parameters
            if (data != null) {
                getNote(updateNoteCode);
            }
        }
    }
}