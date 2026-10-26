package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
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
import com.example.miyank.LanguageManager
import com.example.miyank.ShareManager
import com.example.miyank.UserQueryManager
import com.example.miyank.YatraBooking
import com.example.miyank.YatraBookingStatus
import com.example.miyank.YatraItem
import com.example.miyank.YatraManager
import com.example.miyank.openExternalUrl
import com.example.miyank.rememberHapticManager
import com.example.miyank.rememberSuvicharMediaHandler
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YatraScreen(
    onBookSeatsClick: (yatraId: String) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val haptic = rememberHapticManager()
    val mediaHandler = rememberSuvicharMediaHandler()
    val isHindi = LanguageManager.currentLanguage == com.example.miyank.Language.HINDI

    val allYatras by YatraManager.yatras.collectAsState()
    val activeYatras = remember(allYatras) {
        allYatras.filter { it.isActive }.sortedBy { it.sortOrder }
    }

    val userBookings by YatraManager.bookings.collectAsState()
    val contactSettings by UserQueryManager.contactSettings.collectAsState()
    val helplinePhone = contactSettings.helplinePhone.ifEmpty { "+91 98765 43210" }
    val cleanPhone = helplinePhone.replace(" ", "").replace("-", "")

    var selectedMainTab by remember { mutableIntStateOf(0) } // 0: Yatras, 1: My Bookings
    var cancelTargetBooking by remember { mutableStateOf<YatraBooking?>(null) }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(SurfaceDark)) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = if (isHindi) "धार्मिक यात्रा" else "Holy Yatra",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextWhite
                            )
                            Text(
                                text = if (isHindi) "श्री जामनीवाला दरबार तीर्थ यात्राएं" else "Shree Jamniwala Darbar Pilgrimages",
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

                // TWO MAIN TABS
                TabRow(
                    selectedTabIndex = selectedMainTab,
                    containerColor = SurfaceDark,
                    contentColor = AccentGolden,
                    divider = {}
                ) {
                    Tab(
                        selected = selectedMainTab == 0,
                        onClick = {
                            haptic.playPremiumHaptic()
                            selectedMainTab = 0
                        },
                        text = {
                            Text(
                                text = if (isHindi) "यात्राएं" else "Yatras",
                                fontWeight = FontWeight.Bold,
                                color = if (selectedMainTab == 0) AccentGolden else TextMuted
                            )
                        }
                    )

                    Tab(
                        selected = selectedMainTab == 1,
                        onClick = {
                            haptic.playPremiumHaptic()
                            selectedMainTab = 1
                        },
                        text = {
                            Text(
                                text = if (isHindi) "मेरी बुकिंग (${userBookings.size})" else "My Bookings (${userBookings.size})",
                                fontWeight = FontWeight.Bold,
                                color = if (selectedMainTab == 1) AccentGolden else TextMuted
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
            if (selectedMainTab == 0) {
                // TAB 0: YATRAS LIST
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    // INFO ALERT CARD
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(AccentGolden.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsBus,
                                    contentDescription = null,
                                    tint = AccentGolden,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (isHindi) "सभी यात्राएं श्री जामनीवाला दरबार से प्रस्थान करती हैं। सीटों की बुकिंग नीचे दिए बटन से करें।"
                                       else "All Yatras depart from Shree Jamniwala Darbar. Book your seats using the buttons below.",
                                fontSize = 12.sp,
                                color = TextWhite,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = if (isHindi) "आगामी यात्राएं" else "Upcoming Yatras",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentGolden
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (activeYatras.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                        ) {
                            Text(
                                text = if (isHindi) "यात्रा सूची जल्द उपलब्ध होगी।" else "Yatra schedule will be available soon.",
                                modifier = Modifier.padding(18.dp),
                                color = TextMuted,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            activeYatras.forEach { yatra ->
                                YatraCardUserClean(
                                    yatra = yatra,
                                    isHindi = isHindi,
                                    haptic = haptic,
                                    onBookSeats = { onBookSeatsClick(yatra.id) },
                                    onCall = { openExternalUrl("tel:$cleanPhone") },
                                    onShare = {
                                        val details = "प्रस्थान: ${yatra.displayDateStr}\nसमय: ${yatra.departureTime}\nगंतव्य: ${yatra.destination}\nसेवा राशि: ₹${yatra.price.toInt()}"
                                        val msg = ShareManager.formatYatraShareMessage(yatra.title, details)
                                        mediaHandler.shareSuvichar(
                                            imageUrlOrPath = yatra.imageUrl.ifEmpty { "file:///android_asset/temple_pooja.jpg" },
                                            dateStr = yatra.displayDateStr,
                                            quoteText = yatra.title,
                                            author = "श्री जामनीवाला दरबार",
                                            shareText = msg
                                        )
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            } else {
                // TAB 1: MY BOOKINGS LIST
                if (userBookings.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isHindi) "आपकी कोई यात्रा सीट बुकिंग नहीं है।" else "No Yatra bookings found.",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(userBookings) { booking ->
                            val isCancelled = booking.bookingStatus == YatraBookingStatus.CANCELLED
                            val statusText = if (isCancelled) (if (isHindi) "रद्द (CANCELLED)" else "CANCELLED") else (if (isHindi) "पुष्टि (CONFIRMED)" else "CONFIRMED")
                            val statusColor = if (isCancelled) Color.Red else Color(0xFF81C784)

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = booking.yatraTitle, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = AccentGolden, modifier = Modifier.weight(1f))

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

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = "Booking ID: ${booking.bookingId}", fontSize = 10.sp, color = TextMuted)
                                    Text(text = "बस: ${booking.busName} (${booking.busNumber})", fontSize = 12.sp, color = TextWhite)
                                    Text(text = "सीट संख्या: ${booking.seatNumbers.joinToString(", ")}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccentGolden)
                                    Text(text = "यात्री: ${booking.passengerName} (${booking.mobileNumber})", fontSize = 12.sp, color = TextWhite)
                                    Text(text = "भुगतान: ₹${booking.amountCharged.toInt()} (Paid ✓)", fontSize = 11.sp, color = Color(0xFF81C784))

                                    if (!isCancelled) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    haptic.playPremiumHaptic()
                                                    onBookSeatsClick(booking.yatraId)
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(1.dp, AccentGolden.copy(alpha = 0.6f)),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(if (isHindi) "सीट बदलें (Switch Seat)" else "Switch Seat", color = AccentGolden, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            TextButton(onClick = {
                                                haptic.playPremiumHaptic()
                                                cancelTargetBooking = booking
                                            }) {
                                                Icon(Icons.Default.Cancel, contentDescription = null, tint = Color.Red, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(if (isHindi) "सीट बुकिंग रद्द करें" else "Cancel Seat Booking", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // USER CANCEL BOOKING DIALOG WITH NO REFUND WARNING
    if (cancelTargetBooking != null) {
        val target = cancelTargetBooking!!
        AlertDialog(
            onDismissRequest = { cancelTargetBooking = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHindi) "क्या आप अपनी सीट बुकिंग रद्द करना चाहते हैं?" else "Cancel Seat Booking?",
                        fontSize = 15.sp,
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
                            text = if (isHindi) "ध्यान दें: पूजा व सीट बुकिंग रद्द करने पर भुगतान की गई राशि (₹${target.amountCharged.toInt()}) वापस नहीं की जाएगी।"
                                   else "Are you sure you want to cancel this seat booking? The amount paid (₹${target.amountCharged.toInt()}) will not be refunded.",
                            color = Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.playPremiumHaptic()
                        YatraManager.cancelYatraBooking(target.bookingId)
                        cancelTargetBooking = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(if (isHindi) "रद्द करें" else "Cancel Booking", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { cancelTargetBooking = null }) {
                    Text(if (isHindi) "वापस जाएँ" else "Go Back", color = TextWhite)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

@Composable
private fun YatraCardUserClean(
    yatra: YatraItem,
    isHindi: Boolean,
    haptic: com.example.miyank.HapticManager,
    onBookSeats: () -> Unit,
    onCall: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.35f))
    ) {
        Column {
            if (yatra.imageUrl.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                ) {
                    AsyncImage(
                        model = yatra.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isHindi || yatra.titleEnglish.isEmpty()) yatra.title else yatra.titleEnglish,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextWhite,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        color = AccentGolden.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = yatra.destination,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentGolden,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                if (yatra.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = yatra.description, fontSize = 12.sp, color = TextMuted, lineHeight = 16.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = AccentGolden, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "प्रस्थान: ${yatra.displayDateStr}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = AccentGolden, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "समय: ${yatra.departureTime}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = AccentGolden, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "₹${yatra.price.toInt()} (सेवा राशि प्रति सीट)", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = AccentGolden)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ACTION BUTTONS: BOOK SEATS, CALL, SHARE
                Button(
                    onClick = {
                        haptic.playPremiumHaptic()
                        onBookSeats()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isHindi) "सीट बुक करें" else "Book Seats",
                            color = BackgroundDark,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            haptic.playPremiumHaptic()
                            onCall()
                        },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, AccentGolden.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = AccentGolden, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isHindi) "जानकारी कॉल" else "Call Helpline", color = AccentGolden, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            haptic.playPremiumHaptic()
                            onShare()
                        },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = TextWhite, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isHindi) "शेयर करें" else "Share", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
