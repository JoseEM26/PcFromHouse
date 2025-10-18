package com.cibertec.notasfirebaseapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cibertec.notasfirebaseapp.databinding.ItemNoteBinding
import com.cibertec.notasfirebaseapp.model.Note

class NoteAdapter(
    private val notes: MutableList<Note>,
    private val onEdit: (Note) -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    inner class NoteViewHolder(val b: ItemNoteBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val b = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(b)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.b.tvTitle.text = note.title
        holder.b.tvDescription.text = note.description
        holder.b.root.contentDescription =
            holder.b.root.context.getString(R.string.cd_note_item, note.title)
        holder.b.root.setOnClickListener { onEdit(note) }
    }

    override fun getItemCount() = notes.size

    fun getNoteAt(position: Int): Note = notes[position]
    fun replaceAll(newNotes: List<Note>) {
        notes.clear()
        notes.addAll(newNotes)
        notifyDataSetChanged()
    }
}
