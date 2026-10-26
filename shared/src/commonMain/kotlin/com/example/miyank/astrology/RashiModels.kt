package com.example.miyank.astrology

import kotlinx.serialization.Serializable

/**
 * 12 Sidereal / Nirayana Rashis of Vedic Astrology
 */
enum class Rashi(
    val index: Int,
    val hindiName: String,
    val englishName: String,
    val symbol: String,
    val rulingPlanetHindi: String,
    val rulingPlanetEnglish: String,
    val elementHindi: String,
    val minDegree: Double,
    val maxDegree: Double
) {
    MESHA(0, "मेष", "Mesha", "♈", "मंगल", "Mars", "अग्नि (Fire)", 0.0, 30.0),
    VRISHABHA(1, "वृषभ", "Vrishabha", "♉", "शुक्र", "Venus", "पृथ्वी (Earth)", 30.0, 60.0),
    MITHUNA(2, "मिथुन", "Mithuna", "♊", "बुध", "Mercury", "वायु (Air)", 60.0, 90.0),
    KARKA(3, "कर्क", "Karka", "♋", "चंद्र", "Moon", "जल (Water)", 90.0, 120.0),
    SIMHA(4, "सिंह", "Simha", "♌", "सूर्य", "Sun", "अग्नि (Fire)", 120.0, 150.0),
    KANYA(5, "कन्या", "Kanya", "♍", "बुध", "Mercury", "पृथ्वी (Earth)", 150.0, 180.0),
    TULA(6, "तुला", "Tula", "♎", "शुक्र", "Venus", "वायु (Air)", 180.0, 210.0),
    VRISHCHIKA(7, "वृश्चिक", "Vrishchika", "♏", "मंगल", "Mars", "जल (Water)", 210.0, 240.0),
    DHANU(8, "धनु", "Dhanu", "♐", "गुरु", "Jupiter", "अग्नि (Fire)", 240.0, 270.0),
    MAKARA(9, "मकर", "Makara", "♑", "शनि", "Saturn", "पृथ्वी (Earth)", 270.0, 300.0),
    KUMBHA(10, "कुंभ", "Kumbha", "♒", "शनि", "Saturn", "वायु (Air)", 300.0, 330.0),
    MEENA(11, "मीन", "Meena", "♓", "गुरु", "Jupiter", "जल (Water)", 330.0, 360.0);

    companion object {
        fun fromDegree(degree: Double): Rashi {
            val normalized = (degree % 360.0 + 360.0) % 360.0
            return values().firstOrNull { normalized >= it.minDegree && normalized < it.maxDegree }
                ?: MESHA
        }

        fun fromIndex(index: Int): Rashi {
            val safeIdx = (index % 12 + 12) % 12
            return values().firstOrNull { it.index == safeIdx } ?: MESHA
        }
    }
}

/**
 * Status of calculation accuracy and time confidence
 */
@Serializable
enum class CalculationConfidenceStatus {
    EXACT,
    TIME_UNKNOWN_SAME_RASHI,
    TIME_UNKNOWN_BOUNDARY_CROSSED,
    INVALID_LOCATION
}

/**
 * User birth details
 */
@Serializable
data class BirthDetails(
    val userName: String = "",
    val birthDateStr: String = "", // "YYYY-MM-DD"
    val birthTimeStr: String = "", // "HH:MM AM/PM" or "12:00 PM"
    val isTimeUnknown: Boolean = false,
    val cityName: String = "पांवटा साहिब",
    val stateName: String = "हिमाचल प्रदेश",
    val countryName: String = "भारत",
    val latitude: Double = 30.45,
    val longitude: Double = 77.62,
    val timezone: String = "Asia/Kolkata",
    val tzOffsetHours: Double = 5.5
) {
    fun getFormattedLocation(): String {
        return if (cityName.isNotEmpty()) "$cityName, $stateName ($countryName)" else "पांवटा साहिब"
    }

    fun getFormattedBirthDate(): String {
        if (birthDateStr.isBlank()) return ""
        val parts = birthDateStr.split("-")
        if (parts.size == 3) {
            return "${parts[2]}/${parts[1]}/${parts[0]}"
        }
        return birthDateStr
    }
}

/**
 * City preset model for birth place selection
 */
@Serializable
data class CityLocation(
    val cityName: String,
    val stateName: String,
    val countryName: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val tzOffsetHours: Double
)

/**
 * Result of Rashi calculation
 */
@Serializable
data class RashiCalculationResult(
    val rashiIndex: Int = 0,
    val moonLongitude: Double = 0.0,
    val ayanamsha: Double = 0.0,
    val calculationSystem: String = "वैदिक / निरयन (लाहिरी अयनांश)",
    val status: CalculationConfidenceStatus = CalculationConfidenceStatus.EXACT,
    val birthDetails: BirthDetails = BirthDetails(),
    val calculatedAtMillis: Long = 0L
) {
    val rashi: Rashi
        get() = Rashi.fromIndex(rashiIndex)
}

/**
 * Structured daily/traditional guidance for a Rashi
 */
data class RashiGuidance(
    val rashi: Rashi,
    val rulingPlanet: String,
    val preferredColorsHindi: List<String>,
    val preferredColorsEnglish: List<String>,
    val avoidColorsHindi: List<String>,
    val avoidColorsEnglish: List<String>,
    val donationSuggestionsHindi: List<String>,
    val donationSuggestionsEnglish: List<String>,
    val goodActionsHindi: List<String>,
    val goodActionsEnglish: List<String>,
    val avoidActionsHindi: List<String>,
    val avoidActionsEnglish: List<String>,
    val devotionalPracticeHindi: String,
    val devotionalPracticeEnglish: String,
    val mantraDevanagari: String,
    val mantraTransliteration: String,
    val sevaSuggestionsHindi: List<String>,
    val sevaSuggestionsEnglish: List<String>,
    val disclaimerHindi: String = "परंपरागत ज्योतिषीय मान्यता के अनुसार। ये विचार श्रद्धा और भक्तिपरक मार्गदर्शन के लिए हैं।",
    val disclaimerEnglish: String = "According to traditional astrological beliefs. For devotional guidance."
)
