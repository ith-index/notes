package com.example.notes

import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.EditText
import android.widget.TextView
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

private const val KEY_ARGUMENT_NOTE_ID = "KEY_ARGUMENT_NODE_ID"

fun instantiateNoteFragment(noteID: Long): NoteFragment =
    NoteFragment().apply {
        arguments = Bundle().apply {
            putLong(KEY_ARGUMENT_NOTE_ID, noteID)
        }
    }

class NoteFragment : Fragment() {
    private var noteID: Long by Delegates.notNull()
    private var wasDeleted = false
    private lateinit var editTextViewTitle: EditText
    private lateinit var editTextViewContent: EditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_new_or_old_note, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu?, menuInflater: MenuInflater?) {
        menuInflater!!.inflate(R.menu.menu_new_or_old_note, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean =
        when (item!!.itemId) {
            R.id.action_delete -> {
                wasDeleted = true
                activity!!.run {
                    postOnDataThread {
                        val sqLiteOpenHelper = instantiateSQLiteOpenHelper(applicationContext)
                        deleteNote(sqLiteOpenHelper, noteID)
                        postOnMainThread {
                            publishDeletedNoteID(noteID)
                        }
                    }
                    finish()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editTextViewTitle = view.findViewById(R.id.editTextTitle)
        editTextViewContent = view.findViewById(R.id.editTextContent)

        val arguments = arguments!!
        noteID =
            if (arguments.containsKey(KEY_ARGUMENT_NOTE_ID)) {
                arguments.getLong(KEY_ARGUMENT_NOTE_ID)
            } else {
                throw MissingArgumentException(KEY_ARGUMENT_NOTE_ID)
            }

        if (savedInstanceState == null) {
            val weakReferenceEditTextTitle = WeakReference(editTextViewTitle)
            val weakReferenceEditTextContent = WeakReference(editTextViewContent)
            val applicationContext = activity!!.applicationContext
            val sqLiteOpenHelper = instantiateSQLiteOpenHelper(applicationContext)
            postOnDataThread {
                val note = readNote(sqLiteOpenHelper, noteID)
                postOnMainThread {
                    note.run {
                        weakReferenceEditTextTitle.get()?.setText(title, TextView.BufferType.EDITABLE)
                        weakReferenceEditTextContent.get()?.setText(content, TextView.BufferType.EDITABLE)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        val activity = activity!!
        val applicationContext: Context = activity.applicationContext
        if (activity.isFinishing && !wasDeleted) {
            val sqLiteOpenHelper = instantiateSQLiteOpenHelper(applicationContext)
            val title = editTextViewTitle.text.toString()
            val content = editTextViewContent.text.toString()
            postOnDataThread {
                val note =updateNote(sqLiteOpenHelper, noteID, title, content)
                postOnMainThread {
                    publishUpdatedNote(note)
                }
            }
        }
        super.onDestroyView()
    }
}
