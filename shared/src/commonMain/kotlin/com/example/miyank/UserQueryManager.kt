package com.example.miyank

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
enum class QuerySenderType {
    USER,
    ADMIN
}

@Serializable
enum class QueryStatus {
    NEW,
    IN_PROGRESS,
    REPLIED,
    CLOSED
}

object QueryCategories {
    const val GENERAL_QUERY = "GENERAL_QUERY"
    const val SEVA_DAAN = "SEVA_DAAN"
    const val BHANDARA = "BHANDARA"
    const val POOJA_ANUSHTHAN = "POOJA_ANUSHTHAN"

    val list = listOf(
        GENERAL_QUERY to "सामान्य प्रश्न",
        SEVA_DAAN to "सेवा एवं दान",
        BHANDARA to "भंडारा",
        POOJA_ANUSHTHAN to "पूजा एवं अनुष्ठान"
    )

    fun getDisplayName(key: String, isHindi: Boolean = true): String {
        return when (key) {
            GENERAL_QUERY -> if (isHindi) "सामान्य प्रश्न" else "General Query"
            SEVA_DAAN -> if (isHindi) "सेवा एवं दान" else "Seva & Donation"
            BHANDARA -> if (isHindi) "भंडारा" else "Bhandara"
            POOJA_ANUSHTHAN -> if (isHindi) "पूजा एवं अनुष्ठान" else "Pooja & Anushthan"
            else -> key
        }
    }
}

@Serializable
data class UserReply(
    val id: String = "",
    val queryId: String = "",
    val senderType: QuerySenderType = QuerySenderType.ADMIN,
    val senderName: String = "श्री जामनीवाला दरबार",
    val message: String = "",
    val createdAt: Long = 0L,
    val isRead: Boolean = false
)

@Serializable
data class UserQuery(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val phone: String = "",
    val categoryKey: String = QueryCategories.GENERAL_QUERY,
    val category: String = "सामान्य प्रश्न",
    val message: String = "",
    val date: String = "",
    val createdAt: Long = 0L,
    val status: QueryStatus = QueryStatus.NEW,
    val isResolved: Boolean = false,
    val replies: List<UserReply> = emptyList(),
    val isReadByAdmin: Boolean = false,
    val isReadByUser: Boolean = true,
    val lastReplyAt: Long = 0L
)

@Serializable
data class ContactUsSettings(
    val helplinePhone: String = "+91 98765 43210",
    val whatsappPhone: String = "919876543210",
    val email: String = "info@jamniwaladarbar.org",
    val address: String = "पांवटा साहिब, जिला सिरमौर, हिमाचल प्रदेश - 173025",
    val updatedAt: Long = 0L
)

object UserQueryManager {
    private val settingsStorage = SettingsManager()
    private const val KEY_USER_QUERIES_JSON = "user_queries_list_json"
    private const val KEY_CONTACT_SETTINGS_JSON = "contact_us_settings_json"
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private val _queries = MutableStateFlow(loadQueries())
    val queries: StateFlow<List<UserQuery>> = _queries.asStateFlow()

    private val _contactSettings = MutableStateFlow(loadContactSettings())
    val contactSettings: StateFlow<ContactUsSettings> = _contactSettings.asStateFlow()

