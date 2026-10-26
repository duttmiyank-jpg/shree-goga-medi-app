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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyank.AdminActionGuardDialog
import com.example.miyank.GuardActionContext
import com.example.miyank.GuardDialogState
import com.example.miyank.LanguageManager
import com.example.miyank.PujaBookingItem
import com.example.miyank.PujaBookingStatus
import com.example.miyank.PujaDateOccurrence
import com.example.miyank.PujaDays
import com.example.miyank.PujaItem
import com.example.miyank.PujaManager
import com.example.miyank.PujaPricingMode
import com.example.miyank.openExternalUrl
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPujaScreen(
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val isHindi = LanguageManager.currentLanguage == com.example.miyank.Language.HINDI

    val allPujas by PujaManager.pujas.collectAsState()
    val allBookings by PujaManager.bookings.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Pujas List & Schedule, 1: Bookings

    // Add/Edit Dialog State
    var showAddDialog by remember { mutableStateOf(false) }
    var editingPujaItem by remember { mutableStateOf<PujaItem?>(null) }
    var nameHindiInput by remember { mutableStateOf("") }
    var nameEngInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }
    var availableDaysInput by remember { mutableStateOf(mutableListOf("सोमवार", "बुधवार", "शुक्रवार")) }
    var startTimeInput by remember { mutableStateOf("09:00 AM") }
    var durationInput by remember { mutableStateOf("1 घंटे") }
    var priceInput by remember { mutableStateOf("501") }
    var pricingModeInput by remember { mutableStateOf(PujaPricingMode.PER_BOOKING) }
    var isActiveInput by remember { mutableStateOf(true) }

    // Admin Postpone & Cancel Dialog States
    var postponeBookingTarget by remember { mutableStateOf<PujaBookingItem?>(null) }
    var cancelBookingTarget by remember { mutableStateOf<PujaBookingItem?>(null) }

    // Guard Dialog State
    var showGuardDialog by remember { mutableStateOf(false) }
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(SurfaceDark)) {
                TopAppBar(
                    title = {
                        Text(
                            text = if (isHindi) "पूजा प्रबंधन (Admin)" else "Manage Pujas",
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
                                editingPujaItem = null
                                nameHindiInput = ""
                                nameEngInput = ""
                                descInput = ""
                                availableDaysInput = mutableListOf("सोमवार", "बुधवार", "शुक्रवार")
                                startTimeInput = "09:00 AM"
                                durationInput = "1 घंटे"
                                priceInput = "501"
                                pricingModeInput = PujaPricingMode.PER_BOOKING
                                isActiveInput = true
                                showAddDialog = true
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Add Puja", tint = AccentGolden)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SurfaceDark,
                        titleContentColor = TextWhite,
                        navigationIconContentColor = TextWhite
                    )
                )

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
                                text = if (isHindi) "🪔 पूजा सूची (${allPujas.size})" else "Pujas (${allPujas.size})",
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
                // TAB 0: PUJAS LIST & WEEKLY SCHEDULE
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // DYNAMIC WEEKLY PUJA SCHEDULE CARD
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = if (isHindi) "📅 इस सप्ताह की पूजा सारणी" else "Weekly Puja Schedule",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AccentGolden
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                PujaDays.daysList.forEach { day ->
                                    val dayPujas = allPujas.filter { it.isActive && it.availableDays.contains(day) }
                                    if (dayPujas.isNotEmpty()) {
                                        Text(text = "• $day:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextWhite)
                                        dayPujas.forEach { puja ->
                                            Text(
                                                text = "   ${puja.startTime} — ${puja.nameHindi} (₹${puja.price.toInt()})",
                                                fontSize = 12.sp,
                                                color = TextMuted
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }
                                }
                            }
                        }
                    }

                    items(allPujas) { item ->
                        AdminPujaCard(
                            puja = item,
                            isHindi = isHindi,
                            haptic = haptic,
                            onToggleActive = {
                                PujaManager.toggleActive(item.id)
                            },
                            onEdit = {
                                editingPujaItem = item
                                nameHindiInput = item.nameHindi
                                nameEngInput = item.nameEnglish
                                descInput = item.description
                                availableDaysInput = item.availableDays.toMutableList()
                                startTimeInput = item.startTime
                                durationInput = item.duration
                                priceInput = item.price.toInt().toString()
                                pricingModeInput = item.pricingMode
                                isActiveInput = item.isActive
                                showAddDialog = true
                            },
                            onDelete = {
                                pendingDeleteId = item.id
                                showGuardDialog = true
                            }
                        )
                    }
                }
            } else {
                // TAB 1: PUJA BOOKINGS LIST
                if (allBookings.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isHindi) "कोई पूजा बुकिंग अनुरोध नहीं है।" else "No Puja bookings found.",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(allBookings) { booking ->
                            AdminPujaBookingCard(
                                booking = booking,
                                isHindi = isHindi,
                                haptic = haptic,
                                onUpdateStatus = { newStatus ->
                                    PujaManager.updateBookingStatus(booking.id, newStatus)
                                    statusMessage = when (newStatus) {
                                        PujaBookingStatus.COMPLETED -> if (isHindi) "पूजा 'संपन्न' मार्क कर दी गई! ✓" else "Marked as Completed! ✓"
                                        PujaBookingStatus.INCOMPLETE -> if (isHindi) "पूजा 'अपूर्ण (अनुपस्थित)' मार्क कर दी गई! ✓" else "Marked as Incomplete! ✓"
                                        else -> if (isHindi) "बुकिंग स्थिति अपडेट हो गई! ✓" else "Status updated! ✓"
                                    }
                                },
                                onPostpone = {
                                    postponeBookingTarget = booking
                                },
                                onCancel = {
                                    cancelBookingTarget = booking
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // ADD / EDIT PUJA DIALOG
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = if (editingPujaItem != null) (if (isHindi) "पूजा संपादित करें" else "Edit Puja") else (if (isHindi) "नई पूजा जोड़ें" else "Add New Puja"),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextWhite
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = nameHindiInput,
                        onValueChange = { nameHindiInput = it },
                        label = { Text(if (isHindi) "पूजा नाम (हिंदी) *" else "Puja Name (Hindi) *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = nameEngInput,
                        onValueChange = { nameEngInput = it },
                        label = { Text(if (isHindi) "पूजा नाम (अंग्रेज़ी)" else "Puja Name (English)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        label = { Text(if (isHindi) "पूजा विवरण" else "Description") },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(if (isHindi) "उपलब्ध दिन चुनें *" else "Select Available Days *", fontSize = 12.sp, color = AccentGolden, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))

                    PujaDays.daysList.forEach { day ->
                        val isChecked = availableDaysInput.contains(day)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    val updated = availableDaysInput.toMutableList()
                                    if (checked) updated.add(day) else updated.remove(day)
                                    availableDaysInput = updated
                                }
                            )
                            Text(text = day, fontSize = 13.sp, color = TextWhite)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startTimeInput,
                            onValueChange = { startTimeInput = it },
                            label = { Text(if (isHindi) "प्रारंभ समय" else "Start Time") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )

                        OutlinedTextField(
                            value = durationInput,
                            onValueChange = { durationInput = it },
                            label = { Text(if (isHindi) "अवधि" else "Duration") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = priceInput,
                            onValueChange = { priceInput = it.filter { c -> c.isDigit() } },
                            label = { Text(if (isHindi) "पूजा राशि (₹) *" else "Price (₹) *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (isHindi) "मूल्य प्रकार" else "Pricing Mode", fontSize = 11.sp, color = AccentGolden)
                            Spacer(modifier = Modifier.height(4.dp))
                            FilterChip(
                                selected = pricingModeInput == PujaPricingMode.PER_BOOKING,
                                onClick = { pricingModeInput = if (pricingModeInput == PujaPricingMode.PER_BOOKING) PujaPricingMode.PER_PERSON else PujaPricingMode.PER_BOOKING },
                                label = { Text(if (pricingModeInput == PujaPricingMode.PER_BOOKING) "प्रति बुकिंग" else "प्रति व्यक्ति", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentGolden, containerColor = BackgroundDark)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isHindi) "सक्रिय रखें (Active)" else "Active Status", color = TextWhite, fontSize = 13.sp)
                        Switch(
                            checked = isActiveInput,
                            onCheckedChange = { isActiveInput = it }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameHindiInput.trim().isNotEmpty()) {
                            haptic.playPremiumHaptic()
                            val newItem = PujaItem(
                                id = editingPujaItem?.id ?: "",
                                nameHindi = nameHindiInput.trim(),
                                nameEnglish = nameEngInput.trim(),
                                description = descInput.trim(),
                                availableDays = availableDaysInput.toList(),
                                startTime = startTimeInput.trim(),
                                duration = durationInput.trim(),
                                price = priceInput.toDoubleOrNull() ?: 501.0,
                                pricingMode = pricingModeInput,
                                isActive = isActiveInput,
                                sortOrder = editingPujaItem?.sortOrder ?: 0,
                                createdAt = editingPujaItem?.createdAt ?: 0L
                            )
                            PujaManager.addOrUpdatePuja(newItem)
                            showAddDialog = false
                            statusMessage = if (isHindi) "पूजा सफलतापूर्वक सेव हो गई! ✓" else "Puja saved successfully! ✓"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                ) {
                    Text(if (isHindi) "सेव करें" else "Save", color = BackgroundDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(if (isHindi) "रद्द करें" else "Cancel")
                }
            },
            containerColor = SurfaceDark
        )
    }

    // ADMIN POSTPONE PUJA DIALOG
    if (postponeBookingTarget != null) {
        val targetBooking = postponeBookingTarget!!
        val matchingPuja = allPujas.find { it.id == targetBooking.pujaId } ?: allPujas.firstOrNull()
        val availableOccurrences = remember(matchingPuja) {
            if (matchingPuja != null) PujaManager.getUpcomingOccurrencesForDays(matchingPuja.availableDays, 4) else emptyList()
        }

        var selectedOccurrence by remember { mutableStateOf(availableOccurrences.firstOrNull() ?: PujaDateOccurrence("सोमवार", "2026-09-28", "28 Sep 2026 (सोमवार)", "दशमी", "सप्ताह 2")) }
        var postponeReasonInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { postponeBookingTarget = null },
            title = {
                Text(
                    text = if (isHindi) "⏳ पूजा स्थगित/आगे बढ़ाएं (Postpone)" else "Postpone Puja",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentGolden
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    Text(text = "पूजा: ${targetBooking.pujaName}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextWhite)
                    Text(text = "भक्त: ${targetBooking.devoteeName}", fontSize = 12.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(if (isHindi) "नई आगामी तिथि चुनें *" else "Select New Future Date *", fontSize = 11.sp, color = AccentGolden, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableOccurrences.forEach { occ ->
                            val isSelected = selectedOccurrence.dateStr == occ.dateStr
                            Surface(
                                modifier = Modifier.clickable { selectedOccurrence = occ },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) AccentGolden else BackgroundDark,
                                border = BorderStroke(1.dp, if (isSelected) AccentGolden else WarmGold.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = occ.displayDateStr,
                                    fontSize = 11.sp,
                                    color = if (isSelected) BackgroundDark else TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = postponeReasonInput,
                        onValueChange = { postponeReasonInput = it },
                        label = { Text(if (isHindi) "स्थगित करने का कारण (Postpone Reason)" else "Postpone Reason") },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.playPremiumHaptic()
                        PujaManager.postponeBooking(
                            bookingId = targetBooking.id,
                            newDateStr = selectedOccurrence.dateStr,
                            newDisplayDateStr = selectedOccurrence.displayDateStr,
                            newTime = matchingPuja?.startTime ?: targetBooking.bookingTime,
                            reason = postponeReasonInput.trim()
                        )
                        postponeBookingTarget = null
                        statusMessage = if (isHindi) "पूजा सफलतापूर्वक स्थगित हो गई! ✓" else "Puja postponed successfully! ✓"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                ) {
                    Text(if (isHindi) "स्थगित करें" else "Postpone", color = BackgroundDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { postponeBookingTarget = null }) {
                    Text(if (isHindi) "रद्द करें" else "Cancel")
                }
            },
            containerColor = SurfaceDark
        )
    }

    // ADMIN CANCEL PUJA DIALOG
    if (cancelBookingTarget != null) {
        val targetBooking = cancelBookingTarget!!
        var cancelReasonInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { cancelBookingTarget = null },
            title = {
                Text(
                    text = if (isHindi) "❌ पूजा रद्द करें (Admin)" else "Cancel Puja Booking",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Red
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "पूजा: ${targetBooking.pujaName}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextWhite)
                    Text(text = "भक्त: ${targetBooking.devoteeName} (${targetBooking.mobileNumber})", fontSize = 12.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = cancelReasonInput,
                        onValueChange = { cancelReasonInput = it },
                        label = { Text(if (isHindi) "रद्दीकरण का कारण *" else "Cancellation Reason *") },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Red, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.playPremiumHaptic()
                        PujaManager.cancelBooking(
                            bookingId = targetBooking.id,
                            cancelledBy = "ADMIN",
                            reason = cancelReasonInput.trim()
                        )
                        cancelBookingTarget = null
                        statusMessage = if (isHindi) "पूजा सफलतापूर्वक रद्द कर दी गई! ✓" else "Puja cancelled! ✓"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(if (isHindi) "रद्द करें" else "Cancel Puja", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { cancelBookingTarget = null }) {
                    Text(if (isHindi) "वापस जाएँ" else "Close")
                }
            },
            containerColor = SurfaceDark
        )
    }

    // DELETE GUARD DIALOG
    AdminActionGuardDialog(
        state = GuardDialogState(
            isVisible = showGuardDialog,
            context = GuardActionContext.DELETE_ITEM,
            title = if (isHindi) "पूजा हटाने की पुष्टि" else "Confirm Puja Deletion",
            message = if (isHindi) "क्या आप वाकई में इस पूजा को सूची से हटाना चाहते हैं?" else "Are you sure you want to delete this Puja?",
            onConfirm = {
                showGuardDialog = false
                if (pendingDeleteId != null) {
                    PujaManager.deletePuja(pendingDeleteId!!)
                    pendingDeleteId = null
                    statusMessage = if (isHindi) "सफलतापूर्वक हटा दिया गया! ✓" else "Deleted successfully! ✓"
                }
            },
            onDismiss = { showGuardDialog = false }
        ),
        isHindi = isHindi
    )
}

@Composable
private fun AdminPujaCard(
    puja: PujaItem,
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
                Text(
                    text = puja.nameHindi.ifEmpty { puja.nameEnglish },
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = TextWhite,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "₹${puja.price.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccentGolden)
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = puja.isActive,
                        onCheckedChange = {
                            haptic.playPremiumHaptic()
                            onToggleActive()
                        }
                    )
                }
            }

            Text(text = "समय: ${puja.startTime} (${puja.duration})", fontSize = 12.sp, color = AccentGolden, fontWeight = FontWeight.Bold)
            Text(text = "दिन: ${puja.availableDays.joinToString(", ")}", fontSize = 11.sp, color = TextMuted)

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
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

@Composable
private fun AdminPujaBookingCard(
    booking: PujaBookingItem,
    isHindi: Boolean,
    haptic: com.example.miyank.HapticManager,
    onUpdateStatus: (PujaBookingStatus) -> Unit,
    onPostpone: () -> Unit,
    onCancel: () -> Unit
) {
    val statusText = when (booking.status) {
        PujaBookingStatus.COMPLETED -> "✅ संपन्न (COMPLETED)"
        PujaBookingStatus.INCOMPLETE -> "🔴 अपूर्ण (INCOMPLETE)"
        PujaBookingStatus.POSTPONED -> "⏳ स्थगित (POSTPONED)"
        PujaBookingStatus.CONFIRMED -> "✓ पुष्टि (CONFIRMED)"
        PujaBookingStatus.CANCELLED -> "❌ रद्द (CANCELLED)"
        PujaBookingStatus.PENDING -> "⏳ लंबित (PENDING)"
    }

    val statusColor = when (booking.status) {
        PujaBookingStatus.COMPLETED, PujaBookingStatus.CONFIRMED -> Color(0xFF81C784)
        PujaBookingStatus.POSTPONED -> AccentGolden
        PujaBookingStatus.INCOMPLETE, PujaBookingStatus.CANCELLED -> Color.Red
        PujaBookingStatus.PENDING -> AccentGolden
    }

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
                Text(text = booking.pujaName, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = AccentGolden)

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "भक्त: ${booking.devoteeName} (${booking.mobileNumber})", fontSize = 13.sp, color = TextWhite)
            Text(text = "दिनांक: ${booking.displayDateStr} • समय: ${booking.bookingTime}", fontSize = 12.sp, color = TextMuted)
            Text(text = "भुगतान: ₹${booking.amountCharged.toInt()} (Paid ✓)", fontSize = 12.sp, color = AccentGolden, fontWeight = FontWeight.Bold)

            if (booking.status == PujaBookingStatus.POSTPONED && booking.postponedReason.isNotEmpty()) {
                Text(text = "स्थगित कारण: ${booking.postponedReason}", fontSize = 11.sp, color = AccentGolden)
            }

            if (booking.status == PujaBookingStatus.CANCELLED && booking.cancellationReason.isNotEmpty()) {
                Text(text = "रद्दीकरण कारण: ${booking.cancellationReason}", fontSize = 11.sp, color = Color.Red)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = {
                    haptic.playPremiumHaptic()
                    val cleanPhone = booking.mobileNumber.replace(" ", "").replace("-", "")
                    openExternalUrl("tel:$cleanPhone")
                }) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = AccentGolden, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isHindi) "कॉल करें" else "Call", color = AccentGolden, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (booking.status == PujaBookingStatus.CONFIRMED || booking.status == PujaBookingStatus.PENDING || booking.status == PujaBookingStatus.POSTPONED) {
                        OutlinedButton(
                            onClick = onPostpone,
                            border = BorderStroke(1.dp, AccentGolden),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = AccentGolden, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(if (isHindi) "स्थगित" else "Postpone", fontSize = 10.sp, color = AccentGolden, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onUpdateStatus(PujaBookingStatus.COMPLETED) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(if (isHindi) "संपन्न" else "Completed", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        IconButton(onClick = onCancel, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Cancel, contentDescription = "Cancel", tint = Color.Red, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}
