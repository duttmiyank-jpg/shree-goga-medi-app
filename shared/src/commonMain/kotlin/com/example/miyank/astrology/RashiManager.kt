package com.example.miyank.astrology

import com.example.miyank.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object RashiManager {

    private val settingsStorage = SettingsManager()
    private const val KEY_RASHI_BIRTH_DETAILS_JSON = "rashi_birth_details_json_v1"
    private const val KEY_RASHI_RESULT_JSON = "rashi_result_json_v1"

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private val _birthDetails = MutableStateFlow(loadBirthDetails())
    val birthDetails: StateFlow<BirthDetails> = _birthDetails

    private val _calculationResult = MutableStateFlow(loadCalculationResult())
    val calculationResult: StateFlow<RashiCalculationResult?> = _calculationResult

    private fun loadBirthDetails(): BirthDetails {
        val savedJson = settingsStorage.getString(KEY_RASHI_BIRTH_DETAILS_JSON, "")
        if (savedJson.isNotEmpty()) {
            try {
                return json.decodeFromString<BirthDetails>(savedJson)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return BirthDetails()
    }

    private fun loadCalculationResult(): RashiCalculationResult? {
        val savedJson = settingsStorage.getString(KEY_RASHI_RESULT_JSON, "")
        if (savedJson.isNotEmpty()) {
            try {
                return json.decodeFromString<RashiCalculationResult>(savedJson)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    fun saveBirthDetailsAndCalculate(details: BirthDetails): RashiCalculationResult {
        _birthDetails.value = details
        try {
            val encodedDetails = json.encodeToString(details)
            settingsStorage.setString(KEY_RASHI_BIRTH_DETAILS_JSON, encodedDetails)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val result = MeeusRashiCalculator.calculateChandraRashi(details)
        _calculationResult.value = result

        try {
            val encodedResult = json.encodeToString(result)
            settingsStorage.setString(KEY_RASHI_RESULT_JSON, encodedResult)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return result
    }

    fun clearRashiData() {
        _birthDetails.value = BirthDetails()
        _calculationResult.value = null
        settingsStorage.setString(KEY_RASHI_BIRTH_DETAILS_JSON, "")
        settingsStorage.setString(KEY_RASHI_RESULT_JSON, "")
    }

    // Pre-configured major Indian and international city presets
    val presetCities = listOf(
        CityLocation("पांवटा साहिब", "हिमाचल प्रदेश", "भारत", 30.45, 77.62, "Asia/Kolkata", 5.5),
        CityLocation("नई दिल्ली", "दिल्ली", "भारत", 28.6139, 77.2090, "Asia/Kolkata", 5.5),
        CityLocation("मुंबई", "महाराष्ट्र", "भारत", 19.0760, 72.8777, "Asia/Kolkata", 5.5),
        CityLocation("वाराणसी", "उत्तर प्रदेश", "भारत", 25.3176, 82.9739, "Asia/Kolkata", 5.5),
        CityLocation("जयपुर", "राजस्थान", "भारत", 26.9124, 75.7873, "Asia/Kolkata", 5.5),
        CityLocation("कोलकाता", "पश्चिम बंगाल", "भारत", 22.5726, 88.3639, "Asia/Kolkata", 5.5),
        CityLocation("बेंगलुरु", "कर्नाटक", "भारत", 12.9716, 77.5946, "Asia/Kolkata", 5.5),
        CityLocation("अहमदाबाद", "गुजरात", "भारत", 23.0225, 72.5714, "Asia/Kolkata", 5.5),
        CityLocation("उज्जैन", "मध्य प्रदेश", "भारत", 23.1765, 75.7885, "Asia/Kolkata", 5.5),
        CityLocation("हरिद्वार", "उत्तराखंड", "भारत", 29.9457, 78.1642, "Asia/Kolkata", 5.5),
        CityLocation("अयोध्या", "उत्तर प्रदेश", "भारत", 26.7922, 82.1998, "Asia/Kolkata", 5.5),
        CityLocation("पटना", "बिहार", "भारत", 25.5941, 85.1376, "Asia/Kolkata", 5.5),
        CityLocation("चंडीगढ़", "पंजाब/हरियाणा", "भारत", 30.7333, 76.7794, "Asia/Kolkata", 5.5),
        CityLocation("लंदन", "इंग्लैंड", "यूके", 51.5074, -0.1278, "Europe/London", 0.0),
        CityLocation("न्यू यॉर्क", "न्यूयॉर्क", "अमेरिका", 40.7128, -74.0060, "America/New_York", -5.0)
    )
}
