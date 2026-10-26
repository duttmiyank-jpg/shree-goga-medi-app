package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Warning
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
import com.example.miyank.LanguageManager
import com.example.miyank.PujaBookingItem
import com.example.miyank.PujaBookingStatus
import com.example.miyank.PujaDateOccurrence
import com.example.miyank.PujaItem
import com.example.miyank.PujaManager
import com.example.miyank.PujaPricingMode
import com.example.miyank.getEpochMillis
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PujaScreen(
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val scrollState = rememberScrollState()
    val isHindi = LanguageManager.currentLanguage == com.example.miyank.Language.HINDI

    val allPujas by PujaManager.pujas.collectAsState()
    val activePujas = remember(allPujas) {
        allPujas.filter { it.isActive }.sortedBy { it.sortOrder }
    }

    val userBookings by PujaManager.bookings.collectAsState()

    var selectedPujaForBooking by remember { mutableStateOf<PujaItem?>(null) }
    var confirmedBookingItem by remember { mutableStateOf<PujaBookingItem?>(null) }
    var cancelBookingTarget by remember { mutableStateOf<PujaBookingItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isHindi) "पूजा बुक करें" else "Book Puja",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite
                        )
                        Text(
                            text = if (isHindi) "श्री जामनीवाला दरबार पावन अनुष्ठान व महापूजा" else "Shree Jamniwala Darbar Holy Rituals",
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
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(AccentGolden.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SelfImprovement,
                            contentDescription = null,
                            tint = AccentGolden,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "पावन पूजा एवं अनुष्ठान सेवा",
                        color = AccentGolden,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isHindi) "आगामी 4 सप्ताहों की उपलब्ध तिथियों पर अपनी मनोकामना पूजा बुक करें।" else "Book your desired Puja on available dates across upcoming weeks.",
                        color = TextWhite,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // AVAILABLE PUJAS LIST
            Text(
                text = if (isHindi) "उपलब्ध पूजा एवं अनुष्ठान" else "Available Pujas",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AccentGolden
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (activePujas.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Text(
                        text = if (isHindi) "पूजा सारणी जल्द उपलब्ध होगी।" else "Puja schedule will be available soon.",
                        modifier = Modifier.padding(18.dp),
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    activePujas.forEach { puja ->
                        PujaCardUser(
                            puja = puja,
                            isHindi = isHindi,
                            onBookClick = {
                                haptic.playPremiumHaptic()
                                selectedPujaForBooking = puja
                            }
                        )
                    }
                }
            }

            // USER MY BOOKED PUJAS SECTION
            if (userBookings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = if (isHindi) "📋 मेरी बुक की गई पूजा" else "My Scheduled Pujas",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentGolden
                )
                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    userBookings.forEach { booking ->
                        MyPujaBookingCard(
                            booking = booking,
                            isHindi = isHindi,
                            haptic = haptic,
                            onCancelBooking = {
                                cancelBookingTarget = booking
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // BOOKING FORM DIALOG
    if (selectedPujaForBooking != null) {
        PujaBookingDialog(
            puja = selectedPujaForBooking!!,
            isHindi = isHindi,
            onDismiss = { selectedPujaForBooking = null },
            onConfirmBooking = { booking ->
                PujaManager.bookPuja(booking)
                selectedPujaForBooking = null
                confirmedBookingItem = booking
            }
        )
    }

    // CONFIRMATION SUCCESS DIALOG WITH RECEIPT
    if (confirmedBookingItem != null) {
        PujaReceiptDialog(
            booking = confirmedBookingItem!!,
            isHindi = isHindi,
            onDismiss = { confirmedBookingItem = null }
        )
    }

    // USER CANCEL PUJA DIALOG WITH NO REFUND WARNING
    if (cancelBookingTarget != null) {
        UserCancelPujaDialog(
            booking = cancelBookingTarget!!,
            isHindi = isHindi,
            onDismiss = { cancelBookingTarget = null },
            onConfirmCancel = { reason ->
                PujaManager.cancelBooking(cancelBookingTarget!!.id, "USER", reason)
                cancelBookingTarget = null
            }
        )
    }
}

@Composable
private fun PujaCardUser(
    puja: PujaItem,
    isHindi: Boolean,
    onBookClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isHindi || puja.nameEnglish.isEmpty()) puja.nameHindi else puja.nameEnglish,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextWhite,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    color = AccentGolden.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "₹${puja.price.toInt()}" + if (puja.pricingMode == PujaPricingMode.PER_PERSON) " / व्यक्ति" else "",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentGolden,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            if (puja.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = puja.description,
                    fontSize = 12.sp,
                    color = TextMuted,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Available Days Chips
            Text(if (isHindi) "उपलब्ध दिन:" else "Available Days:", fontSize = 11.sp, color = AccentGolden, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                puja.availableDays.forEach { day ->
                    Surface(
                        color = AccentGolden.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = day,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentGolden,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${puja.startTime} (${puja.duration})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }

                Button(
                    onClick = onBookClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGolden),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isHindi) "पूजा बुक करें" else "Book Puja",
                        color = BackgroundDark,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PujaBookingDialog(
    puja: PujaItem,
    isHindi: Boolean,
    onDismiss: () -> Unit,
    onConfirmBooking: (PujaBookingItem) -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    var mobileInput by remember { mutableStateOf("") }
    var devoteeCountInput by remember { mutableIntStateOf(1) }
    var specialReqInput by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    // DYNAMIC MULTI-WEEK UPCOMING OCCURRENCES (4 WEEKS AHEAD)
    val upcomingOccurrences = remember(puja.availableDays) {
        PujaManager.getUpcomingOccurrencesForDays(puja.availableDays, 4)
    }

    var selectedOccurrence by remember {
        mutableStateOf(upcomingOccurrences.firstOrNull() ?: PujaDateOccurrence("सोमवार", "2026-09-21", "21 Sep 2026 (सोमवार)", "सप्तमी", "सप्ताह 1"))
    }

    // Past Time Slot Validation for selected occurrence
    val isSlotValid = remember(selectedOccurrence, puja.startTime) {
        PujaManager.isSlotInFuture(puja.startTime, selectedOccurrence.dateStr)
    }

    val totalAmount = remember(puja.price, puja.pricingMode, devoteeCountInput) {
        if (puja.pricingMode == PujaPricingMode.PER_PERSON) puja.price * devoteeCountInput else puja.price
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isHindi || puja.nameEnglish.isEmpty()) puja.nameHindi else puja.nameEnglish,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextWhite
                    )
                    Text(text = "समय: ${puja.startTime}", fontSize = 11.sp, color = AccentGolden)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextWhite)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
            ) {
                if (!isSlotValid) {
                    Surface(
                        color = Color.Red.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color.Red),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isHindi) "⚠️ आज का यह समय बीत चुका है। कृपया आगामी तिथि/सप्ताह चुनें।" else "⚠️ Slot time has passed. Please pick an upcoming date.",
                            color = Color.Red,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (errorText != null) {
                    Text(text = errorText!!, color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                }

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it; errorText = null },
                    label = { Text(if (isHindi) "आपका नाम *" else "Full Name *") },
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

                Spacer(modifier = Modifier.height(12.dp))

                // MULTI-WEEK UPCOMING PUJA DATES SELECTION
                Text(if (isHindi) "आगामी 4 सप्ताहों की तिथियाँ *" else "Upcoming Available Dates (4 Weeks) *", fontSize = 11.sp, color = AccentGolden, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    upcomingOccurrences.forEach { occ ->
                        val isSelected = selectedOccurrence.dateStr == occ.dateStr
                        Surface(
                            modifier = Modifier.clickable { selectedOccurrence = occ },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) AccentGolden else BackgroundDark,
                            border = BorderStroke(1.dp, if (isSelected) AccentGolden else WarmGold.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Text(
                                    text = occ.weekLabel,
                                    fontSize = 9.sp,
                                    color = if (isSelected) BackgroundDark else TextMuted,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = occ.displayDateStr,
                                    fontSize = 11.sp,
                                    color = if (isSelected) BackgroundDark else TextWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = BackgroundDark,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "चयनित तिथि: ${selectedOccurrence.displayDateStr}", fontSize = 11.sp, color = AccentGolden, fontWeight = FontWeight.Bold)
                        Text(text = "पंचांग तिथि: ${selectedOccurrence.tithiText}", fontSize = 10.sp, color = TextWhite)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // DEVOTEE COUNT UI: SINGLE HORIZONTAL SCROLLABLE ROW
                Text(if (isHindi) "श्रद्धालुओं की संख्या:" else "Devotees Count:", fontSize = 11.sp, color = AccentGolden, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(1, 2, 3, 4, 5, 10).forEach { count ->
                        FilterChip(
                            selected = devoteeCountInput == count,
                            onClick = { devoteeCountInput = count },
                            label = { Text("$count व्यक्ति", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentGolden, containerColor = BackgroundDark)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = specialReqInput,
                    onValueChange = { specialReqInput = it },
                    label = { Text(if (isHindi) "विशेष मनोकामना / संदेश (Optional)" else "Special Request") },
                    modifier = Modifier.fillMaxWidth().height(70.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // PAYMENT SUMMARY BOX
                Surface(
                    color = BackgroundDark,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = if (isHindi) "कुल देय राशि:" else "Total Amount:", fontSize = 11.sp, color = TextMuted)
                            Text(text = "₹${totalAmount.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = AccentGolden)
                        }

                        Surface(
                            color = Color(0xFF1B5E20).copy(alpha = 0.3f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "सुरक्षित भुगतान",
                                fontSize = 10.sp,
                                color = Color(0xFF81C784),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanName = nameInput.trim()
                    val cleanPhone = mobileInput.trim()
                    if (cleanName.isEmpty()) {
                        errorText = if (isHindi) "कृपया अपना नाम दर्ज करें।" else "Please enter your full name."
                        return@Button
                    }
                    if (cleanPhone.length != 10) {
                        errorText = if (isHindi) "कृपया 10 अंकों का सही मोबाइल नंबर दर्ज करें।" else "Please enter a valid 10-digit mobile number."
                        return@Button
                    }
                    if (!isSlotValid) {
                        errorText = if (isHindi) "यह समय slot बीत चुका है।" else "Slot time has passed."
                        return@Button
                    }

                    val booking = PujaBookingItem(
                        pujaId = puja.id,
                        pujaName = puja.nameHindi.ifEmpty { puja.nameEnglish },
                        devoteeName = cleanName,
                        mobileNumber = "+91 $cleanPhone",
                        bookingDateStr = selectedOccurrence.dateStr,
                        displayDateStr = selectedOccurrence.displayDateStr,
                        bookingTime = puja.startTime,
                        devoteeCount = devoteeCountInput,
                        specialRequest = specialReqInput.trim(),
                        amountCharged = totalAmount,
                        isPaid = true,
                        status = PujaBookingStatus.CONFIRMED,
                        createdAt = getEpochMillis()
                    )
                    onConfirmBooking(booking)
                },
                enabled = isSlotValid,
                colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isHindi) "भुगतान एवं पूजा बुक करें" else "Pay & Book Puja", color = BackgroundDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isHindi) "रद्द करें" else "Cancel")
            }
        },
        containerColor = SurfaceDark
    )
}

@Composable
private fun PujaReceiptDialog(
    booking: PujaBookingItem,
    isHindi: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Receipt, contentDescription = null, tint = AccentGolden, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isHindi) "पूजा बुकिंग रसीद" else "Puja Booking Receipt", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = AccentGolden)
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
                        Text(text = "पूजा बुकिंग रसीद", fontSize = 11.sp, color = TextMuted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = WarmGold.copy(alpha = 0.2f))

                        Text(text = "Booking ID: ${booking.id}", fontSize = 10.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "पूजा: ${booking.pujaName}", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = TextWhite)
                        Text(text = "दिनांक: ${booking.displayDateStr}", fontSize = 12.sp, color = TextWhite)
                        Text(text = "समय: ${booking.bookingTime}", fontSize = 12.sp, color = TextWhite)
                        Text(text = "भक्त: ${booking.devoteeName} (${booking.mobileNumber})", fontSize = 12.sp, color = TextWhite)
                        Text(text = "संख्या: ${booking.devoteeCount} व्यक्ति", fontSize = 12.sp, color = TextWhite)

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = WarmGold.copy(alpha = 0.2f))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "भुगतान राशि:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text(text = "₹${booking.amountCharged.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = AccentGolden)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Payment Status:", fontSize = 11.sp, color = TextMuted)
                            Text(text = "Paid ✓", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF81C784))
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Booking Status:", fontSize = 11.sp, color = TextMuted)
                            Text(text = "Confirmed ✓", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF81C784))
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

@Composable
private fun UserCancelPujaDialog(
    booking: PujaBookingItem,
    isHindi: Boolean,
    onDismiss: () -> Unit,
    onConfirmCancel: (reason: String) -> Unit
) {
    var reasonInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isHindi) "क्या आप अपनी पूजा रद्द करना चाहते हैं?" else "Cancel Puja Booking?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Red
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = Color.Red.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color.Red),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isHindi) "⚠️ ध्यान दें: पूजा रद्द करने पर भुगतान की गई राशि (₹${booking.amountCharged.toInt()}) वापस नहीं की जाएगी।"
                               else "⚠️ Note: Paid amount (₹${booking.amountCharged.toInt()}) will NOT be refunded upon cancellation.",
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = reasonInput,
                    onValueChange = { reasonInput = it },
                    label = { Text(if (isHindi) "रद्दीकरण का कारण (Cancellation Reason)" else "Cancellation Reason") },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Red, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmCancel(reasonInput) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text(if (isHindi) "पूजा रद्द करें (No Refund)" else "Cancel Puja", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isHindi) "बुकिंग रखें" else "Keep Booking", color = TextWhite)
            }
        },
        containerColor = SurfaceDark
    )
}

