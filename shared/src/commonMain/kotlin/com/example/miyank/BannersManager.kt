package com.example.miyank

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class BannerItem(
    val id: String = "",
    val imageUrl: String = "",
    val categoryTag: String = "JAGRAN",
    val title: String = "",
    val titleEnglish: String = "",
    val targetRoute: String = "EVENTS",
    val isActive: Boolean = true,
    val sortOrder: Int = 0,
    val createdAt: Long = 0L
)

object BannerCategories {
    const val JAGRAN = "JAGRAN"
    const val DARSHAN = "DARSHAN"
    const val UTSAV = "UTSAV"
    const val YATRA = "YATRA"
    const val SEWA = "SEWA"
    const val SPECIAL = "SPECIAL"

    val list = listOf(
        JAGRAN,
        DARSHAN,
        UTSAV,
        YATRA,
        SEWA,
        SPECIAL
    )

    fun getDisplayName(categoryTag: String, isHindi: Boolean): String {
        return when (categoryTag.uppercase()) {
            JAGRAN -> if (isHindi) "जागरण" else "JAGRAN"
            DARSHAN -> if (isHindi) "दर्शन" else "DARSHAN"
            UTSAV -> if (isHindi) "उत्सव" else "UTSAV"
            YATRA -> if (isHindi) "यात्रा" else "YATRA"
            SEWA -> if (isHindi) "सेवा" else "SEWA"
            SPECIAL -> if (isHindi) "विशेष" else "SPECIAL"
            else -> categoryTag
        }
    }
}

object TargetRoutes {
    const val HOME = "HOME"
    const val SUVICHAR = "SUVICHAR"
    const val EVENTS = "EVENTS"
    const val LIVE_DARSHAN = "LIVE_DARSHAN"
    const val YATRA = "YATRA"
    const val PUJA = "PUJA"
    const val GALLERY = "GALLERY"
    const val ABOUT_TEMPLE = "ABOUT_TEMPLE"
    const val DONATION = "DONATION"
    const val ANNOUNCEMENTS = "ANNOUNCEMENTS"

    val list = listOf(
        HOME,
        SUVICHAR,
        EVENTS,
        LIVE_DARSHAN,
        YATRA,
        GALLERY,
        ABOUT_TEMPLE,
        DONATION,
        ANNOUNCEMENTS
    )

    fun getDisplayName(routeKey: String, isHindi: Boolean): String {
        return when (routeKey.uppercase()) {
            HOME -> if (isHindi) "होम स्क्रीन" else "Home Screen"
            SUVICHAR -> if (isHindi) "दैनिक सुविचार" else "Daily Suvichar"
            EVENTS -> if (isHindi) "आयोजन (Events)" else "Events Screen"
            LIVE_DARSHAN -> if (isHindi) "लाइव दर्शन" else "Live Darshan"
            YATRA -> if (isHindi) "यात्राएँ (Yatra)" else "Yatra Screen"
            GALLERY -> if (isHindi) "दिव्य गैलरी" else "Gallery Screen"
            ABOUT_TEMPLE -> if (isHindi) "मंदिर का इतिहास" else "Temple Info"
            DONATION -> if (isHindi) "दान एवं सेवा" else "Donation Screen"
            ANNOUNCEMENTS -> if (isHindi) "घोषणाएं" else "Announcements"
            else -> routeKey
        }
    }
}

object BannersManager {
    private val settingsStorage = SettingsManager()
    private const val KEY_BANNERS_JSON = "home_banners_list_json"
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private val _banners = MutableStateFlow<List<BannerItem>>(emptyList())
    val banners: StateFlow<List<BannerItem>> = _banners

    init {
        loadBanners()
    }

    private fun loadBanners() {
        val savedJson = settingsStorage.getString(KEY_BANNERS_JSON, "")
        if (savedJson.isNotEmpty()) {
            try {
                val list = json.decodeFromString<List<BannerItem>>(savedJson)
                _banners.value = list.sortedBy { it.sortOrder }
            } catch (e: Exception) {
                val defaults = getDefaultBanners()
                _banners.value = defaults
                saveBanners(defaults)
            }
        } else {
            val defaults = getDefaultBanners()
            _banners.value = defaults
            saveBanners(defaults)
        }
    }

    fun saveBanners(list: List<BannerItem>) {
        val sorted = list.sortedBy { it.sortOrder }
        _banners.value = sorted
        try {
            val encoded = json.encodeToString(sorted)
            settingsStorage.setString(KEY_BANNERS_JSON, encoded)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getActiveBanners(): List<BannerItem> {
        val active = _banners.value.filter { it.isActive }.sortedBy { it.sortOrder }
        return if (active.isNotEmpty()) active else getDefaultBanners().filter { it.isActive }
    }

    fun addOrUpdateBanner(item: BannerItem) {
        val current = _banners.value.toMutableList()
        val index = current.indexOfFirst { it.id == item.id }

        val now = getEpochMillis()

        if (index >= 0) {
            current[index] = item
        } else {
            val newId = if (item.id.isEmpty()) "banner_${now}" else item.id
            val nextSort = if (current.isEmpty()) 1 else (current.maxOfOrNull { it.sortOrder } ?: 0) + 1
            current.add(
                item.copy(
                    id = newId,
                    sortOrder = if (item.sortOrder == 0) nextSort else item.sortOrder,
                    createdAt = if (item.createdAt <= 0L) now else item.createdAt
                )
            )
        }

        saveBanners(current)
    }

    fun deleteBanner(id: String) {
        val updated = _banners.value.filter { it.id != id }
        saveBanners(updated)
    }

    fun toggleBannerStatus(id: String) {
        val updated = _banners.value.map {
            if (it.id == id) it.copy(isActive = !it.isActive) else it
        }
        saveBanners(updated)
    }

    private fun getDefaultBanners(): List<BannerItem> {
        val now = getEpochMillis()
        return listOf(
            BannerItem(
                id = "banner_1",
                imageUrl = "https://images.unsplash.com/photo-1544986581-efac024faf62?q=80&w=1000",
                categoryTag = BannerCategories.JAGRAN,
                title = "महा जागरण महोत्सव एवं कीर्तन",
                titleEnglish = "Maha Jagran Mahotsav & Kirtan",
                targetRoute = TargetRoutes.EVENTS,
                isActive = true,
                sortOrder = 1,
                createdAt = now
            ),
            BannerItem(
                id = "banner_2",
                imageUrl = "https://images.unsplash.com/photo-1621244249243-436b79b5eea8?q=80&w=1000",
                categoryTag = BannerCategories.DARSHAN,
                title = "बाबा गोगा जी महाराज का भव्य दर्शन",
                titleEnglish = "Baba Goga Ji Maharaj Grand Darshan",
                targetRoute = TargetRoutes.LIVE_DARSHAN,
                isActive = true,
                sortOrder = 2,
                createdAt = now - 1000
            ),
            BannerItem(
                id = "banner_3",
                imageUrl = "https://images.unsplash.com/photo-1609137241280-99c565c026b4?q=80&w=1000",
                categoryTag = BannerCategories.UTSAV,
                title = "विशेष दर्शन एवं पावन उत्सव",
                titleEnglish = "Special Darshan and Holy Utsav",
                targetRoute = TargetRoutes.YATRA,
                isActive = true,
                sortOrder = 3,
                createdAt = now - 2000
            )
        )
    }
}
