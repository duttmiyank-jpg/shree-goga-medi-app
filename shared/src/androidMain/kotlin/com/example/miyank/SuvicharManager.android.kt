package com.example.miyank

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import java.util.TimeZone

actual fun getTodayDateString(): String {
    return try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val zone = ZoneId.of("Asia/Kolkata")
            LocalDate.now(zone).toString()
        } else {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("Asia/Kolkata")
            sdf.format(Date())
        }
    } catch (e: Exception) {
        "2026-09-05"
    }
}

actual fun formatDisplayDate(dateStr: String): String {
    return try {
        if (dateStr.isEmpty()) return ""
        val parts = dateStr.split("-")
        if (parts.size == 3) {
            val year = parts[0]
            val month = parts[1].toIntOrNull() ?: 1
            val day = parts[2]
            val monthsHindi = arrayOf(
                "", "जनवरी", "फरवरी", "मार्च", "अप्रैल", "मई", "जून",
                "जुलाई", "अगस्त", "सितम्बर", "अक्टूबर", "नवम्बर", "दिसम्बर"
            )
            val monthName = if (month in 1..12) monthsHindi[month] else parts[1]
            "$day $monthName $year"
        } else {
            dateStr
        }
    } catch (e: Exception) {
        dateStr
    }
}

actual fun getEpochMillis(): Long {
    return System.currentTimeMillis()
}

actual fun formatEpochToDateString(epochMillis: Long): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        sdf.format(Date(epochMillis))
    } catch (e: Exception) {
        getTodayDateString()
    }
}
