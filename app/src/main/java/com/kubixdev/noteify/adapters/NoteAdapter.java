package com.kubixdev.noteify.adapters;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kubixdev.noteify.R;
import com.kubixdev.noteify.entities.NoteEntity;
import com.kubixdev.noteify.listeners.NoteListener;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewholder> {


    private List<NoteEntity> notes;
    private NoteListener noteListener;
    private Timer timer;
    private List<NoteEntity> notesSearchedList;


    public NoteAdapter(List<NoteEntity> notes, NoteListener noteListener) {
        this.notes = notes;
        this.noteListener = noteListener;
        notesSearchedList = notes;
    }


    ///////////////////////////
    // IMPLEMENTATION METHODS
    ///////////////////////////
    @NonNull
    @Override
    public NoteViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewholder(LayoutInflater.from(parent.getContext()).inflate(R.layout.note_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewholder holder, final int position) {
        holder.setNote(notes.get(position));
        holder.noteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteListener.noteClicked(notes.get(position), position);
            }
        });



        ////////////////////////////
        // LONG CLICK - DELETE NOTE
        ////////////////////////////
        holder.noteLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                v.setHapticFeedbackEnabled(true);
                noteListener.noteLongClicked(notes.get(position), position);

                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


    /////////////////////
    // VIEWHOLDER
    /////////////////////
    static class NoteViewholder extends RecyclerView.ViewHolder {

        TextView noteLayoutTitle;
        TextView noteLayoutBody;
        LinearLayout noteLayout;

        public NoteViewholder(@NonNull View itemView) {
            super(itemView);
            noteLayoutTitle = itemView.findViewById(R.id.noteLayoutTitle);
            noteLayoutBody = itemView.findViewById(R.id.noteLayoutBody);
            noteLayout = itemView.findViewById(R.id.noteLayout);
        }

        // ustawia tytul i podglad notatki w note_layout (uzywany w recyclerview)
        // StringUtils ustawia maksymalna ilosc znakow, aby podglad nie byl za dlugi
        // w note_layout zapobiega temu rowniez android:maxLines
        void setNote(NoteEntity noteEntity) {
            noteLayoutTitle.setText(noteEntity.getNoteTitle());
            noteLayoutBody.setText(StringUtils.abbreviate(noteEntity.getNoteBody(), 120));
        }
    }



    ///////////////////////
    // METODA SEARCH NOTES
    ///////////////////////
    public void searchNotes(final String keyword) {

        // https://developer.android.com/reference/java/util/Timer
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                // jezeli nic nie jest wpisane, to pokazuje wszystkie notatki
                if (keyword.trim().isEmpty()) {
                    notes = notesSearchedList;
                }


                // w innym wypadku, kazda notatka spelniajaca kryteria wyszukiwania (zawiera tekst w tytule albo body notatki)
                // jest dodawana do tymczasowej arraylisty
                else {
                    ArrayList<NoteEntity> temporaryNotes = new ArrayList<>();

                    for (NoteEntity note: notesSearchedList
                         ) {

                        if (note.getNoteTitle().toLowerCase().contains(keyword.toLowerCase()) || note.getNoteBody().toLowerCase().contains(keyword.toLowerCase())) {
                            temporaryNotes.add(note);
                        }
                    }

                    // ustawia liste notatek jako tymczasowa arrayliste z konkretnymi wyszukiwaniami
                    notes = temporaryNotes;
                }

                // powiadamia o zmienionym datasecie, gdy wyniki wyszukiwania sa zawezone
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });

            }
        }, 400);
    }


    // resetuje timer pola wyszukiwania
    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

}