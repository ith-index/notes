package com.example.notes

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

private const val KEY_EXTRA_NOTE_ID = "com.example.notes.KEY_EXTRA_NODE_ID"

fun instantiateNoteActivityIntent(context: Context, noteID: Long) =
    Intent(context, NoteActivity::class.java).apply {
        putExtra(KEY_EXTRA_NOTE_ID, noteID)
    }

class NoteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        supportActionBar!!.run {
            setTitle("")
            setDisplayHomeAsUpEnabled(true)
        }

        val noteID =
            if (intent.hasExtra(KEY_EXTRA_NOTE_ID)) {
                intent.getLongExtra(KEY_EXTRA_NOTE_ID, -1)
            } else {
                throw MissingExtraException(KEY_EXTRA_NOTE_ID)
            }
        val fragmentManager = supportFragmentManager
        if (fragmentManager.findFragmentById(R.id.frameLayoutFragmentContainer) == null) {
            fragmentManager
                .beginTransaction()
                .add(R.id.frameLayoutFragmentContainer, instantiateNoteFragment(noteID))
                .commit()
        }
    }
}
