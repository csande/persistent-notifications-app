package com.github.csande.persistentnotificationsapp

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import com.github.csande.persistentnotificationsapp.data.Note
import com.github.csande.persistentnotificationsapp.data.NoteRepository
import com.github.csande.persistentnotificationsapp.notification.NotificationHelper

/**
 * Holds the editor state. Survives configuration changes, and the
 * [SavedStateHandle] restores the state after process death.
 */
class EditorViewModel(
    application: Application,
    private val savedState: SavedStateHandle,
) : AndroidViewModel(application) {

    private val repository = NoteRepository(application)

    /** ID of the note being edited, or null for a new, unsaved note. */
    private var noteId by mutableStateOf(savedState.get<Int>(KEY_NOTE_ID))

    var field by mutableStateOf(
        savedState.get<String>(KEY_TEXT).orEmpty().let { TextFieldValue(it, TextRange(it.length)) }
    )
        private set

    val isEditing: Boolean get() = noteId != null

    /** Loads the note with the given [id]. A null or unknown ID results in a blank, new note. */
    fun openNote(id: Int?) {
        showNote(id?.let { repository.get(it) })
    }

    fun onFieldChange(value: TextFieldValue) {
        field = value
        savedState[KEY_TEXT] = value.text
    }

    /** Saves the note and posts or updates its notification. Returns false if the text is blank. */
    fun save(): Boolean {
        val text = field.text.trim()
        if (text.isEmpty()) return false

        val note = repository.save(noteId, text)
        NotificationHelper.show(getApplication(), note)
        return true
    }

    /** Deletes the current note and removes its notification. */
    fun delete() {
        val id = noteId ?: return
        repository.delete(id)
        NotificationHelper.cancel(getApplication(), id)
    }

    private fun showNote(note: Note?) {
        noteId = note?.id
        savedState[KEY_NOTE_ID] = note?.id

        val text = note?.text.orEmpty()
        field = TextFieldValue(text, TextRange(text.length))
        savedState[KEY_TEXT] = text
    }

    private companion object {
        const val KEY_NOTE_ID = "note_id"
        const val KEY_TEXT = "text"
    }
}
