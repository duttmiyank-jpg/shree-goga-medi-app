package com.example.miyank

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
enum class ParchaRecipientType {
    SPECIFIC_USER,
    ALL_USERS
}

@Serializable
data class ParchaReadState(
    val parchaId: String = "",
    val userId: String = "",
    val readAt: Long = 0L,
    val isRead: Boolean = true
)

@Serializable
data class ParchaItem(
    val id: String = "",
    val recipientType: ParchaRecipientType = ParchaRecipientType.SPECIFIC_USER,
    val recipientUserId: String? = null,
    val recipientPhoneNumber: String? = null,
    val recipientUserName: String? = null,
    val title: String = "", // Always Hindi
    val problem: String? = null, // Always Hindi
    val guidance: String = "", // Always Hindi (Required)
    val upay: String? = null, // Always Hindi
    val karamSeva: String? = null, // Always Hindi
    val additionalMessage: String? = null, // Always Hindi
    val imageUrl: String? = null,
    val attachmentUrl: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val sentByAdminId: String? = null,
    val isActive: Boolean = true
)

object ParchaManager {
    private val settingsStorage = SettingsManager()
    private const val KEY_PARCHA_LIST_JSON = "parcha_list_json_v2"
    private const val KEY_PARCHA_READ_STATES_JSON = "parcha_read_states_json_v2"
    private const val KEY_VERIFIED_MOBILE = "parcha_verified_mobile"
    private const val KEY_SESSION_VERIFIED = "parcha_session_verified"

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private val _parchas = MutableStateFlow(loadParchas())
    val parchas: StateFlow<List<ParchaItem>> = _parchas.asStateFlow()

    private val _readStates = MutableStateFlow(loadReadStates())
    val readStates: StateFlow<List<ParchaReadState>> = _readStates.asStateFlow()

    private val _isVerifiedSession = MutableStateFlow(loadSessionVerifiedState())
    val isVerifiedSession: StateFlow<Boolean> = _isVerifiedSession.asStateFlow()

    private val _verifiedMobileNumber = MutableStateFlow(loadVerifiedMobileNumber())
    val verifiedMobileNumber: StateFlow<String> = _verifiedMobileNumber.asStateFlow()

    private fun loadSessionVerifiedState(): Boolean {
        return settingsStorage.getString(KEY_SESSION_VERIFIED, "false") == "true"
    }

    private fun loadVerifiedMobileNumber(): String {
        return settingsStorage.getString(KEY_VERIFIED_MOBILE, "")
    }

    fun verifyOtp(mobileNumber: String, otpCode: String): Boolean {
        val cleanMobile = mobileNumber.trim().replace(" ", "").replace("-", "")
        if (cleanMobile.length >= 10 && otpCode.length == 6 && otpCode.all { it.isDigit() }) {
            _isVerifiedSession.value = true
            _verifiedMobileNumber.value = cleanMobile
            settingsStorage.setString(KEY_VERIFIED_MOBILE, cleanMobile)
            settingsStorage.setString(KEY_SESSION_VERIFIED, "true")
            return true
        }
        return false
    }

    fun clearOtpSession() {
        _isVerifiedSession.value = false
        _verifiedMobileNumber.value = ""
        settingsStorage.setString(KEY_VERIFIED_MOBILE, "")
        settingsStorage.setString(KEY_SESSION_VERIFIED, "false")
    }

