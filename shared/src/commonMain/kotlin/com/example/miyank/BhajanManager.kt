package com.example.miyank

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class BhajanItem(
    val id: String = "",
    val title: String = "",
    val titleEnglish: String = "",
    val artist: String = "श्री जामनीवाला दरबार",
    val artistEnglish: String = "Shree Jamniwala Darbar",
    val duration: String = "05:20",
    val audioUrl: String = "",
    val thumbnailUrl: String = "",
    val categoryTag: String = "BHAJAN",
    val lyricsText: String = "",
    val lyricsEnglish: String = "",
    val isActive: Boolean = true,
    val sortOrder: Int = 0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

object BhajanCategories {
    const val ALL = "ALL"
    const val FAVORITES = "FAVORITES"
    const val RECENT = "RECENT"
    const val BHAJAN = "BHAJAN"
    const val AARTI = "AARTI"
    const val MANTRA = "MANTRA"
    const val CHALISA = "CHALISA"
    const val STUTI = "STUTI"
    const val KATHA = "KATHA"

    val filterList = listOf(ALL, FAVORITES, RECENT, BHAJAN, AARTI, MANTRA, CHALISA, STUTI, KATHA)
    val adminList = listOf(BHAJAN, AARTI, MANTRA, CHALISA, STUTI, KATHA)

    fun getDisplayName(categoryKey: String, isHindi: Boolean): String {
        return when (categoryKey.uppercase()) {
            ALL -> if (isHindi) "सभी" else "All"
            FAVORITES -> if (isHindi) "❤️ पसंदीदा" else "Favorites"
            RECENT -> if (isHindi) "🕒 हाल ही में सुने" else "Recent"
            BHAJAN -> if (isHindi) "भजन" else "Bhajan"
            AARTI -> if (isHindi) "आरती" else "Aarti"
            MANTRA -> if (isHindi) "मंत्र" else "Mantra"
            CHALISA -> if (isHindi) "चालीसा" else "Chalisa"
            STUTI -> if (isHindi) "स्तुति" else "Stuti"
            KATHA -> if (isHindi) "कथा" else "Katha"
            else -> categoryKey
        }
    }
}

object BhajanManager {
    private val settingsStorage = SettingsManager()
    private const val KEY_BHAJANS_JSON = "bhajans_list_json"
    private const val KEY_FAVORITES_JSON = "user_favorite_bhajans_json"
    private const val KEY_RECENT_JSON = "user_recent_bhajans_json"
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private val _bhajans = MutableStateFlow<List<BhajanItem>>(emptyList())
    val bhajans: StateFlow<List<BhajanItem>> = _bhajans

    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds

    private val _recentIds = MutableStateFlow<List<String>>(emptyList())
    val recentIds: StateFlow<List<String>> = _recentIds

    init {
        loadBhajans()
        loadFavoritesAndRecents()
    }

    private fun loadBhajans() {
        val savedJson = settingsStorage.getString(KEY_BHAJANS_JSON, "")
        if (savedJson.isNotEmpty()) {
            try {
                val list = json.decodeFromString<List<BhajanItem>>(savedJson)
                _bhajans.value = list.sortedBy { it.sortOrder }
            } catch (e: Exception) {
                val defaults = getDefaultBhajans()
                _bhajans.value = defaults
                saveBhajans(defaults)
            }
        } else {
            val defaults = getDefaultBhajans()
            _bhajans.value = defaults
            saveBhajans(defaults)
        }
    }

    private fun loadFavoritesAndRecents() {
        try {
            val favJson = settingsStorage.getString(KEY_FAVORITES_JSON, "")
            if (favJson.isNotEmpty()) {
                val set = json.decodeFromString<Set<String>>(favJson)
                _favoriteIds.value = set
            }
            val recJson = settingsStorage.getString(KEY_RECENT_JSON, "")
            if (recJson.isNotEmpty()) {
                val list = json.decodeFromString<List<String>>(recJson)
                _recentIds.value = list
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveBhajans(list: List<BhajanItem>) {
        val sorted = list.sortedBy { it.sortOrder }
        _bhajans.value = sorted
        try {
            val encoded = json.encodeToString(sorted)
            settingsStorage.setString(KEY_BHAJANS_JSON, encoded)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getActiveBhajans(): List<BhajanItem> {
        val active = _bhajans.value.filter { it.isActive }.sortedBy { it.sortOrder }
        return if (active.isNotEmpty()) active else getDefaultBhajans().filter { it.isActive }
    }

    fun toggleFavorite(id: String) {
        val currentSet = _favoriteIds.value.toMutableSet()
        if (currentSet.contains(id)) {
            currentSet.remove(id)
        } else {
            currentSet.add(id)
        }
        _favoriteIds.value = currentSet
        try {
            val encoded = json.encodeToString(currentSet)
            settingsStorage.setString(KEY_FAVORITES_JSON, encoded)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addRecentPlayed(id: String) {
        val currentList = _recentIds.value.toMutableList()
        currentList.remove(id)
        currentList.add(0, id)
        val trimmed = currentList.take(15)
        _recentIds.value = trimmed
        try {
            val encoded = json.encodeToString(trimmed)
            settingsStorage.setString(KEY_RECENT_JSON, encoded)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addOrUpdateBhajan(item: BhajanItem) {
        val current = _bhajans.value.toMutableList()
        val index = current.indexOfFirst { it.id == item.id }
        val now = getEpochMillis()

        if (index >= 0) {
            current[index] = item.copy(updatedAt = now)
        } else {
            val newId = if (item.id.isEmpty()) "bhajan_${now}" else item.id
            val nextSort = if (current.isEmpty()) 1 else (current.maxOfOrNull { it.sortOrder } ?: 0) + 1
            current.add(
                item.copy(
                    id = newId,
                    sortOrder = if (item.sortOrder == 0) nextSort else item.sortOrder,
                    createdAt = if (item.createdAt <= 0L) now else item.createdAt,
                    updatedAt = now
                )
            )
        }

        saveBhajans(current)
    }

    fun deleteBhajan(id: String) {
        val updated = _bhajans.value.filter { it.id != id }
        saveBhajans(updated)
    }

    fun toggleBhajanStatus(id: String) {
        val now = getEpochMillis()
        val updated = _bhajans.value.map {
            if (it.id == id) it.copy(isActive = !it.isActive, updatedAt = now) else it
        }
        saveBhajans(updated)
    }

    private fun getDefaultBhajans(): List<BhajanItem> {
        val now = getEpochMillis()
        return listOf(
            BhajanItem(
                id = "bhajan_1",
                title = "श्री गोगा जी महाराज की महाआरती",
                titleEnglish = "Shree Goga Ji Maharaj Maha Aarti",
                artist = "नरेंद्र चंचल",
                artistEnglish = "Narendra Chanchal",
                duration = "05:20",
                audioUrl = "file:///android_asset/background_bhajan.mp3",
                thumbnailUrl = "file:///android_asset/temple_pooja.jpg",
                categoryTag = BhajanCategories.AARTI,
                lyricsText = "जय जय जय गोगा देवा, जय जय जय गोगा देवा।\nबाछल माँ के लाडले, बाग़ड़ के राजा।\nजय जय जय गोगा देवा...\n\nनीले घोड़े की सवारी, भाल सोहे हाथ।\nनागों के स्वामी तुम्ही, सिद्धों के सरताज।\nजय जय जय गोगा देवा...\n\nभक्तों के संकट हरते, महिमा अपरम्पार।\nजो भी शरण में आये, उसका हो उद्धार।\nजय जय जय गोगा देवा...",
                lyricsEnglish = "Jai Jai Jai Goga Deva, Baachal Maa Ke Laadle, Bagad Ke Raja...",
                isActive = true,
                sortOrder = 1,
                createdAt = now
            ),
            BhajanItem(
                id = "bhajan_2",
                title = "चालीसा - श्री गोगा जी महाराज",
                titleEnglish = "Shree Goga Ji Chalisa",
                artist = "लखबीर सिंह लक्खा",
                artistEnglish = "Lakhbir Singh Lakkha",
                duration = "06:45",
                audioUrl = "",
                thumbnailUrl = "https://images.unsplash.com/photo-1621244249243-436b79b5eea8?q=80&w=200",
                categoryTag = BhajanCategories.CHALISA,
                lyricsText = "दोहा: \nगणपति शारद ध्याई के, गुरु के चरण मनाय।\nगोगा जी की महिमा, वरनूँ अति सुख पाय।\n\nचौपाई: \nजय गोगा जी सिद्ध सुजाना। बाछल मात के लाडले जाना।\nबाग़ड़ देश में जनम लियो है। भक्तों का दुःख दूर कियो है।\nकेसरिया बागा सोहे तन पर। नाग देव बिराजे मन पर।\nनीला घोड़ा अति छबि पावे। भक्त खड़े सब जय गुण गावे।",
                lyricsEnglish = "Ganpati Sharad Dhyai Ke, Guru Ke Charan Manay...",
                isActive = true,
                sortOrder = 2,
                createdAt = now - 1000
            ),
            BhajanItem(
                id = "bhajan_3",
                title = "भजन - आयो रे बाग़ड़ का राजा जाहरवीर",
                titleEnglish = "Aayo Re Bagad Ka Raja Jaharveer",
                artist = "राम अवतार शर्मा",
                artistEnglish = "Ram Avtar Sharma",
                duration = "04:30",
                audioUrl = "",
                thumbnailUrl = "https://images.unsplash.com/photo-1609137241280-99c565c026b4?q=80&w=200",
                categoryTag = BhajanCategories.BHAJAN,
                lyricsText = "आयो रे आयो रे, बाग़ड़ का राजा आयो रे।\nनीले घोड़े चढ़ कर देखो, जाहरवीर आयो रे।\n\nधजा नारियल भेंट चढ़ावा, चरणों में हम शीश नवावा।\nमहिमा भारी गावे दुनिया, संकट सब मिटायो रे।\nआयो रे आयो रे...",
                lyricsEnglish = "Aayo Re Aayo Re Bagad Ka Raja Aayo Re...",
                isActive = true,
                sortOrder = 3,
                createdAt = now - 2000
            )
        )
    }
}
