package com.example.miyank

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class NotificationItem(
    val id: String = "",
    val title: String = "",
    val titleEnglish: String = "",
    val message: String = "",
    val messageEnglish: String = "",
    val timestampStr: String = "",
    val categoryTag: String = "MANDIR",
    val targetRoute: String = "HOME",
    val targetId: String = "",
    val imageUrl: String = "",
    val isRead: Boolean = false,
    val isUrgent: Boolean = false,
    val createdAt: Long = 0L
)

object NotificationCategories {
    const val ALL = "ALL"
    const val UNREAD = "UNREAD"
    const val URGENT = "URGENT"
    const val GENERAL = "GENERAL"

    const val DARSHAN = "DARSHAN"
    const val AARTI = "AARTI"
    const val PUJA = "PUJA"
    const val SEWA = "SEWA"
    const val BHANDARA = "BHANDARA"
    const val YATRA = "YATRA"
    const val UTSAV = "UTSAV"
    const val SUVICHAR = "SUVICHAR"
    const val LIVE_DARSHAN = "LIVE_DARSHAN"
    const val MANDIR = "MANDIR"

    val filterTabs = listOf(ALL, UNREAD, URGENT, GENERAL)

    val categoriesList = listOf(
        DARSHAN,
        AARTI,
        PUJA,
        SEWA,
        BHANDARA,
        YATRA,
        UTSAV,
        SUVICHAR,
        LIVE_DARSHAN,
        MANDIR
    )

    fun getDisplayName(categoryKey: String, isHindi: Boolean): String {
        return when (categoryKey.uppercase()) {
            ALL -> if (isHindi) "सभी" else "All"
            UNREAD -> if (isHindi) "अपठित" else "Unread"
            URGENT -> if (isHindi) "अति आवश्यक" else "Important"
            GENERAL -> if (isHindi) "सामान्य" else "General"

            DARSHAN -> if (isHindi) "दर्शन" else "Darshan"
            AARTI -> if (isHindi) "आरती" else "Aarti"
            PUJA -> if (isHindi) "पूजा" else "Puja"
            SEWA -> if (isHindi) "सेवा" else "Sewa"
            BHANDARA -> if (isHindi) "भंडारा" else "Bhandara"
            YATRA -> if (isHindi) "यात्रा" else "Yatra"
            UTSAV -> if (isHindi) "उत्सव" else "Utsav"
            SUVICHAR -> if (isHindi) "सुविचार" else "Suvichar"
            LIVE_DARSHAN -> if (isHindi) "Live Darshan" else "Live Darshan"
            MANDIR -> if (isHindi) "मंदिर सूचना" else "Mandir Notice"
            else -> categoryKey
        }
    }
}

