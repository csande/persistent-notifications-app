package com.github.csande.persistentnotificationsapp

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.NotificationManagerCompat
import com.github.csande.persistentnotificationsapp.ui.EditorScreen
import com.github.csande.persistentnotificationsapp.ui.theme.QuickNoteTheme

/**
 * The only activity. Opened from the launcher it creates a new note;
 * opened from a note's notification it edits that note.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: EditorViewModel by viewModels()

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (savedInstanceState == null) {
            viewModel.openNote(noteIdFrom(intent))
            if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            QuickNoteTheme {
                EditorScreen(
                    isEditing = viewModel.isEditing,
                    field = viewModel.field,
                    onFieldChange = viewModel::onFieldChange,
                    onSave = ::save,
                    onDelete = ::delete,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // A tap on another note's notification while the editor is open.
        noteIdFrom(intent)?.let(viewModel::openNote)
    }

    private fun save() {
        if (!viewModel.save()) return

        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            Toast.makeText(this, R.string.notifications_disabled_message, Toast.LENGTH_LONG).show()
        }
        finish()
    }

    private fun delete() {
        viewModel.delete()
        finish()
    }

    private fun noteIdFrom(intent: Intent?): Int? =
        intent?.getIntExtra(EXTRA_NOTE_ID, NO_NOTE)?.takeIf { it != NO_NOTE }

    companion object {
        const val EXTRA_NOTE_ID = "com.github.csande.persistentnotificationsapp.extra.NOTE_ID"
        const val NO_NOTE = -1
    }
}
