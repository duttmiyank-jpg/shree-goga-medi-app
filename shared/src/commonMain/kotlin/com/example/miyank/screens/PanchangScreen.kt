package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
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
import com.example.miyank.HinduFestival
import com.example.miyank.HinduFestivalProvider
import com.example.miyank.LanguageManager
import com.example.miyank.MandirCalendarEvent
import com.example.miyank.MandirCalendarManager
import com.example.miyank.PanchangProvider
import com.example.miyank.UserCalendarNote
import com.example.miyank.UserNotesManager
import com.example.miyank.formatDisplayDate
import com.example.miyank.getTodayDateString
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanchangScreen(
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val isHindi = LanguageManager.currentLanguage == com.example.miyank.Language.HINDI
    val scrollState = rememberScrollState()

    val todayStr = remember { getTodayDateString() }
    val todayParts = remember(todayStr) { todayStr.split("-") }
    val todayYear = remember(todayParts) { todayParts.getOrNull(0)?.toIntOrNull() ?: 2026 }
    val todayMonth = remember(todayParts) { todayParts.getOrNull(1)?.toIntOrNull() ?: 9 }

    var selectedTab by remember { mutableStateOf(0) } // 0: Panchang, 1: Mandir Calendar
    var selectedPanchangDateStr by remember { mutableStateOf(todayStr) }

    // User Calendar Month & Year State
    var currentYear by remember { mutableStateOf(todayYear) }
    var currentMonthIndex by remember { mutableStateOf(todayMonth) } // 1-12
    var selectedCalendarDateStr by remember { mutableStateOf(todayStr) }

    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, FESTIVAL, MANDIR, NOTES

    var showDateDetailsBottomSheet by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var editingUserNote by remember { mutableStateOf<UserCalendarNote?>(null) }
    var deletingNoteId by remember { mutableStateOf<String?>(null) }

    var isLoadingPanchang by remember { mutableStateOf(false) }
    var panchangDataState by remember { mutableStateOf<com.example.miyank.PanchangData?>(null) }
    var refreshKey by remember { mutableIntStateOf(0) }

    val locationConfig by PanchangProvider.locationConfig.collectAsState()

    LaunchedEffect(selectedPanchangDateStr, locationConfig, refreshKey) {
        isLoadingPanchang = true
        val initialData = PanchangProvider.getPanchangForDate(selectedPanchangDateStr)
        panchangDataState = initialData

        try {
            val onlineData = PanchangProvider.getPanchangForDateOnline(
                dateStr = selectedPanchangDateStr,
                forceRefresh = refreshKey > 0
            )
            panchangDataState = onlineData
        } catch (e: Exception) {
            // Keep initialData on error
        } finally {
            isLoadingPanchang = false
        }
    }

    val panchangData = panchangDataState ?: PanchangProvider.getPanchangForDate(selectedPanchangDateStr)

    // Active Mandir Events and User Personal Notes
    val mandirEventsState by MandirCalendarManager.events.collectAsState()
    val userNotesState by UserNotesManager.userNotes.collectAsState()

    val monthMandirEvents = remember(mandirEventsState, currentYear, currentMonthIndex) {
        MandirCalendarManager.getEventsForMonth(currentYear, currentMonthIndex)
    }

    val monthHinduFestivals = remember(currentYear, currentMonthIndex) {
        HinduFestivalProvider.getFestivalsForMonth(currentYear, currentMonthIndex)
    }

    val allYearFestivals = remember(currentYear, currentMonthIndex) {
        HinduFestivalProvider.getUpcomingFestivalsForYear(currentYear, currentMonthIndex)
    }

    val monthUserNotes = remember(userNotesState, currentYear, currentMonthIndex) {
        UserNotesManager.getNotesForMonth(currentYear, currentMonthIndex)
    }

    val upcomingMandirEvents = remember(mandirEventsState) {
        MandirCalendarManager.getUpcomingEvents(todayStr, limit = 6)
    }

    val monthNamesHindi = arrayOf("", "जनवरी", "फरवरी", "मार्च", "अप्रैल", "मई", "जून", "जुलाई", "अगस्त", "सितम्बर", "अक्टूबर", "नवम्बर", "दिसम्बर")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(scrollState)
    ) {
        // Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextWhite
                )
            }
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    text = if (isHindi) "दैनिक पंचांग एवं मंदिर कैलेंडर" else "Panchang & Mandir Calendar",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextWhite
                )
                Text(
                    text = PanchangProvider.locationConfig.value.getFormattedLocationName(),
                    fontSize = 10.sp,
                    color = AccentGolden,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // TABS BAR: PANCHANG vs MANDIR CALENDAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable {
                        haptic.playPremiumHaptic()
                        selectedTab = 0
                    },
                color = if (selectedTab == 0) AccentGolden else SurfaceDark,
                border = BorderStroke(1.dp, if (selectedTab == 0) AccentGolden else WarmGold.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (isHindi) "☀️ दैनिक पंचांग" else "☀️ Panchang",
                    color = if (selectedTab == 0) BackgroundDark else TextWhite,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable {
                        haptic.playPremiumHaptic()
                        selectedTab = 1
                    },
                color = if (selectedTab == 1) AccentGolden else SurfaceDark,
                border = BorderStroke(1.dp, if (selectedTab == 1) AccentGolden else WarmGold.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (isHindi) "🗓️ मंदिर कैलेंडर" else "🗓️ Mandir Calendar",
                    color = if (selectedTab == 1) BackgroundDark else TextWhite,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            // TAB 1: PANCHANG VIEW WITH DATE NAVIGATION
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        haptic.playPremiumHaptic()
                        selectedPanchangDateStr = PanchangProvider.getPreviousDateString(selectedPanchangDateStr)
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.4f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentGolden)
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isHindi) "पिछला दिन" else "Previous", fontSize = 11.sp)
                }

                Surface(
                    color = AccentGolden.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, AccentGolden),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.clickable {
                        haptic.playPremiumHaptic()
                        selectedPanchangDateStr = todayStr
                    }
                ) {
                    Text(
                        text = if (isHindi) "आज" else "Today",
                        color = AccentGolden,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }

                OutlinedButton(
                    onClick = {
                        haptic.playPremiumHaptic()
                        selectedPanchangDateStr = PanchangProvider.getNextDateString(selectedPanchangDateStr)
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.4f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentGolden)
                ) {
                    Text(if (isHindi) "अगला दिन" else "Next", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // TODAY HEADER CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = AccentGolden,
                            modifier = Modifier.size(32.dp)
                        )
                        if (panchangData.isToday) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = AccentGolden,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("आज (TODAY)", color = BackgroundDark, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = panchangData.hindiDateFormatted,
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = panchangData.pakshaMasaFormatted,
                        color = AccentGolden,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // COMPACT LOCATION ROW (BELOW DATE CARD & BEFORE PANCHANG DETAILS)
            val locationConfig by PanchangProvider.locationConfig.collectAsState()

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = SurfaceDark,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📍 ", fontSize = 14.sp)
                        Text(
                            text = locationConfig.getFormattedLocationName(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            maxLines = 1
                        )
                    }

                    Surface(
                        color = AccentGolden.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = locationConfig.getReadableTimezoneName(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AccentGolden,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // ONLINE STATUS & REFRESH ROW
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (panchangData.isOnlineData) Color(0xFF1B5E20).copy(alpha = 0.35f) else AccentGolden.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, if (panchangData.isOnlineData) Color(0xFF4CAF50) else WarmGold.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isLoadingPanchang) {
                            CircularProgressIndicator(color = AccentGolden, modifier = Modifier.size(10.dp), strokeWidth = 2.dp)
                        } else {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(if (panchangData.isOnlineData) Color(0xFF4CAF50) else AccentGolden))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isLoadingPanchang) (if (isHindi) "ऑनलाइन डेटा लोड हो रहा है..." else "Loading online data...")
                                   else if (panchangData.isOnlineData) (if (isHindi) "🟢 ऑनलाइन पंचांग डेटा (सत्यापित)" else "🟢 Online Panchang (Verified)")
                                   else (if (isHindi) "💾 पंचांग डेटा (कैश/लोकल)" else "💾 Panchang Data (Cached)"),
                            color = if (panchangData.isOnlineData) Color(0xFF81C784) else AccentGolden,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(
                    onClick = {
                        haptic.playPremiumHaptic()
                        refreshKey++
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Panchang",
                        tint = AccentGolden,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (panchangData.currentPeriodStatus.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = AccentGolden.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, AccentGolden.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = panchangData.currentPeriodStatus,
                        color = TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            SectionHeaderPanchang(if (isHindi) "शुभ पंचांग विवरण" else "Panchang Details")

            PanchangInfoCard(
                listOf(
                    (if (isHindi) "तिथि" else "Tithi") to panchangData.tithi,
                    (if (isHindi) "नक्षत्र" else "Nakshatra") to panchangData.nakshatra,
                    (if (isHindi) "योग" else "Yoga") to panchangData.yoga,
                    (if (isHindi) "करण" else "Karan") to panchangData.karan,
                    (if (isHindi) "वार" else "Day") to panchangData.vaar
                )
            )

            SectionHeaderPanchang(if (isHindi) "सूर्योदय, सूर्यास्त एवं चंद्रोदय" else "Celestial Timings")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SunMoonCard(
                    modifier = Modifier.weight(1f),
                    title = if (isHindi) "सूर्योदय" else "Sunrise",
                    time = panchangData.sunrise,
                    icon = Icons.Default.WbSunny,
                    color = AccentGolden.copy(alpha = 0.2f)
                )
                SunMoonCard(
                    modifier = Modifier.weight(1f),
                    title = if (isHindi) "सूर्यास्त" else "Sunset",
                    time = panchangData.sunset,
                    icon = Icons.Default.WbTwilight,
                    color = Color(0xFFE3F2FD).copy(alpha = 0.2f)
                )
                SunMoonCard(
                    modifier = Modifier.weight(1f),
                    title = if (isHindi) "चन्द्रोदय" else "Moonrise",
                    time = panchangData.moonrise,
                    icon = Icons.Default.NightsStay,
                    color = WarmGold.copy(alpha = 0.2f)
                )
            }

            SectionHeaderPanchang(if (isHindi) "शुभ एवं अशुभ मुहूर्त समय" else "Muhurat Timings")

            PanchangTimingRow(if (isHindi) "अभिजीत मुहूर्त (शुभ)" else "Abhijit Muhurat", panchangData.abhijitMuhurat, isAuspicious = true)
            PanchangTimingRow(if (isHindi) "राहू काल (अशुभ)" else "Rahu Kaal", panchangData.rahuKaal, isAuspicious = false)
            PanchangTimingRow(if (isHindi) "यमगण्ड" else "Yamagand", panchangData.yamagand, isAuspicious = false)
            PanchangTimingRow(if (isHindi) "गुलिक काल" else "Gulik Kaal", panchangData.gulikKaal, isAuspicious = true)

        } else {
            // TAB 2: MANDIR CALENDAR VIEW
            // MONTH SELECTOR & TODAY BUTTON
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    haptic.playPremiumHaptic()
                    if (currentMonthIndex == 1) {
                        currentMonthIndex = 12
                        currentYear--
                    } else {
                        currentMonthIndex--
                    }
                }) {
                    Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Prev Month", tint = AccentGolden)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${monthNamesHindi.getOrElse(currentMonthIndex) { "सितम्बर" }} $currentYear",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentGolden
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = AccentGolden.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, AccentGolden),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable {
                            haptic.playPremiumHaptic()
                            currentYear = todayYear
                            currentMonthIndex = todayMonth
                            selectedCalendarDateStr = todayStr
                        }
                    ) {
                        Text("आज", color = AccentGolden, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }
                }

                IconButton(onClick = {
                    haptic.playPremiumHaptic()
                    if (currentMonthIndex == 12) {
                        currentMonthIndex = 1
                        currentYear++
                    } else {
                        currentMonthIndex++
                    }
                }) {
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Month", tint = AccentGolden)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // COMPACT CATEGORY FILTERS ROW
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val filters = listOf("ALL" to "सभी", "FESTIVAL" to "🪔 त्योहार", "MANDIR" to "🛕 मंदिर", "NOTES" to "📝 मेरी नोट")
                filters.forEach { (key, label) ->
                    val isSelected = selectedFilter == key
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                haptic.playPremiumHaptic()
                                selectedFilter = key
                            },
                        color = if (isSelected) AccentGolden else SurfaceDark,
                        border = BorderStroke(1.dp, if (isSelected) AccentGolden else WarmGold.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) BackgroundDark else TextWhite,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Calendar Grid with Indicators
            CalendarGridWithIndicators(
                year = currentYear,
                month = currentMonthIndex,
                selectedDateStr = selectedCalendarDateStr,
                mandirEvents = monthMandirEvents,
                hinduFestivals = monthHinduFestivals,
                userNotes = monthUserNotes,
                selectedFilter = selectedFilter,
                isHindi = isHindi,
                haptic = haptic,
                onDateSelected = { dateStr ->
                    selectedCalendarDateStr = dateStr
                    showDateDetailsBottomSheet = true
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            SectionHeaderPanchang(if (isHindi) "वर्ष $currentYear के प्रमुख हिन्दू त्योहार (दिसम्बर तक)" else "Major Festivals $currentYear (Up to Dec)")

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allYearFestivals.forEach { fest ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = WarmGold.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${fest.day}/${fest.month}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = AccentGolden
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isHindi) fest.nameHindi else fest.nameEnglish,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                if (fest.description.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = fest.description,
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionHeaderPanchang(if (isHindi) "आगामी मंदिर कार्यक्रम एवं आयोजन" else "Upcoming Mandir Events")

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                upcomingMandirEvents.forEach { event ->
                    MandirCalendarEventCardItem(event, isHindi)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // MEDIUM POPUP MODAL BOTTOM SHEET FOR SELECTED DATE
    if (showDateDetailsBottomSheet) {
        DateDetailsBottomSheet(
            dateStr = selectedCalendarDateStr,
            mandirEvents = MandirCalendarManager.getEventsForDate(selectedCalendarDateStr),
            userNotes = UserNotesManager.getNotesForDate(selectedCalendarDateStr),
            hinduFestival = HinduFestivalProvider.getFestivalForDate(
                selectedCalendarDateStr.split("-").getOrNull(0)?.toIntOrNull() ?: currentYear,
                selectedCalendarDateStr.split("-").getOrNull(1)?.toIntOrNull() ?: currentMonthIndex,
                selectedCalendarDateStr.split("-").getOrNull(2)?.toIntOrNull() ?: 1
            ),
            isHindi = isHindi,
            onDismiss = { showDateDetailsBottomSheet = false },
            onAddNote = {
                showDateDetailsBottomSheet = false
                editingUserNote = UserCalendarNote(dateStr = selectedCalendarDateStr)
                showAddNoteDialog = true
            },
            onEditNote = { note ->
                showDateDetailsBottomSheet = false
                editingUserNote = note
                showAddNoteDialog = true
            },
            onDeleteNote = { noteId ->
                deletingNoteId = noteId
            }
        )
    }

    // ADD / EDIT USER NOTE DIALOG
    if (showAddNoteDialog) {
        AddEditUserNoteDialog(
            existingNote = editingUserNote,
            dateStr = selectedCalendarDateStr,
            isHindi = isHindi,
            onDismiss = { showAddNoteDialog = false },
            onSave = { updatedNote ->
                UserNotesManager.addOrUpdateNote(updatedNote)
                showAddNoteDialog = false
            }
        )
    }

    // DELETE NOTE CONFIRMATION DIALOG
    deletingNoteId?.let { noteId ->
        AlertDialog(
            onDismissRequest = { deletingNoteId = null },
            containerColor = SurfaceDark,
            titleContentColor = Color.Red,
            textContentColor = TextWhite,
            title = {
                Text(if (isHindi) "पर्सनल नोट हटाने की पुष्टि" else "Confirm Delete Note", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Text(if (isHindi) "क्या आप वाकई इस पर्सनल नोट को हटाना चाहते हैं?" else "Are you sure you want to delete this personal note?", fontSize = 13.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = noteId
                        deletingNoteId = null
                        UserNotesManager.deleteNote(id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(if (isHindi) "हाँ, हटाएँ" else "Yes, Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingNoteId = null }) {
                    Text(if (isHindi) "रद्द करें" else "Cancel", color = TextMuted)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateDetailsBottomSheet(
    dateStr: String,
    mandirEvents: List<MandirCalendarEvent>,
    userNotes: List<UserCalendarNote>,
    hinduFestival: HinduFestival?,
    isHindi: Boolean,
    onDismiss: () -> Unit,
    onAddNote: () -> Unit,
    onEditNote: (UserCalendarNote) -> Unit,
    onDeleteNote: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        contentColor = TextWhite,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(WarmGold.copy(alpha = 0.5f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp, top = 4.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🗓️ ${formatDisplayDate(dateStr)}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentGolden
                )

                Surface(
                    color = AccentGolden,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.clickable { onAddNote() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isHindi) "+ नोट जोड़ें" else "+ Add Note", color = BackgroundDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 1: HINDU FESTIVAL
            if (hinduFestival != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BackgroundDark),
                    border = BorderStroke(1.dp, WarmGold)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🪔 ", fontSize = 16.sp)
                            Text(if (isHindi) "हिन्दू त्योहार / पर्व" else "Hindu Festival", fontSize = 11.sp, color = WarmGold, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isHindi) hinduFestival.nameHindi else hinduFestival.nameEnglish,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite
                        )
                        if (hinduFestival.description.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(hinduFestival.description, fontSize = 12.sp, color = TextMuted)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // SECTION 2: TEMPLE EVENTS
            if (mandirEvents.isNotEmpty()) {
                Text(if (isHindi) "🛕 मंदिर कार्यक्रम" else "🛕 Temple Events", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccentGolden)
                Spacer(modifier = Modifier.height(6.dp))
                mandirEvents.forEach { event ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = BackgroundDark),
                        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(event.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            if (event.description.isNotEmpty()) {
                                Text(event.description, fontSize = 11.sp, color = TextMuted)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // SECTION 3: USER PERSONAL NOTES
            Text(if (isHindi) "📝 मेरी पर्सनल नोट (My Notes)" else "📝 Personal Notes", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccentGolden)
            Spacer(modifier = Modifier.height(6.dp))

            if (userNotes.isEmpty()) {
                Text(
                    text = if (isHindi) "इस तारीख पर कोई पर्सनल नोट नहीं है।" else "No personal notes on this date.",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            } else {
                userNotes.forEach { note ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = BackgroundDark),
                        border = BorderStroke(1.dp, SaffronAccent.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(note.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                    if (note.timeStr.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("(${note.timeStr})", fontSize = 10.sp, color = AccentGolden)
                                    }
                                }
                                if (note.description.isNotEmpty()) {
                                    Text(note.description, fontSize = 11.sp, color = TextMuted)
                                }
                            }

                            Row {
                                IconButton(onClick = { onEditNote(note) }, modifier = Modifier.size(30.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextWhite, modifier = Modifier.size(16.dp))
                                }
                                IconButton(onClick = { onDeleteNote(note.id) }, modifier = Modifier.size(30.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditUserNoteDialog(
    existingNote: UserCalendarNote?,
    dateStr: String,
    isHindi: Boolean,
    onDismiss: () -> Unit,
    onSave: (UserCalendarNote) -> Unit
) {
    var titleInput by remember { mutableStateOf(existingNote?.title ?: "") }
    var descInput by remember { mutableStateOf(existingNote?.description ?: "") }
    var timeInput by remember { mutableStateOf(existingNote?.timeStr ?: "07:00 PM") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        titleContentColor = TextWhite,
        textContentColor = TextWhite,
        title = {
            Text(
                text = if (existingNote == null || existingNote.id.isEmpty()) (if (isHindi) "नयी पर्सनल नोट जोड़ें" else "Add Personal Note") else (if (isHindi) "नोट संपादित करें" else "Edit Note"),
                color = AccentGolden,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "🗓️ ${formatDisplayDate(dateStr)}",
                    fontSize = 12.sp,
                    color = AccentGolden,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text(if (isHindi) "शीर्षक (e.g. घर में पूजा)" else "Title") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = descInput,
                    onValueChange = { descInput = it },
                    label = { Text(if (isHindi) "विवरण (Optional)" else "Description") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = timeInput,
                    onValueChange = { timeInput = it },
                    label = { Text(if (isHindi) "समय (Optional e.g. 07:00 PM)" else "Time") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(errorText!!, color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (titleInput.trim().isEmpty()) {
                        errorText = if (isHindi) "❌ कृपया शीर्षक दर्ज करें!" else "❌ Please enter title!"
                        return@Button
                    }
                    val item = UserCalendarNote(
                        id = existingNote?.id ?: "",
                        dateStr = dateStr,
                        title = titleInput.trim(),
                        description = descInput.trim(),
                        timeStr = timeInput.trim()
                    )
                    onSave(item)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
            ) {
                Text(if (isHindi) "नोट सेव करें" else "Save Note", color = BackgroundDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isHindi) "रद्द करें" else "Cancel", color = TextMuted)
            }
        }
    )
}

@Composable
fun CalendarGridWithIndicators(
    year: Int,
    month: Int,
    selectedDateStr: String,
    mandirEvents: List<MandirCalendarEvent>,
    hinduFestivals: List<HinduFestival>,
    userNotes: List<UserCalendarNote>,
    selectedFilter: String,
    isHindi: Boolean,
    haptic: com.example.miyank.HapticManager,
    onDateSelected: (String) -> Unit
) {
    val daysOfWeek = if (isHindi) listOf("सोम", "मंगल", "बुध", "गुरु", "शुक्र", "शनि", "रवि")
    else listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    val totalDays = PanchangProvider.getDaysInMonth(year, month)

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGolden
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .height(240.dp)
                    .padding(8.dp),
                userScrollEnabled = false
            ) {
                items(totalDays) { index ->
                    val dayNum = index + 1
                    val dayDateStr = "${year}-${month.toString().padStart(2, '0')}-${dayNum.toString().padStart(2, '0')}"
                    val isSelected = dayDateStr == selectedDateStr

                    val hasMandirEvents = mandirEvents.any { it.dateStr == dayDateStr }
                    val hasHinduFestivals = hinduFestivals.any { it.day == dayNum }
                    val hasUserNotes = userNotes.any { it.dateStr == dayDateStr }

                    val matchesFilter = when (selectedFilter) {
                        "FESTIVAL" -> hasHinduFestivals
                        "MANDIR" -> hasMandirEvents
                        "NOTES" -> hasUserNotes
                        else -> true
                    }

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) AccentGolden.copy(alpha = 0.25f)
                                else if (hasHinduFestivals && matchesFilter) WarmGold.copy(alpha = 0.20f)
                                else Color.Transparent
                            )
                            .border(
                                1.dp,
                                if (isSelected) AccentGolden else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                haptic.playPremiumHaptic()
                                onDateSelected(dayDateStr)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = dayNum.toString(),
                                fontSize = 13.sp,
                                fontWeight = if (isSelected || hasHinduFestivals || hasMandirEvents) FontWeight.ExtraBold else FontWeight.Normal,
                                color = if (isSelected) AccentGolden else TextWhite
                            )

                            if (matchesFilter) {
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    if (hasHinduFestivals) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(WarmGold)
                                        )
                                    }
                                    if (hasMandirEvents) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(AccentGolden)
                                        )
                                    }
                                    if (hasUserNotes) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(SaffronAccent)
                                        )
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

@Composable
fun MandirCalendarEventCardItem(event: MandirCalendarEvent, isHindi: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(12.dp),
                color = AccentGolden.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = event.dateStr.split("-").getOrNull(2) ?: "1",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentGolden
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = if (isHindi || event.titleEnglish.isEmpty()) event.title else event.titleEnglish,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                if (event.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isHindi || event.descriptionEnglish.isEmpty()) event.description else event.descriptionEnglish,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun PanchangInfoCard(items: List<Pair<String, String>>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = item.first, color = TextMuted, fontSize = 14.sp)
                    Text(text = item.second, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextWhite)
                }
                if (index < items.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = WarmGold.copy(alpha = 0.2f))
                }
            }
        }
    }
}

@Composable
fun SunMoonCard(modifier: Modifier, title: String, time: String, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = AccentGolden, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = title, fontSize = 11.sp, color = TextMuted)
            Text(text = time, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = TextWhite)
        }
    }
}

@Composable
fun PanchangTimingRow(label: String, time: String, isAuspicious: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(if (isAuspicious) Color(0xFF2ECC71) else Color.Red, CircleShape)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(text = label, modifier = Modifier.weight(1f), fontSize = 14.sp, color = TextWhite)
            Text(text = time, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isAuspicious) Color(0xFF2ECC71) else Color.Red)
        }
    }
}

@Composable
private fun SectionHeaderPanchang(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.ExtraBold,
        color = AccentGolden,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
    )
}
