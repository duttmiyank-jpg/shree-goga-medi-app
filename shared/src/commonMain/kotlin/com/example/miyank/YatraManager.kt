package com.example.miyank

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
enum class BusLayoutType {
    TWO_BY_TWO,
    TWO_BY_THREE
}

@Serializable
enum class SeatStatus {
    AVAILABLE,
    SELECTED,
    HELD,
    BOOKED,
    BLOCKED
}

@Serializable
enum class YatraPaymentStatus {
    PENDING,
    SUCCESS,
    NOT_REQUIRED,
    FAILED,
    CANCELLED
}

@Serializable
enum class YatraBookingStatus {
    PENDING_PAYMENT,
    CONFIRMED,
    CANCELLED,
    COMPLETED
}

@Serializable
data class YatraItem(
    val id: String = "",
    val title: String = "",
    val titleEnglish: String = "",
    val destination: String = "",
    val departureDateStr: String = "", // "YYYY-MM-DD"
    val displayDateStr: String = "", // "20 Oct 2026, मंगलवार"
    val departureTime: String = "सुबह 05:00 बजे",
    val price: Double = 1500.0,
    val description: String = "",
    val imageUrl: String = "",
    val isActive: Boolean = true,
    val sortOrder: Int = 0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

@Serializable
data class YatraBus(
    val id: String = "",
    val yatraId: String = "",
    val busName: String = "",
    val busNumber: String = "",
    val driverName: String = "",
    val driverPhone: String = "",
    val conductorName: String = "",
    val conductorPhone: String = "",
    val totalSeats: Int = 52,
    val layoutType: BusLayoutType = BusLayoutType.TWO_BY_TWO,
    val isActive: Boolean = true,
    val sortOrder: Int = 1
)

@Serializable
data class YatraSeat(
    val id: String = "",
    val busId: String = "",
    val seatNumber: String = "", // e.g. "1A", "1B", "1C", "1D"
    val rowNumber: Int = 1,
    val columnNumber: Int = 1,
    val isRearBench: Boolean = false,
    val status: SeatStatus = SeatStatus.AVAILABLE,
    val blockedReason: String = "",
    val bookingId: String = "",
    val heldByName: String = "",
    val heldByMobile: String = "",
    val holdStartAt: Long = 0L,
    val holdExpiresAt: Long = 0L
)

@Serializable
data class YatraBooking(
    val bookingId: String = "",
    val yatraId: String = "",
    val yatraTitle: String = "",
    val busId: String = "",
    val busName: String = "",
    val busNumber: String = "",
    val passengerName: String = "",
    val mobileNumber: String = "",
    val seatNumbers: List<String> = emptyList(),
    val passengerCount: Int = 1,
    val amountCharged: Double = 1500.0,
    val paymentStatus: YatraPaymentStatus = YatraPaymentStatus.SUCCESS,
    val bookingStatus: YatraBookingStatus = YatraBookingStatus.CONFIRMED,
    val isAdminBooking: Boolean = false,
    val isHoldBooking: Boolean = false,
    val holdExpiresAt: Long = 0L,
    val createdAt: Long = 0L
)

object YatraManager {
    private val settingsStorage = SettingsManager()
    private const val KEY_YATRAS_JSON = "yatras_list_json"
    private const val KEY_YATRA_BUSES_JSON = "yatra_buses_list_json"
    private const val KEY_YATRA_SEATS_JSON = "yatra_seats_list_json"
    private const val KEY_YATRA_BOOKINGS_JSON = "yatra_bookings_list_json"
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private val _yatras = MutableStateFlow(loadYatras())
    val yatras: StateFlow<List<YatraItem>> = _yatras.asStateFlow()

    private val _buses = MutableStateFlow(loadBuses())
    val buses: StateFlow<List<YatraBus>> = _buses.asStateFlow()

    private val _seats = MutableStateFlow(loadSeats())
    val seats: StateFlow<List<YatraSeat>> = _seats.asStateFlow()

    private val _bookings = MutableStateFlow(loadBookings())
    val bookings: StateFlow<List<YatraBooking>> = _bookings.asStateFlow()

    init {
        cleanExpiredHolds()
    }

    private fun loadYatras(): List<YatraItem> {
        val savedJson = settingsStorage.getString(KEY_YATRAS_JSON, "")
        if (savedJson.isNotEmpty()) {
            try { return json.decodeFromString<List<YatraItem>>(savedJson).sortedBy { it.sortOrder } } catch (e: Exception) { e.printStackTrace() }
        }
        return getDefaultYatras()
    }

    private fun loadBuses(): List<YatraBus> {
        val savedJson = settingsStorage.getString(KEY_YATRA_BUSES_JSON, "")
        if (savedJson.isNotEmpty()) {
            try { return json.decodeFromString<List<YatraBus>>(savedJson).sortedBy { it.sortOrder } } catch (e: Exception) { e.printStackTrace() }
        }
        return getDefaultBuses()
    }

    private fun loadSeats(): List<YatraSeat> {
        val savedJson = settingsStorage.getString(KEY_YATRA_SEATS_JSON, "")
        if (savedJson.isNotEmpty()) {
            try { return json.decodeFromString<List<YatraSeat>>(savedJson) } catch (e: Exception) { e.printStackTrace() }
        }
        return getDefaultSeats()
    }

    private fun loadBookings(): List<YatraBooking> {
        val savedJson = settingsStorage.getString(KEY_YATRA_BOOKINGS_JSON, "")
        if (savedJson.isNotEmpty()) {
            try { return json.decodeFromString<List<YatraBooking>>(savedJson).sortedByDescending { it.createdAt } } catch (e: Exception) { e.printStackTrace() }
        }
        return getDefaultBookings()
    }

    fun saveYatras(list: List<YatraItem>) {
        val sorted = list.sortedBy { it.sortOrder }
        _yatras.value = sorted
        try { settingsStorage.setString(KEY_YATRAS_JSON, json.encodeToString(sorted)) } catch (e: Exception) { e.printStackTrace() }
    }

    fun saveBuses(list: List<YatraBus>) {
        val sorted = list.sortedBy { it.sortOrder }
        _buses.value = sorted
        try { settingsStorage.setString(KEY_YATRA_BUSES_JSON, json.encodeToString(sorted)) } catch (e: Exception) { e.printStackTrace() }
    }

    fun saveSeats(list: List<YatraSeat>) {
        _seats.value = list
        try { settingsStorage.setString(KEY_YATRA_SEATS_JSON, json.encodeToString(list)) } catch (e: Exception) { e.printStackTrace() }
    }

    fun saveBookings(list: List<YatraBooking>) {
        val sorted = list.sortedByDescending { it.createdAt }
        _bookings.value = sorted
        try { settingsStorage.setString(KEY_YATRA_BOOKINGS_JSON, json.encodeToString(sorted)) } catch (e: Exception) { e.printStackTrace() }
    }

    fun addOrUpdateYatra(item: YatraItem) {
        val current = _yatras.value.toMutableList()
        val now = getEpochMillis()
        val index = current.indexOfFirst { it.id == item.id }

        if (index >= 0) {
            current[index] = item.copy(updatedAt = now)
        } else {
            val newItem = item.copy(
                id = if (item.id.isEmpty()) "yatra_$now" else item.id,
                createdAt = if (item.createdAt <= 0L) now else item.createdAt,
                updatedAt = now,
                sortOrder = if (item.sortOrder <= 0) current.size + 1 else item.sortOrder
            )
            current.add(0, newItem)
            // Automatically add Bus 1 for new Yatra
            addOrUpdateBus(YatraBus(id = "bus_1_${newItem.id}", yatraId = newItem.id, busName = "बस 01 (दरबार रथ)", busNumber = "HP 17 2026", totalSeats = 52, layoutType = BusLayoutType.TWO_BY_TWO))
        }
        saveYatras(current)

        // Sync with Mandir Calendar & Notifications
        val calEvent = MandirCalendarEvent(
            id = "yatra_evt_${item.id.ifEmpty { "yatra_$now" }}",
            title = "🚩 ${item.title}",
            titleEnglish = item.titleEnglish.ifEmpty { item.title },
            dateStr = item.departureDateStr,
            startTime = item.departureTime,
            categoryTag = "YATRA",
            description = "गंतव्य: ${item.destination}. ${item.description}",
            descriptionEnglish = "Destination: ${item.destination}.",
            isImportant = true,
            isActive = item.isActive,
            targetRoute = TargetRoutes.YATRA,
            targetId = item.id,
            createdAt = now
        )
        MandirCalendarManager.addOrUpdateEvent(calEvent)

        val notif = NotificationItem(
            id = "notif_yatra_${item.id.ifEmpty { "yatra_$now" }}",
            title = "🚩 आगामी पावन यात्रा: ${item.title}",
            titleEnglish = "🚩 Upcoming Yatra: ${item.titleEnglish.ifEmpty { item.title }}",
            message = "श्री जामनीवाला दरबार से ${item.title} प्रस्थान तिथि ${item.displayDateStr} (${item.departureTime})। सीटों की बुकिंग प्रारंभ है। 🙏",
            messageEnglish = "Yatra ${item.title} departing on ${item.displayDateStr}.",
            timestampStr = "अभी",
            categoryTag = NotificationCategories.YATRA,
            targetRoute = TargetRoutes.YATRA,
            isRead = false,
            isUrgent = true,
            createdAt = now
        )
        NotificationManager.addNotification(notif)
    }

    fun deleteYatra(id: String) {
        saveYatras(_yatras.value.filter { it.id != id })
        saveBuses(_buses.value.filter { it.yatraId != id })
        MandirCalendarManager.deleteEvent("yatra_evt_$id")
    }

    fun toggleYatraActive(id: String) {
        val now = getEpochMillis()
        saveYatras(_yatras.value.map { if (it.id == id) it.copy(isActive = !it.isActive, updatedAt = now) else it })
    }

    fun addOrUpdateBus(bus: YatraBus) {
        val currentBuses = _buses.value.toMutableList()
        val index = currentBuses.indexOfFirst { it.id == bus.id }
        val busId = if (bus.id.isEmpty()) "bus_${getEpochMillis()}" else bus.id
        val updatedBus = bus.copy(id = busId)

        if (index >= 0) {
            currentBuses[index] = updatedBus
        } else {
            currentBuses.add(updatedBus)
        }
        saveBuses(currentBuses)
        generateSeatsForBus(updatedBus)
    }

    fun deleteBus(busId: String) {
        saveBuses(_buses.value.filter { it.id != busId })
        saveSeats(_seats.value.filter { it.busId != busId })
    }

    private fun generateSeatsForBus(bus: YatraBus) {
        cleanExpiredHolds()
        val existingBusSeats = _seats.value.filter { it.busId == bus.id }.associateBy { it.seatNumber }
        val colsPerRow = if (bus.layoutType == BusLayoutType.TWO_BY_TWO) 4 else 5
        val rearCols = if (bus.layoutType == BusLayoutType.TWO_BY_TWO) 5 else 6
        val normalSeatsCount = (bus.totalSeats - rearCols).coerceAtLeast(0)
        val numNormalRows = (normalSeatsCount + colsPerRow - 1) / colsPerRow

        val newSeats = mutableListOf<YatraSeat>()
        val colLetters = arrayOf("A", "B", "C", "D", "E", "F")

        var seatCounter = 1
        // Normal Rows
        for (r in 1..numNormalRows) {
            for (c in 1..colsPerRow) {
                if (seatCounter > normalSeatsCount) break
                val seatNum = "$r${colLetters[c - 1]}"
                val existing = existingBusSeats[seatNum]

                val seat = YatraSeat(
                    id = "seat_${bus.id}_$seatNum",
                    busId = bus.id,
                    seatNumber = seatNum,
                    rowNumber = r,
                    columnNumber = c,
                    isRearBench = false,
                    status = existing?.status ?: SeatStatus.AVAILABLE,
                    blockedReason = existing?.blockedReason ?: "",
                    bookingId = existing?.bookingId ?: "",
                    heldByName = existing?.heldByName ?: "",
                    heldByMobile = existing?.heldByMobile ?: "",
                    holdStartAt = existing?.holdStartAt ?: 0L,
                    holdExpiresAt = existing?.holdExpiresAt ?: 0L
                )
                newSeats.add(seat)
                seatCounter++
            }
        }

        // Special Last Row (Rear Bench Row)
        val rearRowIndex = numNormalRows + 1
        for (c in 1..rearCols) {
            if (seatCounter > bus.totalSeats) break
            val seatNum = "$rearRowIndex${colLetters[c - 1]}"
            val existing = existingBusSeats[seatNum]

            val seat = YatraSeat(
                id = "seat_${bus.id}_$seatNum",
                busId = bus.id,
                seatNumber = seatNum,
                rowNumber = rearRowIndex,
                columnNumber = c,
                isRearBench = true,
                status = existing?.status ?: SeatStatus.AVAILABLE,
                blockedReason = existing?.blockedReason ?: "",
                bookingId = existing?.bookingId ?: "",
                heldByName = existing?.heldByName ?: "",
                heldByMobile = existing?.heldByMobile ?: "",
                holdStartAt = existing?.holdStartAt ?: 0L,
                holdExpiresAt = existing?.holdExpiresAt ?: 0L
            )
            newSeats.add(seat)
            seatCounter++
        }

        val otherSeats = _seats.value.filter { it.busId != bus.id }
        saveSeats(otherSeats + newSeats)
    }

    fun canUserSelectBus(yatraId: String, busId: String): Pair<Boolean, String> {
        val buses = getBusesForYatra(yatraId)
        val busIdx = buses.indexOfFirst { it.id == busId }
        if (busIdx <= 0) return Pair(true, "")

        cleanExpiredHolds()
        for (i in 0 until busIdx) {
            val prevBus = buses[i]
            val prevSeats = getSeatsForBus(prevBus.id)
            val availCount = prevSeats.count { it.status == SeatStatus.AVAILABLE }
            if (availCount > 0) {
                val isHindi = LanguageManager.currentLanguage == Language.HINDI
                val msgHindi = "पहली बस (${prevBus.busName}) में अभी $availCount सीटें उपलब्ध हैं। कृपया पहले उसी बस में सीट बुक करें।"
                val msgEnglish = "Seats are still available in ${prevBus.busName}. Please book a seat in that bus first."
                return Pair(false, if (isHindi) msgHindi else msgEnglish)
            }
        }
        return Pair(true, "")
    }

    fun isSeatHoldAllowedForYatra(departureDateStr: String): Boolean {
        val today = getTodayDateString()
        val parts1 = today.split("-").mapNotNull { it.toIntOrNull() }
        val parts2 = departureDateStr.split("-").mapNotNull { it.toIntOrNull() }
        if (parts1.size == 3 && parts2.size == 3) {
            val days1 = parts1[0] * 365 + parts1[1] * 30 + parts1[2]
            val days2 = parts2[0] * 365 + parts2[1] * 30 + parts2[2]
            return (days2 - days1) >= 3
        }
        return true
    }

    fun cleanExpiredHolds() {
        val now = getEpochMillis()
        val currentSeats = _seats.value.toMutableList()
        var modified = false

        currentSeats.forEachIndexed { idx, seat ->
            if (seat.status == SeatStatus.HELD && seat.holdExpiresAt > 0L && seat.holdExpiresAt <= now) {
                currentSeats[idx] = seat.copy(
                    status = SeatStatus.AVAILABLE,
                    heldByName = "",
                    heldByMobile = "",
                    holdStartAt = 0L,
                    holdExpiresAt = 0L
                )
                modified = true
            }
        }

        if (modified) {
            saveSeats(currentSeats)
        }
    }

    fun holdYatraSeat(busId: String, seatNumber: String, name: String, mobile: String): Boolean {
        cleanExpiredHolds()
        val now = getEpochMillis()
        val expiry3Days = now + (3 * 24 * 60 * 60 * 1000L) // 3 Days Hold
        val currentSeats = _seats.value.toMutableList()

        val idx = currentSeats.indexOfFirst { it.busId == busId && it.seatNumber == seatNumber }
        if (idx >= 0 && currentSeats[idx].status == SeatStatus.AVAILABLE) {
            currentSeats[idx] = currentSeats[idx].copy(
                status = SeatStatus.HELD,
                heldByName = name.trim(),
                heldByMobile = mobile.trim(),
                holdStartAt = now,
                holdExpiresAt = expiry3Days
            )
            saveSeats(currentSeats)
            return true
        }
        return false
    }

    fun releaseSeatHold(busId: String, seatNumber: String) {
        val currentSeats = _seats.value.toMutableList()
        val idx = currentSeats.indexOfFirst { it.busId == busId && it.seatNumber == seatNumber }
        if (idx >= 0) {
            currentSeats[idx] = currentSeats[idx].copy(
                status = SeatStatus.AVAILABLE,
                heldByName = "",
                heldByMobile = "",
                holdStartAt = 0L,
                holdExpiresAt = 0L
            )
            saveSeats(currentSeats)
        }
    }

    fun toggleSeatBlock(busId: String, seatNumber: String, reason: String = "Reserved for Seva") {
        cleanExpiredHolds()
        val currentSeats = _seats.value.toMutableList()
        val idx = currentSeats.indexOfFirst { it.busId == busId && it.seatNumber == seatNumber }
        if (idx >= 0) {
            val s = currentSeats[idx]
            val newStatus = if (s.status == SeatStatus.BLOCKED) SeatStatus.AVAILABLE else SeatStatus.BLOCKED
            currentSeats[idx] = s.copy(status = newStatus, blockedReason = if (newStatus == SeatStatus.BLOCKED) reason else "")
            saveSeats(currentSeats)
        }
    }

    fun bookYatraSeats(booking: YatraBooking, isAdminBooking: Boolean = false): Boolean {
        cleanExpiredHolds()
        val now = getEpochMillis()
        val currentSeats = _seats.value.toMutableList()

        booking.seatNumbers.forEach { seatNum ->
            val idx = currentSeats.indexOfFirst { it.busId == booking.busId && it.seatNumber == seatNum }
            if (idx >= 0) {
                currentSeats[idx] = currentSeats[idx].copy(
                    status = SeatStatus.BOOKED,
                    bookingId = booking.bookingId,
                    heldByName = "",
                    heldByMobile = "",
                    holdStartAt = 0L,
                    holdExpiresAt = 0L
                )
            }
        }
        saveSeats(currentSeats)

        val currentBookings = _bookings.value.toMutableList()
        val finalBooking = booking.copy(
            bookingId = if (booking.bookingId.isEmpty()) "yatra_book_$now" else booking.bookingId,
            createdAt = if (booking.createdAt <= 0L) now else booking.createdAt,
            paymentStatus = if (isAdminBooking) YatraPaymentStatus.NOT_REQUIRED else YatraPaymentStatus.SUCCESS,
            bookingStatus = YatraBookingStatus.CONFIRMED,
            isAdminBooking = isAdminBooking
        )
        currentBookings.add(0, finalBooking)
        saveBookings(currentBookings)

        if (!isAdminBooking) {
            val notif = NotificationItem(
                id = "notif_yatra_book_${finalBooking.bookingId}",
                title = "🎉 यात्रा सीट बुकिंग सफल (Paid)",
                titleEnglish = "Yatra Seat Booking Confirmed (Paid)",
                message = "श्री जामनीवाला दरबार से ${finalBooking.yatraTitle} हेतु आपकी सीटें (${finalBooking.seatNumbers.joinToString(", ")}) सफलतापूर्वक बुक हो गई हैं।",
                messageEnglish = "Yatra seats confirmed.",
                timestampStr = "अभी",
                categoryTag = NotificationCategories.YATRA,
                targetRoute = TargetRoutes.YATRA,
                isRead = false,
                isUrgent = true,
                createdAt = now
            )
            NotificationManager.addNotification(notif)
        }
        return true
    }

    fun cancelYatraBooking(bookingId: String) {
        val now = getEpochMillis()
        val currentBookings = _bookings.value.toMutableList()
        val idx = currentBookings.indexOfFirst { it.bookingId == bookingId }
        if (idx >= 0) {
            val booking = currentBookings[idx]
            currentBookings[idx] = booking.copy(bookingStatus = YatraBookingStatus.CANCELLED)
            saveBookings(currentBookings)

            // Release seats back to AVAILABLE
            val currentSeats = _seats.value.toMutableList()
            booking.seatNumbers.forEach { seatNum ->
                val seatIdx = currentSeats.indexOfFirst { it.busId == booking.busId && it.seatNumber == seatNum }
                if (seatIdx >= 0) {
                    currentSeats[seatIdx] = currentSeats[seatIdx].copy(
                        status = SeatStatus.AVAILABLE,
                        bookingId = ""
                    )
                }
            }
            saveSeats(currentSeats)
        }
    }

    fun getBusesForYatra(yatraId: String): List<YatraBus> {
        return _buses.value.filter { it.yatraId == yatraId && it.isActive }.sortedBy { it.sortOrder }
    }

    fun getSeatsForBus(busId: String): List<YatraSeat> {
        cleanExpiredHolds()
        return _seats.value.filter { it.busId == busId }
    }

    private fun getDefaultYatras(): List<YatraItem> {
        val now = getEpochMillis()
        return listOf(
            YatraItem(
                id = "yatra_1",
                title = "श्री गोगामेड़ी बागढ़ पावन धाम यात्रा",
                titleEnglish = "Shree Gogamedi Bagar Dham Yatra",
                destination = "गोगामेड़ी, राजस्थान",
                departureDateStr = "2026-10-20",
                displayDateStr = "20 Oct 2026, मंगलवार",
                departureTime = "सुबह 05:00 बजे",
                price = 1500.0,
                description = "श्री जामनीवाला दरबार से श्री गोगामेड़ी धाम बागढ़ यात्रा। भोजन, आवास एवं वाहन व्यवस्था दरबार द्वारा आयोजित है।",
                imageUrl = "file:///android_asset/temple_pooja.jpg",
                isActive = true,
                sortOrder = 1,
                createdAt = now,
                updatedAt = now
            ),
            YatraItem(
                id = "yatra_2",
                title = "श्री सालासर बालाजी एवं खाटू श्याम धाम यात्रा",
                titleEnglish = "Salasar Balaji & Khatu Shyam Dham Yatra",
                destination = "राजस्थान",
                departureDateStr = "2026-11-05",
                displayDateStr = "05 Nov 2026, गुरुवार",
                departureTime = "शाम 08:00 बजे",
                price = 2500.0,
                description = "श्री जामनीवाला दरबार से सालासर बालाजी एवं खाटू श्याम जी दर्शन।",
                imageUrl = "",
                isActive = true,
                sortOrder = 2,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    private fun getDefaultBuses(): List<YatraBus> {
        return listOf(
            YatraBus(
                id = "bus_1_yatra_1",
                yatraId = "yatra_1",
                busName = "बस 01 (दरबार रथ)",
                busNumber = "HP 17 2026",
                driverName = "सत्पाल सिंह",
                driverPhone = "+91 98765 43210",
                conductorName = "राम कुमार",
                conductorPhone = "+91 98123 45678",
                totalSeats = 52,
                layoutType = BusLayoutType.TWO_BY_TWO,
                isActive = true,
                sortOrder = 1
            ),
            YatraBus(
                id = "bus_2_yatra_1",
                yatraId = "yatra_1",
                busName = "बस 02 (भगत रथ)",
                busNumber = "HP 17 2027",
                driverName = "सुरेंद्र भगत",
                driverPhone = "+91 98111 22233",
                conductorName = "विकास शर्मा",
                conductorPhone = "+91 98333 44455",
                totalSeats = 52,
                layoutType = BusLayoutType.TWO_BY_TWO,
                isActive = true,
                sortOrder = 2
            )
        )
    }

    private fun getDefaultSeats(): List<YatraSeat> {
        val list = mutableListOf<YatraSeat>()
        val busId = "bus_1_yatra_1"
        val colLetters = arrayOf("A", "B", "C", "D")
        var counter = 1
        for (r in 1..13) {
            for (c in 1..4) {
                if (counter > 52) break
                val seatNum = "$r${colLetters[c - 1]}"
                val status = if (seatNum == "1A" || seatNum == "1B") SeatStatus.BLOCKED else if (seatNum == "2A" || seatNum == "2B") SeatStatus.BOOKED else SeatStatus.AVAILABLE
                list.add(
                    YatraSeat(
                        id = "seat_${busId}_$seatNum",
                        busId = busId,
                        seatNumber = seatNum,
                        rowNumber = r,
                        columnNumber = c,
                        status = status,
                        blockedReason = if (status == SeatStatus.BLOCKED) "चालक/स्टाफ आरक्षित" else ""
                    )
                )
                counter++
            }
        }
        return list
    }

    private fun getDefaultBookings(): List<YatraBooking> {
        val now = getEpochMillis()
        return listOf(
            YatraBooking(
                bookingId = "yatra_book_1",
                yatraId = "yatra_1",
                yatraTitle = "श्री गोगामेड़ी बागढ़ पावन धाम यात्रा",
                busId = "bus_1_yatra_1",
                busName = "बस 01 (दरबार रथ)",
                busNumber = "HP 17 2026",
                passengerName = "राहुल वर्मा",
                mobileNumber = "+91 9876543210",
                seatNumbers = listOf("2A", "2B"),
                passengerCount = 2,
                amountCharged = 3000.0,
                paymentStatus = YatraPaymentStatus.SUCCESS,
                bookingStatus = YatraBookingStatus.CONFIRMED,
                createdAt = now - 1000 * 60 * 60 * 6
            )
        )
    }
}
