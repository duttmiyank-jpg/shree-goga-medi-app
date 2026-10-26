package com.example.miyank

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object EventCategories {
    const val ALL = "ALL"
    const val CHOWKI = "CHOWKI"
    const val BHANDARA = "BHANDARA"
    const val UTSAV = "UTSAV"
    const val ANNIVERSARY = "ANNIVERSARY"
    const val GENERAL = "GENERAL"

    val filterList = listOf(ALL, CHOWKI, BHANDARA, UTSAV, ANNIVERSARY, GENERAL)
    val adminList = listOf(CHOWKI, BHANDARA, UTSAV, ANNIVERSARY, GENERAL)

    fun getDisplayName(categoryKey: String, isHindi: Boolean = true): String {
        return when (categoryKey.uppercase()) {
            ALL -> if (isHindi) "सभी" else "All"
            CHOWKI -> if (isHindi) "चौकी" else "Chowki"
            BHANDARA -> if (isHindi) "भंडारा" else "Bhandara"
            UTSAV -> if (isHindi) "उत्सव" else "Utsav"
            ANNIVERSARY -> if (isHindi) "वार्षिकोत्सव" else "Anniversary"
            GENERAL -> if (isHindi) "सामान्य" else "General"
            else -> categoryKey
        }
    }
}