    private fun loadParchas(): List<ParchaItem> {
        val savedJson = settingsStorage.getString(KEY_PARCHA_LIST_JSON, "")
        if (savedJson.isNotEmpty()) {
            try {
                return json.decodeFromString<List<ParchaItem>>(savedJson).sortedByDescending { it.createdAt }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return getDefaultParchas()
    }

    private fun loadReadStates(): List<ParchaReadState> {
        val savedJson = settingsStorage.getString(KEY_PARCHA_READ_STATES_JSON, "")
        if (savedJson.isNotEmpty()) {
            try {
                return json.decodeFromString<List<ParchaReadState>>(savedJson)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return emptyList()
    }

    private fun saveParchas(list: List<ParchaItem>) {
        val sorted = list.sortedByDescending { it.createdAt }
        _parchas.value = sorted
        try {
            val encoded = json.encodeToString(sorted)
            settingsStorage.setString(KEY_PARCHA_LIST_JSON, encoded)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveReadStates(list: List<ParchaReadState>) {
        _readStates.value = list
        try {
            val encoded = json.encodeToString(list)
            settingsStorage.setString(KEY_PARCHA_READ_STATES_JSON, encoded)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getParchasForUser(userMobileOrId: String): List<ParchaItem> {
        val cleanMobile = userMobileOrId.trim().replace(" ", "").replace("-", "")
        return _parchas.value.filter { parcha ->
            parcha.isActive && (
                parcha.recipientType == ParchaRecipientType.ALL_USERS ||
                (parcha.recipientUserId != null && parcha.recipientUserId.trim() == cleanMobile) ||
                (parcha.recipientPhoneNumber != null && parcha.recipientPhoneNumber.trim() == cleanMobile)
            )
        }
    }

    fun isParchaReadByUser(parchaId: String, userMobileOrId: String): Boolean {
        val cleanMobile = userMobileOrId.trim().replace(" ", "").replace("-", "")
        val state = _readStates.value.find { it.parchaId == parchaId && it.userId == cleanMobile }
        return state?.isRead ?: false
    }

    fun markAsReadByUser(parchaId: String, userMobileOrId: String) {
        val cleanMobile = userMobileOrId.trim().replace(" ", "").replace("-", "")
        val currentStates = _readStates.value.toMutableList()
        val index = currentStates.indexOfFirst { it.parchaId == parchaId && it.userId == cleanMobile }
        val now = getEpochMillis()
        if (index >= 0) {
            currentStates[index] = currentStates[index].copy(isRead = true, readAt = now)
        } else {
            currentStates.add(ParchaReadState(parchaId = parchaId, userId = cleanMobile, readAt = now, isRead = true))
        }
        saveReadStates(currentStates)
    }

    fun getUnreadCountForUser(userMobileOrId: String): Int {
        val userParchas = getParchasForUser(userMobileOrId)
        return userParchas.count { !isParchaReadByUser(it.id, userMobileOrId) }
    }

    fun addParcha(parcha: ParchaItem) {
        val now = getEpochMillis()
        val newItem = parcha.copy(
            id = if (parcha.id.isEmpty()) "parcha_$now" else parcha.id,
            createdAt = if (parcha.createdAt <= 0L) now else parcha.createdAt,
            updatedAt = now,
            isActive = true
        )
        val current = _parchas.value.toMutableList()
        current.add(0, newItem)
        saveParchas(current)
    }

    fun updateParcha(parcha: ParchaItem) {
        val now = getEpochMillis()
        val current = _parchas.value.toMutableList()
        val index = current.indexOfFirst { it.id == parcha.id }
        if (index >= 0) {
            current[index] = parcha.copy(updatedAt = now)
            saveParchas(current)
        }
    }

    fun toggleActive(parchaId: String) {
        val current = _parchas.value.toMutableList()
        val index = current.indexOfFirst { it.id == parchaId }
        if (index >= 0) {
            val item = current[index]
            current[index] = item.copy(isActive = !item.isActive, updatedAt = getEpochMillis())
            saveParchas(current)
        }
    }

    fun deleteParcha(parchaId: String) {
        val updatedParchas = _parchas.value.filter { it.id != parchaId }
        saveParchas(updatedParchas)
        val updatedStates = _readStates.value.filter { it.parchaId != parchaId }
        saveReadStates(updatedStates)
    }

    fun getParchaById(parchaId: String): ParchaItem? {
        return _parchas.value.find { it.id == parchaId }
    }

    private fun getDefaultParchas(): List<ParchaItem> {
        val now = getEpochMillis()
        return listOf(
            ParchaItem(
                id = "parcha_sample_1",
                recipientType = ParchaRecipientType.ALL_USERS,
                title = "सभी श्रद्धालुओं के लिए सामान्य दरबार मार्गदर्शन",
                problem = "पारिवारिक जीवन में सुख-शांति एवं समृद्धि हेतु",
                guidance = "प्रतिदिन घर पर संध्या आरती एवं बाबा जी का सिमरन करें। मन की पवित्रता बनाए रखें एवं गोगा जी महाराज पर पूर्ण विश्वास रखें।",
                upay = "प्रत्येक मंगलवार एवं रविवार को घर के मंदिर में शुद्ध देसी घी का दीपक प्रज्वलित करें।",
                karamSeva = "प्रतिदिन पक्षियों एवं बेजुबान जीवों को अन्न-जल अर्पित करें।",
                additionalMessage = "जय श्री जामनीवाला दरबार। सतगुरु बाबा सोम दत्त जी का आशीर्वाद सदा आप पर बना रहे।",
                createdAt = now - 1000 * 60 * 60 * 24 * 2,
                updatedAt = now - 1000 * 60 * 60 * 24 * 2,
                isActive = true
            )
        )
    }
}
