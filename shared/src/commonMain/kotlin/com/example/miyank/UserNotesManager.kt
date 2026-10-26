package com.example.miyank

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class UserCalendarNote(
    val id: String = "",
    val dateStr: String = "", // "YYYY-MM-DD"
    val title: String = "",
    val description: String = "",
    val timeStr: String = "", // e.g. "07:00 PM"
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

object UserNotesManager {
    private val settingsStorage = SettingsManager()
    private const val KEY_USER_NOTES_JSON = "user_personal_calendar_notes_json"
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private val _userNotes = MutableStateFlow<List<UserCalendarNote>>(emptyList())
    val userNotes: StateFlow<List<UserCalendarNote>> = _userNotes

    init {
        loadUserNotes()
    }

    private fun loadUserNotes() {
        val savedJson = settingsStorage.getString(KEY_USER_NOTES_JSON, "")
        if (savedJson.isNotEmpty()) {
            try {
                val list = json.decodeFromString<List<UserCalendarNote>>(savedJson)
                _userNotes.value = list.sortedBy { it.dateStr }
            } catch (e: Exception) {
                _userNotes.value = emptyList()
            }
        }
    }

    fun saveUserNotes(list: List<UserCalendarNote>) {
        val sorted = list.sortedBy { it.dateStr }
        _userNotes.value = sorted
        try {
            val encoded = json.encodeToString(sorted)
            settingsStorage.setString(KEY_USER_NOTES_JSON, encoded)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getNotesForDate(dateStr: String): List<UserCalendarNote> {
        return _userNotes.value.filter { it.dateStr == dateStr }
    }

    fun getNotesForMonth(year: Int, month: Int): List<UserCalendarNote> {
        val monthPrefix = "${year}-${month.toString().padStart(2, '0')}"
        return _userNotes.value.filter { it.dateStr.startsWith(monthPrefix) }
    }

    fun addOrUpdateNote(note: UserCalendarNote) {
        val current = _userNotes.value.toMutableList()
        val index = current.indexOfFirst { it.id == note.id }
        val now = getEpochMillis()

        if (index >= 0) {
            current[index] = note.copy(updatedAt = now)
        } else {
            val newId = if (note.id.isEmpty()) "note_${now}" else note.id
            current.add(
                note.copy(
                    id = newId,
                    createdAt = if (note.createdAt <= 0L) now else note.createdAt,
                    updatedAt = now
                )
            )
        }

        saveUserNotes(current)
    }

    fun deleteNote(id: String) {
        val updated = _userNotes.value.filter { it.id != id }
        saveUserNotes(updated)
    }
}
