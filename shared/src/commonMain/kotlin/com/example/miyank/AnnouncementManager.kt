package com.example.miyank

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class AnnouncementItem(
    val id: String = "",
    val title: String = "",
    val titleEnglish: String = "",
    val dateStr: String = "", // "YYYY-MM-DD"
    val content: String = "",
    val contentEnglish: String = "",
    val categoryTag: String = "GENERAL",
    val imageUrl: String = "",
    val isUrgent: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: Long = 0L
)

object AnnouncementCategories {
    const val ALL = "ALL"
    const val URGENT = "URGENT"
    const val GENERAL = "GENERAL"
    const val YATRA = "YATRA"
    const val CHOWKI = "CHOWKI"
    const val RULES = "RULES"

    val filterList = listOf(ALL, URGENT, GENERAL, YATRA, CHOWKI, RULES)
    val adminList = listOf(GENERAL, URGENT, YATRA, CHOWKI, RULES)

    fun getDisplayName(categoryKey: String, isHindi: Boolean): String {
        return when (categoryKey.uppercase()) {
            ALL -> if (isHindi) "सभी" else "All"
            URGENT -> if (isHindi) "अति आवश्यक" else "Urgent"
            GENERAL -> if (isHindi) "सामान्य सूचना" else "General"
            YATRA, "UTSAV" -> if (isHindi) "बागढ़ यात्रा" else "Bagad Yatra"
            CHOWKI -> if (isHindi) "चौंकी / बसेरा" else "Chowki / Basera"
            RULES -> if (isHindi) "मंदिर नियम" else "Mandir Rules"
            else -> categoryKey
        }
    }
}

object AnnouncementManager {
    private val settingsStorage = SettingsManager()
    private const val KEY_ANNOUNCEMENTS_JSON = "announcements_list_json"
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private val _announcements = MutableStateFlow<List<AnnouncementItem>>(emptyList())
    val announcements: StateFlow<List<AnnouncementItem>> = _announcements

    init {
        loadAnnouncements()
    }

    private fun loadAnnouncements() {
        val savedJson = settingsStorage.getString(KEY_ANNOUNCEMENTS_JSON, "")
        if (savedJson.isNotEmpty()) {
            try {
                val list = json.decodeFromString<List<AnnouncementItem>>(savedJson)
                _announcements.value = list.sortedByDescending { it.dateStr }
            } catch (e: Exception) {
                val defaults = getDefaultAnnouncements()
                _announcements.value = defaults
                saveAnnouncements(defaults)
            }
        } else {
            val defaults = getDefaultAnnouncements()
            _announcements.value = defaults
            saveAnnouncements(defaults)
        }
    }

