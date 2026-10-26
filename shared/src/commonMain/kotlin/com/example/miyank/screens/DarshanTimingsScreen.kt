package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyank.AartiTimingItem
import com.example.miyank.LanguageManager
import com.example.miyank.LiveDarshanManager
import com.example.miyank.NotificationCategories
import com.example.miyank.NotificationItem
import com.example.miyank.NotificationManager
import com.example.miyank.ShareManager
import com.example.miyank.getEpochMillis
import com.example.miyank.rememberHapticManager
import com.example.miyank.rememberSuvicharMediaHandler
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DarshanTimingsScreen(
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val mediaHandler = rememberSuvicharMediaHandler()
    val scrollState = rememberScrollState()
    val isHindi = LanguageManager.currentLanguage == com.example.miyank.Language.HINDI

    val settings by LiveDarshanManager.settings.collectAsState()
    val activeAartis = remember(settings.aartiTimings) {
        settings.aartiTimings.filter { it.isActive }.sortedBy { it.sortOrder }
    }

    var reminderStatusMessage by remember { mutableStateOf<String?>(null) }

    fun handleSetAartiReminder() {
        haptic.playPremiumHaptic()
        val now = getEpochMillis()
        var scheduledCount = 0

        activeAartis.forEach { aarti ->
            val name = if (isHindi || aarti.nameEnglish.isEmpty()) aarti.nameHindi else aarti.nameEnglish
            val notif = NotificationItem(
                id = "aarti_reminder_${aarti.id}_${now}",
                title = "🪔 $name का समय हो गया है!",
                titleEnglish = "🪔 Time for $name!",
                message = "श्री जामनीवाला दरबार में $name (${aarti.time}) का समय हो गया है। दर्शन एवं आरती में सम्मिलित हों। 🙏",
                messageEnglish = "It is time for $name (${aarti.time}) at Shree Jamniwala Darbar. Join for Aarti. 🙏",
                timestampStr = "अभी",
                categoryTag = NotificationCategories.AARTI,
                targetRoute = com.example.miyank.TargetRoutes.LIVE_DARSHAN,
                isRead = false,
                isUrgent = true,
                createdAt = now + (scheduledCount * 1000)
            )
            NotificationManager.addNotification(notif)
            scheduledCount++
        }

        reminderStatusMessage = if (isHindi) "आरती समय का नोटिफिकेशन एक्टिव कर दिया गया है! 🙏" else "Aarti reminder notification set successfully! 🙏"
    }

    fun handleShareTimings() {
        haptic.playPremiumHaptic()
        val shareText = ShareManager.formatDarshanTimingsShareMessage(
            morningOpening = settings.morningDarshanOpening,
            morningClosing = settings.morningDarshanClosing,
            eveningOpening = settings.eveningDarshanOpening,
            eveningClosing = settings.eveningDarshanClosing,
            aartis = activeAartis,
            isHindi = isHindi
        )
        mediaHandler.shareSuvichar(
            imageUrlOrPath = "file:///android_asset/temple_pooja.jpg",
            dateStr = "",
            quoteText = if (isHindi) "दर्शन व आरती समय" else "Darshan & Aarti Schedule",
            author = "श्री जामनीवाला दरबार",
            shareText = shareText
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isHindi) "दर्शन व आरती समय" else "Darshan & Aarti Timings",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite
                        )
                        Text(
                            text = if (isHindi) "श्री जामनीवाला दरबार — दैनिक दर्शन एवं महाआरती सारणी" else "Shree Jamniwala Darbar Daily Schedule",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
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
            // 1. TEMPLE SUMMARY CARD
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
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = AccentGolden,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "श्री जामनीवाला दरबार दर्शन सारणी",
                        color = AccentGolden,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isHindi) "प्रतिदिन प्रातः एवं सायं पावन महाआरती एवं दरबार दर्शन।" else "Daily Morning & Evening Aarti and Temple Darshan.",
                        color = TextWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. DAILY AARTI SECTION (READ DYNAMICALLY FROM LiveDarshanManager)
            Text(
                text = if (isHindi) "दैनिक महाआरती समय" else "Daily Aarti Timings",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AccentGolden
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (activeAartis.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Text(
                        text = if (isHindi) "आरती समय की जानकारी जल्द उपलब्ध होगी।" else "Aarti timing details will be available soon.",
                        modifier = Modifier.padding(18.dp),
                        color = TextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    activeAartis.forEach { aarti ->
                        AartiCardDynamic(
                            aarti = aarti,
                            isHindi = isHindi
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 3. TEMPLE DARSHAN WINDOWS SECTION
            Text(
                text = if (isHindi) "दरबार द्वार खुलने व बंद होने का समय" else "Temple Opening & Closing Hours",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AccentGolden
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    DarshanWindowRow(
                        label = if (isHindi) "प्रातः दर्शन काल" else "Morning Darshan",
                        timeStr = if (settings.morningDarshanOpening.isNotEmpty() && settings.morningDarshanClosing.isNotEmpty())
                            "${settings.morningDarshanOpening} - ${settings.morningDarshanClosing}"
                        else if (isHindi) "दर्शन समय की जानकारी जल्द उपलब्ध होगी।" else "Schedule available soon"
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = WarmGold.copy(alpha = 0.2f))

                    DarshanWindowRow(
                        label = if (isHindi) "सायं दर्शन काल" else "Evening Darshan",
                        timeStr = if (settings.eveningDarshanOpening.isNotEmpty() && settings.eveningDarshanClosing.isNotEmpty())
                            "${settings.eveningDarshanOpening} - ${settings.eveningDarshanClosing}"
                        else if (isHindi) "दर्शन समय की जानकारी जल्द उपलब्ध होगी।" else "Schedule available soon"
                    )

                    if (settings.morningDarshanClosing.isNotEmpty() && settings.eveningDarshanOpening.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            color = AccentGolden.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isHindi) "विशेष नोट: दोपहर ${settings.morningDarshanClosing} से ${settings.eveningDarshanOpening} तक विश्राम काल रहता है।"
                                       else "Special Note: Rest period from ${settings.morningDarshanClosing} to ${settings.eveningDarshanOpening}.",
                                modifier = Modifier.padding(12.dp),
                                fontSize = 12.sp,
                                color = AccentGolden,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. ACTION BUTTONS: REMINDER & SHARE
            if (reminderStatusMessage != null) {
                Surface(
                    color = Color(0xFF1B5E20).copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = reminderStatusMessage!!,
                            color = Color(0xFF81C784),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Button(
                onClick = { handleSetAartiReminder() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHindi) "आरती समय याद दिलाएँ" else "Set Aarti Reminder",
                        color = BackgroundDark,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = { handleShareTimings() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, AccentGolden.copy(alpha = 0.6f))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = AccentGolden, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHindi) "समय सारणी शेयर करें" else "Share Schedule",
                        color = AccentGolden,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AartiCardDynamic(
    aarti: AartiTimingItem,
    isHindi: Boolean
) {
    val isMorning = aarti.time.contains("AM", ignoreCase = true)
    val icon = if (isMorning) Icons.Default.WbSunny else Icons.Default.WbTwilight

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AccentGolden.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = AccentGolden, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isHindi || aarti.nameEnglish.isEmpty()) aarti.nameHindi else aarti.nameEnglish,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextWhite
                )
                if (aarti.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = aarti.description,
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            Text(
                text = aarti.time,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = AccentGolden
            )
        }
    }
}

@Composable
private fun DarshanWindowRow(label: String, timeStr: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextWhite)
        Text(text = timeStr, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = AccentGolden)
    }
}
