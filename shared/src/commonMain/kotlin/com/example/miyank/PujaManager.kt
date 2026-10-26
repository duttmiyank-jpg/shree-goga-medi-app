package com.example.miyank

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
enum class PujaPricingMode {
    PER_BOOKING,
    PER_PERSON
}

@Serializable
enum class PujaBookingStatus {
    PENDING,
    CONFIRMED,
    POSTPONED,
    COMPLETED,
    INCOMPLETE,
    CANCELLED
}

@Serializable
data class PujaItem(
    val id: String = "",
    val nameHindi: String = "",
    val nameEnglish: String = "",
    val description: String = "",
    val availableDays: List<String> = emptyList(), // "सोमवार", "मंगलवार", "बुधवार", "गुरुवार", "शुक्रवार", "शनिवार", "रविवार"
    val startTime: String = "09:00 AM",
    val duration: String = "1 घंटे",
    val price: Double = 501.0,
    val pricingMode: PujaPricingMode = PujaPricingMode.PER_BOOKING,
    val isActive: Boolean = true,
    val sortOrder: Int = 0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

@Serializable
data class PujaBookingItem(
    val id: String = "",
    val pujaId: String = "",
    val pujaName: String = "",
    val devoteeName: String = "",
    val mobileNumber: String = "",
    val bookingDateStr: String = "", // "YYYY-MM-DD"
    val displayDateStr: String = "", // "18 Sep 2026 (शुक्रवार)"
    val bookingTime: String = "",
    val devoteeCount: Int = 1,
    val specialRequest: String = "",
    val amountCharged: Double = 501.0,
    val isPaid: Boolean = true,
    val status: PujaBookingStatus = PujaBookingStatus.CONFIRMED,
    val cancelledBy: String = "", // "USER" or "ADMIN"
    val cancellationReason: String = "",
    val cancelledAt: Long = 0L,
    val postponedReason: String = "",
    val postponedAt: Long = 0L,
    val createdAt: Long = 0L
)

data class PujaDateOccurrence(
    val dayName: String,
    val dateStr: String,
    val displayDateStr: String,
    val tithiText: String,
    val weekLabel: String
)

object PujaDays {
    val daysList = listOf("सोमवार", "मंगलवार", "बुधवार", "गुरुवार", "शुक्रवार", "शनिवार", "रविवार")

    fun getDayIndex(dayHindi: String): Int {
        return when (dayHindi.trim()) {
            "सोमवार" -> 1
            "मंगलवार" -> 2
            "बुधवार" -> 3
            "गुरुवार" -> 4
            "शुक्रवार" -> 5
            "शनिवार" -> 6
            "रविवार" -> 7
            else -> 1
        }
    }
}

object PujaManager {
    private val settingsStorage = SettingsManager()
    private const val KEY_PUJAS_JSON = "pujas_list_json"
    private const val KEY_PUJA_BOOKINGS_JSON = "puja_bookings_list_json"
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private val _pujas = MutableStateFlow(loadPujas())
    val pujas: StateFlow<List<PujaItem>> = _pujas.asStateFlow()

    private val _bookings = MutableStateFlow(loadBookings())
    val bookings: StateFlow<List<PujaBookingItem>> = _bookings.asStateFlow()

    private fun loadPujas(): List<PujaItem> {
        val savedJson = settingsStorage.getString(KEY_PUJAS_JSON, "")
        if (savedJson.isNotEmpty()) {
            try {
                return json.decodeFromString<List<PujaItem>>(savedJson).sortedBy { it.sortOrder }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return getDefaultPujas()
    }

    private fun loadBookings(): List<PujaBookingItem> {
        val savedJson = settingsStorage.getString(KEY_PUJA_BOOKINGS_JSON, "")
        if (savedJson.isNotEmpty()) {
            try {
                return json.decodeFromString<List<PujaBookingItem>>(savedJson).sortedByDescending { it.createdAt }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return getDefaultBookings()
    }

    fun savePujas(list: List<PujaItem>) {
        val sorted = list.sortedBy { it.sortOrder }
        _pujas.value = sorted
        try {
            val encoded = json.encodeToString(sorted)
            settingsStorage.setString(KEY_PUJAS_JSON, encoded)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveBookings(list: List<PujaBookingItem>) {
        val sorted = list.sortedByDescending { it.createdAt }
        _bookings.value = sorted
        try {
            val encoded = json.encodeToString(sorted)
            settingsStorage.setString(KEY_PUJA_BOOKINGS_JSON, encoded)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addOrUpdatePuja(item: PujaItem) {
        val current = _pujas.value.toMutableList()
        val now = getEpochMillis()
        val index = current.indexOfFirst { it.id == item.id }

        if (index >= 0) {
            current[index] = item.copy(updatedAt = now)
        } else {
            val newItem = item.copy(
                id = if (item.id.isEmpty()) "puja_$now" else item.id,
                createdAt = if (item.createdAt <= 0L) now else item.createdAt,
                updatedAt = now,
                sortOrder = if (item.sortOrder <= 0) current.size + 1 else item.sortOrder
            )
            current.add(0, newItem)
        }
        savePujas(current)
    }

    fun deletePuja(id: String) {
        val updated = _pujas.value.filter { it.id != id }
        savePujas(updated)
    }

    fun toggleActive(id: String) {
        val updated = _pujas.value.map {
            if (it.id == id) it.copy(isActive = !it.isActive, updatedAt = getEpochMillis()) else it
        }
        savePujas(updated)
    }

    fun bookPuja(booking: PujaBookingItem) {
        val now = getEpochMillis()
        val current = _bookings.value.toMutableList()
        val newBooking = booking.copy(
            id = if (booking.id.isEmpty()) "booking_$now" else booking.id,
            createdAt = if (booking.createdAt <= 0L) now else booking.createdAt,
            status = PujaBookingStatus.CONFIRMED,
            isPaid = true
        )
        current.add(0, newBooking)
        saveBookings(current)

        // 1. AUTOMATIC MANDIR CALENDAR ENTRY
        val calEvent = MandirCalendarEvent(
            id = "puja_evt_${newBooking.id}",
            title = "🪔 ${newBooking.pujaName} (${newBooking.devoteeName})",
            titleEnglish = "${newBooking.pujaName} (${newBooking.devoteeName})",
            dateStr = newBooking.bookingDateStr,
            startTime = newBooking.bookingTime,
            categoryTag = "PUJA",
            description = "श्रद्धालु: ${newBooking.devoteeName} (${newBooking.mobileNumber}), राशि: ₹${newBooking.amountCharged.toInt()}। ${newBooking.specialRequest}",
            descriptionEnglish = "Devotee: ${newBooking.devoteeName}, Paid: ₹${newBooking.amountCharged.toInt()}.",
            isImportant = true,
            isActive = true,
            createdAt = now
        )
        MandirCalendarManager.addOrUpdateEvent(calEvent)

        // 2. AUTOMATIC APP NOTIFICATION & REMINDER
        val notif = NotificationItem(
            id = "puja_notif_${newBooking.id}",
            title = "🙏 पूजा बुकिंग की पुष्टि (Paid)",
            titleEnglish = "Puja Booking Confirmed (Paid)",
            message = "आपकी ${newBooking.pujaName} दिनांक ${newBooking.displayDateStr} को ${newBooking.bookingTime} पर निर्धारित है। (राशि: ₹${newBooking.amountCharged.toInt()})",
            messageEnglish = "Your ${newBooking.pujaName} is scheduled on ${newBooking.displayDateStr} at ${newBooking.bookingTime}.",
            timestampStr = "अभी",
            categoryTag = NotificationCategories.PUJA,
            targetRoute = "PUJA",
            isRead = false,
            isUrgent = true,
            createdAt = now
        )
        NotificationManager.addNotification(notif)
    }

    fun updateBookingStatus(bookingId: String, newStatus: PujaBookingStatus) {
        val now = getEpochMillis()
        val currentList = _bookings.value.toMutableList()
        val idx = currentList.indexOfFirst { it.id == bookingId }
        if (idx >= 0) {
            val booking = currentList[idx]
            val updated = booking.copy(status = newStatus)
            currentList[idx] = updated
            saveBookings(currentList)

            // NOTIFY USER ON COMPLETED OR INCOMPLETE OR CANCELLED
            val notifMsg = when (newStatus) {
                PujaBookingStatus.COMPLETED -> "🙏 आपकी ${booking.pujaName} श्री जामनीवाला दरबार में सफलतापूर्वक पूर्ण हो गई है। आशीर्वाद प्राप्त करें।"
                PujaBookingStatus.INCOMPLETE -> "आपकी निर्धारित ${booking.pujaName} पूर्ण नहीं हो सकी (उपस्थित नहीं हुए)।"
                PujaBookingStatus.CANCELLED -> "आपकी ${booking.pujaName} बुकिंग रद्द कर दी गई है।"
                else -> null
            }

            if (notifMsg != null) {
                val notif = NotificationItem(
                    id = "puja_status_notif_${booking.id}_${now}",
                    title = if (newStatus == PujaBookingStatus.COMPLETED) "🙏 पूजा संपन्न हो गई!" else "पूजा अपडेट",
                    titleEnglish = "Puja Status Updated",
                    message = notifMsg,
                    messageEnglish = notifMsg,
                    timestampStr = "अभी",
                    categoryTag = NotificationCategories.PUJA,
                    targetRoute = "PUJA",
                    isRead = false,
                    isUrgent = true,
                    createdAt = now
                )
                NotificationManager.addNotification(notif)
            }
        }
    }

    fun cancelBooking(bookingId: String, cancelledBy: String, reason: String) {
        val now = getEpochMillis()
        val currentList = _bookings.value.toMutableList()
        val idx = currentList.indexOfFirst { it.id == bookingId }
        if (idx >= 0) {
            val booking = currentList[idx]
            val updated = booking.copy(
                status = PujaBookingStatus.CANCELLED,
                cancelledBy = cancelledBy,
                cancellationReason = reason.trim(),
                cancelledAt = now
            )
            currentList[idx] = updated
            saveBookings(currentList)

            // Remove/cancel calendar event
            MandirCalendarManager.deleteEvent("puja_evt_${booking.id}")

            // Add notification
            val notifMsg = if (cancelledBy == "ADMIN") {
                "आपकी ${booking.pujaName} बुकिंग रद्द कर दी गई है। कारण: ${reason.ifEmpty { "प्रशासनिक कारण" }}"
            } else {
                "आपकी ${booking.pujaName} बुकिंग आपके द्वारा सफलतापूर्वक रद्द कर दी गई है।"
            }

            val notif = NotificationItem(
                id = "puja_cancel_notif_${booking.id}_${now}",
                title = "❌ पूजा बुकिंग रद्द",
                titleEnglish = "Puja Booking Cancelled",
                message = notifMsg,
                messageEnglish = notifMsg,
                timestampStr = "अभी",
                categoryTag = NotificationCategories.PUJA,
                targetRoute = "PUJA",
                isRead = false,
                isUrgent = true,
                createdAt = now
            )
            NotificationManager.addNotification(notif)
        }
    }

    fun postponeBooking(
        bookingId: String,
        newDateStr: String,
        newDisplayDateStr: String,
        newTime: String,
        reason: String
    ) {
        val now = getEpochMillis()
        val currentList = _bookings.value.toMutableList()
        val idx = currentList.indexOfFirst { it.id == bookingId }
        if (idx >= 0) {
            val booking = currentList[idx]
            val updated = booking.copy(
                status = PujaBookingStatus.POSTPONED,
                bookingDateStr = newDateStr,
                displayDateStr = newDisplayDateStr,
                bookingTime = newTime,
                postponedReason = reason.trim(),
                postponedAt = now
            )
            currentList[idx] = updated
            saveBookings(currentList)

            // Update Mandir Calendar event
            val calEvent = MandirCalendarEvent(
                id = "puja_evt_${booking.id}",
                title = "🪔 ${booking.pujaName} (${booking.devoteeName})",
                titleEnglish = "${booking.pujaName} (${booking.devoteeName})",
                dateStr = newDateStr,
                startTime = newTime,
                categoryTag = "PUJA",
                description = "स्थगित पूजा (Postponed). हेतु: ${reason.ifEmpty { "प्रशासनिक कारण" }}. भक्त: ${booking.devoteeName} (${booking.mobileNumber})",
                descriptionEnglish = "Postponed Puja for ${booking.devoteeName}.",
                isImportant = true,
                isActive = true,
                createdAt = now
            )
            MandirCalendarManager.addOrUpdateEvent(calEvent)

            // Send Notification
            val notifMsg = "आपकी ${booking.pujaName} अब नई तिथि ${newDisplayDateStr} को ${newTime} पर निर्धारित की गई है। कारण: ${reason.ifEmpty { "प्रशासनिक कारण" }}"
            val notif = NotificationItem(
                id = "puja_postpone_notif_${booking.id}_${now}",
                title = "⏳ पूजा स्थगित (Postponed)",
                titleEnglish = "Puja Schedule Postponed",
                message = notifMsg,
                messageEnglish = notifMsg,
                timestampStr = "अभी",
                categoryTag = NotificationCategories.PUJA,
                targetRoute = "PUJA",
                isRead = false,
                isUrgent = true,
                createdAt = now
            )
            NotificationManager.addNotification(notif)
        }
    }

    // Dynamic Multi-Week Upcoming Occurrences Generator (Weeks 1 to 4)
    fun getUpcomingOccurrencesForDays(availableDays: List<String>, weeksAhead: Int = 4): List<PujaDateOccurrence> {
        val result = mutableListOf<PujaDateOccurrence>()
        val now = getEpochMillis()
        val totalSeconds = now / 1000 + 19800
        val epochDays = (totalSeconds / 86400).toInt()
        val currentDayOfWeek = ((epochDays + 3) % 7) + 1

        for (weekOffset in 0 until weeksAhead) {
            val weekLabel = when (weekOffset) {
                0 -> "सप्ताह 1 (This Week)"
                1 -> "सप्ताह 2 (Next Week)"
                2 -> "सप्ताह 3 (In 3 Weeks)"
                else -> "सप्ताह 4 (In 4 Weeks)"
            }

            availableDays.forEach { dayHindi ->
                val targetDayIndex = PujaDays.getDayIndex(dayHindi)
                var diffDays = targetDayIndex - currentDayOfWeek
                if (diffDays < 0) {
                    diffDays += 7
                }
                val totalDiffDays = diffDays + (weekOffset * 7)

                val targetEpochMillis = now + (totalDiffDays * 86400000L)
                val dateStr = formatEpochToDateStr(targetEpochMillis)
                val displayDate = formatEpochToDisplayDate(targetEpochMillis)

                val panchang = PanchangProvider.getPanchangForDate(dateStr)
                val fullDisplayStr = "$displayDate ($dayHindi)"
                val tithiInfo = "${panchang.tithi} (${panchang.pakshaMasaFormatted})"

                result.add(
                    PujaDateOccurrence(
                        dayName = dayHindi,
                        dateStr = dateStr,
                        displayDateStr = fullDisplayStr,
                        tithiText = tithiInfo,
                        weekLabel = weekLabel
                    )
                )
            }
        }
        return result.distinctBy { it.dateStr }.sortedBy { it.dateStr }
    }

    // Dynamic Weekday Date Calculator
    fun getUpcomingDateInfoForDay(dayHindiName: String): Triple<String, String, String> {
        val occurrences = getUpcomingOccurrencesForDays(listOf(dayHindiName), 1)
        val first = occurrences.firstOrNull()
        if (first != null) {
            return Triple(first.dateStr, first.displayDateStr, first.tithiText)
        }
        val dateStr = formatEpochToDateStr(getEpochMillis())
        return Triple(dateStr, "$dateStr ($dayHindiName)", "सप्तमी")
    }

    // Helper to check if a specific time slot on a specific day/date is in the future
    fun isSlotInFuture(timeStr: String, dateStr: String): Boolean {
        val now = getEpochMillis()
        val todayStr = getTodayDateString()

        if (dateStr > todayStr) return true // Future date
        if (dateStr < todayStr) return false // Past date

        // TODAY: Compare time hours/mins with current time
        val totalSeconds = now / 1000 + 19800
        val epochDays = (totalSeconds / 86400).toInt()
        val slotHourMin = parseTimeStrToHoursMins(timeStr)
        val dayStartMillis = (epochDays * 86400000L)
        val slotEpochMillis = dayStartMillis + (slotHourMin.first * 3600000L) + (slotHourMin.second * 60000L)

        return slotEpochMillis > (now - 15 * 60 * 1000L)
    }

    private fun formatEpochToDateStr(epochMillis: Long): String {
        val totalSeconds = epochMillis / 1000 + 19800
        val totalDays = (totalSeconds / 86400).toInt()

        val z = totalDays + 719468
        val era = (if (z >= 0) z else z - 146096) / 146097
        val doe = z - era * 146097
        val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
        var y = yoe + era * 400
        val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
        val mp = (5 * doy + 2) / 153
        val d = doy - (153 * mp + 2) / 5 + 1
        val m = mp + (if (mp < 10) 3 else -9)
        if (m <= 2) y += 1

        val yearStr = y.toString().padStart(4, '0')
        val monthStr = m.toString().padStart(2, '0')
        val dayStr = d.toString().padStart(2, '0')
        return "$yearStr-$monthStr-$dayStr"
    }

    private fun formatEpochToDisplayDate(epochMillis: Long): String {
        val monthNames = arrayOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val dateStr = formatEpochToDateStr(epochMillis)
        val parts = dateStr.split("-")
        val year = parts.getOrNull(0) ?: "2026"
        val monthInt = parts.getOrNull(1)?.toIntOrNull() ?: 9
        val day = parts.getOrNull(2) ?: "16"
        val monthName = monthNames.getOrElse(monthInt) { "Sep" }
        return "$day $monthName $year"
    }

    private fun parseTimeStrToHoursMins(timeStr: String): Pair<Int, Int> {
        return try {
            val trimmed = timeStr.trim().uppercase()
            val isPm = trimmed.contains("PM")
            val cleanStr = trimmed.replace("AM", "").replace("PM", "").trim()
            val parts = cleanStr.split(":")
            var hour = parts[0].trim().toInt()
            val min = if (parts.size > 1) parts[1].trim().toInt() else 0

            if (isPm && hour < 12) hour += 12
            if (!isPm && hour == 12) hour = 0

            Pair(hour, min)
        } catch (e: Exception) {
            Pair(9, 0)
        }
    }

    private fun getDefaultPujas(): List<PujaItem> {
        val now = getEpochMillis()
        return listOf(
            PujaItem(
                id = "puja_1",
                nameHindi = "विशेष मनोकामना महापूजा",
                nameEnglish = "Special Manokamna Maha Puja",
                description = "बाबा गोगा जी महाराज एवं सत् गुरु बाबा सोम दत्त जी महाराज की विशेष कृपा एवं आशीर्वाद प्राप्ति हेतु महापूजा।",
                availableDays = listOf("सोमवार", "बुधवार", "शुक्रवार", "रविवार"),
                startTime = "09:00 AM",
                duration = "1 घंटे 30 मिनट",
                price = 501.0,
                pricingMode = PujaPricingMode.PER_BOOKING,
                isActive = true,
                sortOrder = 1,
                createdAt = now,
                updatedAt = now
            ),
            PujaItem(
                id = "puja_2",
                nameHindi = "रुद्राभिषेक एवं नवग्रह शान्ति पूजा",
                nameEnglish = "Rudrabhishek & Navgrah Shanti Puja",
                description = "शिव-गोगा पूजन, रुद्राभिषेक एवं परिवार में सुख-समृद्धि हेतु विशेष अर्चना।",
                availableDays = listOf("सोमवार", "गुरुवार", "शनिवार"),
                startTime = "10:30 AM",
                duration = "2 घंटे",
                price = 1100.0,
                pricingMode = PujaPricingMode.PER_BOOKING,
                isActive = true,
                sortOrder = 2,
                createdAt = now,
                updatedAt = now
            ),
            PujaItem(
                id = "puja_3",
                nameHindi = "सांध्य महापूजा एवं दीपदान",
                nameEnglish = "Sandhya Maha Puja & Deepdaan",
                description = "सायंकालीन पावन आरती के उपरांत विशेष दीपदान एवं हवन महापूजा।",
                availableDays = listOf("मंगलवार", "शुक्रवार", "रविवार"),
                startTime = "05:00 PM",
                duration = "1 घंटे",
                price = 251.0,
                pricingMode = PujaPricingMode.PER_PERSON,
                isActive = true,
                sortOrder = 3,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    private fun getDefaultBookings(): List<PujaBookingItem> {
        val today = getTodayDateString()
        val now = getEpochMillis()
        return listOf(
            PujaBookingItem(
                id = "booking_1",
                pujaId = "puja_1",
                pujaName = "विशेष मनोकामना महापूजा",
                devoteeName = "राहुल शर्मा",
                mobileNumber = "+91 9876543210",
                bookingDateStr = today,
                displayDateStr = "$today (सोमवार)",
                bookingTime = "09:00 AM",
                devoteeCount = 2,
                specialRequest = "परिवार के उत्तम स्वास्थ्य एवं सुख हेतु विशेष प्रार्थना।",
                amountCharged = 501.0,
                isPaid = true,
                status = PujaBookingStatus.CONFIRMED,
                createdAt = now - 1000 * 60 * 60 * 3
            )
        )
    }
}
