package com.cibertec.notasfirebaseapp

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cibertec.notasfirebaseapp.databinding.ActivityMainBinding
import com.cibertec.notasfirebaseapp.model.Note
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    private val adapter = NoteAdapter(mutableListOf(), onEdit = this::showEditDialog)
    private var listenerReg: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        // AppBar + logout
        val toolbar: MaterialToolbar = b.topAppBar
        toolbar.setOnMenuItemClickListener { item: MenuItem ->
            if (item.itemId == R.id.action_logout) {
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                })
                finish()
                true
            } else false
        }

        // Recycler
        b.rvNotes.layoutManager = LinearLayoutManager(this)
        b.rvNotes.adapter = adapter
        b.rvNotes.addItemDecoration(DividerItemDecoration(this, RecyclerView.VERTICAL))

        // Swipe para eliminar
        val swipe = object : ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {
                val pos = vh.bindingAdapterPosition
                val note = adapter.getNoteAt(pos)
                deleteNote(note)
            }
        }
        ItemTouchHelper(swipe).attachToRecyclerView(b.rvNotes)

        // Crear
        b.btnAddNote.setOnClickListener { saveNote() }
    }

    override fun onStart() {
        super.onStart()
        val user = auth.currentUser ?: run { finish(); return }
        listenNotes(user.uid)
    }

    override fun onStop() {
        super.onStop()
        listenerReg?.remove()
        listenerReg = null
    }

    private fun listenNotes(userId: String) {
        listenerReg?.remove()
        listenerReg = db.collection("users").document(userId).collection("notes")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    toast(getString(R.string.msg_error, e.message ?: "")); return@addSnapshotListener
                }
                val list = snapshot?.documents?.map { doc ->
                    doc.toObject(Note::class.java)!!.apply { id = doc.id }
                }.orEmpty()
                adapter.replaceAll(list)
                b.tvEmpty.visibility = if (list.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            }
    }

    private fun saveNote() {
        val userId = auth.currentUser?.uid ?: return
        val title = b.etTitle.text?.toString()?.trim().orEmpty()
        val desc = b.etDescription.text?.toString()?.trim().orEmpty()
        if (title.isEmpty() || desc.isEmpty()) {
            toast(getString(R.string.msg_complete_fields)); return
        }
        val note = Note(title = title, description = desc)
        db.collection("users").document(userId).collection("notes")
            .add(note)
            .addOnSuccessListener {
                toast(getString(R.string.msg_note_saved))
                b.etTitle.text?.clear()
                b.etDescription.text?.clear()
            }
            .addOnFailureListener { e ->
                toast(getString(R.string.msg_error, e.message ?: "")) }
    }

    private fun showEditDialog(note: Note) {
        val ctx = this
        val container = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
        }
        val etTitle = EditText(ctx).apply {
            hint = getString(R.string.hint_title)
            setText(note.title)
        }
        val etDesc = EditText(ctx).apply {
            hint = getString(R.string.hint_description)
            setText(note.description)
        }
        container.addView(etTitle)
        container.addView(etDesc)

        AlertDialog.Builder(ctx)
            .setTitle(getString(R.string.title_edit_note))
            .setView(container)
            .setPositiveButton(getString(R.string.btn_update)) { _, _ ->
                val newTitle = etTitle.text.toString().trim()
                val newDesc = etDesc.text.toString().trim()
                if (newTitle.isEmpty() || newDesc.isEmpty()) {
                    toast(getString(R.string.msg_complete_fields)); return@setPositiveButton
                }
                updateNote(note.id ?: return@setPositiveButton, newTitle, newDesc)
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }

    private fun updateNote(noteId: String, title: String, desc: String) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("notes")
            .document(noteId)
            .update(
                mapOf(
                    "title" to title,
                    "description" to desc,
                    "updatedAt" to Timestamp.now()
                )
            )
            .addOnSuccessListener { toast(getString(R.string.msg_note_updated)) }
            .addOnFailureListener { e -> toast(getString(R.string.msg_error, e.message ?: "")) }
    }

    private fun deleteNote(note: Note) {
        val userId = auth.currentUser?.uid ?: return
        val noteId = note.id ?: return
        db.collection("users").document(userId).collection("notes")
            .document(noteId)
            .delete()
            .addOnSuccessListener { toast(getString(R.string.msg_note_deleted)) }
            .addOnFailureListener { e -> toast(getString(R.string.msg_error, e.message ?: "")) }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
