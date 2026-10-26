package com.example.miyank.astrology

import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.sin

interface RashiCalculator {
    fun calculateChandraRashi(birthDetails: BirthDetails): RashiCalculationResult
}

object MeeusRashiCalculator : RashiCalculator {

    override fun calculateChandraRashi(birthDetails: BirthDetails): RashiCalculationResult {
        if (birthDetails.latitude !in -90.0..90.0 || birthDetails.longitude !in -180.0..180.0) {
            return RashiCalculationResult(
                status = CalculationConfidenceStatus.INVALID_LOCATION,
                birthDetails = birthDetails
            )
        }

        val parts = birthDetails.birthDateStr.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: 2000
        val month = parts.getOrNull(1)?.toIntOrNull() ?: 1
        val day = parts.getOrNull(2)?.toIntOrNull() ?: 1

        if (birthDetails.isTimeUnknown) {
            // Check across 24 hours (00:00, 06:00, 12:00, 18:00, 23:59)
            val sampleHours = listOf(0.0, 6.0, 12.0, 18.0, 23.98)
            val sampleResults = sampleHours.map { hour ->
                calculateNirayanaMoonLongitude(year, month, day, hour, birthDetails.tzOffsetHours)
            }
            val firstRashiIndex = (floor(sampleResults[0].first / 30.0).toInt() % 12 + 12) % 12
            val isSameAllDay = sampleResults.all {
                (floor(it.first / 30.0).toInt() % 12 + 12) % 12 == firstRashiIndex
            }

            val noonResult = calculateNirayanaMoonLongitude(year, month, day, 12.0, birthDetails.tzOffsetHours)
            val status = if (isSameAllDay) {
                CalculationConfidenceStatus.TIME_UNKNOWN_SAME_RASHI
            } else {
                CalculationConfidenceStatus.TIME_UNKNOWN_BOUNDARY_CROSSED
            }

            return RashiCalculationResult(
                rashiIndex = firstRashiIndex,
                moonLongitude = noonResult.first,
                ayanamsha = noonResult.second,
                status = status,
                birthDetails = birthDetails
            )
        }

        // Exact time provided
        val timeHours = parseTimeToDecimalHours(birthDetails.birthTimeStr)
        val (nirayanaMoonLong, ayanamsa) = calculateNirayanaMoonLongitude(
            year, month, day, timeHours, birthDetails.tzOffsetHours
        )

        val rashiIdx = (floor(nirayanaMoonLong / 30.0).toInt() % 12 + 12) % 12

        return RashiCalculationResult(
            rashiIndex = rashiIdx,
            moonLongitude = nirayanaMoonLong,
            ayanamsha = ayanamsa,
            status = CalculationConfidenceStatus.EXACT,
            birthDetails = birthDetails
        )
    }

    /**
     * Parses time string "HH:MM AM/PM" or "HH:MM" into decimal hours (0.0 to 24.0)
     */
    fun parseTimeToDecimalHours(timeStr: String): Double {
        val trimmed = timeStr.trim().uppercase()
        val isPm = trimmed.contains("PM")
        val isAm = trimmed.contains("AM")
        val cleanTime = trimmed.replace("AM", "").replace("PM", "").trim()
        val parts = cleanTime.split(":")
        var hours = parts.getOrNull(0)?.toIntOrNull() ?: 12
        val minutes = parts.getOrNull(1)?.toIntOrNull() ?: 0

        if (isPm && hours < 12) hours += 12
        if (isAm && hours == 12) hours = 0

        return hours.toDouble() + (minutes.toDouble() / 60.0)
    }

    /**
     * Calculates Nirayana Sidereal Moon Longitude using Meeus Ephemeris algorithm & Lahiri Ayanamsha
     */
    fun calculateNirayanaMoonLongitude(
        year: Int,
        month: Int,
        day: Int,
        localHourDecimal: Double,
        tzOffsetHours: Double
    ): Pair<Double, Double> {
        // 1. UTC Decimal Time
        val utcHourDecimal = localHourDecimal - tzOffsetHours
        val dayFraction = utcHourDecimal / 24.0

        // 2. Julian Day Number (Meeus formula)
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = y / 100
        val b = 2 - a + (a / 4)
        val julianDay = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + dayFraction + b - 1524.5
        val d = julianDay - 2451545.0 // Days since J2000.0 epoch

        // 3. Meeus Lunar Orbital Elements
        val lMoon = (218.3164477 + 13.17639648 * d) % 360.0
        val gMoon = ((134.9633964 + 13.06499295 * d) % 360.0) * PI / 180.0
        val fMoon = ((93.272095 + 13.22935026 * d) % 360.0) * PI / 180.0
        val dMoon = ((297.8501921 + 12.19074912 * d) % 360.0) * PI / 180.0
        val gSun = ((357.52911 + 0.98560028 * d) % 360.0) * PI / 180.0

        // 4. Main Periodic Terms for Moon's Geocentric Longitude
        val deltaLMoon = 6.288774 * sin(gMoon) +
                1.274027 * sin(2 * dMoon - gMoon) +
                0.658314 * sin(2 * dMoon) +
                0.213618 * sin(2 * gMoon) -
                0.185116 * sin(gSun) -
                0.114332 * sin(2 * fMoon) +
                0.058793 * sin(2 * dMoon - 2 * gMoon) +
                0.057066 * sin(2 * dMoon - gSun - gMoon) +
                0.053322 * sin(2 * dMoon + gMoon) +
                0.045758 * sin(2 * dMoon - gSun)

        val tropicalMoonLong = (lMoon + deltaLMoon + 360.0) % 360.0

        // 5. Lahiri / Chitra Paksha Ayanamsha (Meeus / Astronomical formulation)
        // Ayanamsha for J2000 = 23.85626° + precessional rate (~1.396° per century)
        val tCentury = d / 36525.0
        val ayanamsha = 23.85626 + 1.396 * tCentury

        // 6. Nirayana Sidereal Moon Longitude = (Tropical Moon - Ayanamsha + 360) mod 360
        val nirayanaMoonLong = (tropicalMoonLong - ayanamsha + 360.0) % 360.0

        return Pair(nirayanaMoonLong, ayanamsha)
    }
}
