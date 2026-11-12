package com.github.csande.persistentnotificationsapp.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.csande.persistentnotificationsapp.MainActivity
import com.github.csande.persistentnotificationsapp.data.NoteRepository

/**
 * Triggered when the user swipes a note notification away.
 * If the note still exists (i.e. it was not deleted in the app), the notification is posted again.
 */
class NotificationDismissedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val noteId = intent.getIntExtra(MainActivity.EXTRA_NOTE_ID, MainActivity.NO_NOTE)
        if (noteId == MainActivity.NO_NOTE) return

        val note = NoteRepository(context).get(noteId) ?: return
        NotificationHelper.show(context, note)
    }
}
