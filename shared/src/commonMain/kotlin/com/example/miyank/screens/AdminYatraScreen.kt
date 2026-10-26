package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.miyank.AdminActionGuardDialog
import com.example.miyank.AdminSaveButton
import com.example.miyank.BusLayoutType
import com.example.miyank.GuardActionContext
import com.example.miyank.GuardDialogState
import com.example.miyank.LanguageManager
import com.example.miyank.SeatStatus
import com.example.miyank.YatraBooking
import com.example.miyank.YatraBus
import com.example.miyank.YatraItem
import com.example.miyank.YatraManager
import com.example.miyank.YatraSeat
import com.example.miyank.getEpochMillis
import com.example.miyank.getTodayDateString
import com.example.miyank.openExternalUrl
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminYatraScreen(
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val isHindi = LanguageManager.currentLanguage == com.example.miyank.Language.HINDI

    val allYatras by YatraManager.yatras.collectAsState()
    val allBuses by YatraManager.buses.collectAsState()
    val allSeats by YatraManager.seats.collectAsState()
    val allBookings by YatraManager.bookings.collectAsState()

    val today = getTodayDateString()

    // Dashboard Analytics Calculations
    val activeYatrasCount = remember(allYatras) { allYatras.count { it.isActive } }
    val totalBusesCount = allBuses.size
    val totalSeatsCount = remember(allBuses) { allBuses.sumOf { it.totalSeats } }
    val bookedSeatsCount = remember(allSeats) { allSeats.count { it.status == SeatStatus.BOOKED } }
    val heldSeatsCount = remember(allSeats) { allSeats.count { it.status == SeatStatus.HELD } }
    val blockedSeatsCount = remember(allSeats) { allSeats.count { it.status == SeatStatus.BLOCKED } }
    val availableSeatsCount = remember(allSeats) { allSeats.count { it.status == SeatStatus.AVAILABLE } }

    val cancelledBookingsCount = remember(allBookings) { allBookings.count { it.bookingStatus == com.example.miyank.YatraBookingStatus.CANCELLED } }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Yatras, 1: Bookings, 2: Analytics

    // Yatra Form States
    var showYatraDialog by remember { mutableStateOf(false) }
    var editingYatraItem by remember { mutableStateOf<YatraItem?>(null) }
    var titleHindiInput by remember { mutableStateOf("") }
    var titleEngInput by remember { mutableStateOf("") }
    var destInput by remember { mutableStateOf("") }
    var depDateInput by remember { mutableStateOf("2026-10-20") }
    var depTimeInput by remember { mutableStateOf("सुबह 05:00 बजे") }
    var priceInput by remember { mutableStateOf("1500") }
    var descInput by remember { mutableStateOf("") }
    var imageUrlInput by remember { mutableStateOf("") }
    var isActiveInput by remember { mutableStateOf(true) }

    // Bus Form States
    var selectedYatraForBus by remember(allYatras) { mutableStateOf(allYatras.firstOrNull()) }
    var showBusDialog by remember { mutableStateOf(false) }
    var editingBusItem by remember { mutableStateOf<YatraBus?>(null) }
    var busNameInput by remember { mutableStateOf("बस 01 (दरबार रथ)") }
    var busNumInput by remember { mutableStateOf("HP 17 2026") }
    var driverNameInput by remember { mutableStateOf("") }
    var driverPhoneInput by remember { mutableStateOf("") }
    var conductorNameInput by remember { mutableStateOf("") }
    var conductorPhoneInput by remember { mutableStateOf("") }
    var totalSeatsInput by remember { mutableStateOf("52") }
    var layoutTypeInput by remember { mutableStateOf(BusLayoutType.TWO_BY_TWO) }

    // Selected Seat Inspection Sheet State
    var activeBusForSeatMap by remember(allBuses, selectedYatraForBus) {
        mutableStateOf(allBuses.find { it.yatraId == selectedYatraForBus?.id } ?: allBuses.firstOrNull())
    }
    var selectedSeatForAdminSheet by remember { mutableStateOf<YatraSeat?>(null) }

    // Guard Dialog State
    var showGuardDialog by remember { mutableStateOf(false) }
    var pendingDeleteYatraId by remember { mutableStateOf<String?>(null) }
    var pendingDeleteBusId by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(SurfaceDark)) {
                TopAppBar(
                    title = {
                        Text(
                            text = if (isHindi) "यात्रा व बस प्रबंधन (Admin)" else "Manage Yatras & Buses",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite
                        )
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
                    actions = {
                        if (selectedTab == 0) {
                            IconButton(onClick = {
                                editingYatraItem = null
                                titleHindiInput = ""
                                titleEngInput = ""
                                destInput = "राजस्थान"
                                depDateInput = today
                                depTimeInput = "सुबह 05:00 बजे"
                                priceInput = "1500"
                                descInput = ""
                                imageUrlInput = ""
                                isActiveInput = true
                                showYatraDialog = true
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Add Yatra", tint = AccentGolden)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SurfaceDark,
                        titleContentColor = TextWhite,
                        navigationIconContentColor = TextWhite
                    )
                )

                // 3 DEDICATED TABS: YATRAS, BOOKINGS, ANALYTICS
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SurfaceDark,
                    contentColor = AccentGolden,
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            haptic.playPremiumHaptic()
                            selectedTab = 0
                        },
                        text = {
                            Text(
                                text = if (isHindi) "🚩 यात्राएं (${allYatras.size})" else "Yatras (${allYatras.size})",
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == 0) AccentGolden else TextMuted
                            )
                        }
                    )

                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            haptic.playPremiumHaptic()
                            selectedTab = 1
                        },
                        text = {
                            Text(
                                text = if (isHindi) "📋 बुकिंग्स (${allBookings.size})" else "Bookings (${allBookings.size})",
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == 1) AccentGolden else TextMuted
                            )
                        }
                    )

                    Tab(
                        selected = selectedTab == 2,
                        onClick = {
                            haptic.playPremiumHaptic()
                            selectedTab = 2
                        },
                        text = {
                            Text(
                                text = if (isHindi) "📊 एनालिटिक्स" else "Analytics",
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == 2) AccentGolden else TextMuted
                            )
                        }
                    )
                }
            }
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (statusMessage != null) {
                Surface(
                    color = AccentGolden.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, AccentGolden.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text(
                        text = statusMessage!!,
                        color = TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            if (selectedTab == 0) {
                // TAB 0: YATRAS & BUS MANAGEMENT
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(allYatras) { item ->
                        AdminYatraCard(
                            yatra = item,
                            isHindi = isHindi,
                            haptic = haptic,
                            onToggleActive = { YatraManager.toggleYatraActive(item.id) },
                            onEdit = {
                                editingYatraItem = item
                                titleHindiInput = item.title
                                titleEngInput = item.titleEnglish
                                destInput = item.destination
                                depDateInput = item.departureDateStr
                                depTimeInput = item.departureTime
                                priceInput = item.price.toInt().toString()
                                descInput = item.description
                                imageUrlInput = item.imageUrl
                                isActiveInput = item.isActive
                                showYatraDialog = true
                            },
                            onDelete = {
                                pendingDeleteYatraId = item.id
                                showGuardDialog = true
                            }
                        )
                    }
                }
            } else if (selectedTab == 1) {
                // TAB 1: YATRA BOOKINGS LIST
                if (allBookings.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = if (isHindi) "कोई यात्रा सीट बुकिंग नहीं है।" else "No Yatra bookings found.", color = TextMuted, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(allBookings) { booking ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = booking.yatraTitle, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = AccentGolden)
                                        Surface(
                                            color = Color(0xFF1B5E20).copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(text = if (booking.isAdminBooking) "Admin Booking ✓" else "Paid ✓", color = Color(0xFF81C784), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "यात्री: ${booking.passengerName} (${booking.mobileNumber})", fontSize = 13.sp, color = TextWhite)
                                    Text(text = "बस: ${booking.busName} (${booking.busNumber})", fontSize = 12.sp, color = TextMuted)
                                    Text(text = "सीट संख्या: ${booking.seatNumbers.joinToString(", ")} • ₹${booking.amountCharged.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentGolden)

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        TextButton(onClick = {
                                            haptic.playPremiumHaptic()
                                            val cleanPhone = booking.mobileNumber.replace(" ", "").replace("-", "")
                                            openExternalUrl("tel:$cleanPhone")
                                        }) {
                                            Icon(Icons.Default.Phone, contentDescription = null, tint = AccentGolden, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isHindi) "कॉल करें" else "Call", color = AccentGolden, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // TAB 2: YATRA BOOKING ANALYTICS DASHBOARD
                val analyticsScrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(analyticsScrollState)
                        .padding(16.dp)
                ) {
                    // OVERALL YATRA STATS GRID
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = if (isHindi) "📊 लाइव सीट एवं बुकिंग एनालिटिक्स" else "📊 Live Seat & Booking Analytics", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = AccentGolden)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                AdminSummaryStatItem(if (isHindi) "कुल यात्राएं" else "Total Yatras", "${allYatras.size}")
                                AdminSummaryStatItem(if (isHindi) "कुल बसें" else "Buses", "$totalBusesCount")
                                AdminSummaryStatItem(if (isHindi) "कुल सीटें" else "Seats", "$totalSeatsCount")
                                AdminSummaryStatItem(if (isHindi) "बुक सीटें" else "Booked", "$bookedSeatsCount")
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                AdminSummaryStatItem(if (isHindi) "होल्ड सीटें" else "Held", "$heldSeatsCount")
                                AdminSummaryStatItem(if (isHindi) "ब्लॉक सीटें" else "Blocked", "$blockedSeatsCount")
                                AdminSummaryStatItem(if (isHindi) "उपलब्ध सीटें" else "Available", "$availableSeatsCount")
                                AdminSummaryStatItem(if (isHindi) "रद्द बुकिंग्स" else "Cancelled", "$cancelledBookingsCount")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // INDIVIDUAL YATRA ANALYTICS CARDS (Yatra 1, Yatra 2, etc.)
                    Text(text = if (isHindi) "🚩 यात्रा-वार सीट एनालिटिक्स एवं ऑक्यूपेंसी" else "Yatra-wise Analytics & Occupancy", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = AccentGolden)
                    Spacer(modifier = Modifier.height(10.dp))

                    allYatras.forEach { yatra ->
                        val yatraBuses = allBuses.filter { it.yatraId == yatra.id }
                        val yatraBusIds = yatraBuses.map { it.id }.toSet()
                        val yatraSeats = allSeats.filter { yatraBusIds.contains(it.busId) }

                        val totalSeatsYatra = yatraBuses.sumOf { it.totalSeats }
                        val bookedYatra = yatraSeats.count { it.status == SeatStatus.BOOKED }
                        val heldYatra = yatraSeats.count { it.status == SeatStatus.HELD }
                        val blockedYatra = yatraSeats.count { it.status == SeatStatus.BLOCKED }
                        val availYatra = yatraSeats.count { it.status == SeatStatus.AVAILABLE }
                        val yatraOccupancyPct = if (totalSeatsYatra > 0) (bookedYatra * 100) / totalSeatsYatra else 0

                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isHindi || yatra.titleEnglish.isEmpty()) yatra.title else yatra.titleEnglish,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = AccentGolden,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Surface(
                                        color = if (yatraOccupancyPct >= 100) Color.Red.copy(alpha = 0.2f) else AccentGolden.copy(alpha = 0.18f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "$yatraOccupancyPct% FULL",
                                            color = if (yatraOccupancyPct >= 100) Color.Red else AccentGolden,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = (if (isHindi) "गंतव्य: " else "Destination: ") + yatra.destination + " • " + yatra.displayDateStr, fontSize = 11.sp, color = TextMuted)

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    AdminSummaryStatItem(if (isHindi) "बसें" else "Buses", "${yatraBuses.size}")
                                    AdminSummaryStatItem(if (isHindi) "कुल सीटें" else "Seats", "$totalSeatsYatra")
                                    AdminSummaryStatItem(if (isHindi) "बुक" else "Booked", "$bookedYatra")
                                    AdminSummaryStatItem(if (isHindi) "होल्ड" else "Held", "$heldYatra")
                                    AdminSummaryStatItem(if (isHindi) "उपलब्ध" else "Available", "$availYatra")
                                }

                                if (yatraBuses.isNotEmpty()) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = WarmGold.copy(alpha = 0.2f))

                                    Text(text = if (isHindi) "बस-वार ऑक्यूपेंसी विवरण:" else "Bus Occupancy Breakdown:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                    Spacer(modifier = Modifier.height(6.dp))

                                    yatraBuses.forEach { bus ->
                                        val bSeats = yatraSeats.filter { it.busId == bus.id }
                                        val bBooked = bSeats.count { it.status == SeatStatus.BOOKED }
                                        val bAvail = bSeats.count { it.status == SeatStatus.AVAILABLE }
                                        val bHeld = bSeats.count { it.status == SeatStatus.HELD }
                                        val bPct = if (bus.totalSeats > 0) (bBooked * 100) / bus.totalSeats else 0

                                        Surface(
                                            color = BackgroundDark,
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.15f)),
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(text = "${bus.busName} (${bus.busNumber})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                                    Text(text = (if (isHindi) "चालक: " else "Driver: ") + bus.driverName.ifEmpty { "N/A" }, fontSize = 10.sp, color = TextMuted)
                                                }
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text(text = "$bBooked/${bus.totalSeats} " + (if (isHindi) "सीटें बुक" else "Seats Booked") + " ($bPct%)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentGolden)
                                                    Text(text = (if (isHindi) "उपलब्ध: " else "Avail: ") + "$bAvail • " + (if (isHindi) "होल्ड: " else "Held: ") + "$bHeld", fontSize = 10.sp, color = TextMuted)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // ADMIN SEAT DETAILS INSPECTION SHEET / DIALOG
    if (selectedSeatForAdminSheet != null && activeBusForSeatMap != null) {
        val seat = selectedSeatForAdminSheet!!
        val bus = activeBusForSeatMap!!
        val relatedBooking = remember(allBookings, seat.bookingId) {
            allBookings.find { it.bookingId == seat.bookingId }
        }

        var adminPassengerName by remember { mutableStateOf("") }
        var adminPassengerMobile by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { selectedSeatForAdminSheet = null },
            title = {
                Text(
                    text = (if (isHindi) "सीट विवरण: " else "Seat Details: ") + "${seat.seatNumber} (${bus.busName})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentGolden
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    Text(text = (if (isHindi) "स्थिति: " else "Status: ") + seat.status.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextWhite)

                    if (seat.status == SeatStatus.BOOKED && relatedBooking != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = (if (isHindi) "यात्री: " else "Passenger: ") + relatedBooking.passengerName, fontSize = 12.sp, color = TextWhite)
                        Text(text = (if (isHindi) "फ़ोन: " else "Mobile: ") + relatedBooking.mobileNumber, fontSize = 12.sp, color = TextWhite)
                        Text(text = "Booking ID: ${relatedBooking.bookingId}", fontSize = 11.sp, color = TextMuted)
                        Text(text = (if (isHindi) "भुगतान: " else "Payment: ") + if (relatedBooking.isAdminBooking) "Admin Booking (Not Required)" else "Paid ✓", fontSize = 11.sp, color = Color(0xFF81C784))
                    } else if (seat.status == SeatStatus.HELD) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = (if (isHindi) "होल्ड भक्त: " else "Held By: ") + seat.heldByName.ifEmpty { "Devotee" } + " (${seat.heldByMobile})", fontSize = 12.sp, color = TextWhite)
                    } else if (seat.status == SeatStatus.BLOCKED) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = (if (isHindi) "कारण: " else "Reason: ") + seat.blockedReason.ifEmpty { "Staff Reserved" }, fontSize = 12.sp, color = Color.Red)
                    } else if (seat.status == SeatStatus.AVAILABLE) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = if (isHindi) "निःशुल्क एडमिन मैनुअल बुकिंग:" else "Admin Manual Booking (No Payment):", fontSize = 11.sp, color = AccentGolden, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = adminPassengerName,
                            onValueChange = { adminPassengerName = it },
                            label = { Text(if (isHindi) "यात्री नाम *" else "Passenger Name *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = adminPassengerMobile,
                            onValueChange = { adminPassengerMobile = it },
                            label = { Text(if (isHindi) "मोबाइल नंबर *" else "Mobile Number *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (seat.status == SeatStatus.AVAILABLE) {
                        Button(
                            onClick = {
                                if (adminPassengerName.trim().isNotEmpty()) {
                                    val newBooking = YatraBooking(
                                        yatraId = bus.yatraId,
                                        yatraTitle = selectedYatraForBus?.title ?: "",
                                        busId = bus.id,
                                        busName = bus.busName,
                                        busNumber = bus.busNumber,
                                        passengerName = adminPassengerName.trim(),
                                        mobileNumber = adminPassengerMobile.trim(),
                                        seatNumbers = listOf(seat.seatNumber),
                                        passengerCount = 1,
                                        amountCharged = 0.0,
                                        isAdminBooking = true
                                    )
                                    YatraManager.bookYatraSeats(newBooking, isAdminBooking = true)
                                    selectedSeatForAdminSheet = null
                                    statusMessage = if (isHindi) "एडमिन मैनुअल सीट बुकिंग सफल! ✓" else "Admin Manual Booking Confirmed! ✓"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                        ) {
                            Text(if (isHindi) "सीट बुक करें" else "Book Seat", color = BackgroundDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                YatraManager.toggleSeatBlock(bus.id, seat.seatNumber)
                                selectedSeatForAdminSheet = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                        ) {
                            Text(if (isHindi) "ब्लॉक करें" else "Block Seat", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    } else if (seat.status == SeatStatus.BLOCKED) {
                        Button(
                            onClick = {
                                YatraManager.toggleSeatBlock(bus.id, seat.seatNumber)
                                selectedSeatForAdminSheet = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
                        ) {
                            Text(if (isHindi) "अनब्लॉक करें" else "Unblock Seat", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    } else if (seat.status == SeatStatus.HELD) {
                        Button(
                            onClick = {
                                YatraManager.releaseSeatHold(bus.id, seat.seatNumber)
                                selectedSeatForAdminSheet = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                        ) {
                            Text(if (isHindi) "होल्ड हटाएँ" else "Release Hold", color = BackgroundDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedSeatForAdminSheet = null }) { Text(if (isHindi) "बंद करें" else "Close") }
            },
            containerColor = SurfaceDark
        )
    }

    // ADD / EDIT YATRA DIALOG
    if (showYatraDialog) {
        AlertDialog(
            onDismissRequest = { showYatraDialog = false },
            title = { Text(if (editingYatraItem != null) (if (isHindi) "यात्रा संपादित करें" else "Edit Yatra") else (if (isHindi) "नई यात्रा जोड़ें" else "Add New Yatra"), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = titleHindiInput,
                        onValueChange = { titleHindiInput = it },
                        label = { Text(if (isHindi) "यात्रा शीर्षक (हिंदी) *" else "Yatra Title (Hindi) *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = destInput,
                        onValueChange = { destInput = it },
                        label = { Text(if (isHindi) "गंतव्य (Destination) *" else "Destination *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = depDateInput,
                            onValueChange = { depDateInput = it },
                            label = { Text("Date (YYYY-MM-DD)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )
                        OutlinedTextField(
                            value = priceInput,
                            onValueChange = { priceInput = it.filter { c -> c.isDigit() } },
                            label = { Text(if (isHindi) "सेवा राशि (₹)" else "Fee (₹)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        label = { Text(if (isHindi) "विवरण" else "Description") },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (titleHindiInput.trim().isNotEmpty()) {
                            haptic.playPremiumHaptic()
                            val item = YatraItem(
                                id = editingYatraItem?.id ?: "",
                                title = titleHindiInput.trim(),
                                titleEnglish = titleEngInput.trim(),
                                destination = destInput.trim(),
                                departureDateStr = depDateInput.trim(),
                                displayDateStr = "${depDateInput.trim()}, ${if (isHindi) "प्रस्थान" else "Departure"}",
                                departureTime = depTimeInput.trim(),
                                price = priceInput.toDoubleOrNull() ?: 1500.0,
                                description = descInput.trim(),
                                imageUrl = imageUrlInput.trim(),
                                isActive = isActiveInput,
                                sortOrder = editingYatraItem?.sortOrder ?: 0,
                                createdAt = editingYatraItem?.createdAt ?: 0L
                            )
                            YatraManager.addOrUpdateYatra(item)
                            showYatraDialog = false
                            statusMessage = if (isHindi) "यात्रा सफलतापूर्वक सेव हो गई! ✓" else "Yatra saved successfully! ✓"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                ) {
                    Text(if (isHindi) "सेव करें" else "Save", color = BackgroundDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showYatraDialog = false }) { Text(if (isHindi) "रद्द करें" else "Cancel") }
            },
            containerColor = SurfaceDark
        )
    }

    // ADD / EDIT BUS DIALOG
    if (showBusDialog && selectedYatraForBus != null) {
        AlertDialog(
            onDismissRequest = { showBusDialog = false },
            title = { Text(if (isHindi) "बस जोड़ें / संपादित करें" else "Add / Edit Bus", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = busNameInput,
                        onValueChange = { busNameInput = it },
                        label = { Text(if (isHindi) "बस का नाम (e.g. बस 01)" else "Bus Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = busNumInput,
                        onValueChange = { busNumInput = it },
                        label = { Text(if (isHindi) "बस नंबर (e.g. HP 17 2026)" else "Bus Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = driverNameInput,
                            onValueChange = { driverNameInput = it },
                            label = { Text(if (isHindi) "चालक नाम" else "Driver Name") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )
                        OutlinedTextField(
                            value = totalSeatsInput,
                            onValueChange = { totalSeatsInput = it.filter { c -> c.isDigit() } },
                            label = { Text(if (isHindi) "कुल सीटें (52)" else "Total Seats") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(if (isHindi) "सीट लेआउट प्रकार:" else "Layout Type:", fontSize = 11.sp, color = AccentGolden)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = layoutTypeInput == BusLayoutType.TWO_BY_TWO,
                            onClick = { layoutTypeInput = BusLayoutType.TWO_BY_TWO },
                            label = { Text("2 × 2", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentGolden, containerColor = BackgroundDark)
                        )
                        FilterChip(
                            selected = layoutTypeInput == BusLayoutType.TWO_BY_THREE,
                            onClick = { layoutTypeInput = BusLayoutType.TWO_BY_THREE },
                            label = { Text("2 × 3", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentGolden, containerColor = BackgroundDark)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (busNameInput.trim().isNotEmpty()) {
                            haptic.playPremiumHaptic()
                            val bus = YatraBus(
                                id = editingBusItem?.id ?: "",
                                yatraId = selectedYatraForBus!!.id,
                                busName = busNameInput.trim(),
                                busNumber = busNumInput.trim(),
                                driverName = driverNameInput.trim(),
                                driverPhone = driverPhoneInput.trim(),
                                conductorName = conductorNameInput.trim(),
                                conductorPhone = conductorPhoneInput.trim(),
                                totalSeats = totalSeatsInput.toIntOrNull() ?: 52,
                                layoutType = layoutTypeInput
                            )
                            YatraManager.addOrUpdateBus(bus)
                            showBusDialog = false
                            statusMessage = if (isHindi) "बस एवं सीटें सफलतापूर्वक जोड़ी गईं! ✓" else "Bus and seats saved successfully! ✓"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                ) {
                    Text(if (isHindi) "सेव करें" else "Save", color = BackgroundDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBusDialog = false }) { Text(if (isHindi) "रद्द करें" else "Cancel") }
            },
            containerColor = SurfaceDark
        )
    }

    // DELETE GUARD DIALOG
    AdminActionGuardDialog(
        state = GuardDialogState(
            isVisible = showGuardDialog,
            context = GuardActionContext.DELETE_ITEM,
            title = if (isHindi) "हटाने की पुष्टि" else "Confirm Deletion",
            message = if (pendingDeleteBusId != null) (if (isHindi) "क्या आप वाकई इस बस और उसकी सीटों को हटाना चाहते हैं?" else "Are you sure you want to delete this bus?")
                      else (if (isHindi) "क्या आप वाकई इस यात्रा को हटाना चाहते हैं?" else "Are you sure you want to delete this yatra?"),
            onConfirm = {
                showGuardDialog = false
                if (pendingDeleteYatraId != null) {
                    YatraManager.deleteYatra(pendingDeleteYatraId!!)
                    pendingDeleteYatraId = null
                    statusMessage = if (isHindi) "यात्रा सफलतापूर्वक हटा दी गई! ✓" else "Yatra deleted successfully! ✓"
                } else if (pendingDeleteBusId != null) {
                    YatraManager.deleteBus(pendingDeleteBusId!!)
                    pendingDeleteBusId = null
                    statusMessage = if (isHindi) "बस सफलतापूर्वक हटा दी गई! ✓" else "Bus deleted successfully! ✓"
                }
            },
            onDismiss = { showGuardDialog = false }
        ),
        isHindi = isHindi
    )
}

@Composable
private fun AdminSummaryStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = AccentGolden)
        Text(text = label, fontSize = 10.sp, color = TextMuted)
    }
}

@Composable
private fun AdminYatraCard(
    yatra: YatraItem,
    isHindi: Boolean,
    haptic: com.example.miyank.HapticManager,
    onToggleActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = if (isHindi || yatra.titleEnglish.isEmpty()) yatra.title else yatra.titleEnglish, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = TextWhite, modifier = Modifier.weight(1f))
                Switch(
                    checked = yatra.isActive,
                    onCheckedChange = {
                        haptic.playPremiumHaptic()
                        onToggleActive()
                    }
                )
            }

            Text(text = (if (isHindi) "गंतव्य: " else "Destination: ") + yatra.destination + " • ₹${yatra.price.toInt()}", fontSize = 12.sp, color = AccentGolden, fontWeight = FontWeight.Bold)
            Text(text = (if (isHindi) "प्रस्थान: " else "Departure: ") + yatra.displayDateStr + " (" + yatra.departureTime + ")", fontSize = 11.sp, color = TextMuted)

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AccentGolden, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
