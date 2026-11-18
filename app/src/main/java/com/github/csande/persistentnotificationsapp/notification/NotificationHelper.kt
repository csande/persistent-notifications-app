package com.github.csande.persistentnotificationsapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import androidx.core.app.NotificationCompat
import com.github.csande.persistentnotificationsapp.MainActivity
import com.github.csande.persistentnotificationsapp.R
import com.github.csande.persistentnotificationsapp.data.Note
import com.github.csande.persistentnotificationsapp.data.NoteRepository

object NotificationHelper {

    private const val CHANNEL_ID = "persistent_notes"

    /** Posts a new notification for [note], or updates the existing one with the same ID. */
    fun show(context: Context, note: Note) {
        val appContext = context.applicationContext
        val manager = appContext.getSystemService(NotificationManager::class.java)
        ensureChannel(appContext, manager)

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        // Tapping the notification opens the editor for this note.
        val openIntent = Intent(appContext, MainActivity::class.java)
            .putExtra(MainActivity.EXTRA_NOTE_ID, note.id)
        val contentIntent = PendingIntent.getActivity(appContext, note.id, openIntent, flags)

        // Since Android 14, ongoing notifications can still be swiped away.
        // The delete intent fires on dismissal so the notification can be re-posted.
        val dismissIntent = Intent(appContext, NotificationDismissedReceiver::class.java)
            .putExtra(MainActivity.EXTRA_NOTE_ID, note.id)
        val deleteIntent = PendingIntent.getBroadcast(appContext, note.id, dismissIntent, flags)

        // Text-only "Delete" action, shown when the notification is expanded. It deletes the
        // note without opening the app. Authentication is deliberately not required, so the
        // action also works on the lock screen and, as a plain broadcast, does not close the
        // notification shade.
        val deleteNoteIntent = Intent(appContext, DeleteNoteReceiver::class.java)
            .putExtra(MainActivity.EXTRA_NOTE_ID, note.id)
        val deleteActionIntent =
            PendingIntent.getBroadcast(appContext, note.id, deleteNoteIntent, flags)
        val deleteAction = NotificationCompat.Action.Builder(
            0,
            appContext.getString(R.string.action_delete),
            deleteActionIntent,
        ).build()

        // Collapsed: the note text is the notification title. The system renders titles in
        // its bold title style, and it hides the app name in the collapsed state whenever a
        // title is present (it only shows the app name when there is no title or subtext).
        //
        // Expanded: the full note text is shown in bold. The empty big content title hides the
        // title line in the expanded view, so the text is not repeated.
        val boldText = SpannableString(note.text).apply {
            setSpan(StyleSpan(Typeface.BOLD), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(note.text)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle("")
                    .bigText(boldText)
            )
            .setContentIntent(contentIntent)
            .setDeleteIntent(deleteIntent)
            .addAction(deleteAction)
            .setOngoing(true)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            // Show the full note text on the lock screen. The default (private) hides the
            // content behind a generic placeholder when the device is locked.
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        manager.notify(note.id, notification)
    }

    fun cancel(context: Context, noteId: Int) {
        val manager = context.applicationContext.getSystemService(NotificationManager::class.java)
        manager.cancel(noteId)
    }

    /** Posts a notification for every saved note. */
    fun showAll(context: Context) {
        NoteRepository(context).getAll().forEach { show(context, it) }
    }

    private fun ensureChannel(context: Context, manager: NotificationManager) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = context.getString(R.string.channel_description)
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }
}
