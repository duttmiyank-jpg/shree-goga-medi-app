package com.example.miyank

actual fun getTodayDateString(): String {
    return "2026-09-05"
}

actual fun formatDisplayDate(dateStr: String): String {
    return dateStr
}

actual fun getEpochMillis(): Long {
    return 0L
}

actual fun formatEpochToDateString(epochMillis: Long): String {
    return "2026-09-05"
}