object NotificationManager {
    private val settingsStorage = SettingsManager()
    private const val KEY_NOTIFICATIONS_JSON = "notifications_list_json"
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        val savedJson = settingsStorage.getString(KEY_NOTIFICATIONS_JSON, "")
        if (savedJson.isNotEmpty()) {
            try {
                val list = json.decodeFromString<List<NotificationItem>>(savedJson)
                _notifications.value = list.sortedByDescending { it.createdAt }
            } catch (e: Exception) {
                val defaults = getDefaultNotifications()
                _notifications.value = defaults
                saveNotifications(defaults)
            }
        } else {
            val defaults = getDefaultNotifications()
            _notifications.value = defaults
            saveNotifications(defaults)
        }
    }

    fun saveNotifications(list: List<NotificationItem>) {
        val sorted = list.sortedByDescending { it.createdAt }
        _notifications.value = sorted
        try {
            val encoded = json.encodeToString(sorted)
            settingsStorage.setString(KEY_NOTIFICATIONS_JSON, encoded)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun markAsRead(id: String) {
        val updated = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
        saveNotifications(updated)
    }

    fun markAllAsRead() {
        val updated = _notifications.value.map { it.copy(isRead = true) }
        saveNotifications(updated)
    }

    fun clearHistory() {
        saveNotifications(emptyList())
    }

    fun addNotification(item: NotificationItem) {
        val current = _notifications.value.toMutableList()
        val now = getEpochMillis()
        val newId = if (item.id.isEmpty()) "notif_${now}" else item.id

        val newItem = item.copy(
            id = newId,
            createdAt = if (item.createdAt <= 0L) now else item.createdAt,
            timestampStr = if (item.timestampStr.isEmpty()) formatDisplayDate(getTodayDateString()) else item.timestampStr
        )
        current.add(0, newItem)
        saveNotifications(current)
    }

    fun deleteNotification(id: String) {
        val updated = _notifications.value.filter { it.id != id }
        saveNotifications(updated)
    }

    fun getUnreadCount(): Int {
        return _notifications.value.count { !it.isRead }
    }

    private fun getDefaultNotifications(): List<NotificationItem> {
        val today = getTodayDateString()
        val now = getEpochMillis()
        return listOf(
            NotificationItem(
                id = "notif_1",
                title = "श्री जामनीवाला दरबार में आपका स्वागत है 🙏",
                titleEnglish = "Welcome to Shree Jamniwala Darbar",
                message = "बाबा गोगा जी महाराज और सत् गुरु बाबा सोम दत्त जी महाराज के पावन चरणों में आपका स्वागत है।",
                messageEnglish = "Welcome to the holy feet of Baba Goga Ji Maharaj and Sat Guru Baba Som Dutt Ji Maharaj.",
                timestampStr = formatDisplayDate(today),
                categoryTag = NotificationCategories.MANDIR,
                targetRoute = TargetRoutes.HOME,
                isRead = false,
                isUrgent = false,
                createdAt = now
            ),
            NotificationItem(
                id = "notif_2",
                title = "आज का दिव्य संदेश 🌸",
                titleEnglish = "Today's Suvichar",
                message = "भक्ति, श्रद्धा और सेवा के मार्ग पर निरंतर आगे बढ़ते रहें। जय श्री गोगा जी महाराज।",
                messageEnglish = "Keep moving forward on the path of devotion, faith and service.",
                timestampStr = "2 घंटे पहले",
                categoryTag = NotificationCategories.SUVICHAR,
                targetRoute = "SUVICHAR",
                isRead = false,
                isUrgent = false,
                createdAt = now - 1000 * 60 * 60 * 2
            ),
            NotificationItem(
                id = "notif_3",
                title = "🔴 Live Darshan उपलब्ध",
                titleEnglish = "Live Darshan Available",
                message = "अब आप श्री जामनीवाला दरबार के लाइव दर्शन मोबाइल एप के माध्यम से कर सकते हैं।",
                messageEnglish = "You can now view live darshan of Shree Jamniwala Darbar via mobile app.",
                timestampStr = "कल",
                categoryTag = NotificationCategories.LIVE_DARSHAN,
                targetRoute = TargetRoutes.LIVE_DARSHAN,
                isRead = true,
                isUrgent = true,
                createdAt = now - 1000 * 60 * 60 * 24
            ),
            NotificationItem(
                id = "notif_4",
                title = "विशेष सांध्य महाआरती सूचना 🪔",
                titleEnglish = "Special Evening Aarti Notice",
                message = "आज सायं 7:00 बजे विशेष महाआरती एवं महाप्रसाद वितरण का आयोजन किया जाएगा।",
                messageEnglish = "Special Maha Aarti and Mahaprasad distribution will be organized today at 7:00 PM.",
                timestampStr = "2 दिन पहले",
                categoryTag = NotificationCategories.AARTI,
                targetRoute = TargetRoutes.EVENTS,
                isRead = true,
                isUrgent = false,
                createdAt = now - 1000 * 60 * 60 * 48
            )
        )
    }
}
