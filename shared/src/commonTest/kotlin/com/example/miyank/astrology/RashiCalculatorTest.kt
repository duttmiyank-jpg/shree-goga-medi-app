package com.example.miyank.astrology

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RashiCalculatorTest {

    @Test
    fun testRashiFromDegreeBoundaries() {
        assertEquals(Rashi.MESHA, Rashi.fromDegree(0.0))
        assertEquals(Rashi.MESHA, Rashi.fromDegree(15.5))
        assertEquals(Rashi.VRISHABHA, Rashi.fromDegree(30.0))
        assertEquals(Rashi.VRISHABHA, Rashi.fromDegree(45.0))
        assertEquals(Rashi.MITHUNA, Rashi.fromDegree(60.0))
        assertEquals(Rashi.KARKA, Rashi.fromDegree(90.0))
        assertEquals(Rashi.SIMHA, Rashi.fromDegree(120.0))
        assertEquals(Rashi.KANYA, Rashi.fromDegree(150.0))
        assertEquals(Rashi.TULA, Rashi.fromDegree(180.0))
        assertEquals(Rashi.VRISHCHIKA, Rashi.fromDegree(210.0))
        assertEquals(Rashi.DHANU, Rashi.fromDegree(240.0))
        assertEquals(Rashi.MAKARA, Rashi.fromDegree(270.0))
        assertEquals(Rashi.KUMBHA, Rashi.fromDegree(300.0))
        assertEquals(Rashi.MEENA, Rashi.fromDegree(330.0))
        assertEquals(Rashi.MEENA, Rashi.fromDegree(359.9))
    }

    @Test
    fun testTimeToDecimalHoursParsing() {
        assertEquals(6.5, MeeusRashiCalculator.parseTimeToDecimalHours("06:30 AM"), 0.001)
        assertEquals(18.5, MeeusRashiCalculator.parseTimeToDecimalHours("06:30 PM"), 0.001)
        assertEquals(0.0, MeeusRashiCalculator.parseTimeToDecimalHours("12:00 AM"), 0.001)
        assertEquals(12.0, MeeusRashiCalculator.parseTimeToDecimalHours("12:00 PM"), 0.001)
        assertEquals(14.25, MeeusRashiCalculator.parseTimeToDecimalHours("14:15"), 0.001)
    }

    @Test
    fun testDeterministicCalculationOutput() {
        val details = BirthDetails(
            userName = "Test User",
            birthDateStr = "1995-08-15",
            birthTimeStr = "08:30 AM",
            isTimeUnknown = false,
            cityName = "पांवटा साहिब",
            latitude = 30.45,
            longitude = 77.62,
            tzOffsetHours = 5.5
        )

        val result1 = MeeusRashiCalculator.calculateChandraRashi(details)
        val result2 = MeeusRashiCalculator.calculateChandraRashi(details)

        assertEquals(result1.rashiIndex, result2.rashiIndex)
        assertEquals(result1.moonLongitude, result2.moonLongitude, 0.0001)
        assertEquals(result1.ayanamsha, result2.ayanamsha, 0.0001)
        assertEquals(CalculationConfidenceStatus.EXACT, result1.status)
    }

    @Test
    fun testTimeUnknownHandling() {
        val detailsUnknown = BirthDetails(
            userName = "Unknown Time User",
            birthDateStr = "2000-01-01",
            birthTimeStr = "",
            isTimeUnknown = true,
            cityName = "नई दिल्ली",
            latitude = 28.6139,
            longitude = 77.2090,
            tzOffsetHours = 5.5
        )

        val result = MeeusRashiCalculator.calculateChandraRashi(detailsUnknown)
        assertNotNull(result)
        assertTrue(
            result.status == CalculationConfidenceStatus.TIME_UNKNOWN_SAME_RASHI ||
                    result.status == CalculationConfidenceStatus.TIME_UNKNOWN_BOUNDARY_CROSSED
        )
    }

    @Test
    fun testInvalidLocationHandling() {
        val invalidDetails = BirthDetails(
            latitude = 150.0, // Invalid latitude (> 90)
            longitude = 77.62
        )

        val result = MeeusRashiCalculator.calculateChandraRashi(invalidDetails)
        assertEquals(CalculationConfidenceStatus.INVALID_LOCATION, result.status)
    }
}
