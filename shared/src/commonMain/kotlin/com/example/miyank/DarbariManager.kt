package com.example.miyank

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object DarbariCategories {
    const val ALL = "ALL"
    const val PUJARI = "PUJARI"
    const val SEVADAR = "SEVADAR"
    const val DARBARIS = "DARBARIS"

    val filterList = listOf(ALL, PUJARI, SEVADAR, DARBARIS)
    val adminList = listOf(PUJARI, SEVADAR, DARBARIS)

    fun getDisplayName(categoryKey: String, isHindi: Boolean = true): String {
        return when (categoryKey.uppercase()) {
            ALL -> if (isHindi) "सभी" else "All"
            PUJARI -> if (isHindi) "पुजारी" else "Priest (Pujari)"
            SEVADAR -> if (isHindi) "सेवादार" else "Sevadar"
            DARBARIS -> if (isHindi) "दरबारी" else "Darbari"
            else -> categoryKey
        }
    }
}

@Serializable
data class DarbariItem(
    val id: String = "",
    val name: String = "",
    val profileImage: String = "",
    val category: String = DarbariCategories.DARBARIS,
    val mobileNumber: String = "",
    val description: String = "",
    val isActive: Boolean = true,
    val sortOrder: Int = 0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

@Serializable
data class DarbariMessage(
    val id: String = "",
    val darbariId: String = "",
    val darbariName: String = "",
    val userName: String = "श्रद्धालु",
    val userMobile: String = "",
    val senderType: QuerySenderType = QuerySenderType.USER,
    val message: String = "",
    val createdAt: Long = 0L,
    val isReadByAdmin: Boolean = false,
    val isReadByUser: Boolean = true
)

object DarbariManager {
    private val settingsStorage = SettingsManager()
    private const val KEY_DARBARIS_LIST_JSON = "darbaris_list_json"
    private const val KEY_DARBARI_MESSAGES_JSON = "darbari_messages_list_json"
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private val _darbaris = MutableStateFlow(loadDarbaris())
    val darbaris: StateFlow<List<DarbariItem>> = _darbaris.asStateFlow()

    private val _messages = MutableStateFlow(loadMessages())
    val messages: StateFlow<List<DarbariMessage>> = _messages.asStateFlow()

    private fun loadDarbaris(): List<DarbariItem> {
        val savedJson = settingsStorage.getString(KEY_DARBARIS_LIST_JSON, "")
        if (savedJson.isNotEmpty()) {
            try {
                return json.decodeFromString<List<DarbariItem>>(savedJson).sortedBy { it.sortOrder }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return getDefaultDarbaris()
    }

    private fun loadMessages(): List<DarbariMessage> {
        val savedJson = settingsStorage.getString(KEY_DARBARI_MESSAGES_JSON, "")
        if (savedJson.isNotEmpty()) {
            try {
                return json.decodeFromString<List<DarbariMessage>>(savedJson).sortedByDescending { it.createdAt }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return getDefaultMessages()
    }

    fun saveDarbaris(list: List<DarbariItem>) {
        val sorted = list.sortedBy { it.sortOrder }
        _darbaris.value = sorted
        try {
            val encoded = json.encodeToString(sorted)
            settingsStorage.setString(KEY_DARBARIS_LIST_JSON, encoded)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveMessages(list: List<DarbariMessage>) {
        val sorted = list.sortedByDescending { it.createdAt }
        _messages.value = sorted
        try {
            val encoded = json.encodeToString(sorted)
            settingsStorage.setString(KEY_DARBARI_MESSAGES_JSON, encoded)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addOrUpdateDarbari(item: DarbariItem) {
        val current = _darbaris.value.toMutableList()
        val now = getEpochMillis()
        val index = current.indexOfFirst { it.id == item.id }

        if (index >= 0) {
            current[index] = item.copy(updatedAt = now)
        } else {
            val newItem = item.copy(
                id = if (item.id.isEmpty()) "darbari_$now" else item.id,
                createdAt = if (item.createdAt <= 0L) now else item.createdAt,
                updatedAt = now,
                sortOrder = if (item.sortOrder <= 0) current.size + 1 else item.sortOrder
            )
            current.add(0, newItem)
        }
        saveDarbaris(current)
    }

    fun toggleActive(id: String) {
        val updated = _darbaris.value.map {
            if (it.id == id) it.copy(isActive = !it.isActive, updatedAt = getEpochMillis()) else it
        }
        saveDarbaris(updated)
    }

    fun deleteDarbari(id: String) {
        val updated = _darbaris.value.filter { it.id != id }
        saveDarbaris(updated)
    }

    fun sendMessage(darbariId: String, darbariName: String, userName: String, userMobile: String, messageText: String, senderType: QuerySenderType = QuerySenderType.USER) {
        val now = getEpochMillis()
        val current = _messages.value.toMutableList()
        val msg = DarbariMessage(
            id = "msg_$now",
            darbariId = darbariId,
            darbariName = darbariName,
            userName = userName.ifEmpty { "श्रद्धालु" },
            userMobile = userMobile,
            senderType = senderType,
            message = messageText.trim(),
            createdAt = now,
            isReadByAdmin = senderType == QuerySenderType.ADMIN,
            isReadByUser = senderType == QuerySenderType.USER
        )
        current.add(0, msg)
        saveMessages(current)
    }

    fun markReadByAdmin(darbariId: String) {
        val updated = _messages.value.map {
            if (it.darbariId == darbariId) it.copy(isReadByAdmin = true) else it
        }
        saveMessages(updated)
    }

    fun getUnreadCountForAdmin(): Int {
        return _messages.value.count { !it.isReadByAdmin }
    }

    private fun getDefaultDarbaris(): List<DarbariItem> {
        val now = getEpochMillis()
        return listOf(
            DarbariItem(
                id = "darbari_1",
                name = "पंडित सोम दत्त शर्मा",
                category = DarbariCategories.PUJARI,
                mobileNumber = "+91 98765 43210",
                description = "मुख्य पुजारी — श्री जामनीवाला दरबार मंदिर",
                isActive = true,
                sortOrder = 1,
                createdAt = now,
                updatedAt = now
            ),
            DarbariItem(
                id = "darbari_2",
                name = "सत्पाल भगत जी",
                category = DarbariCategories.SEVADAR,
                mobileNumber = "+91 98123 45678",
                description = "प्रधान सेवादार — आरती एवं भंडारा सेवा",
                isActive = true,
                sortOrder = 2,
                createdAt = now,
                updatedAt = now
            ),
            DarbariItem(
                id = "darbari_3",
                name = "राकेश कुमार",
                category = DarbariCategories.DARBARIS,
                mobileNumber = "+91 91234 56789",
                description = "वरिष्ठ दरबारी — श्री गोगा जी महाराज धाम",
                isActive = true,
                sortOrder = 3,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    private fun getDefaultMessages(): List<DarbariMessage> {
        val now = getEpochMillis()
        return listOf(
            DarbariMessage(
                id = "msg_1",
                darbariId = "darbari_1",
                darbariName = "पंडित सोम दत्त शर्मा",
                userName = "राहुल वर्मा",
                userMobile = "+91 9876543210",
                senderType = QuerySenderType.USER,
                message = "जय श्री गोगा जी महाराज! पंडित जी, विशेष पूजा की जानकारी चाहिए।",
                createdAt = now - 1000 * 60 * 60 * 5,
                isReadByAdmin = false,
                isReadByUser = true
            )
        )
    }
}
