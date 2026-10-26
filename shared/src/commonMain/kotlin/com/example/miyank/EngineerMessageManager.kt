package com.example.miyank

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Lead App Engineer Profile Model
 */
@Serializable
data class EngineerProfile(
    val name: String = "मियांका दत्त (Miyank Dutt)",
    val roleTitle: String = "चीफ सॉफ्टवेयर आर्किटेक्ट एवं डेवलपर",
    val photoUrl: String = "",
    val bio: String = "श्री जामनीवाला दरबार मोबाइल एप्लीकेशन के मुख्य डेवलपर। केएमपी (Kotlin Multiplatform) एवं जेटपैक कंपोज तकनीक द्वारा निर्मित।",
    val appPurpose: String = "इस दिव्य मोबाइल एप्प का मुख्य उद्देश्य विश्व भर के श्रद्धालुओं को श्री गोगा जी महाराज धाम से डिजिटल रूप से जोड़ना एवं मंदिर प्रबंधन को आधुनिक, पारदर्शी व सुलभ बनाना है।",
    val phone: String = "+91 98765 43210",
    val whatsapp: String = "+91 98765 43210",
    val email: String = "support@jamniwaladarbar.org",
    val githubUrl: String = "https://github.com",
    val linkedInUrl: String = "https://linkedin.com"
)

/**
 * Engineer Profile & Message Manager
 */
object EngineerMessageManager {

    private val settingsStorage = SettingsManager()
    private const val KEY_ENGINEER_PROFILE_JSON = "engineer_profile_v1"
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private val _engineerProfile = MutableStateFlow(loadProfile())
    val engineerProfile: StateFlow<EngineerProfile> = _engineerProfile.asStateFlow()

    private fun loadProfile(): EngineerProfile {
        val savedJson = settingsStorage.getString(KEY_ENGINEER_PROFILE_JSON, "")
        if (savedJson.isNotEmpty()) {
            try {
                return json.decodeFromString<EngineerProfile>(savedJson)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return EngineerProfile()
    }

    fun updateProfile(profile: EngineerProfile) {
        _engineerProfile.value = profile
        try {
            val encoded = json.encodeToString(profile)
            settingsStorage.setString(KEY_ENGINEER_PROFILE_JSON, encoded)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
