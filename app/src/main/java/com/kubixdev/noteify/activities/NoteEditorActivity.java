package com.kubixdev.noteify.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
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


        // przycisk wstecz
        ImageView buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                closeKeyboard();
            }
        });


        // przycisk zapisu notatki
        TextView saveNoteButton = findViewById(R.id.buttonSave);
        saveNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // wywoluje metode zapisu notatki przy nacisnieciu
                saveNote();
                closeKeyboard();
            }
        });




        // TODO: 26.06.2021 znalezc sposob na przechowywanie formatowania, nie zapisuje sie w android room
//        // przycisk bold
//        ImageView buttonBold = findViewById(R.id.buttonBold);
//        buttonBold.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Spannable spannable = new SpannableStringBuilder(inputNote.getText());
//                spannable.setSpan(new StyleSpan(Typeface.BOLD), inputNote.getSelectionStart(), inputNote.getSelectionEnd(), 0);
//                inputNote.setText(spannable);
//            }
//        });
//
//
//        // przycisk italics
//        ImageView buttonItalic = findViewById(R.id.buttonItalic);
//        buttonItalic.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Spannable spannable = new SpannableStringBuilder(inputNote.getText());
//                spannable.setSpan(new StyleSpan(Typeface.ITALIC), inputNote.getSelectionStart(), inputNote.getSelectionEnd(), 0);
//                inputNote.setText(spannable);
//            }
//        });
//
//
//        // przycisk underline
//        ImageView buttonUnderline = findViewById(R.id.buttonUnderline);
//        buttonUnderline.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Spannable spannable = new SpannableStringBuilder(inputNote.getText());
//                spannable.setSpan(new UnderlineSpan(), inputNote.getSelectionStart(), inputNote.getSelectionEnd(), 0);
//                inputNote.setText(spannable);
//            }
//        });



        // domyslnie notatka nie jest aktualizowana, stad wartosc false
        // Serializable	the value of an item previously added with putExtra(), or null if no Serializable value was found
        if (getIntent().getBooleanExtra("isNoteUpdated", false)) {
            noteAlreadyExists = (NoteEntity)getIntent().getSerializableExtra("note");

            // wywoluje metode aktualizacji notatki
            setNoteUpdate();
        }
    }


    ////////////////////////
    // METODA HIDE KEYBOARD
    ////////////////////////
    // aby nie uruchamiala sie ponownie przy powrocie do MainActivity
    public void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    ///////////////////////////////
    // METODA AKTUALIZACJI NOTATKI
    ///////////////////////////////
    private void setNoteUpdate() {

        // wstawia nowy tytul i tekst do notatki ktora juz istnieje (noteAlreadyExists to NoteEntity!)
        inputTitle.setText(noteAlreadyExists.getNoteTitle());
        inputNote.setText(noteAlreadyExists.getNoteBody());
    }


    ///////////////////////////
    // METODA ZAPISU NOTATKI
    ///////////////////////////
    private void saveNote() {
        // trim skraca niepotrzebne spacje, cala instrukcja sprawdza czy tytul jest pusty
        // jezeli jest pusty, to wyswietla sie komunikat
        if (inputTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Title is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        // sprawdza czy tresc notatki jest pusta, wyswietla komunikat
        else if (inputNote.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Note is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        // jezeli notatka zawiera tytul i tekst, to tworzy NoteEntity z tym co wprowadzono
        final NoteEntity noteEntity = new NoteEntity();
        noteEntity.setNoteTitle(inputTitle.getText().toString());
        noteEntity.setNoteBody(inputNote.getText().toString());


        // jezeli notatka juz istnieje, to znaczy ze jest modyfikowana i ustawia sie dla niej nowe ID
        if (noteAlreadyExists != null) {
            noteEntity.setNoteID(noteAlreadyExists.getNoteID());
        }


        /////////////////////
        // ZAPIS NOTATKI
        /////////////////////
        @SuppressLint("StaticFieldLeak")
        class SaveNoteClass extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                // asynctask tworzy notatke w bazie danych
                NotepadDatabase.getNoteDatabase(getApplicationContext()).notepadDao().createNote(noteEntity);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                // https://developer.android.com/reference/android/content/Intent
                // tworzony jest intent, ktorego wynik przekazywany jest pozniej aby wywolac konkretny request code (dodanie lub modyfikacja notatki)
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }

        new SaveNoteClass().execute();
    }
}