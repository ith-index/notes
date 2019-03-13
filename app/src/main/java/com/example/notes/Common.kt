package com.example.notes

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import java.lang.Exception

var subscriberToNewNote: ((Note) -> Unit)? = null
var subscriberToUpdatedNote: ((Note) -> Unit)? = null
var subscribeToDeletedNoteID: ((Long) -> Unit)? = null

fun publishNewNote(note: Note) {
    subscriberToNewNote?.invoke(note)
}

fun publishUpdatedNote(note: Note) {
    subscriberToUpdatedNote?.invoke(note)
}

fun publishDeletedNoteID(noteID: Long) {
    subscribeToDeletedNoteID?.invoke(noteID)
}

fun postOnMainThread(f: () -> Unit) {
    MainHandler.post(f)
}

fun postOnDataThread(f: () -> Unit) {
    DataHandler.post(f)
}

private object DataHandlerThread: HandlerThread("DATA_HANDLER_THREAD")
private object MainHandler: Handler(Looper.getMainLooper())
private object DataHandler: Handler(DataHandlerThread.run {
    if (looper == null) {
        start()
    }
    looper
})

class MissingExtraException(key: String) : Exception("missing extra: $key")
class MissingArgumentException(key: String) : Exception("missing argument: $key")