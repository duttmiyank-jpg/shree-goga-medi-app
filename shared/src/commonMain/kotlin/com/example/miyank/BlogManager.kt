package com.example.miyank

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class BlogItem(
    val id: String = "",
    val title: String = "",
    val titleEnglish: String = "",
    val category: String = "HISTORY",
    val shortDescription: String = "",
    val shortDescriptionEnglish: String = "",
    val content: String = "",
    val contentEnglish: String = "",
    val coverImageUrl: String = "",
    val contentImageUrl: String = "",
    val author: String = "श्री जामनीवाला दरबार",
    val publishedDate: String = "", // "YYYY-MM-DD"
    val updatedDate: String = "",
    val isPublished: Boolean = true,
    val isFeatured: Boolean = false,
    val sortOrder: Int = 0
)

object BlogCategories {
    const val ALL = "ALL"
    const val HISTORY = "HISTORY"
    const val SEVADARS = "SEVADARS"
    const val ACTIVITIES = "ACTIVITIES"
    const val OPERATIONS = "OPERATIONS"
    const val EVENTS = "EVENTS"
    const val RELIGIOUS = "RELIGIOUS"
    const val OTHER = "OTHER"

    val list = listOf(
        ALL,
        HISTORY,
        SEVADARS,
        ACTIVITIES,
        OPERATIONS,
        EVENTS,
        RELIGIOUS,
        OTHER
    )

    fun normalizeCategoryKey(rawCategory: String): String {
        val trimmed = rawCategory.trim()
        if (trimmed.isEmpty()) return HISTORY

        val upper = trimmed.uppercase()
        return when {
            upper == ALL || upper == "ALL" -> ALL
            upper == HISTORY || upper.contains("HISTORY") || upper.contains("इतिहास") -> HISTORY
            upper == SEVADARS || upper.contains("SEVADAR") || upper.contains("DARBARI") || upper.contains("दरबारी") || upper.contains("सेवादार") -> SEVADARS
            upper == ACTIVITIES || upper.contains("ACTIVIT") || upper.contains("कार्य") || upper.contains("WORK") -> ACTIVITIES
            upper == OPERATIONS || upper.contains("OPERAT") || upper.contains("संचालन") || upper.contains("DAILY") -> OPERATIONS
            upper == EVENTS || upper.contains("EVENT") || upper.contains("YATRA") || upper.contains("आयोजन") || upper.contains("यात्रा") -> EVENTS
            upper == RELIGIOUS || upper.contains("RELIGIO") || upper.contains("धार्मिक") || upper.contains("PUJA") || upper.contains("POOJA") -> RELIGIOUS
            else -> OTHER
        }
    }

    fun matchesCategory(blogCategory: String, targetCategoryKey: String): Boolean {
        if (targetCategoryKey == ALL) return true
        val normalizedBlog = normalizeCategoryKey(blogCategory)
        val normalizedTarget = normalizeCategoryKey(targetCategoryKey)
        return normalizedBlog == normalizedTarget
    }

    fun getDisplayName(categoryKey: String, isHindi: Boolean): String {
        val norm = normalizeCategoryKey(categoryKey)
        return when (norm) {
            ALL -> if (isHindi) "सभी" else "All"
            HISTORY -> if (isHindi) "दरबार का इतिहास" else "Darbar History"
            SEVADARS -> if (isHindi) "दरबारी एवं सेवादार" else "Darbaris & Sevadars"
            ACTIVITIES -> if (isHindi) "दरबार के कार्य-काज" else "Temple Activities"
            OPERATIONS -> if (isHindi) "दैनिक संचालन" else "Daily Operations"
            EVENTS -> if (isHindi) "आयोजन एवं यात्राएँ" else "Events & Yatras"
            RELIGIOUS -> if (isHindi) "धार्मिक जानकारी" else "Religious Information"
            else -> if (isHindi) "अन्य" else "Other"
        }
    }
}

object BlogManager {
    private val settingsStorage = SettingsManager()
    private const val KEY_BLOG_LIST = "about_darbar_list_json"

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private val _blogs = MutableStateFlow<List<BlogItem>>(emptyList())
    val blogs: StateFlow<List<BlogItem>> = _blogs

    init {
        loadBlogs()
    }

