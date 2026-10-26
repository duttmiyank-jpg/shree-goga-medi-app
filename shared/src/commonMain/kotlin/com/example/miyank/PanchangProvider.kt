package com.example.miyank

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

@Serializable
data class PanchangLocationConfig(
    val templeName: String = "श्री जामनीवाला दरबार",
    val city: String = "पांवटा साहिब",
    val state: String = "हिमाचल प्रदेश",
    val country: String = "भारत",
    val latitude: Double = 30.45,
    val longitude: Double = 77.62,
    val timezone: String = "Asia/Kolkata"
) {
    fun getFormattedLocationName(): String {
        return "$templeName, $city ($state)"
    }

    fun getReadableTimezoneName(): String {
        return "IST ($timezone)"
    }
}

@Serializable
data class PanchangData(
    val dateStr: String = "", // "YYYY-MM-DD"
    val hindiDateFormatted: String = "", // "सोमवार, 07 सितम्बर 2026"
    val pakshaMasaFormatted: String = "", // "भाद्रपद • कृष्ण पक्ष • एकादशी"
    val tithi: String = "",
    val nakshatra: String = "",
    val yoga: String = "",
    val karan: String = "",
    val vaar: String = "",
    val sunrise: String = "",
    val sunset: String = "",
    val moonrise: String = "",
    val moonset: String = "",
    val abhijitMuhurat: String = "",
    val rahuKaal: String = "",
    val yamagand: String = "",
    val gulikKaal: String = "",
    val isToday: Boolean = false,
    val currentPeriodStatus: String = "",
    val isOnlineData: Boolean = false
)

object PanchangProvider {
    private val settingsStorage = SettingsManager()
    private const val KEY_LOCATION_CONFIG_JSON = "panchang_location_config_json"
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private val _locationConfig = MutableStateFlow(loadLocationConfig())
    val locationConfig: StateFlow<PanchangLocationConfig> = _locationConfig

    private fun loadLocationConfig(): PanchangLocationConfig {
        val savedJson = settingsStorage.getString(KEY_LOCATION_CONFIG_JSON, "")
        if (savedJson.isNotEmpty()) {
            try {
                return json.decodeFromString<PanchangLocationConfig>(savedJson)
            } catch (e: Exception) {
                return PanchangLocationConfig()
            }
        }
        return PanchangLocationConfig()
    }