@Composable
private fun MyPujaBookingCard(
    booking: PujaBookingItem,
    isHindi: Boolean,
    haptic: com.example.miyank.HapticManager,
    onCancelBooking: () -> Unit
) {
    val statusText = when (booking.status) {
        PujaBookingStatus.COMPLETED -> "✅ पूजा पूर्ण हो गई (COMPLETED)"
        PujaBookingStatus.INCOMPLETE -> "🔴 पूजा पूर्ण नहीं हुई (अनुपस्थित)"
        PujaBookingStatus.POSTPONED -> "⏳ स्थगित/आगे बढ़ाई गई (POSTPONED)"
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
            Text(text = "दिनांक: ${booking.displayDateStr} • ${booking.bookingTime}", fontSize = 12.sp, color = TextWhite)
            Text(text = "भक्त: ${booking.devoteeName} (${booking.devoteeCount} व्यक्ति) • Paid: ₹${booking.amountCharged.toInt()}", fontSize = 11.sp, color = TextMuted)

            if (booking.status == PujaBookingStatus.POSTPONED && booking.postponedReason.isNotEmpty()) {
                Text(text = "स्थगित करने का कारण: ${booking.postponedReason}", fontSize = 11.sp, color = AccentGolden)
            }

            if (booking.status == PujaBookingStatus.CANCELLED && booking.cancellationReason.isNotEmpty()) {
                Text(text = "रद्दीकरण का कारण: ${booking.cancellationReason}", fontSize = 11.sp, color = Color.Red)
            }

            if (booking.status == PujaBookingStatus.CONFIRMED || booking.status == PujaBookingStatus.PENDING || booking.status == PujaBookingStatus.POSTPONED) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = {
                        haptic.playPremiumHaptic()
                        onCancelBooking()
                    }) {
                        Icon(Icons.Default.Cancel, contentDescription = null, tint = Color.Red, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isHindi) "पूजा रद्द करें" else "Cancel Puja", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
