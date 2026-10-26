package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyank.BusLayoutType
import com.example.miyank.LanguageManager
import com.example.miyank.SeatStatus
import com.example.miyank.YatraBooking
import com.example.miyank.YatraBus
import com.example.miyank.YatraItem
import com.example.miyank.YatraManager
import com.example.miyank.YatraSeat
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YatraBookingScreen(
    yatraId: String,
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val scrollState = rememberScrollState()
    val isHindi = LanguageManager.currentLanguage == com.example.miyank.Language.HINDI

    val allYatras by YatraManager.yatras.collectAsState()
    val yatra = remember(allYatras, yatraId) {
        allYatras.find { it.id == yatraId } ?: allYatras.firstOrNull() ?: YatraItem(id = yatraId, title = "श्री गोगामेड़ी बागढ़ धाम यात्रा")
    }

    val buses = remember(yatra) { YatraManager.getBusesForYatra(yatra.id) }
    var selectedBus by remember(buses) { mutableStateOf(buses.firstOrNull()) }

    val allSeats by YatraManager.seats.collectAsState()
    val busSeats = remember(allSeats, selectedBus) {
        if (selectedBus != null) YatraManager.getSeatsForBus(selectedBus!!.id) else emptyList()
    }

    var selectedSeatNumbers by remember { mutableStateOf(setOf<String>()) }

    var nameInput by remember { mutableStateOf("") }
    var mobileInput by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    var confirmedBooking by remember { mutableStateOf<YatraBooking?>(null) }

    val isHoldAllowed = remember(yatra.departureDateStr) {
        YatraManager.isSeatHoldAllowedForYatra(yatra.departureDateStr)
    }

    val totalAmount = remember(selectedSeatNumbers, yatra.price) {
        selectedSeatNumbers.size * yatra.price
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isHindi) "सीट बुकिंग — " + yatra.title else "Book Seats — " + yatra.title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite,
                            maxLines = 1
                        )
                        Text(
                            text = "प्रस्थान: ${yatra.displayDateStr} • ${yatra.departureTime}",
                            fontSize = 11.sp,
                            color = AccentGolden
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark,
                    titleContentColor = TextWhite,
                    navigationIconContentColor = TextWhite
                )
            )
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // STEP 1: SELECT BUS WITH SEQUENTIAL FILLING RULE
            Text(
                text = if (isHindi) "1. बस का चयन करें *" else "1. Select Bus *",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AccentGolden
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (buses.isEmpty()) {
                Text(text = if (isHindi) "इस यात्रा के लिए कोई बस उपलब्ध नहीं है।" else "No buses available.", color = TextMuted, fontSize = 12.sp)
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    buses.forEach { bus ->
                        val isSelected = selectedBus?.id == bus.id
                        val (canSelect, lockMsg) = YatraManager.canUserSelectBus(yatra.id, bus.id)
                        val totalSeatsCount = bus.totalSeats
                        val bookedCount = allSeats.count { it.busId == bus.id && (it.status == SeatStatus.BOOKED || it.status == SeatStatus.BLOCKED) }
                        val availCount = (totalSeatsCount - bookedCount).coerceAtLeast(0)

                        Card(
                            modifier = Modifier
                                .width(180.dp)
                                .clickable(enabled = canSelect) {
                                    haptic.playPremiumHaptic()
                                    selectedBus = bus
                                    selectedSeatNumbers = emptySet()
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = if (!canSelect) BackgroundDark else if (isSelected) AccentGolden.copy(alpha = 0.2f) else SurfaceDark),
                            border = BorderStroke(1.dp, if (!canSelect) Color.Red.copy(alpha = 0.4f) else if (isSelected) AccentGolden else WarmGold.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = bus.busName, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = if (canSelect) TextWhite else TextMuted)
                                    Icon(if (canSelect) Icons.Default.DirectionsBus else Icons.Default.Lock, contentDescription = null, tint = if (canSelect) AccentGolden else Color.Red, modifier = Modifier.size(16.dp))
                                }
                                Text(text = bus.busNumber, fontSize = 11.sp, color = if (canSelect) AccentGolden else TextMuted)
                                Spacer(modifier = Modifier.height(4.dp))

                                if (!canSelect) {
                                    Text(text = "🔒 LOCKED: " + lockMsg, fontSize = 9.sp, color = Color.Red, lineHeight = 12.sp)
                                } else {
                                    Text(text = "$availCount / $totalSeatsCount सीट उपलब्ध", fontSize = 11.sp, color = if (availCount > 0) Color(0xFF81C784) else Color.Red, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // STEP 2: INTERACTIVE BUS SEAT MAP & BUS HEADER DETAILS
            if (selectedBus != null) {
                // BUS HEADER INFORMATION CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = selectedBus!!.busName + " (" + selectedBus!!.busNumber + ")", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = AccentGolden)
                            Surface(
                                color = AccentGolden.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (selectedBus!!.layoutType == BusLayoutType.TWO_BY_TWO) "2 × 2" else "2 × 3",
                                    color = AccentGolden,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = (if (isHindi) "चालक: " else "Driver: ") + selectedBus!!.driverName.ifEmpty { "Satpal Singh" } + " (" + selectedBus!!.driverPhone.ifEmpty { "+91 98765 43210" } + ")", fontSize = 12.sp, color = TextWhite)
                        Text(text = (if (isHindi) "परिचालक: " else "Conductor: ") + selectedBus!!.conductorName.ifEmpty { "Ram Kumar" } + " (" + selectedBus!!.conductorPhone.ifEmpty { "+91 98123 45678" } + ")", fontSize = 12.sp, color = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isHindi) "2. अपनी सीट का चयन करें *" else "2. Select Your Seats *",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentGolden
                )
                Spacer(modifier = Modifier.height(8.dp))

                // RESPONSIVE SEAT LEGEND IN SELECTED LANGUAGE
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(if (isHindi) "उपलब्ध" else "Available", Color(0xFF1B5E20))
                    LegendItem(if (isHindi) "चयनित" else "Selected", AccentGolden)
                    LegendItem(if (isHindi) "होल्ड" else "Held", Color(0xFFFF9800))
                    LegendItem(if (isHindi) "बुक" else "Booked", Color.Red)
                    LegendItem(if (isHindi) "ब्लॉक" else "Blocked", Color.DarkGray)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // BUS SEAT MAP CONTAINER
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // DRIVER SECTION
                        Surface(
                            color = BackgroundDark,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "🚘 चालन केबिन (DRIVER CABIN) — FRONT",
                                modifier = Modifier.padding(8.dp),
                                textAlign = TextAlign.Center,
                                fontSize = 11.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val colsPerRow = if (selectedBus!!.layoutType == BusLayoutType.TWO_BY_TWO) 4 else 5
                        val seatRows = busSeats.groupBy { it.rowNumber }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            seatRows.forEach { (_, rowSeats) ->
                                val isRearRow = rowSeats.any { it.isRearBench }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    rowSeats.forEachIndexed { colIdx, seat ->
                                        if (!isRearRow && ((colsPerRow == 4 && colIdx == 2) || (colsPerRow == 5 && colIdx == 2))) {
                                            Spacer(modifier = Modifier.width(16.dp))
                                        }

                                        val isSelected = selectedSeatNumbers.contains(seat.seatNumber)
                                        val isBooked = seat.status == SeatStatus.BOOKED
                                        val isBlocked = seat.status == SeatStatus.BLOCKED
                                        val isHeld = seat.status == SeatStatus.HELD

                                        val bgColor = when {
                                            isBlocked -> Color.DarkGray
                                            isBooked -> Color.Red
                                            isHeld -> Color(0xFFFF9800)
                                            isSelected -> AccentGolden
                                            else -> Color(0xFF1B5E20)
                                        }

                                        val textColor = if (isSelected) BackgroundDark else TextWhite

                                        Box(
                                            modifier = Modifier
                                                .size(width = if (isRearRow) 38.dp else 40.dp, height = 40.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(bgColor)
                                                .clickable(enabled = !isBooked && !isBlocked && !isHeld) {
                                                    haptic.playPremiumHaptic()
                                                    selectedSeatNumbers = if (isSelected) {
                                                        selectedSeatNumbers - seat.seatNumber
                                                    } else {
                                                        selectedSeatNumbers + seat.seatNumber
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = seat.seatNumber,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = textColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedSeatNumbers.isNotEmpty()) {
                    Surface(
                        color = AccentGolden.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, AccentGolden.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "चयनित सीटें (${selectedSeatNumbers.size}): " + selectedSeatNumbers.joinToString(", "),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentGolden,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // STEP 3: PASSENGER DETAILS
                    Text(
                        text = if (isHindi) "3. यात्री विवरण *" else "3. Passenger Details *",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentGolden
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (errorText != null) {
                        Text(text = errorText!!, color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it; errorText = null },
                        label = { Text(if (isHindi) "मुख्य यात्री का नाम *" else "Passenger Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AccentGolden) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = mobileInput,
                        onValueChange = { input ->
                            val digits = input.filter { it.isDigit() }
                            if (digits.length <= 10) {
                                mobileInput = digits
                                errorText = null
                            }
                        },
                        label = { Text(if (isHindi) "10 अंकों का मोबाइल नंबर *" else "Mobile Number *") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = AccentGolden) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!isHoldAllowed) {
                        Text(
                            text = if (isHindi) "⚠️ यात्रा में 3 दिन से कम समय शेष है, इसलिए सीट होल्ड की सुविधा उपलब्ध नहीं है।"
                                   else "⚠️ Seat hold is unavailable because less than 3 days remain before journey.",
                            fontSize = 11.sp,
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // PAYMENT SUMMARY & HOLD ACTION
                    Surface(
                        color = BackgroundDark,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "कुल सीटें: ${selectedSeatNumbers.size} × ₹${yatra.price.toInt()}", fontSize = 11.sp, color = TextMuted)
                                    Text(text = "कुल देय राशि: ₹${totalAmount.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = AccentGolden)
                                }

                                Button(
                                    onClick = {
                                        val cleanName = nameInput.trim()
                                        val cleanPhone = mobileInput.trim()
                                        if (cleanName.isEmpty()) {
                                            errorText = if (isHindi) "कृपया यात्री का नाम दर्ज करें।" else "Please enter passenger name."
                                            return@Button
                                        }
                                        if (cleanPhone.length != 10) {
                                            errorText = if (isHindi) "कृपया 10 अंकों का सही मोबाइल नंबर दर्ज करें।" else "Please enter a valid 10-digit mobile number."
                                            return@Button
                                        }

                                        haptic.playPremiumHaptic()
                                        val booking = YatraBooking(
                                            yatraId = yatra.id,
                                            yatraTitle = yatra.title,
                                            busId = selectedBus!!.id,
                                            busName = selectedBus!!.busName,
                                            busNumber = selectedBus!!.busNumber,
                                            passengerName = cleanName,
                                            mobileNumber = "+91 $cleanPhone",
                                            seatNumbers = selectedSeatNumbers.toList().sorted(),
                                            passengerCount = selectedSeatNumbers.size,
                                            amountCharged = totalAmount
                                        )

                                        val success = YatraManager.bookYatraSeats(booking)
                                        if (success) {
                                            confirmedBooking = booking
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isHindi) "भुगतान एवं सीट बुक करें" else "Pay & Book Seats", color = BackgroundDark, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (isHoldAllowed && selectedSeatNumbers.size == 1) {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = {
                                        val cleanName = nameInput.trim()
                                        val cleanPhone = mobileInput.trim()
                                        if (cleanName.isEmpty() || cleanPhone.length != 10) {
                                            errorText = if (isHindi) "होल्ड हेतु नाम व मोबाइल नंबर अनिवार्य है।" else "Name and mobile are required for hold."
                                            return@OutlinedButton
                                        }
                                        haptic.playPremiumHaptic()
                                        val seatNum = selectedSeatNumbers.first()
                                        YatraManager.holdYatraSeat(selectedBus!!.id, seatNum, cleanName, cleanPhone)
                                        selectedSeatNumbers = emptySet()
                                        errorText = null
                                    },
                                    modifier = Modifier.fillMaxWidth().height(42.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFF9800))
                                ) {
                                    Text(if (isHindi) "3 दिन के लिए सीट होल्ड करें (Hold Seat 3 Days)" else "Hold Seat for 3 Days", color = Color(0xFFFF9800), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // CONFIRMATION RECEIPT DIALOG
    if (confirmedBooking != null) {
        YatraBookingReceiptDialog(
            booking = confirmedBooking!!,
            yatra = yatra,
            isHindi = isHindi,
            onDismiss = {
                confirmedBooking = null
                onBackClick()
            }
        )
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 10.sp, color = TextWhite)
    }
}

@Composable
private fun YatraBookingReceiptDialog(
    booking: YatraBooking,
    yatra: YatraItem,
    isHindi: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Receipt, contentDescription = null, tint = AccentGolden, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isHindi) "यात्रा सीट बुकिंग रसीद" else "Yatra Seats Receipt", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = AccentGolden)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = BackgroundDark,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "🛕 श्री जामनीवाला दरबार", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = AccentGolden, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        Text(text = "पावन धार्मिक यात्रा बुकिंग रसीद", fontSize = 11.sp, color = TextMuted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = WarmGold.copy(alpha = 0.2f))

                        Text(text = "Booking ID: ${booking.bookingId}", fontSize = 10.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = booking.yatraTitle, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = TextWhite)
                        Text(text = "प्रस्थान: ${yatra.displayDateStr} (${yatra.departureTime})", fontSize = 12.sp, color = TextWhite)
                        Text(text = "गंतव्य: ${yatra.destination}", fontSize = 12.sp, color = TextWhite)
                        Text(text = "बस: ${booking.busName} (${booking.busNumber})", fontSize = 12.sp, color = TextWhite)
                        Text(text = "सीट संख्या: ${booking.seatNumbers.joinToString(", ")}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccentGolden)
                        Text(text = "यात्री: ${booking.passengerName} (${booking.mobileNumber})", fontSize = 12.sp, color = TextWhite)

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = WarmGold.copy(alpha = 0.2f))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "कुल भुगतान राशि:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text(text = "₹${booking.amountCharged.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = AccentGolden)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Payment Status:", fontSize = 11.sp, color = TextMuted)
                            Text(text = "Paid ✓", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF81C784))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
            ) {
                Text(if (isHindi) "ठीक है" else "OK", color = BackgroundDark, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = SurfaceDark
    )
}
