package com.github.csande.persistentnotificationsapp.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.csande.persistentnotificationsapp.MainActivity
import com.github.csande.persistentnotificationsapp.data.NoteRepository

/**
 * Triggered by the "Delete" action on a note's notification.
 * Deletes the note and removes its notification without opening the app.
 */
class DeleteNoteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val noteId = intent.getIntExtra(MainActivity.EXTRA_NOTE_ID, MainActivity.NO_NOTE)
        if (noteId == MainActivity.NO_NOTE) return

        // Delete the note first so the dismiss receiver finds nothing to re-post.
        NoteRepository(context).delete(noteId)
        NotificationHelper.cancel(context, noteId)
    }
}
