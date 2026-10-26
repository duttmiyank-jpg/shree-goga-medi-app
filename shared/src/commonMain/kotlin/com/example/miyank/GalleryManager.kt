package com.example.miyank

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class GalleryItem(
    val id: String = "",
    val title: String = "",
    val titleEnglish: String = "",
    val description: String = "",
    val descriptionEnglish: String = "",
    val category: String = "TEMPLE",
    val thumbnailUrl: String = "",
    val optimizedImageUrl: String = "",
    val originalImageUrl: String = "",
    val createdAt: Long = 0L,
    val uploadDate: String = "",
    val isFeatured: Boolean = false,
    val sortOrder: Int = 0
)

object GalleryCategories {
    const val ALL = "ALL"
    const val TEMPLE = "TEMPLE"
    const val GOGA_JI = "GOGA_JI"
    const val EVENTS = "EVENTS"
    const val YATRA = "YATRA"
    const val FESTIVALS = "FESTIVALS"
    const val DEVOTEES = "DEVOTEES"
    const val OTHER = "OTHER"

    val defaultList = listOf(
        ALL,
        TEMPLE,
        GOGA_JI,
        EVENTS,
        YATRA,
        FESTIVALS,
        DEVOTEES,
        OTHER
    )

    fun normalizeKey(rawKey: String): String {
        val upper = rawKey.trim().uppercase()
        return when {
            upper == ALL || upper == "ALL" -> ALL
            upper == TEMPLE || upper.contains("TEMPLE") || upper.contains("मंदिर") || upper.contains("DARBAR") -> TEMPLE
            upper == GOGA_JI || upper.contains("BABA") || upper.contains("GOGA") || upper.contains("बाबा") -> GOGA_JI
            upper == EVENTS || upper.contains("EVENT") || upper.contains("आयोजन") || upper.contains("AARTI") -> EVENTS
            upper == YATRA || upper.contains("YATRA") || upper.contains("यात्रा") -> YATRA
            upper == FESTIVALS || upper.contains("FESTIVAL") || upper.contains("पर्व") || upper.contains("मेले") || upper.contains("SHRINGAR") -> FESTIVALS
            upper == DEVOTEES || upper.contains("DEVOTEE") || upper.contains("SEVADAR") || upper.contains("भक्त") || upper.contains("सेवादार") -> DEVOTEES
            else -> OTHER
        }
    }

    fun getDisplayName(categoryKey: String, isHindi: Boolean): String {
        val norm = normalizeKey(categoryKey)
        return when (norm) {
            ALL -> if (isHindi) "सभी फोटो" else "All Photos"
            TEMPLE -> if (isHindi) "दरबार एवं मंदिर परिसर" else "Darbar & Temple"
            GOGA_JI -> if (isHindi) "बाबा गोगा जी महाराज" else "Baba Gogaji Maharaj"
            EVENTS -> if (isHindi) "आयोजन एवं उत्सव" else "Events & Aarti"
            YATRA -> if (isHindi) "धार्मिक यात्राएँ" else "Yatras"
            FESTIVALS -> if (isHindi) "पर्व एवं मेले" else "Festivals"
            DEVOTEES -> if (isHindi) "भक्तजन एवं सेवादार" else "Devotees & Sevadars"
            else -> if (isHindi) "अन्य" else "Other"
        }
    }
}

object GalleryManager {
    private val settingsStorage = SettingsManager()
    private const val KEY_GALLERY_ITEMS_JSON = "gallery_items_metadata_json"
    private const val KEY_CUSTOM_CATEGORIES_JSON = "gallery_custom_categories_json"
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private val _galleryItems = MutableStateFlow<List<GalleryItem>>(emptyList())
    val galleryItems: StateFlow<List<GalleryItem>> = _galleryItems

    private val _customCategories = MutableStateFlow<List<String>>(emptyList())
    val customCategories: StateFlow<List<String>> = _customCategories

    init {
        loadGallery()
    }

