package com.example.notes

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.EditText

class NewNoteFragment : Fragment() {
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
                activity!!.finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editTextViewTitle = view.findViewById(R.id.editTextTitle)
        editTextViewContent = view.findViewById(R.id.editTextContent)
    }

    override fun onDestroy() {
        val activity = activity!!
        val applicationContext: Context = activity.applicationContext
        if (activity.isFinishing) {
            val title = editTextViewTitle.text.toString()
            val content = editTextViewContent.text.toString()
            if (title.isNotBlank() || content.isNotBlank()) {
                val sqLiteOpenHelper = instantiateSQLiteOpenHelper(applicationContext)
                postOnDataThread {
                    val note = createNote(sqLiteOpenHelper, title, content)
                    postOnMainThread {
                        publishNewNote(note)
                    }
                }
            }
        }
        super.onDestroy()
    }
}