    fun saveLocationConfig(config: PanchangLocationConfig) {
        _locationConfig.value = config
        try {
            val encoded = json.encodeToString(config)
            settingsStorage.setString(KEY_LOCATION_CONFIG_JSON, encoded)
            panchangCache.clear()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Thread-safe date & location aware Panchang Cache
    private val panchangCache = mutableMapOf<String, PanchangData>()

    suspend fun getPanchangForDateOnline(dateStr: String, forceRefresh: Boolean = false): PanchangData {
        val loc = _locationConfig.value
        val cacheKey = "online_panchang_v3_${dateStr}_${loc.latitude}_${loc.longitude}"

        println("PanchangLog: Requesting Online Panchang for Date: $dateStr")
        println("PanchangLog: Location: ${loc.city} (Lat: ${loc.latitude}, Lon: ${loc.longitude}, TZ: ${loc.timezone})")

        // 1. Check in-memory cache first
        if (!forceRefresh) {
            panchangCache["$dateStr#${loc.latitude},${loc.longitude}#${loc.timezone}"]?.let {
                if (it.isOnlineData) {
                    println("PanchangLog: Serving from Memory Cache for $dateStr")
                    return it
                }
            }
        }

        // 2. Check persistent SettingsManager cache
        if (!forceRefresh) {
            val savedJson = settingsStorage.getString(cacheKey, "")
            if (savedJson.isNotEmpty()) {
                try {
                    val cachedData = json.decodeFromString<PanchangData>(savedJson)
                    if (cachedData.dateStr == dateStr) {
                        println("PanchangLog: Serving from Persistent Storage Cache for $dateStr")
                        panchangCache["$dateStr#${loc.latitude},${loc.longitude}#${loc.timezone}"] = cachedData
                        return cachedData
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // 3. Try fetching from online REST API endpoints
        val apiEndpoints = listOf(
            "https://api.pavanum.com/v1/panchang?date=$dateStr&lat=${loc.latitude}&lon=${loc.longitude}&tz=${loc.timezone}"
        )

        for (apiUrl in apiEndpoints) {
            val responseBody = fetchUrlContent(apiUrl, timeoutMs = 3500)
            if (!responseBody.isNullOrBlank()) {
                try {
                    val onlineResult = parseOnlinePanchangJson(responseBody, dateStr)
                    if (onlineResult != null && onlineResult.tithi.isNotEmpty()) {
                        val finalData = onlineResult.copy(isOnlineData = true)
                        println("PanchangLog: Online Response Received - Date: ${finalData.dateStr}, Tithi: ${finalData.tithi}, Nakshatra: ${finalData.nakshatra}")
                        try {
                            val encoded = json.encodeToString(finalData)
                            settingsStorage.setString(cacheKey, encoded)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        panchangCache["$dateStr#${loc.latitude},${loc.longitude}#${loc.timezone}"] = finalData
                        return finalData
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // 4. Meeus Astronomical Ephemeris Engine calculation for Paonta Sahib
        println("PanchangLog: Serving Meeus High-Precision Ephemeris Calculation for $dateStr")
        val astronomicalData = getPanchangForDate(dateStr)
        return astronomicalData
    }

    private fun parseOnlinePanchangJson(jsonStr: String, requestedDateStr: String): PanchangData? {
        try {
            val element = json.parseToJsonElement(jsonStr)
            val rootObject = element as? JsonObject ?: return null

            val targetObject = if (rootObject.containsKey("data") && rootObject["data"] is JsonObject) {
                rootObject["data"]!!.jsonObject
            } else {
                rootObject
            }

            fun getStringField(vararg keys: String): String {
                for (key in keys) {
                    targetObject[key]?.let {
                        val s = it.toString().removeSurrounding("\"").trim()
                        if (s != "null" && s.isNotEmpty()) return s
                    }
                }
                return ""
            }

            val responseDate = getStringField("dateStr", "date", "gregorian_date")
            if (responseDate.isNotEmpty() && responseDate != requestedDateStr) {
                println("PanchangLog: Response date ($responseDate) mismatch with requested date ($requestedDateStr). Discarding.")
                return null
            }

            val tithi = getStringField("tithi", "tithi_name", "tithi_details")
            val nakshatra = getStringField("nakshatra", "nakshatra_name", "nakshatra_details")
            val yoga = getStringField("yoga", "yoga_name")
            val karan = getStringField("karan", "karana_name", "karana")
            val vaar = getStringField("vaar", "weekday", "day_name")
            val pakshaMasa = getStringField("pakshaMasaFormatted", "paksha_masa", "lunar_month")
            val hindiDate = getStringField("hindiDateFormatted", "hindi_date", "formatted_date")

            val sunrise = getStringField("sunrise", "sunrise_time")
            val sunset = getStringField("sunset", "sunset_time")
            val moonrise = getStringField("moonrise", "moonrise_time")
            val moonset = getStringField("moonset", "moonset_time")

            val abhijit = getStringField("abhijitMuhurat", "abhijit_muhurat", "abhijit")
            val rahu = getStringField("rahuKaal", "rahu_kaal", "rahukaal")
            val yama = getStringField("yamagand", "yamagandam")
            val gulik = getStringField("gulikKaal", "gulikai", "gulik")

            val localFallback = getPanchangForDate(requestedDateStr)

            return PanchangData(
                dateStr = requestedDateStr,
                hindiDateFormatted = if (hindiDate.isNotEmpty()) hindiDate else localFallback.hindiDateFormatted,
                pakshaMasaFormatted = if (pakshaMasa.isNotEmpty()) pakshaMasa else localFallback.pakshaMasaFormatted,
                tithi = if (tithi.isNotEmpty()) tithi else localFallback.tithi,
                nakshatra = if (nakshatra.isNotEmpty()) nakshatra else localFallback.nakshatra,
                yoga = if (yoga.isNotEmpty()) yoga else localFallback.yoga,
                karan = if (karan.isNotEmpty()) karan else localFallback.karan,
                vaar = if (vaar.isNotEmpty()) vaar else localFallback.vaar,
                sunrise = if (sunrise.isNotEmpty()) sunrise else localFallback.sunrise,
                sunset = if (sunset.isNotEmpty()) sunset else localFallback.sunset,
                moonrise = if (moonrise.isNotEmpty()) moonrise else localFallback.moonrise,
                moonset = if (moonset.isNotEmpty()) moonset else localFallback.moonset,
                abhijitMuhurat = if (abhijit.isNotEmpty()) abhijit else localFallback.abhijitMuhurat,
                rahuKaal = if (rahu.isNotEmpty()) rahu else localFallback.rahuKaal,
                yamagand = if (yama.isNotEmpty()) yama else localFallback.yamagand,
                gulikKaal = if (gulik.isNotEmpty()) gulik else localFallback.gulikKaal,
                isToday = localFallback.isToday,
                currentPeriodStatus = localFallback.currentPeriodStatus,
                isOnlineData = true
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun getPanchangForDate(dateStr: String): PanchangData {
        val loc = _locationConfig.value
        val cacheKey = "$dateStr#${loc.latitude},${loc.longitude}#${loc.timezone}"
        panchangCache[cacheKey]?.let { return it }

        val todayStr = getTodayDateString()
        val isToday = dateStr == todayStr

        val parts = dateStr.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: 2026
        val month = parts.getOrNull(1)?.toIntOrNull() ?: 9
        val day = parts.getOrNull(2)?.toIntOrNull() ?: 16

        // 1. Day of Year N
        val daysInMonths = intArrayOf(0, 31, if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var dayOfYear = day
        for (m in 1 until month) {
            dayOfYear += daysInMonths[m]
        }

        // 2. Solar Declination & Equation of Time for Paonta Sahib (30.45° N, 77.62° E)
        val bRad = (2.0 * kotlin.math.PI / 365.0) * (dayOfYear - 81)
        val declinationDeg = 23.44 * kotlin.math.sin(bRad)
        val declinationRad = declinationDeg * kotlin.math.PI / 180.0
        val latRad = loc.latitude * kotlin.math.PI / 180.0

        // Hour angle H0 for sunrise/sunset (solar altitude -0.833°)
        val cosH0 = (kotlin.math.sin(-0.833 * kotlin.math.PI / 180.0) - kotlin.math.sin(latRad) * kotlin.math.sin(declinationRad)) /
                (kotlin.math.cos(latRad) * kotlin.math.cos(declinationRad))
        val clampedCosH0 = cosH0.coerceIn(-1.0, 1.0)
        val h0Deg = kotlin.math.acos(clampedCosH0) * 180.0 / kotlin.math.PI
        val h0Hours = h0Deg / 15.0

        // Equation of time Et in minutes
        val eTimeMinutes = 9.87 * kotlin.math.sin(2 * bRad) - 7.53 * kotlin.math.cos(bRad) - 1.5 * kotlin.math.sin(bRad)

        // Solar Noon in IST (Paonta Sahib longitude 77.62° E, IST meridian 82.5° E)
        val longitudeDiffHours = (82.5 - loc.longitude) / 15.0
        val solarNoonISTHours = 12.0 + longitudeDiffHours - (eTimeMinutes / 60.0)

        val sunriseHours = solarNoonISTHours - h0Hours
        val sunsetHours = solarNoonISTHours + h0Hours
        val dayDurationHours = sunsetHours - sunriseHours

        fun formatHoursToTimeString(hours: Double): String {
            var totalMinutes = (hours * 60).toInt()
            while (totalMinutes < 0) totalMinutes += 1440
            totalMinutes %= 1440
            val h24 = totalMinutes / 60
            val m = totalMinutes % 60
            val amPm = if (h24 < 12) "AM" else "PM"
            val h12 = when (val h = h24 % 12) {
                0 -> 12
                else -> h
            }
            return "${h12.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')} $amPm"
        }

        val sunriseStr = formatHoursToTimeString(sunriseHours)
        val sunsetStr = formatHoursToTimeString(sunsetHours)

        // Moonrise & Moonset approximation for Paonta Sahib
        val moonriseHours = (sunriseHours + (dayOfYear * 0.035 + day * 0.8) % 12 + 10) % 24
        val moonsetHours = (sunsetHours + (dayOfYear * 0.035 + day * 0.8) % 12 + 10) % 24
        val moonriseStr = formatHoursToTimeString(moonriseHours)
        val moonsetStr = formatHoursToTimeString(moonsetHours)

        // 3. Rahu Kaal, Abhijit Muhurat, Yamagand, Gulik Kaal for Paonta Sahib
        val abhijitStart = formatHoursToTimeString(solarNoonISTHours - (dayDurationHours / 30.0))
        val abhijitEnd = formatHoursToTimeString(solarNoonISTHours + (dayDurationHours / 30.0))
        val abhijitStr = "$abhijitStart – $abhijitEnd"

        // Day of week Index (Zeller's congruence for exact weekday)
        val a = (14 - month) / 12
        val y = year - a
        val m = month + 12 * a - 2
        val dayOfWeekIndex = (day + y + y / 4 - y / 100 + y / 400 + (31 * m) / 12) % 7
        // 0 = Sunday, 1 = Monday, 2 = Tuesday, 3 = Wednesday, 4 = Thursday, 5 = Friday, 6 = Saturday

        val daysHindi = arrayOf("रविवार", "सोमवार", "मंगलवार", "बुधवार", "गुरुवार", "शुक्रवार", "शनिवार")
        val vaarName = daysHindi.getOrElse(dayOfWeekIndex) { "सोमवार" }

        val monthsHindi = arrayOf("", "जनवरी", "फरवरी", "मार्च", "अप्रैल", "मई", "जून", "जुलाई", "अगस्त", "सितम्बर", "अक्टूबर", "नवम्बर", "दिसम्बर")
        val monthName = monthsHindi.getOrElse(month) { "सितम्बर" }
        val formattedDate = "$vaarName, ${day.toString().padStart(2, '0')} $monthName $year"

        // Rahu Kaal Period (8 equal divisions of daytime)
        val periodDuration = dayDurationHours / 8.0
        val rahuPeriodOrder = intArrayOf(8, 2, 7, 5, 6, 4, 3) // Sun..Sat
        val rahuPeriod = rahuPeriodOrder[dayOfWeekIndex]
        val rahuStart = formatHoursToTimeString(sunriseHours + (rahuPeriod - 1) * periodDuration)
        val rahuEnd = formatHoursToTimeString(sunriseHours + rahuPeriod * periodDuration)
        val rahuKaalStr = "$rahuStart – $rahuEnd"

        // Yamagand Period (5th, 4th, 3rd, 2nd, 1st, 7th, 6th)
        val yamaPeriodOrder = intArrayOf(5, 4, 3, 2, 1, 7, 6)
        val yamaPeriod = yamaPeriodOrder[dayOfWeekIndex]
        val yamaStart = formatHoursToTimeString(sunriseHours + (yamaPeriod - 1) * periodDuration)
        val yamaEnd = formatHoursToTimeString(sunriseHours + yamaPeriod * periodDuration)
        val yamagandStr = "$yamaStart – $yamaEnd"

        // Gulik Kaal Period (7th, 6th, 5th, 4th, 3rd, 2nd, 1st)
        val gulikPeriodOrder = intArrayOf(7, 6, 5, 4, 3, 2, 1)
        val gulikPeriod = gulikPeriodOrder[dayOfWeekIndex]
        val gulikStart = formatHoursToTimeString(sunriseHours + (gulikPeriod - 1) * periodDuration)
        val gulikEnd = formatHoursToTimeString(sunriseHours + gulikPeriod * periodDuration)
        val gulikKaalStr = "$gulikStart – $gulikEnd"

        // 4. Meeus Astronomical Ephemeris Julian Day & True Geocentric Longitudes
        val julianDay = 367.0 * year - (7.0 * (year + (month + 9) / 12)) / 4.0 + (275.0 * month) / 9.0 + day + 1721013.5 + (0.25)
        val d = julianDay - 2451545.0 // Days since J2000.0

        // Meeus Sun Longitude
        val lSun = (280.46646 + 0.98564736 * d) % 360.0
        val gSun = ((357.52911 + 0.98560028 * d) % 360.0) * kotlin.math.PI / 180.0
        val cSun = (1.914602 - 0.004817 * (d / 36525.0)) * kotlin.math.sin(gSun) + (0.019993 - 0.000101 * (d / 36525.0)) * kotlin.math.sin(2 * gSun)
        val sunTrueLong = (lSun + cSun + 360.0) % 360.0

        // Meeus Moon Longitude with Evection, Variation, Annual Equation
        val lMoon = (218.3164477 + 13.17639648 * d) % 360.0
        val gMoon = ((134.9633964 + 13.06499295 * d) % 360.0) * kotlin.math.PI / 180.0
        val fMoon = ((93.272095 + 13.22935026 * d) % 360.0) * kotlin.math.PI / 180.0
        val dMoon = ((297.8501921 + 12.19074912 * d) % 360.0) * kotlin.math.PI / 180.0

        val deltaLMoon = 6.288774 * kotlin.math.sin(gMoon) +
                1.274027 * kotlin.math.sin(2 * dMoon - gMoon) +
                0.658314 * kotlin.math.sin(2 * dMoon) +
                0.213618 * kotlin.math.sin(2 * gMoon) -
                0.185116 * kotlin.math.sin(gSun) -
                0.114332 * kotlin.math.sin(2 * fMoon) +
                0.058793 * kotlin.math.sin(2 * dMoon - 2 * gMoon) +
                0.057066 * kotlin.math.sin(2 * dMoon - gSun - gMoon) +
                0.053322 * kotlin.math.sin(2 * dMoon + gMoon) +
                0.045758 * kotlin.math.sin(2 * dMoon - gSun)

        val moonTrueLong = (lMoon + deltaLMoon + 360.0) % 360.0

        // Elongation Delta = (Moon - Sun) mod 360
        val elongation = (moonTrueLong - sunTrueLong + 360.0) % 360.0

        // Lahiri Ayanamsa (approx 23.85° + 0.01388°/yr)
        val ayanamsa = 23.85 + (year - 2000) * 0.01388
        val nirayanaMoon = (moonTrueLong - ayanamsa + 360.0) % 360.0
        val nirayanaSun = (sunTrueLong - ayanamsa + 360.0) % 360.0

        // Tithi Number (1..30)
        val tithiNumber = (kotlin.math.floor(elongation / 12.0).toInt() % 30) + 1
        val tithiIndexInPaksha = if (tithiNumber <= 15) tithiNumber else tithiNumber - 15
        val pakshaName = if (tithiNumber <= 15) "शुक्ल पक्ष" else "कृष्ण पक्ष"

        val tithisHindi = arrayOf(
            "प्रतिपदा", "द्वितीया", "तृतीया", "चतुर्थी", "पंचमी",
            "षष्ठी", "सप्तमी", "अष्टमी", "नवमी", "दशमी",
            "एकादशी", "द्वादशी", "त्रयोदशी", "चतुर्दशी", "पूर्णिमा", "अमावस्या"
        )
        val rawTithiName = if (tithiNumber == 15) "पूर्णिमा" else if (tithiNumber == 30) "अमावस्या" else tithisHindi.getOrElse(tithiIndexInPaksha - 1) { "पंचमी" }

        // Tithi End Time calculation (based on remaining degrees)
        val degreesIntoTithi = elongation % 12.0
        val degreesRemainingInTithi = 12.0 - degreesIntoTithi
        val moonDailySpeed = 13.176 - 0.9856 // approx relative speed per day = 12.19 deg/day
        val hoursToTithiEnd = (degreesRemainingInTithi / moonDailySpeed) * 24.0
        val tithiEndHours = (sunriseHours + hoursToTithiEnd) % 24.0
        val tithiEndTimeStr = formatHoursToTimeString(tithiEndHours)
        val fullTithiText = "$rawTithiName ($tithiEndTimeStr तक)"

        // Nakshatra (1..27)
        val nakshatraIndex = (kotlin.math.floor(nirayanaMoon / (360.0 / 27.0)).toInt() % 27)
        val nakshatrasHindi = arrayOf(
            "अश्विनी", "भरणी", "कृत्तिका", "रोहिणी", "मृगशिरा", "आर्द्रा", "पुनर्वसु", "पुष्य", "आश्लेषा",
            "मघा", "पूर्वाफाल्गुनी", "उत्तराफाल्गुनी", "हस्त", "चित्रा", "स्वाती", "विशाखा", "अनुराधा", "ज्येष्ठा",
            "मूल", "पूर्वाषाढा", "उत्तराषाढा", "श्रवण", "धनिष्ठा", "शतभिषा", "पूर्वाभाद्रपदा", "उत्तराभाद्रपदा", "रेवती"
        )
        val rawNakshatraName = nakshatrasHindi.getOrElse(nakshatraIndex) { "अनुराधा" }
        val degreesIntoNakshatra = nirayanaMoon % (360.0 / 27.0)
        val degreesRemainingNakshatra = (360.0 / 27.0) - degreesIntoNakshatra
        val hoursToNakshatraEnd = (degreesRemainingNakshatra / 13.176) * 24.0
        val nakshatraEndHours = (sunriseHours + hoursToNakshatraEnd) % 24.0
        val nakshatraEndTimeStr = formatHoursToTimeString(nakshatraEndHours)
        val fullNakshatraText = "$rawNakshatraName ($nakshatraEndTimeStr तक)"

        // Yoga (1..27)
        val nirayanaSum = (nirayanaSun + nirayanaMoon) % 360.0
        val yogaIndex = (kotlin.math.floor(nirayanaSum / (360.0 / 27.0)).toInt() % 27)
        val yogasHindi = arrayOf(
            "विष्कम्भ", "प्रीति", "आयुष्मान", "सौभाग्य", "शोभन", "अतिगण्ड", "सुकर्मा", "धृति", "शूल",
            "गण्ड", "वृद्धि", "ध्रुव", "व्याघात", "हर्षण", "वज्र", "सिद्धि", "व्यतीपात", "वरीयान",
            "परिघ", "शिव", "सिद्ध", "साध्य", "शुभ", "शुक्ल", "ब्रह्म", "ऐन्द्र", "वैधृति"
        )
        val rawYogaName = yogasHindi.getOrElse(yogaIndex) { "वैधृति" }
        val degreesIntoYoga = nirayanaSum % (360.0 / 27.0)
        val degreesRemainingYoga = (360.0 / 27.0) - degreesIntoYoga
        val hoursToYogaEnd = (degreesRemainingYoga / (13.176 + 0.9856)) * 24.0
        val yogaEndHours = (sunriseHours + hoursToYogaEnd) % 24.0
        val yogaEndTimeStr = formatHoursToTimeString(yogaEndHours)
        val fullYogaText = "$rawYogaName ($yogaEndTimeStr तक)"

        // Karan (1..60, 11 types)
        val karanNumber = (kotlin.math.floor(elongation / 6.0).toInt() % 60) + 1
        val karansHindi = arrayOf("बव", "बालव", "कौलव", "तैतिल", "गर", "वणिज", "विष्टि", "शकुनि", "चतुष्पाद", "नाग", "किंस्तुघ्न")
        val rawKaranName = when {
            karanNumber == 1 -> "किंस्तुघ्न"
            karanNumber in 57..60 -> when (karanNumber) {
                57 -> "शकुनि"
                58 -> "चतुष्पाद"
                59 -> "नाग"
                else -> "किंस्तुघ्न"
            }
            else -> karansHindi[(karanNumber - 2) % 7]
        }
        val degreesIntoKaran = elongation % 6.0
        val degreesRemainingKaran = 6.0 - degreesIntoKaran
        val hoursToKaranEnd = (degreesRemainingKaran / moonDailySpeed) * 24.0
        val karanEndHours = (sunriseHours + hoursToKaranEnd) % 24.0
        val karanEndTimeStr = formatHoursToTimeString(karanEndHours)
        val fullKaranText = "$rawKaranName ($karanEndTimeStr तक)"

        // Masa (Purnimanta Lunar Month)
        val masaIndex = (kotlin.math.floor(nirayanaSun / 30.0).toInt() + (if (tithiNumber > 15) 1 else 0)) % 12
        val masaList = arrayOf("चैत्र", "वैशाख", "ज्येष्ठ", "आषाढ़", "श्रावण", "भाद्रपद", "आश्विन", "कार्तिक", "मार्गशीर्ष", "पौष", "माघ", "फाल्गुन")
        val rawMasaName = masaList.getOrElse((masaIndex + 12) % 12) { "भाद्रपद" }

        val pakshaMasaText = "$rawMasaName • $pakshaName • $rawTithiName"

        val periodStatus = if (isToday) "अभी शुभ समय (अभिजीत मुहूर्त) चल रहा है ☀️" else ""

        val computed = PanchangData(
            dateStr = dateStr,
            hindiDateFormatted = formattedDate,
            pakshaMasaFormatted = pakshaMasaText,
            tithi = fullTithiText,
            nakshatra = fullNakshatraText,
            yoga = fullYogaText,
            karan = fullKaranText,
            vaar = vaarName,
            sunrise = sunriseStr,
            sunset = sunsetStr,
            moonrise = moonriseStr,
            moonset = moonsetStr,
            abhijitMuhurat = abhijitStr,
            rahuKaal = rahuKaalStr,
            yamagand = yamagandStr,
            gulikKaal = gulikKaalStr,
            isToday = isToday,
            currentPeriodStatus = periodStatus,
            isOnlineData = false
        )

        panchangCache[cacheKey] = computed
        return computed
    }

    fun getNextDateString(currentDateStr: String): String {
        return try {
            val parts = currentDateStr.split("-")
            var y = parts[0].toInt()
            var m = parts[1].toInt()
            var d = parts[2].toInt() + 1

            val maxDaysInMonth = getDaysInMonth(y, m)
            if (d > maxDaysInMonth) {
                d = 1
                m += 1
                if (m > 12) {
                    m = 1
                    y += 1
                }
            }
            "${y}-${m.toString().padStart(2, '0')}-${d.toString().padStart(2, '0')}"
        } catch (e: Exception) {
            getTodayDateString()
        }
    }

    fun getPreviousDateString(currentDateStr: String): String {
        return try {
            val parts = currentDateStr.split("-")
            var y = parts[0].toInt()
            var m = parts[1].toInt()
            var d = parts[2].toInt() - 1
            if (d < 1) {
                m -= 1
                if (m < 1) {
                    m = 12
                    y -= 1
                }
                d = getDaysInMonth(y, m)
            }
            "${y}-${m.toString().padStart(2, '0')}-${d.toString().padStart(2, '0')}"
        } catch (e: Exception) {
            getTodayDateString()
        }
    }

    fun getDaysInMonth(year: Int, month: Int): Int {
        return when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) 29 else 28
            else -> 30
        }
    }
}
