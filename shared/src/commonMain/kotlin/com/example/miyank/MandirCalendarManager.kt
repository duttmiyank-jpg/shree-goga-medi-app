package com.example.miyank

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class MandirCalendarEvent(
    val id: String = "",
    val title: String = "",
    val titleEnglish: String = "",
    val dateStr: String = "", // "YYYY-MM-DD"
    val startTime: String = "",
    val endTime: String = "",
    val categoryTag: String = "FESTIVAL", // FESTIVAL, AARTI, FAST, PUJA, BHANDARA, YATRA, NOTICE
    val description: String = "",
    val descriptionEnglish: String = "",
    val imageUrl: String = "",
    val isImportant: Boolean = false,
    val isActive: Boolean = true,
    val targetRoute: String = "HOME",
    val targetId: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

object MandirCalendarManager {
    private val settingsStorage = SettingsManager()
    private const val KEY_CALENDAR_EVENTS_JSON = "mandir_calendar_events_json"
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private val _events = MutableStateFlow<List<MandirCalendarEvent>>(emptyList())
    val events: StateFlow<List<MandirCalendarEvent>> = _events

    init {
        loadEvents()
    }

    private fun loadEvents() {
        val savedJson = settingsStorage.getString(KEY_CALENDAR_EVENTS_JSON, "")
        if (savedJson.isNotEmpty()) {
            try {
                val list = json.decodeFromString<List<MandirCalendarEvent>>(savedJson)
                _events.value = list.sortedBy { it.dateStr }
            } catch (e: Exception) {
                val defaults = getDefaultEvents()
                _events.value = defaults
                saveEvents(defaults)
            }
        } else {
            val defaults = getDefaultEvents()
            _events.value = defaults
            saveEvents(defaults)
        }
    }

    fun saveEvents(list: List<MandirCalendarEvent>) {
        val sorted = list.sortedBy { it.dateStr }
        _events.value = sorted
        try {
            val encoded = json.encodeToString(sorted)
            settingsStorage.setString(KEY_CALENDAR_EVENTS_JSON, encoded)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getActiveEvents(): List<MandirCalendarEvent> {
        val active = _events.value.filter { it.isActive }.sortedBy { it.dateStr }
        return if (active.isNotEmpty()) active else getDefaultEvents().filter { it.isActive }
    }

    fun getEventsForMonth(year: Int, month: Int): List<MandirCalendarEvent> {
        val monthPrefix = "${year}-${month.toString().padStart(2, '0')}"
        return getActiveEvents().filter { it.dateStr.startsWith(monthPrefix) }
    }

    fun getEventsForDate(dateStr: String): List<MandirCalendarEvent> {
        return getActiveEvents().filter { it.dateStr == dateStr }
    }

    fun getUpcomingEvents(fromDateStr: String = getTodayDateString(), limit: Int = 6): List<MandirCalendarEvent> {
        return getActiveEvents()
            .filter { it.dateStr >= fromDateStr }
            .sortedBy { it.dateStr }
            .take(limit)
    }

    fun addOrUpdateEvent(item: MandirCalendarEvent) {
        val current = _events.value.toMutableList()
        val index = current.indexOfFirst { it.id == item.id }
        val now = getEpochMillis()

        if (index >= 0) {
            current[index] = item.copy(updatedAt = now)
        } else {
            val newId = if (item.id.isEmpty()) "cal_event_${now}" else item.id
            current.add(
                item.copy(
                    id = newId,
                    createdAt = if (item.createdAt <= 0L) now else item.createdAt,
                    updatedAt = now
                )
            )
        }

        saveEvents(current)
    }

    fun deleteEvent(id: String) {
        val updated = _events.value.filter { it.id != id }
        saveEvents(updated)
    }

    fun toggleEventStatus(id: String) {
        val now = getEpochMillis()
        val updated = _events.value.map {
            if (it.id == id) it.copy(isActive = !it.isActive, updatedAt = now) else it
        }
        saveEvents(updated)
    }

    private fun getDefaultEvents(): List<MandirCalendarEvent> {
        val today = getTodayDateString()
        val now = getEpochMillis()
        return listOf(
            MandirCalendarEvent(
                id = "evt_1",
                title = "बाबा गोगा जी महाराज पावन वार्षिकोत्सव",
                titleEnglish = "Baba Goga Ji Maharaj Annual Festival",
                dateStr = today,
                startTime = "06:00 AM",
                endTime = "10:00 PM",
                categoryTag = "FESTIVAL",
                description = "श्री जामनीवाला दरबार में मुख्य वार्षिकोत्सव, अखण्ड दीप दर्शन एवं विशाल महाप्रसाद भंडारा आयोजन।",
                descriptionEnglish = "Annual Festival, Akhand Deep Darshan and Bhandara at Shree Jamniwala Darbar.",
                imageUrl = "file:///android_asset/temple_pooja.jpg",
                isImportant = true,
                isActive = true,
                createdAt = now
            ),
            MandirCalendarEvent(
                id = "evt_2",
                title = "सोमवार विशेष महाआरती एवं शिव-गोगा पूजन",
                titleEnglish = "Monday Special Maha Aarti & Puja",
                dateStr = "2026-09-14",
                startTime = "07:00 PM",
                endTime = "08:30 PM",
                categoryTag = "AARTI",
                description = "प्रत्येक सोमवार सायं 7:00 बजे विशेष आरती एवं दूध-अभिषेक पूजना",
                descriptionEnglish = "Special evening Aarti and Milk Abhishek Puja.",
                imageUrl = "",
                isImportant = false,
                isActive = true,
                createdAt = now - 1000
            ),
            MandirCalendarEvent(
                id = "evt_3",
                title = "सालासर बालाजी एवं खाटू धाम पावन यात्रा",
                titleEnglish = "Salasar & Khatu Dham Yatra Departure",
                dateStr = "2026-09-20",
                startTime = "08:00 PM",
                endTime = "11:00 PM",
                categoryTag = "YATRA",
                description = "दरबार परिसर से पावन यात्रा प्रस्थान। पंजीयन हेतु मंदिर कार्यालय संपर्क करें।",
                descriptionEnglish = "Holy Yatra departure from temple premises.",
                imageUrl = "",
                isImportant = true,
                isActive = true,
                createdAt = now - 2000
            ),
            MandirCalendarEvent(
                id = "evt_4",
                title = "भाद्रपद पूर्णिमा महापूजा एवं दीपदान",
                titleEnglish = "Bhadrapada Purnima Maha Puja & Deepdaan",
                dateStr = "2026-09-28",
                startTime = "05:00 PM",
                endTime = "09:00 PM",
                categoryTag = "PUJA",
                description = "सत्यनारायण कथा, सामूहिक दीपदान एवं महाप्रसाद वितरण।",
                descriptionEnglish = "Satyanarayan Katha, Deepdaan and Prasad Distribution.",
                imageUrl = "",
                isImportant = false,
                isActive = true,
                createdAt = now - 3000
            )
        )
    }
}
