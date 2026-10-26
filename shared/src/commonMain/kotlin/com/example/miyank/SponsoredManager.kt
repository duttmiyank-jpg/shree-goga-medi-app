package com.example.miyank

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Sponsored Business Promotion Model
 */
@Serializable
data class SponsoredItem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "Local Business",
    val imageUrl: String = "",
    val address: String = "",
    val phone: String = "",
    val whatsapp: String = "",
    val websiteUrl: String = "",
    val offerText: String = "",
    val isActive: Boolean = true,
    val priority: Int = 0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

/**
 * Sponsored Promotions Manager
 */
object SponsoredManager {

    private val settingsStorage = SettingsManager()
    private const val KEY_SPONSORED_LIST_JSON = "sponsored_items_v1"
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private val _sponsoredItems = MutableStateFlow(loadItems())
    val sponsoredItems: StateFlow<List<SponsoredItem>> = _sponsoredItems.asStateFlow()

    private fun loadItems(): List<SponsoredItem> {
        val savedJson = settingsStorage.getString(KEY_SPONSORED_LIST_JSON, "")
        if (savedJson.isNotEmpty()) {
            try {
                return json.decodeFromString<List<SponsoredItem>>(savedJson).sortedBy { it.priority }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return emptyList()
    }

    private fun saveItems(list: List<SponsoredItem>) {
        val sorted = list.sortedBy { it.priority }
        _sponsoredItems.value = sorted
        try {
            val encoded = json.encodeToString(sorted)
            settingsStorage.setString(KEY_SPONSORED_LIST_JSON, encoded)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getActivePromotions(): List<SponsoredItem> {
        return _sponsoredItems.value.filter { it.isActive }
    }

    fun addOrUpdatePromotion(item: SponsoredItem) {
        val current = _sponsoredItems.value.toMutableList()
        val now = getEpochMillis()
        val index = current.indexOfFirst { it.id == item.id }

        if (index >= 0) {
            current[index] = item.copy(updatedAt = now)
        } else {
            val newItem = item.copy(
                id = if (item.id.isEmpty()) "sponsor_$now" else item.id,
                createdAt = if (item.createdAt <= 0L) now else item.createdAt,
                updatedAt = now
            )
            current.add(0, newItem)
        }
        saveItems(current)
    }

    fun toggleActive(id: String) {
        val updated = _sponsoredItems.value.map {
            if (it.id == id) it.copy(isActive = !it.isActive, updatedAt = getEpochMillis()) else it
        }
        saveItems(updated)
    }

    fun deletePromotion(id: String) {
        val updated = _sponsoredItems.value.filter { it.id != id }
        saveItems(updated)
    }
}
