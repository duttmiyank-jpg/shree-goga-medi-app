package com.example.miyank

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
enum class SuggestionType {
    APP_SUGGESTION,
    DARBAR_SUGGESTION,
    COMPLAINT;

    fun getDisplayName(isHindi: Boolean = true): String {
        return when (this) {
            APP_SUGGESTION -> if (isHindi) "ऐप सुझाव (App)" else "App Suggestion"
            DARBAR_SUGGESTION -> if (isHindi) "दरबार सुझाव (Darbar)" else "Darbar Suggestion"
            COMPLAINT -> if (isHindi) "शिकायत (Complaint)" else "Complaint"
        }
    }
}

@Serializable
enum class SuggestionStatus {
    SUBMITTED,
    UNDER_REVIEW,
    IN_PROGRESS,
    RESOLVED,
    CLOSED;

    fun getDisplayName(isHindi: Boolean = true): String {
        return when (this) {
            SUBMITTED -> if (isHindi) "दर्ज की गई" else "Submitted"
            UNDER_REVIEW -> if (isHindi) "समीक्षाधीन" else "Under Review"
            IN_PROGRESS -> if (isHindi) "प्रगति पर" else "In Progress"
            RESOLVED -> if (isHindi) "हल कर दी गई" else "Resolved"
            CLOSED -> if (isHindi) "समाप्त" else "Closed"
        }
    }
}

@Serializable
data class SuggestionItem(
    val id: String = "",
    val userId: String = "",
    val userName: String = "श्रद्धालु",
    val userMobile: String = "",
    val type: SuggestionType = SuggestionType.APP_SUGGESTION,
    val category: String = "General",
    val subject: String = "",
    val description: String = "",
    val status: SuggestionStatus = SuggestionStatus.SUBMITTED,
    val adminResponse: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

/**
 * Suggestions & Complaints Manager
 */
object SuggestionManager {

    private val settingsStorage = SettingsManager()
    private const val KEY_SUGGESTIONS_JSON = "user_suggestions_v1"
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private val _suggestions = MutableStateFlow(loadSuggestions())
    val suggestions: StateFlow<List<SuggestionItem>> = _suggestions.asStateFlow()

    private fun loadSuggestions(): List<SuggestionItem> {
        val savedJson = settingsStorage.getString(KEY_SUGGESTIONS_JSON, "")
        if (savedJson.isNotEmpty()) {
            try {
                return json.decodeFromString<List<SuggestionItem>>(savedJson).sortedByDescending { it.createdAt }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return getDefaultSuggestions()
    }

    private fun saveSuggestions(list: List<SuggestionItem>) {
        val sorted = list.sortedByDescending { it.createdAt }
        _suggestions.value = sorted
        try {
            val encoded = json.encodeToString(sorted)
            settingsStorage.setString(KEY_SUGGESTIONS_JSON, encoded)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun submitSuggestion(
        userId: String,
        userName: String,
        userMobile: String,
        type: SuggestionType,
        category: String,
        subject: String,
        description: String
    ) {
        val now = getEpochMillis()
        val newItem = SuggestionItem(
            id = "sug_$now",
            userId = userId.ifEmpty { "guest_user" },
            userName = userName.ifEmpty { "श्रद्धालु" },
            userMobile = userMobile,
            type = type,
            category = category.ifEmpty { "General" },
            subject = subject.trim(),
            description = description.trim(),
            status = SuggestionStatus.SUBMITTED,
            createdAt = now,
            updatedAt = now
        )
        val current = _suggestions.value.toMutableList()
        current.add(0, newItem)
        saveSuggestions(current)
    }

    fun updateStatus(id: String, newStatus: SuggestionStatus, response: String = "") {
        val now = getEpochMillis()
        val current = _suggestions.value.map {
            if (it.id == id) {
                it.copy(
                    status = newStatus,
                    adminResponse = if (response.isNotBlank()) response.trim() else it.adminResponse,
                    updatedAt = now
                )
            } else {
                it
            }
        }
        saveSuggestions(current)
    }

    fun deleteSuggestion(id: String) {
        val updated = _suggestions.value.filter { it.id != id }
        saveSuggestions(updated)
    }

    private fun getDefaultSuggestions(): List<SuggestionItem> {
        val now = getEpochMillis()
        return listOf(
            SuggestionItem(
                id = "sug_sample_1",
                userId = "sample_user",
                userName = "राहुल वर्मा",
                userMobile = "+91 9876543210",
                type = SuggestionType.APP_SUGGESTION,
                category = "UI & Features",
                subject = "डार्क मोड एवं भक्ति संगीत",
                description = "ऐप में डार्क मोड और निरंतर भजन संगीत का अनुभव बहुत सुंदर है।",
                status = SuggestionStatus.RESOLVED,
                adminResponse = "धन्यवाद राहुल जी! जय श्री गोगा जी महाराज।",
                createdAt = now - 1000 * 60 * 60 * 24,
                updatedAt = now - 1000 * 60 * 60 * 24
            )
        )
    }
}