    private fun loadQueries(): List<UserQuery> {
        val savedJson = settingsStorage.getString(KEY_USER_QUERIES_JSON, "")
        if (savedJson.isNotEmpty()) {
            try {
                return json.decodeFromString<List<UserQuery>>(savedJson).sortedByDescending { it.createdAt }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return getDefaultQueries()
    }

    private fun loadContactSettings(): ContactUsSettings {
        val savedJson = settingsStorage.getString(KEY_CONTACT_SETTINGS_JSON, "")
        if (savedJson.isNotEmpty()) {
            try {
                return json.decodeFromString<ContactUsSettings>(savedJson)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return ContactUsSettings()
    }

    fun updateContactSettings(newSettings: ContactUsSettings) {
        _contactSettings.value = newSettings
        try {
            val encoded = json.encodeToString(newSettings)
            settingsStorage.setString(KEY_CONTACT_SETTINGS_JSON, encoded)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveQueries(list: List<UserQuery>) {
        val sorted = list.sortedByDescending { it.createdAt }
        _queries.value = sorted
        try {
            val encoded = json.encodeToString(sorted)
            settingsStorage.setString(KEY_USER_QUERIES_JSON, encoded)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addQuery(query: UserQuery) {
        val current = _queries.value.toMutableList()
        val now = getEpochMillis()
        val newItem = query.copy(
            id = if (query.id.isEmpty()) "query_$now" else query.id,
            createdAt = if (query.createdAt <= 0L) now else query.createdAt,
            date = if (query.date.isEmpty()) formatDisplayDate(getTodayDateString()) else query.date,
            status = QueryStatus.NEW,
            isReadByAdmin = false,
            isReadByUser = true
        )
        current.add(0, newItem)
        saveQueries(current)
    }

    fun addReply(queryId: String, replyMessage: String, senderType: QuerySenderType = QuerySenderType.ADMIN) {
        val now = getEpochMillis()
        val currentList = _queries.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == queryId }
        if (index >= 0) {
            val query = currentList[index]
            val newReply = UserReply(
                id = "reply_$now",
                queryId = queryId,
                senderType = senderType,
                senderName = if (senderType == QuerySenderType.ADMIN) "श्री जामनीवाला दरबार" else query.name,
                message = replyMessage.trim(),
                createdAt = now,
                isRead = false
            )
            val updatedReplies = query.replies + newReply
            val updatedQuery = query.copy(
                replies = updatedReplies,
                status = if (senderType == QuerySenderType.ADMIN) QueryStatus.REPLIED else QueryStatus.IN_PROGRESS,
                isResolved = if (senderType == QuerySenderType.ADMIN) false else query.isResolved,
                isReadByAdmin = senderType == QuerySenderType.ADMIN,
                isReadByUser = senderType != QuerySenderType.ADMIN,
                lastReplyAt = now
            )
            currentList[index] = updatedQuery
            saveQueries(currentList)
        }
    }

    fun markReadByAdmin(queryId: String) {
        val updated = _queries.value.map {
            if (it.id == queryId) it.copy(isReadByAdmin = true) else it
        }
        saveQueries(updated)
    }

    fun markReadByUser(queryId: String) {
        val updated = _queries.value.map {
            if (it.id == queryId) it.copy(isReadByUser = true) else it
        }
        saveQueries(updated)
    }

    fun markResolved(queryId: String) {
        val updated = _queries.value.map {
            if (it.id == queryId) it.copy(isResolved = true, status = QueryStatus.CLOSED) else it
        }
        saveQueries(updated)
    }

    fun deleteQuery(queryId: String) {
        val updated = _queries.value.filter { it.id != queryId }
        saveQueries(updated)
    }

    fun getUnreadCountForAdmin(): Int {
        return _queries.value.count { !it.isReadByAdmin || it.status == QueryStatus.NEW }
    }

    private fun getDefaultQueries(): List<UserQuery> {
        val now = getEpochMillis()
        return listOf(
            UserQuery(
                id = "query_1",
                name = "राहुल शर्मा",
                phone = "+91 9876543210",
                categoryKey = QueryCategories.BHANDARA,
                category = "भंडारा",
                message = "क्या इस रविवार मंदिर में विशेष भंडारा का आयोजन है?",
                date = "17 Aug, 10:20 AM",
                createdAt = now - 1000 * 60 * 60 * 24,
                status = QueryStatus.REPLIED,
                isResolved = false,
                isReadByAdmin = true,
                isReadByUser = false,
                replies = listOf(
                    UserReply(
                        id = "reply_1",
                        queryId = "query_1",
                        senderType = QuerySenderType.ADMIN,
                        senderName = "श्री जामनीवाला दरबार",
                        message = "जय श्री गोगा जी महाराज 🙏 जी हाँ, इस रविवार दोपहर 12 बजे से विशेष भंडारा प्रसाद वितरण होगा।",
                        createdAt = now - 1000 * 60 * 60 * 20
                    )
                )
            ),
            UserQuery(
                id = "query_2",
                name = "प्रिया गुप्ता",
                phone = "+91 9123456789",
                categoryKey = QueryCategories.SEVA_DAAN,
                category = "सेवा एवं दान",
                message = "क्या हम मंदिर में वस्त्र एवं खाद्यान्न दान कर सकते हैं?",
                date = "16 Aug, 04:45 PM",
                createdAt = now - 1000 * 60 * 60 * 48,
                status = QueryStatus.CLOSED,
                isResolved = true,
                isReadByAdmin = true,
                isReadByUser = true
            )
        )
    }
}
