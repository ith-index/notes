package com.example.notes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.TextView
import java.lang.ref.WeakReference

class NotesFragment : Fragment() {
    private lateinit var noteAdapter: NoteAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_notes, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activityContext: Context = activity!!
        val applicationContext: Context = activityContext.applicationContext

        noteAdapter = NoteAdapter()
        view.findViewById<RecyclerView>(R.id.recyclerViewNotes).apply {
            layoutManager = LinearLayoutManager(activityContext)
            adapter = noteAdapter
        }
        val floatingActionButtonAdd: FloatingActionButton = view.findViewById(R.id.floatingActionButtonAdd)

        floatingActionButtonAdd.setOnClickListener {
            startActivity(
                Intent(activityContext, NewNoteActivity::class.java)
            )
        }

        val weakReferenceNoteAdapter = WeakReference(noteAdapter)

        val sqLiteOpenHelper = instantiateSQLiteOpenHelper(applicationContext)
        postOnDataThread {
            val notes = readNotes(sqLiteOpenHelper)
            postOnMainThread {
                weakReferenceNoteAdapter.get()?.notes = notes
            }
        }

        subscriberToNewNote = {
            weakReferenceNoteAdapter.get()?.add(it)
        }

        subscriberToUpdatedNote = {
            weakReferenceNoteAdapter.get()?.update(it)
        }

        subscribeToDeletedNoteID = {
            weakReferenceNoteAdapter.get()?.delete(it)
        }

    }

    private class NoteAdapter : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {
        var notes: MutableList<Note> = mutableListOf()
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        fun add(note: Note) {
            notes.add(0, note)
            notifyItemInserted(0)
        }

        fun update(note: Note) {
            val index = notes.indexOfFirst {
                it.id == note.id
            }
            notes[index] = note
            notifyItemChanged(index)
        }

        fun delete(noteID: Long) {
            val index = notes.indexOfFirst {
                it.id == noteID
            }
            notes.removeAt(index)
            notifyItemRemoved(index)
        }

        override fun getItemCount(): Int = notes.size

        override fun onCreateViewHolder(viewGroup: ViewGroup, ignore: Int): NoteViewHolder =
            NoteViewHolder(
                LayoutInflater.from(viewGroup.context).inflate(R.layout.item_note, viewGroup, false)
            )

        override fun onBindViewHolder(noteViewHolder: NoteViewHolder, index: Int) {
            noteViewHolder.bind(notes[index])
        }

        private class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val textViewTitle: TextView = itemView.findViewById(R.id.textViewTitle)
            private val textViewContent: TextView = itemView.findViewById(R.id.textViewContent)

            fun bind(note: Note) {
                note.run {
                    if (title.isBlank()) {
                        textViewTitle.visibility = View.GONE
                    } else {
                        textViewTitle.text = title
                        textViewTitle.visibility = View.VISIBLE
                    }
                    if (content.isBlank()) {
                        textViewContent.visibility = View.GONE
                    } else {
                        textViewContent.text = content
                        textViewContent.visibility = View.VISIBLE
                    }
                    itemView.run {
                        setOnClickListener {
                            itemView.context.let {
                                it.startActivity(
                                    instantiateNoteActivityIntent(it, note.id)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
