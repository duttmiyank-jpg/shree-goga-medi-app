package com.example.miyank

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object DevotionalCategories {
    const val CHALISA = "CHALISA"
    const val STOTRAM = "STOTRAM"
    const val KATHA = "KATHA"
    const val STORY = "STORY"

    val list = listOf(CHALISA, STOTRAM, KATHA, STORY)

    fun getDisplayName(categoryKey: String, isHindi: Boolean = true): String {
        return when (categoryKey.uppercase()) {
            CHALISA -> if (isHindi) "श्री चालीसा" else "Chalisa"
            STOTRAM -> if (isHindi) "स्तोत्रम एवं मंत्र" else "Stotram"
            KATHA -> if (isHindi) "श्री कथा" else "Katha"
            STORY -> if (isHindi) "पौराणिक कथाएं" else "Devotional Stories"
            else -> categoryKey
        }
    }
}

@Serializable
data class DevotionalContentItem(
    val id: String = "",
    val title: String = "",
    val category: String = DevotionalCategories.CHALISA,
    val thumbnailUrl: String = "",
    val contentHindi: String = "",
    val contentEnglish: String = "",
    val author: String = "श्री जामनीवाला दरबार",
    val isFeatured: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

/**
 * Chalisa, Stotram & Katha Devotional Library Manager
 */
object DevotionalContentManager {

    private val settingsStorage = SettingsManager()
    private const val KEY_LIBRARY_JSON = "devotional_library_v1"
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private val _items = MutableStateFlow(loadItems())
    val items: StateFlow<List<DevotionalContentItem>> = _items.asStateFlow()

    private fun loadItems(): List<DevotionalContentItem> {
        val savedJson = settingsStorage.getString(KEY_LIBRARY_JSON, "")
        if (savedJson.isNotEmpty()) {
            try {
                return json.decodeFromString<List<DevotionalContentItem>>(savedJson).sortedByDescending { it.createdAt }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return getDefaultLibrary()
    }

    private fun saveItems(list: List<DevotionalContentItem>) {
        val sorted = list.sortedByDescending { it.createdAt }
        _items.value = sorted
        try {
            val encoded = json.encodeToString(sorted)
            settingsStorage.setString(KEY_LIBRARY_JSON, encoded)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getActiveItems(): List<DevotionalContentItem> {
        return _items.value.filter { it.isActive }
    }

    fun addOrUpdateItem(item: DevotionalContentItem) {
        val current = _items.value.toMutableList()
        val now = getEpochMillis()
        val index = current.indexOfFirst { it.id == item.id }

        if (index >= 0) {
            current[index] = item.copy(updatedAt = now)
        } else {
            val newItem = item.copy(
                id = if (item.id.isEmpty()) "devo_$now" else item.id,
                createdAt = if (item.createdAt <= 0L) now else item.createdAt,
                updatedAt = now
            )
            current.add(0, newItem)
        }
        saveItems(current)
    }

    fun deleteItem(id: String) {
        val updated = _items.value.filter { it.id != id }
        saveItems(updated)
    }

    private fun getDefaultLibrary(): List<DevotionalContentItem> {
        val now = getEpochMillis()
        return listOf(
            DevotionalContentItem(
                id = "devo_chalisa_1",
                title = "श्री गोगा जी चालीसा",
                category = DevotionalCategories.CHALISA,
                contentHindi = "॥ दोहा ॥\nजय जय जय जाहर पीर, बाबा गोगा वीर।\nसंकट मोचन कृपा करो, हरो भक्त की पीर॥\n\n॥ चौपाई ॥\nजय गोगा जी देव दयाला। संकट हरण करो गोपाला॥\nबाछल सूत वीर अवतारा। ददरेवा उपजे सुकुमारा॥\nगुरु गोरख का वरदान पाया। नागलोक तक यश फैलाया॥\nजो जन श्रद्धा से ध्यावै। ताके संकट दूर नशावै॥",
                contentEnglish = "Shree Goga Ji Chalisa\n\nJai Jai Jai Jahar Veer, Baba Goga Veer.\nSankat Mochan Kripa Karo, Haro Bhakt Ki Peer.",
                isFeatured = true,
                isActive = true,
                createdAt = now,
                updatedAt = now
            ),
            DevotionalContentItem(
                id = "devo_katha_1",
                title = "श्री गोगा जी महाराज की पावन जन्म कथा",
                category = DevotionalCategories.KATHA,
                contentHindi = "माता बाछल जी की कठोर तपस्या से प्रसन्न होकर गुरु गोरखनाथ जी ने उन्हें गूगल फल (गुग्गल) का वरदान दिया। इस दिव्य फल के प्रभाव से महान शूरवीर एवं सिद्ध पीर श्री गोगा जी महाराज का प्राकट्य हुआ।",
                contentEnglish = "Story of Lord Goga Ji Maharaj's Divine Birth through the blessings of Guru Gorakhnath Ji.",
                isFeatured = true,
                isActive = true,
                createdAt = now,
                updatedAt = now
            )
        )
    }
}