    fun saveAnnouncements(list: List<AnnouncementItem>) {
        val sorted = list.sortedByDescending { it.dateStr }
        _announcements.value = sorted
        try {
            val encoded = json.encodeToString(sorted)
            settingsStorage.setString(KEY_ANNOUNCEMENTS_JSON, encoded)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getActiveAnnouncements(): List<AnnouncementItem> {
        val active = _announcements.value.filter { it.isActive }.sortedByDescending { it.dateStr }
        return if (active.isNotEmpty()) active else getDefaultAnnouncements().filter { it.isActive }
    }

    fun getLatestImportantAnnouncements(limit: Int = 2): List<AnnouncementItem> {
        return getActiveAnnouncements().take(limit)
    }

    fun addOrUpdateAnnouncement(item: AnnouncementItem) {
        val current = _announcements.value.toMutableList()
        val index = current.indexOfFirst { it.id == item.id }
        val now = getEpochMillis()

        if (index >= 0) {
            current[index] = item
        } else {
            val newId = if (item.id.isEmpty()) "announcement_${now}" else item.id
            current.add(
                item.copy(
                    id = newId,
                    createdAt = if (item.createdAt <= 0L) now else item.createdAt
                )
            )
        }

        saveAnnouncements(current)
    }

    fun deleteAnnouncement(id: String) {
        val updated = _announcements.value.filter { it.id != id }
        saveAnnouncements(updated)
    }

    fun toggleAnnouncementStatus(id: String) {
        val updated = _announcements.value.map {
            if (it.id == id) it.copy(isActive = !it.isActive) else it
        }
        saveAnnouncements(updated)
    }

    private fun getDefaultAnnouncements(): List<AnnouncementItem> {
        val today = getTodayDateString()
        val now = getEpochMillis()
        return listOf(
            AnnouncementItem(
                id = "ann_1",
                title = "श्री गोगामेड़ी बागढ़ यात्रा एवं विशाल धार्मिक आयोजन सूचना",
                titleEnglish = "Shree Gogamedi Bagad Yatra & Religious Function Notice",
                dateStr = today,
                content = "श्री जामनीवाला दरबार की ओर से आगामी पावन बागढ़ यात्रा का आयोजन किया जा रहा है। यात्रा एवं महाप्रसाद भंडारे में सभी श्रद्धालु सादर आमंत्रित हैं।",
                contentEnglish = "Shree Gogamedi Bagad Yatra & religious function is organized by Shree Jamniwala Darbar. All devotees are cordially invited.",
                categoryTag = AnnouncementCategories.YATRA,
                imageUrl = "file:///android_asset/temple_pooja.jpg",
                isUrgent = true,
                isActive = true,
                createdAt = now
            ),
            AnnouncementItem(
                id = "ann_2",
                title = "रविवार विशेष भजन एवं महाआरती समय सूचना",
                titleEnglish = "Sunday Special Bhajan & Maha Aarti Timings",
                dateStr = "2026-09-02",
                content = "प्रत्येक रविवार सायं 5:00 बजे से विशेष भजन-कीर्तन एवं सायं 7:00 बजे महाआरती का आयोजन किया जाएगा। सभी श्रद्धालु समय से दरबार में उपस्थित होकर पुण्य लाभ कमाएँ।",
                contentEnglish = "Special Bhajan-Kirtan will be organized every Sunday from 5:00 PM and Maha Aarti at 7:00 PM. All devotees are requested to be present on time.",
                categoryTag = AnnouncementCategories.GENERAL,
                imageUrl = "",
                isUrgent = false,
                isActive = true,
                createdAt = now - 1000
            ),
            AnnouncementItem(
                id = "ann_3",
                title = "साप्ताहिक चौंकी एवं पावन बसेरा कार्यक्रम सूचना",
                titleEnglish = "Weekly Chowki & Holy Basera Notice",
                dateStr = "2026-08-30",
                content = "श्री जामनीवाला दरबार में पावन चौंकी एवं रात्रि बसेरा कार्यक्रम का आयोजन किया जा रहा है। दरबार में भक्तों के रुकने एवं प्रसाद की समुचित व्यवस्था है।",
                contentEnglish = "Holy Chowki and night Basera program is being organized at Shree Jamniwala Darbar. Proper arrangements for devotees stay and prasad are available.",
                categoryTag = AnnouncementCategories.CHOWKI,
                imageUrl = "",
                isUrgent = false,
                isActive = true,
                createdAt = now - 2000
            ),
            AnnouncementItem(
                id = "ann_4",
                title = "मंदिर परिसर एवं दर्शन पंक्ति संबंधी आवश्यक नियम",
                titleEnglish = "Important Rules for Temple Premises & Queue",
                dateStr = "2026-08-25",
                content = "कृपया दर्शन के समय शांति बनाए रखें, मोबाइल साइलेंट मोड पर रखें, एवं स्वच्छता का विशेष ध्यान दें। दरबार परिसर में सेवा भाव से प्रवेश करें।",
                contentEnglish = "Please maintain silence during Darshan, keep mobile on silent mode, and maintain cleanliness in temple premises.",
                categoryTag = AnnouncementCategories.RULES,
                imageUrl = "",
                isUrgent = false,
                isActive = true,
                createdAt = now - 3000
            )
        )
    }
}