    private fun loadGallery() {
        val savedJson = settingsStorage.getString(KEY_GALLERY_ITEMS_JSON, "")
        if (savedJson.isNotEmpty()) {
            try {
                val list = json.decodeFromString<List<GalleryItem>>(savedJson)
                _galleryItems.value = list.sortedByDescending { it.createdAt }
            } catch (e: Exception) {
                val defaults = getDefaultGalleryItems()
                _galleryItems.value = defaults
                saveItems(defaults)
            }
        } else {
            val defaults = getDefaultGalleryItems()
            _galleryItems.value = defaults
            saveItems(defaults)
        }

        val categoriesJson = settingsStorage.getString(KEY_CUSTOM_CATEGORIES_JSON, "")
        if (categoriesJson.isNotEmpty()) {
            try {
                val list = json.decodeFromString<List<String>>(categoriesJson)
                _customCategories.value = list
            } catch (e: Exception) {
                _customCategories.value = emptyList()
            }
        }
    }

    private fun saveItems(list: List<GalleryItem>) {
        val sorted = list.sortedByDescending { it.createdAt }
        _galleryItems.value = sorted
        try {
            val encoded = json.encodeToString(sorted)
            settingsStorage.setString(KEY_GALLERY_ITEMS_JSON, encoded)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAllCategories(): List<String> {
        val defaults = GalleryCategories.defaultList
        val custom = _customCategories.value
        return (defaults + custom).distinct()
    }

    fun addCustomCategory(newCategoryName: String) {
        val trimmed = newCategoryName.trim()
        if (trimmed.isNotEmpty() && !getAllCategories().contains(trimmed)) {
            val updated = _customCategories.value + trimmed
            _customCategories.value = updated
            try {
                settingsStorage.setString(KEY_CUSTOM_CATEGORIES_JSON, json.encodeToString(updated))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getGalleryItemsPaged(
        categoryKey: String = GalleryCategories.ALL,
        searchQuery: String = "",
        sortNewestFirst: Boolean = true,
        page: Int = 1,
        pageSize: Int = 24
    ): List<GalleryItem> {
        var filtered = _galleryItems.value

        // Category Filter
        if (categoryKey != GalleryCategories.ALL) {
            val normTarget = GalleryCategories.normalizeKey(categoryKey)
            filtered = filtered.filter {
                GalleryCategories.normalizeKey(it.category) == normTarget ||
                        it.category.equals(categoryKey, ignoreCase = true)
            }
        }

        // Search Query Filter
        if (searchQuery.trim().isNotEmpty()) {
            val query = searchQuery.trim().lowercase()
            filtered = filtered.filter {
                it.title.lowercase().contains(query) ||
                        it.titleEnglish.lowercase().contains(query) ||
                        it.description.lowercase().contains(query) ||
                        it.descriptionEnglish.lowercase().contains(query) ||
                        it.category.lowercase().contains(query)
            }
        }

        // Sorting
        filtered = if (sortNewestFirst) {
            filtered.sortedByDescending { it.createdAt }
        } else {
            filtered.sortedBy { it.createdAt }
        }

        // Pagination windowing (Cursor/Page Based)
        val startIndex = (page - 1) * pageSize
        if (startIndex >= filtered.size) return emptyList()

        val endIndex = (startIndex + pageSize).coerceAtMost(filtered.size)
        return filtered.subList(startIndex, endIndex)
    }

    fun getFeaturedItems(): List<GalleryItem> {
        return _galleryItems.value.filter { it.isFeatured }
    }

    fun addOrUpdateGalleryItem(item: GalleryItem) {
        val current = _galleryItems.value.toMutableList()
        val index = current.indexOfFirst { it.id == item.id }

        val now = getEpochMillis()
        val todayStr = getTodayDateString()

        if (index >= 0) {
            current[index] = item
        } else {
            val newId = if (item.id.isEmpty()) "gallery_${now}" else item.id
            current.add(
                item.copy(
                    id = newId,
                    createdAt = if (item.createdAt <= 0L) now else item.createdAt,
                    uploadDate = if (item.uploadDate.isEmpty()) todayStr else item.uploadDate
                )
            )
        }

        saveItems(current)
    }

    fun deleteGalleryItem(id: String) {
        val updated = _galleryItems.value.filter { it.id != id }
        saveItems(updated)
    }

    fun getPhotosForCategory(categoryKey: String): List<String> {
        val items = getGalleryItemsPaged(categoryKey = categoryKey, pageSize = 100)
        return items.map { if (it.thumbnailUrl.isNotEmpty()) it.thumbnailUrl else it.optimizedImageUrl }
    }

    private fun getDefaultGalleryItems(): List<GalleryItem> {
        val now = getEpochMillis()
        val today = getTodayDateString()

        return listOf(
            GalleryItem(
                id = "item_1",
                title = "बाबा गोगा जी महाराज मुख्य दरबार दर्शन",
                titleEnglish = "Baba Gogaji Maharaj Main Darbar View",
                description = "श्री जामनीवाला दरबार में बाबा गोगा जी महाराज के पावन एवं अलौकिक मुख्य दरबार के दर्शन...",
                descriptionEnglish = "Sacred and divine main Darbar view of Baba Gogaji Maharaj at Shree Jamniwala Darbar...",
                category = GalleryCategories.GOGA_JI,
                thumbnailUrl = "https://images.unsplash.com/photo-1544986581-efac024faf62?q=80&w=400",
                optimizedImageUrl = "https://images.unsplash.com/photo-1544986581-efac024faf62?q=80&w=1200",
                createdAt = now,
                uploadDate = today,
                isFeatured = true,
                sortOrder = 1
            ),
            GalleryItem(
                id = "item_2",
                title = "मंदिर परिसर एवं पावन द्वार",
                titleEnglish = "Temple Premises and Sacred Entrance",
                description = "श्री जामनीवाला दरबार मंदिर परिसर का भव्य एवं शांत आध्यात्मिक वातावरण...",
                descriptionEnglish = "Grand and serene spiritual environment of Shree Jamniwala Darbar temple premises...",
                category = GalleryCategories.TEMPLE,
                thumbnailUrl = "https://images.unsplash.com/photo-1621244249243-436b79b5eea8?q=80&w=400",
                optimizedImageUrl = "https://images.unsplash.com/photo-1621244249243-436b79b5eea8?q=80&w=1200",
                createdAt = now - 10000,
                uploadDate = today,
                isFeatured = true,
                sortOrder = 2
            ),
            GalleryItem(
                id = "item_3",
                title = "विशेष महा आरती एवं ज्योति दर्शन",
                titleEnglish = "Special Maha Aarti and Holy Lamp",
                description = "दरबार में आयोजित होने वाली संध्या महा आरती और दिव्य ज्योति के दर्शन...",
                descriptionEnglish = "Evening Maha Aarti and divine Jyoti Darshan held at the Darbar...",
                category = GalleryCategories.EVENTS,
                thumbnailUrl = "https://images.unsplash.com/photo-1609137241280-99c565c026b4?q=80&w=400",
                optimizedImageUrl = "https://images.unsplash.com/photo-1609137241280-99c565c026b4?q=80&w=1200",
                createdAt = now - 20000,
                uploadDate = today,
                isFeatured = false,
                sortOrder = 3
            ),
            GalleryItem(
                id = "item_4",
                title = "पावन शृंगार एवं पुष्प सेवा",
                titleEnglish = "Sacred Shringar and Flower Adornment",
                description = "बाबा जी के पावन स्वरूप का दिव्य शृंगार एवं ताजे पुष्पों से सजी माड़ी...",
                descriptionEnglish = "Divine Shringar of Baba Ji and flower adornment at the holy Medi...",
                category = GalleryCategories.FESTIVALS,
                thumbnailUrl = "https://images.unsplash.com/photo-1577717903315-1691ae25a3d7?q=80&w=400",
                optimizedImageUrl = "https://images.unsplash.com/photo-1577717903315-1691ae25a3d7?q=80&w=1200",
                createdAt = now - 30000,
                uploadDate = today,
                isFeatured = true,
                sortOrder = 4
            )
        )
    }
}