    private fun loadBlogs() {
        val savedJson = settingsStorage.getString(KEY_BLOG_LIST, "")
        if (savedJson.isNotEmpty()) {
            try {
                val list = json.decodeFromString<List<BlogItem>>(savedJson)
                _blogs.value = list.sortedByDescending { it.publishedDate }
            } catch (e: Exception) {
                val defaults = getDefaultInitialBlogs()
                _blogs.value = defaults
                saveBlogs(defaults)
            }
        } else {
            val defaults = getDefaultInitialBlogs()
            _blogs.value = defaults
            saveBlogs(defaults)
        }
    }

    private fun saveBlogs(list: List<BlogItem>) {
        val sorted = list.sortedByDescending { it.publishedDate }
        _blogs.value = sorted
        try {
            val encoded = json.encodeToString(sorted)
            settingsStorage.setString(KEY_BLOG_LIST, encoded)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getDefaultInitialBlogs(): List<BlogItem> {
        val today = getTodayDateString()
        return listOf(
            BlogItem(
                id = "darbar_history_1",
                title = "श्री जामनीवाला दरबार का पावन इतिहास एवं स्थापना",
                titleEnglish = "Holy History and Establishment of Shree Jamniwala Darbar",
                category = BlogCategories.HISTORY,
                shortDescription = "जानिए श्री जामनीवाला दरबार मंदिर की स्थापना, इतिहास और बाबा जी के पावन आशीर्वाद की कथा...",
                shortDescriptionEnglish = "Discover the sacred history, establishment and divine blessings of Shree Jamniwala Darbar...",
                content = """
                    श्री जामनीवाला दरबार मंदिर एक अत्यंत प्राचीन एवं पवित्र धार्मिक स्थल है। यह स्थान बाबा गोगा जी महाराज (जाहरवीर) और सत् गुरु बाबा सोम दत्त जी महाराज के चरणों में समर्पित है।
                    
                    सदियों से यह पावन दरबार श्रद्धालुओं की अटूट श्रद्धा और भक्ति का केंद्र रहा है। ऐसी मान्यता है कि जो भी भक्त यहाँ सच्चे मन से आकर शीश नवाता है, बाबा जी उसकी हर मनोकामना पूर्ण करते हैं।
                    
                    दरबार में आयोजित होने वाली आरती, जागरण और चौकियों में दूर-दूर से भक्त पहुँचकर बाबा जी का आशीर्वाद प्राप्त करते हैं।
                """.trimIndent(),
                contentEnglish = """
                    Shree Jamniwala Darbar is an ancient and holy pilgrimage center dedicated to Baba Goga Ji Maharaj (Jaharveer) and Sat Guru Baba Som Dutt Ji Maharaj.
                    
                    For generations, this sacred Darbar has been a sanctuary of divine faith and spiritual peace. Devotees believe that whoever prays here with pure devotion receives Baba Ji's divine blessings and fulfillment of their desires.
                    
                    Thousands of devotees gather for special Aartis, Jagrans, and Choukis held regularly at the Darbar.
                """.trimIndent(),
                coverImageUrl = "file:///android_asset/temple_pooja.jpg",
                contentImageUrl = "https://images.unsplash.com/photo-1544986581-efac024faf62?q=80&w=1000",
                author = "श्री जामनीवाला दरबार",
                publishedDate = today,
                updatedDate = today,
                isPublished = true,
                isFeatured = true,
                sortOrder = 1
            ),
            BlogItem(
                id = "darbaris_sevadars_1",
                title = "दरबार के मुख्य दरबारी एवं सेवादार",
                titleEnglish = "Prominent Sevadars and Devotees of Darbar",
                category = BlogCategories.SEVADARS,
                shortDescription = "सत् गुरु बाबा सोम दत्त जी महाराज और दरबार के समर्पित सेवादारों का परिचय एवं योगदान...",
                shortDescriptionEnglish = "Introduction and selfless service of Sat Guru Baba Som Dutt Ji Maharaj and dedicated Sevadars...",
                content = """
                    श्री सोम दत्त जी महाराज ने अपना संपूर्ण जीवन बाबा जी के चरणों में समर्पित कर दिया। उनकी निष्काम सेवा और पावन संदेशों से प्रेरित होकर हज़ारों श्रद्धालु भक्ति मार्ग पर अग्रसर हुए।
                    
                    दरबार के सेवादार प्रतिदिन मंदिर परिसर की स्वच्छता, लंगर सेवा, भक्तजनों के सुगम दर्शन और आयोजनों की व्यवस्था में पूर्ण निष्ठा के साथ योगदान देते हैं।
                """.trimIndent(),
                contentEnglish = """
                    Sat Guru Baba Som Dutt Ji Maharaj dedicated his entire life at the holy feet of Baba Ji. Inspired by his selfless service and divine teachings, thousands of devotees walk the path of faith and devotion.
                    
                    The dedicated Sevadars of the Darbar serve tirelessly each day in maintaining cleanliness, conducting Langar service, facilitating smooth Darshan, and managing major religious events.
                """.trimEnglish(),
                coverImageUrl = "https://images.unsplash.com/photo-1544986581-efac024faf62?q=80&w=1000",
                contentImageUrl = "",
                author = "श्री जामनीवाला दरबार",
                publishedDate = today,
                updatedDate = today,
                isPublished = true,
                isFeatured = false,
                sortOrder = 2
            ),
            BlogItem(
                id = "temple_activities_1",
                title = "दरबार के सामाजिक एवं धार्मिक कार्य-काज",
                titleEnglish = "Social and Religious Activities of Darbar",
                category = BlogCategories.ACTIVITIES,
                shortDescription = "दरबार द्वारा चलाए जा रहे समाज कल्याण, अन्नदान और धार्मिक अनुष्ठानों का विवरण...",
                shortDescriptionEnglish = "Details of social welfare, free Langar, cloth distribution and spiritual rituals conducted by the Darbar...",
                content = """
                    श्री जामनीवाला दरबार केवल पूजा-अर्चना का केंद्र ही नहीं, बल्कि मानव सेवा का पावन धाम है। यहाँ नियमित रूप से लंगर सेवा, वस्त्र वितरण और चिकित्सा शिविर आयोजित किए जाते हैं।
                    
                    विशेष पर्वों एवं मेलों के अवसर पर श्रद्धालुओं के लिए निःशुल्क आवास और भोजन की समुचित व्यवस्था की जाती है।
                """.trimIndent(),
                contentEnglish = """
                    Shree Jamniwala Darbar is not only a place of worship but also a center for human service. Regular free Langar, cloth distribution, and medical camps are organized for the needy.
                    
                    During grand festivals and fairs, free lodging and dining facilities are arranged for visiting pilgrims from across the country.
                """.trimEnglish(),
                coverImageUrl = "https://images.unsplash.com/photo-1621244249243-436b79b5eea8?q=80&w=1000",
                contentImageUrl = "",
                author = "श्री जामनीवाला दरबार",
                publishedDate = today,
                updatedDate = today,
                isPublished = true,
                isFeatured = false,
                sortOrder = 3
            )
        )
    }

    fun getPublishedBlogs(): List<BlogItem> {
        return _blogs.value.filter { it.isPublished }
    }

    fun getFeaturedBlog(): BlogItem? {
        return _blogs.value.firstOrNull { it.isPublished && it.isFeatured } ?: _blogs.value.firstOrNull { it.isPublished }
    }

    fun getBlogById(id: String): BlogItem? {
        return _blogs.value.firstOrNull { it.id == id }
    }

    fun addOrUpdateBlog(item: BlogItem): Boolean {
        val currentList = _blogs.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.id == item.id }
        val now = getTodayDateString()

        if (existingIndex >= 0) {
            currentList[existingIndex] = item.copy(updatedDate = now)
        } else {
            val newId = if (item.id.isEmpty()) "blog_${getEpochMillis()}" else item.id
            currentList.add(
                item.copy(
                    id = newId,
                    publishedDate = if (item.publishedDate.isEmpty()) now else item.publishedDate,
                    updatedDate = now
                )
            )
        }

        saveBlogs(currentList)
        return true
    }

    fun deleteBlog(id: String) {
        val currentList = _blogs.value.filter { it.id != id }
        saveBlogs(currentList)
    }

    fun togglePublishStatus(id: String) {
        val currentList = _blogs.value.map {
            if (it.id == id) it.copy(isPublished = !it.isPublished) else it
        }
        saveBlogs(currentList)
    }

    fun toggleFeaturedStatus(id: String) {
        val currentList = _blogs.value.map {
            if (it.id == id) it.copy(isFeatured = !it.isFeatured) else it
        }
        saveBlogs(currentList)
    }
}

private fun String.trimEnglish(): String = this.trimIndent()
