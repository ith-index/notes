package com.example.notes

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class NewNoteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_note)

        supportActionBar!!.run {
            setTitle("")
            setDisplayHomeAsUpEnabled(true)
        }

        val fragmentManager = supportFragmentManager
        if (fragmentManager.findFragmentById(R.id.frameLayoutFragmentContainer) == null) {
            fragmentManager
                .beginTransaction()
                .add(R.id.frameLayoutFragmentContainer, NewNoteFragment())
                .commit()
        }
    }
}
