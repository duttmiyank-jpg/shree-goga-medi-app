package com.example.miyank

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class SuvicharItem(
    val id: String = "",
    val date: String = "", // "YYYY-MM-DD"
    val imageUrl: String = "",
    val quoteText: String = "",
    val author: String = "श्री जामनीवाला दरबार",
    val isActive: Boolean = true,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

expect fun getTodayDateString(): String
expect fun formatDisplayDate(dateStr: String): String
expect fun formatEpochToDateString(epochMillis: Long): String
expect fun getEpochMillis(): Long

object SuvicharManager {
    private val settingsStorage = SettingsManager()
    private const val KEY_SUVICHAR_LIST = "daily_suvichar_list_json"

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private val _suvichars = MutableStateFlow<List<SuvicharItem>>(emptyList())
    val suvichars: StateFlow<List<SuvicharItem>> = _suvichars

    init {
        loadSuvichars()
    }

    private fun loadSuvichars() {
        val savedJson = settingsStorage.getString(KEY_SUVICHAR_LIST, "")
        if (savedJson.isNotEmpty()) {
            try {
                val list = json.decodeFromString<List<SuvicharItem>>(savedJson)
                _suvichars.value = list.sortedBy { it.date }
            } catch (e: Exception) {
                val defaults = getDefaultInitialSuvichars()
                _suvichars.value = defaults
                saveSuvichars(defaults)
            }
        } else {
            val defaults = getDefaultInitialSuvichars()
            _suvichars.value = defaults
            saveSuvichars(defaults)
        }
    }

    private fun saveSuvichars(list: List<SuvicharItem>) {
        val sorted = list.sortedBy { it.date }
        _suvichars.value = sorted
        try {
            val encoded = json.encodeToString(sorted)
            settingsStorage.setString(KEY_SUVICHAR_LIST, encoded)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getDefaultInitialSuvichars(): List<SuvicharItem> {
        val today = getTodayDateString()
        val now = getEpochMillis()
        return listOf(
            SuvicharItem(
                id = "default_today",
                date = today,
                imageUrl = "file:///android_asset/temple_pooja.jpg",
                quoteText = "जहाँ भक्ति है, वहाँ शक्ति है। श्रद्धा से किया गया हर कार्य फलदायी होता है।",
                author = "श्री जामनीवाला दरबार",
                isActive = true,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    fun getTodaySuvichar(): SuvicharItem {
        val todayStr = getTodayDateString()
        val scheduled = _suvichars.value.firstOrNull { it.date == todayStr && it.isActive }
        if (scheduled != null) return scheduled

        // Default Fallback Image when Admin forgets to schedule Suvichar for today
        return SuvicharItem(
            id = "default_fallback_today",
            date = todayStr,
            imageUrl = "file:///android_asset/temple_pooja.jpg",
            quoteText = "जहाँ भक्ति है, वहाँ शक्ति है। श्रद्धा से किया गया हर कार्य फलदायी होता है।",
            author = "श्री जामनीवाला दरबार",
            isActive = true,
            createdAt = 0L,
            updatedAt = 0L
        )
    }

    fun isDateAlreadyScheduled(dateStr: String, excludeId: String? = null): Boolean {
        return _suvichars.value.any { it.date == dateStr && it.isActive && it.id != excludeId }
    }

    fun addOrUpdateSuvichar(item: SuvicharItem): Boolean {
        val currentList = _suvichars.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.id == item.id }
        val now = getEpochMillis()

        if (existingIndex >= 0) {
            currentList[existingIndex] = item.copy(updatedAt = now)
        } else {
            val newId = if (item.id.isEmpty()) "suvichar_$now" else item.id
            currentList.add(
                item.copy(
                    id = newId,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }

        saveSuvichars(currentList)
        return true
    }

    fun deleteSuvichar(id: String) {
        val currentList = _suvichars.value.filter { it.id != id }
        saveSuvichars(currentList)
    }

    fun toggleSuvicharStatus(id: String) {
        val now = getEpochMillis()
        val currentList = _suvichars.value.map {
            if (it.id == id) it.copy(isActive = !it.isActive, updatedAt = now) else it
        }
        saveSuvichars(currentList)
    }
}
