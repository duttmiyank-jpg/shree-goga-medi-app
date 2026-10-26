package com.example.miyank

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
enum class LivePlatformType {
    YOUTUBE,
    INSTAGRAM,
    FACEBOOK,
    OTHER
}

@Serializable
data class LivePlatform(
    val id: String = "",
    val name: String = "",
    val nameHindi: String = "",
    val platformType: LivePlatformType = LivePlatformType.YOUTUBE,
    val url: String = "",
    val isEnabled: Boolean = true,
    val isLive: Boolean = false,
    val title: String = "",
    val description: String = "",
    val sortOrder: Int = 1,
    val updatedAt: Long = 0L
)

@Serializable
data class AartiTimingItem(
    val id: String = "",
    val nameHindi: String = "",
    val nameEnglish: String = "",
    val time: String = "",
    val description: String = "",
    val isActive: Boolean = true,
    val sortOrder: Int = 0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

@Serializable
data class LiveDarshanSettings(
    val streamUrl: String = "",
    val isLiveNow: Boolean = false,
    val title: String = "लाइव दर्शन",
    val titleEnglish: String = "Live Darshan",
    val description: String = "श्री जामनीवाला दरबार से सीधे लाइव दर्शन एवं आरती",
    val shareCardImage: String = "",
    val shareTitle: String = "🔴 लाइव दर्शन",
    val shareDescription: String = "श्री जामनीवाला दरबार से सीधे लाइव दर्शन एवं आरती देखें।",
    val shareMessageHindi: String = "🔴 लाइव दर्शन\n\nश्री जामनीवाला दरबार से सीधे लाइव दर्शन एवं आरती देखें।",
    val shareMessageEnglish: String = "🔴 Live Darshan\n\nWatch direct Live Darshan and Aarti from Shree Jamniwala Darbar.",
    val morningDarshanOpening: String = "05:00 AM",
    val morningDarshanClosing: String = "12:00 PM",
    val eveningDarshanOpening: String = "04:00 PM",
    val eveningDarshanClosing: String = "09:00 PM",
    val aartiTimings: List<AartiTimingItem> = getDefaultAartiTimings(),
    val platforms: List<LivePlatform> = emptyList(),
    val updatedAt: Long = 0L
)

fun getDefaultLivePlatforms(legacyUrl: String = "", legacyIsLive: Boolean = false, now: Long = getEpochMillis()): List<LivePlatform> {
    return listOf(
        LivePlatform(
            id = "platform_youtube",
            name = "YouTube Live",
            nameHindi = "YouTube पर लाइव दर्शन देखें",
            platformType = LivePlatformType.YOUTUBE,
            url = legacyUrl,
            isEnabled = true,
            isLive = legacyIsLive,
            sortOrder = 1,
            updatedAt = now
        ),
        LivePlatform(
            id = "platform_instagram",
            name = "Instagram Live",
            nameHindi = "Instagram पर लाइव दर्शन देखें",
            platformType = LivePlatformType.INSTAGRAM,
            url = "",
            isEnabled = true,
            isLive = false,
            sortOrder = 2,
            updatedAt = now
        ),
        LivePlatform(
            id = "platform_facebook",
            name = "Facebook Live",
            nameHindi = "Facebook पर लाइव दर्शन देखें",
            platformType = LivePlatformType.FACEBOOK,
            url = "",
            isEnabled = false,
            isLive = false,
            sortOrder = 3,
            updatedAt = now
        )
    )
}

fun getDefaultAartiTimings(): List<AartiTimingItem> {
    val now = getEpochMillis()
    return listOf(
        AartiTimingItem(
            id = "aarti_1",
            nameHindi = "मंगला आरती",
            nameEnglish = "Mangala Aarti",
            time = "06:00 AM",
            description = "प्रातःकालीन मंगला आरती",
            isActive = true,
            sortOrder = 1,
            createdAt = now,
            updatedAt = now
        ),
        AartiTimingItem(
            id = "aarti_2",
            nameHindi = "संध्या महाआरती",
            nameEnglish = "Sandhya Maha Aarti",
            time = "07:00 PM",
            description = "सायंकालीन भव्य आरती",
            isActive = true,
            sortOrder = 2,
            createdAt = now,
            updatedAt = now
        )
    )
}

object LiveDarshanManager {
    private val settingsStorage = SettingsManager()
    private const val KEY_LIVE_DARSHAN_JSON = "live_darshan_settings_json"
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<LiveDarshanSettings> = _settings.asStateFlow()

    private var previousIsLiveState = _settings.value.isLiveNow

    private fun loadSettings(): LiveDarshanSettings {
        val savedJson = settingsStorage.getString(KEY_LIVE_DARSHAN_JSON, "")
        if (savedJson.isNotEmpty()) {
            try {
                var loaded = json.decodeFromString<LiveDarshanSettings>(savedJson)
                if (loaded.aartiTimings.isEmpty()) {
                    loaded = loaded.copy(aartiTimings = getDefaultAartiTimings())
                }
                if (loaded.platforms.isEmpty()) {
                    loaded = loaded.copy(platforms = getDefaultLivePlatforms(loaded.streamUrl, loaded.isLiveNow))
                }
                return loaded
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return LiveDarshanSettings(platforms = getDefaultLivePlatforms())
    }

    fun updateSettings(newSettings: LiveDarshanSettings) {
        // Sync legacy YouTube fields with YouTube platform if present
        val ytPlatform = newSettings.platforms.find { it.platformType == LivePlatformType.YOUTUBE }
        val syncedSettings = if (ytPlatform != null && ytPlatform.url.isNotBlank()) {
            newSettings.copy(
                streamUrl = ytPlatform.url,
                isLiveNow = ytPlatform.isLive && ytPlatform.isEnabled
            )
        } else {
            newSettings
        }

        val currentIsLive = syncedSettings.isLiveNow || syncedSettings.platforms.any { it.isEnabled && it.isLive }
        val wasLiveBefore = previousIsLiveState

        _settings.value = syncedSettings
        previousIsLiveState = currentIsLive

        try {
            val encoded = json.encodeToString(syncedSettings)
            settingsStorage.setString(KEY_LIVE_DARSHAN_JSON, encoded)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // TRIGGER NOTIFICATION ONLY ON OFFLINE -> LIVE TRANSITION
        if (!wasLiveBefore && currentIsLive) {
            val notif = NotificationItem(
                id = "live_notif_${getEpochMillis()}",
                title = "🔴 लाइव दर्शन प्रारंभ हो गए हैं!",
                titleEnglish = "🔴 Live Darshan has Started!",
                message = "अभी श्री जामनीवाला दरबार के पावन लाइव दर्शन करें। 🙏",
                messageEnglish = "Watch Live Darshan from Shree Jamniwala Darbar now. 🙏",
                timestampStr = "अभी",
                categoryTag = NotificationCategories.LIVE_DARSHAN,
                targetRoute = TargetRoutes.LIVE_DARSHAN,
                isRead = false,
                isUrgent = true,
                createdAt = getEpochMillis()
            )
            NotificationManager.addNotification(notif)
        }
    }

    fun updateAartiList(aartiList: List<AartiTimingItem>) {
        val current = _settings.value
        val updated = current.copy(
            aartiTimings = aartiList.sortedBy { it.sortOrder },
            updatedAt = getEpochMillis()
        )
        updateSettings(updated)
    }

    fun getActivePlatforms(): List<LivePlatform> {
        val current = _settings.value
        val list = current.platforms.filter { it.isEnabled && it.url.isNotBlank() }.sortedBy { it.sortOrder }
        if (list.isNotEmpty()) return list

        // Fallback to YouTube if legacy streamUrl is present
        if (current.streamUrl.isNotBlank()) {
            return listOf(
                LivePlatform(
                    id = "platform_youtube",
                    name = "YouTube Live",
                    nameHindi = "YouTube पर लाइव दर्शन देखें",
                    platformType = LivePlatformType.YOUTUBE,
                    url = current.streamUrl,
                    isEnabled = true,
                    isLive = current.isLiveNow,
                    sortOrder = 1
                )
            )
        }
        return emptyList()
    }

    fun extractYouTubeVideoId(url: String): String? {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return null

        if (trimmed.contains("?")) {
            val beforeQuery = trimmed.substringBefore("?")
            if (beforeQuery.length == 11 && beforeQuery.matches(Regex("^[a-zA-Z0-9_-]{11}$"))) {
                return beforeQuery
            }
        }

        if (trimmed.length == 11 && trimmed.matches(Regex("^[a-zA-Z0-9_-]{11}$"))) {
            return trimmed
        }

        val patterns = listOf(
            Regex("(?:v=|v/|vi=|vi/|youtu\\.be/|embed/|live/|shorts/)([a-zA-Z0-9_-]{11})"),
            Regex("(?:youtube\\.com/(?:[^/]+/.+/|(?:v|e(?:mbed)?|live|shorts)/|.*[?&]v=)|youtu\\.be/)([a-zA-Z0-9_-]{11})")
        )

        for (pattern in patterns) {
            val match = pattern.find(trimmed)
            if (match != null) {
                val groupVal = if (match.groupValues.size > 1) match.groupValues[1] else match.value
                if (groupVal.length == 11 && groupVal.matches(Regex("^[a-zA-Z0-9_-]{11}$"))) {
                    return groupVal
                }
            }
        }
        return null
    }

    fun getValidLaunchUrl(urlInput: String): String {
        val trimmed = urlInput.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed
        }
        val videoId = extractYouTubeVideoId(trimmed)
        if (videoId != null) {
            return "https://www.youtube.com/watch?v=$videoId"
        }
        return if (trimmed.isNotEmpty()) "https://$trimmed" else "https://www.youtube.com"
    }

    fun normalizeYouTubeUrl(input: String): String {
        return getValidLaunchUrl(input)
    }

    fun getYoutubeThumbnailUrl(videoId: String): String {
        return "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
    }

    fun validatePlatformUrl(type: LivePlatformType, urlInput: String): Boolean {
        val trimmed = urlInput.trim()
        if (trimmed.isEmpty()) return false
        return when (type) {
            LivePlatformType.YOUTUBE -> trimmed.contains("youtube") || trimmed.contains("youtu.be") || extractYouTubeVideoId(trimmed) != null
            LivePlatformType.INSTAGRAM -> trimmed.contains("instagram")
            LivePlatformType.FACEBOOK -> trimmed.contains("facebook") || trimmed.contains("fb.")
            LivePlatformType.OTHER -> trimmed.startsWith("http://") || trimmed.startsWith("https://")
        }
    }

    fun getActualPlayableStatus(): Boolean {
        val current = _settings.value
        if (current.platforms.any { it.isEnabled && it.isLive && it.url.isNotBlank() }) {
            return true
        }
        if (!current.isLiveNow) return false
        val videoId = extractYouTubeVideoId(current.streamUrl)
        return videoId != null
    }
}

// Single source alias
typealias LiveStreamManager = LiveDarshanManager
