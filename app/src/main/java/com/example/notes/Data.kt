package com.example.notes

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Note(
    val id: Long,
    val title: String,
    val content: String
)

fun createNote(sqLiteOpenHelper: SQLiteOpenHelper, title: String, content: String): Note {
    sqLiteOpenHelper.writableDatabase.use {
        val noteID = it.compileStatement(SQL_INSERT_NOTE).run {
            bindString(1, title)
            bindString(2, content)
            bindLong(3, System.currentTimeMillis())
            executeInsert()
        }
        selectNote(it, noteID).use {
            it.moveToFirst()
            return extractNote(it)
        }
    }
}

fun readNote(sqLiteOpenHelper: SQLiteOpenHelper, noteID: Long): Note =
    sqLiteOpenHelper.readableDatabase.use {
        selectNote(it, noteID).use {
            it.moveToFirst()
            extractNote(it)
        }
    }

fun readNotes(sqLiteOpenHelper: SQLiteOpenHelper): MutableList<Note> =
    sqLiteOpenHelper.readableDatabase.use {
        it.rawQuery(SQL_SELECT_NOTES, null).use { it.run {
            val notes: MutableList<Note> = mutableListOf()
            repeat(count) {
                moveToNext()
                notes.add(
                    extractNote(this)
                )
            }
            notes
        } }
    }

fun updateNote(sqLiteOpenHelper: SQLiteOpenHelper, noteID: Long, title: String, content: String): Note {
    sqLiteOpenHelper.writableDatabase.use {
        it.compileStatement(SQL_UPDATE_NOTE).run {
            bindString(1, title)
            bindString(2, content)
            bindLong(3, noteID)
            executeUpdateDelete()
        }
        selectNote(it, noteID).use {
            it.moveToFirst()
            return extractNote(it)
        }
    }
}

fun deleteNote(sqLiteOpenHelper: SQLiteOpenHelper, noteID: Long) {
    sqLiteOpenHelper.writableDatabase.use {
        it.execSQL(generateSQLDeletetNote(noteID))
    }
}

private fun selectNote(sqLiteDatabase: SQLiteDatabase, noteID: Long): Cursor =
    sqLiteDatabase.rawQuery(generateSQLSelectNote(noteID), null)

private fun extractNote(cursor: Cursor): Note =
    cursor.run {
        Note(
            id = getLong(getColumnIndex("id")),
            title = getString(getColumnIndex("title")),
            content = getString(getColumnIndex("content"))
        )
    }

private const val DATABASE_NAME = "notes.db"
private const val DATABASE_VERSION = 1

fun instantiateSQLiteOpenHelper(context: Context): SQLiteOpenHelper =
    TheSQLiteOpenHelper(context)

private class TheSQLiteOpenHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(sqLiteDatabase: SQLiteDatabase?) {
        sqLiteDatabase!!.run {
            execSQL(SQL_CREATE_TABLE_NOTES)
        }
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}
}

private val SQL_CREATE_TABLE_NOTES: String =
    """
        CREATE TABLE notes (
            id INTEGER PRIMARY KEY,
            title TEXT NOT NULL,
            content TEXT NOT NULL,
            datetime INTEGER NOT NULL
        );
    """.trimIndent()

private val SQL_INSERT_NOTE: String =
    """
        INSERT INTO notes (title, content, datetime)
        VALUES (?, ?, ?);
    """.trimIndent()

private val SQL_UPDATE_NOTE: String = "UPDATE notes SET title =? , content = ? WHERE id = ?"

private val SQL_SELECT_NOTES: String =
    """
        SELECT id, title, content
        FROM notes
        ORDER BY datetime DESC
    """.trimIndent()

private fun generateSQLSelectNote(id: Long): String =
    "SELECT id, title, content FROM notes WHERE id = $id"

private fun generateSQLDeletetNote(id: Long): String =
    "DELETE FROM notes WHERE id = $id"
