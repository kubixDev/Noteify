package com.kubixdev.notepad.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.util.StringUtil;

import com.kubixdev.notepad.R;
import com.kubixdev.notepad.entities.NoteEntity;
import com.kubixdev.notepad.listeners.NoteListener;

import org.apache.commons.lang3.StringUtils;

import java.util.List;



public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewholder> {


    private List<NoteEntity> notes;
    private NoteListener noteListener;


    public NoteAdapter(List<NoteEntity> notes, NoteListener noteListener) {
        this.notes = notes;
        this.noteListener = noteListener;
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
                noteListener.noteLongClicked(notes.get(position), position);

//                int currPosition = position;
//                notes.remove(currPosition);
//                notifyItemRemoved(currPosition);

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
            noteLayoutBody.setText(StringUtils.abbreviate(noteEntity.getNoteBody(), 80));
        }
    }
}