@Serializable
data class EventItem(
    val id: String = "",
    val title: String = "",
    val titleEnglish: String = "",
    val dateStr: String = "", // "YYYY-MM-DD"
    val displayDateStr: String = "", // "18 Sep 2026, शुक्रवार"
    val startTime: String = "07:00 PM",
    val location: String = "श्री जामनीवाला दरबार",
    val categoryTag: String = EventCategories.CHOWKI,
    val description: String = "",
    val imageUrl: String = "",
    val isImportant: Boolean = false,
    val isActive: Boolean = true,
    val sortOrder: Int = 0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

object EventManager {
    private val settingsStorage = SettingsManager()
    private const val KEY_EVENTS_JSON = "events_list_json"
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private val _events = MutableStateFlow(loadEvents())
    val events: StateFlow<List<EventItem>> = _events.asStateFlow()

    private fun loadEvents(): List<EventItem> {
        val savedJson = settingsStorage.getString(KEY_EVENTS_JSON, "")
        if (savedJson.isNotEmpty()) {
            try {
                return json.decodeFromString<List<EventItem>>(savedJson).sortedBy { it.dateStr }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return getDefaultEvents()
    }

    fun saveEvents(list: List<EventItem>) {
        val sorted = list.sortedBy { it.dateStr }
        _events.value = sorted
        try {
            val encoded = json.encodeToString(sorted)
            settingsStorage.setString(KEY_EVENTS_JSON, encoded)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addOrUpdateEvent(item: EventItem) {
        val current = _events.value.toMutableList()
        val now = getEpochMillis()
        val index = current.indexOfFirst { it.id == item.id }

        if (index >= 0) {
            current[index] = item.copy(updatedAt = now)
        } else {
            val newItem = item.copy(
                id = if (item.id.isEmpty()) "event_$now" else item.id,
                createdAt = if (item.createdAt <= 0L) now else item.createdAt,
                updatedAt = now
            )
            current.add(0, newItem)
        }
        saveEvents(current)

        // 1. AUTOMATIC MANDIR CALENDAR ENTRY
        val calEvent = MandirCalendarEvent(
            id = "evt_cal_${item.id.ifEmpty { "event_$now" }}",
            title = "🎉 ${item.title}",
            titleEnglish = item.titleEnglish.ifEmpty { item.title },
            dateStr = item.dateStr,
            startTime = item.startTime,
            categoryTag = item.categoryTag,
            description = item.description,
            imageUrl = item.imageUrl,
            isImportant = item.isImportant,
            isActive = item.isActive,
            targetRoute = TargetRoutes.EVENTS,
            targetId = item.id,
            createdAt = now
        )
        MandirCalendarManager.addOrUpdateEvent(calEvent)

        // 2. AUTOMATIC APP NOTIFICATION BROADCAST
        val notif = NotificationItem(
            id = "notif_evt_${item.id.ifEmpty { "event_$now" }}",
            title = "🎉 नया पावन धार्मिक आयोजन: ${item.title}",
            titleEnglish = "🎉 New Event: ${item.titleEnglish.ifEmpty { item.title }}",
            message = "श्री जामनीवाला दरबार में ${item.title} दिनांक ${item.displayDateStr} को ${item.startTime} बजे आयोजित किया जाएगा। दर्शन हेतु पधारें। 🙏",
            messageEnglish = "Event ${item.title} scheduled on ${item.displayDateStr} at ${item.startTime}.",
            timestampStr = "अभी",
            categoryTag = NotificationCategories.UTSAV,
            targetRoute = TargetRoutes.EVENTS,
            isRead = false,
            isUrgent = item.isImportant,
            createdAt = now
        )
        NotificationManager.addNotification(notif)
    }

    fun deleteEvent(id: String) {
        val updated = _events.value.filter { it.id != id }
        saveEvents(updated)
        MandirCalendarManager.deleteEvent("evt_cal_$id")
    }

    fun toggleActive(id: String) {
        val now = getEpochMillis()
        val updated = _events.value.map {
            if (it.id == id) it.copy(isActive = !it.isActive, updatedAt = now) else it
        }
        saveEvents(updated)
    }

    fun getUpcomingActiveEvents(): List<EventItem> {
        val today = getTodayDateString()
        return _events.value.filter { it.isActive && it.dateStr >= today }.sortedBy { it.dateStr }
    }

    fun getPastEvents(): List<EventItem> {
        val today = getTodayDateString()
        return _events.value.filter { it.isActive && it.dateStr < today }.sortedByDescending { it.dateStr }
    }

    fun getFeaturedUpcomingEvent(): EventItem? {
        return getUpcomingActiveEvents().firstOrNull()
    }

    fun getDaysCountdownText(targetDateStr: String, isHindi: Boolean = true): String {
        val today = getTodayDateString()
        if (targetDateStr == today) return if (isHindi) "आज (Today)" else "Today"

        return try {
            val parts1 = today.split("-").map { it.toInt() }
            val parts2 = targetDateStr.split("-").map { it.toInt() }

            val year1 = parts1[0]; val month1 = parts1[1]; val day1 = parts1[2]
            val year2 = parts2[0]; val month2 = parts2[1]; val day2 = parts2[2]

            val days1 = year1 * 365 + month1 * 30 + day1
            val days2 = year2 * 365 + month2 * 30 + day2
            val diff = days2 - days1

            if (diff == 1) return if (isHindi) "कल (Tomorrow)" else "Tomorrow"
            if (diff in 2..365) return if (isHindi) "$diff दिन बाकी" else "In $diff Days"
            if (isHindi) "आगामी" else "Upcoming"
        } catch (e: Exception) {
            if (isHindi) "आगामी" else "Upcoming"
        }
    }

    private fun getDefaultEvents(): List<EventItem> {
        val today = getTodayDateString()
        val now = getEpochMillis()
        return listOf(
            EventItem(
                id = "event_1",
                title = "विशेष सांध्य चौकी एवं महाआरती",
                titleEnglish = "Special Evening Chowki & Maha Aarti",
                dateStr = today,
                displayDateStr = "$today, आज",
                startTime = "07:00 PM",
                location = "श्री जामनीवाला दरबार मुख्य परिसर",
                categoryTag = EventCategories.CHOWKI,
                description = "बाबा गोगा जी महाराज एवं सत् गुरु बाबा सोम दत्त जी महाराज के पावन दरबार में विशेष चौकी, दिव्य संकीर्तन एवं भव्य आरती आयोजन।",
                imageUrl = "file:///android_asset/temple_pooja.jpg",
                isImportant = true,
                isActive = true,
                createdAt = now
            ),
            EventItem(
                id = "event_2",
                title = "पूर्णिमा महाप्रसाद विशाल भंडारा",
                titleEnglish = "Purnima Mahaprasad Bhandara",
                dateStr = "2026-09-28",
                displayDateStr = "28 Sep 2026, सोमवार",
                startTime = "12:00 PM",
                location = "दरबार भंडारा प्रांगण",
                categoryTag = EventCategories.BHANDARA,
                description = "पूर्णिमा के पावन पर्व पर महाप्रसाद एवं भंडारा वितरण। सभी श्रद्धालु आमंत्रित हैं।",
                imageUrl = "",
                isImportant = true,
                isActive = true,
                createdAt = now - 1000
            )
        )
    }
}
