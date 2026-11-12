package com.github.csande.persistentnotificationsapp.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/** A single note. [id] doubles as the notification ID. */
data class Note(val id: Int, val text: String)

/**
 * Stores notes as a JSON array in SharedPreferences.
 * The data set is small, so a full database is unnecessary.
 */
class NoteRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getAll(): List<Note> {
        val json = prefs.getString(KEY_NOTES, null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            List(array.length()) { index ->
                val obj = array.getJSONObject(index)
                Note(id = obj.getInt(FIELD_ID), text = obj.getString(FIELD_TEXT))
            }
        } catch (e: JSONException) {
            emptyList()
        }
    }

    fun get(id: Int): Note? = getAll().firstOrNull { it.id == id }

    /**
     * Updates the note with the given [id], or creates a new note when [id] is null
     * or no longer exists. Returns the stored note.
     */
    fun save(id: Int?, text: String): Note {
        val notes = getAll().toMutableList()
        val existingIndex = if (id == null) -1 else notes.indexOfFirst { it.id == id }

        val note: Note
        if (existingIndex >= 0) {
            note = Note(notes[existingIndex].id, text)
            notes[existingIndex] = note
        } else {
            note = Note(allocateId(), text)
            notes.add(note)
        }

        write(notes)
        return note
    }

    fun delete(id: Int) {
        write(getAll().filterNot { it.id == id })
    }

    private fun allocateId(): Int {
        val id = prefs.getInt(KEY_NEXT_ID, 1)
        prefs.edit().putInt(KEY_NEXT_ID, id + 1).apply()
        return id
    }

    private fun write(notes: List<Note>) {
        val array = JSONArray()
        notes.forEach { note ->
            array.put(
                JSONObject()
                    .put(FIELD_ID, note.id)
                    .put(FIELD_TEXT, note.text)
            )
        }
        prefs.edit().putString(KEY_NOTES, array.toString()).apply()
    }

    private companion object {
        const val PREFS_NAME = "notes"
        const val KEY_NOTES = "notes_json"
        const val KEY_NEXT_ID = "next_id"
        const val FIELD_ID = "id"
        const val FIELD_TEXT = "text"
    }
}
