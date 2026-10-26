package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
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
import com.example.miyank.EventCategories
import com.example.miyank.EventItem
import com.example.miyank.EventManager
import com.example.miyank.GuardActionContext
import com.example.miyank.GuardDialogState
import com.example.miyank.LanguageManager
import com.example.miyank.getTodayDateString
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEventsScreen(
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val isHindi = LanguageManager.currentLanguage == com.example.miyank.Language.HINDI

    val allEvents by EventManager.events.collectAsState()
    val today = getTodayDateString()

    // 2.2 Dynamic Summary Dashboard
    val totalCount = allEvents.size
    val activeCount = remember(allEvents) { allEvents.count { it.isActive } }
    val upcomingCount = remember(allEvents, today) { allEvents.count { it.isActive && it.dateStr >= today } }
    val pastCount = remember(allEvents, today) { allEvents.count { it.isActive && it.dateStr < today } }

    // 2.3 Next Upcoming Event
    val featuredEvent = remember(allEvents, today) {
        EventManager.getFeaturedUpcomingEvent()
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Events List, 1: Add/Edit Form
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterCategory by remember { mutableStateOf(EventCategories.ALL) }

    // Filtered Events
    val filteredEvents = remember(allEvents, searchQuery, selectedFilterCategory, today) {
        allEvents.filter { event ->
            val matchesCategory = when (selectedFilterCategory) {
                EventCategories.ALL -> true
                "UPCOMING" -> event.isActive && event.dateStr >= today
                "ACTIVE" -> event.isActive
                "PAST" -> event.isActive && event.dateStr < today
                "IMPORTANT" -> event.isImportant
                else -> event.categoryTag.equals(selectedFilterCategory, ignoreCase = true)
            }

            val matchesSearch = searchQuery.trim().isEmpty() ||
                    event.title.contains(searchQuery, ignoreCase = true) ||
                    event.titleEnglish.contains(searchQuery, ignoreCase = true) ||
                    event.location.contains(searchQuery, ignoreCase = true) ||
                    event.description.contains(searchQuery, ignoreCase = true)

            matchesCategory && matchesSearch
        }
    }

    // Form inputs
    var editingEventItem by remember { mutableStateOf<EventItem?>(null) }
    var titleHindiInput by remember { mutableStateOf("") }
    var titleEngInput by remember { mutableStateOf("") }
    var dateInput by remember { mutableStateOf(today) }
    var timeInput by remember { mutableStateOf("07:00 PM") }
    var locationInput by remember { mutableStateOf("श्री जामनीवाला दरबार") }
    var categoryInput by remember { mutableStateOf(EventCategories.CHOWKI) }
    var descInput by remember { mutableStateOf("") }
    var imageUrlInput by remember { mutableStateOf("") }
    var isImportantInput by remember { mutableStateOf(false) }
    var isActiveInput by remember { mutableStateOf(true) }

    // Guard Dialog State
    var showGuardDialog by remember { mutableStateOf(false) }
    var showSaveGuardDialog by remember { mutableStateOf(false) }
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(SurfaceDark)) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = if (isHindi) "आयोजन एवं उत्सव प्रबंधन" else "Manage Events & Festivals",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextWhite
                            )
                            Text(
                                text = if (isHindi) "धार्मिक आयोजनों, चौकी, भंडारा एवं महाउत्सव की जानकारी प्रबंधित करें" else "Manage Chowki, Bhandara and Temple Festivals",
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
                    actions = {
                        if (selectedTab == 0) {
                            IconButton(onClick = {
                                editingEventItem = null
                                titleHindiInput = ""
                                titleEngInput = ""
                                dateInput = today
                                timeInput = "07:00 PM"
                                locationInput = "श्री जामनीवाला दरबार"
                                categoryInput = EventCategories.CHOWKI
                                descInput = ""
                                imageUrlInput = ""
                                isImportantInput = false
                                isActiveInput = true
                                selectedTab = 1
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Add Event", tint = AccentGolden)
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
                                text = if (isHindi) "🎉 आयोजन सूची (${allEvents.size})" else "Events (${allEvents.size})",
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
                                text = if (editingEventItem != null) (if (isHindi) "✏️ संपादित करें" else "Edit") else (if (isHindi) "➕ नया आयोजन" else "Add Event"),
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
                // TAB 0: EVENTS SUMMARY & LIST
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 2.2 SUMMARY STATS CARDS
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = if (isHindi) "📊 आयोजन सारांश" else "Events Summary",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AccentGolden
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    AdminSummaryStatItem(if (isHindi) "कुल" else "Total", "$totalCount")
                                    AdminSummaryStatItem(if (isHindi) "सक्रिय" else "Active", "$activeCount")
                                    AdminSummaryStatItem(if (isHindi) "आगामी" else "Upcoming", "$upcomingCount")
                                    AdminSummaryStatItem(if (isHindi) "समाप्त" else "Past", "$pastCount")
                                }
                            }
                        }
                    }

                    // 2.3 NEXT UPCOMING EVENT HIGHLIGHT
                    if (featuredEvent != null) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                border = BorderStroke(1.dp, Color.Red)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = Color.Red.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "🔥 अगला आगामी आयोजन (NEXT UPCOMING)",
                                                color = Color.Red,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        Text(
                                            text = featuredEvent.displayDateStr,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentGolden
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = featuredEvent.title,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextWhite
                                    )

                                    Text(
                                        text = "समय: ${featuredEvent.startTime} • स्थान: ${featuredEvent.location}",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }

                    // 2.6 SEARCH BAR & 2.5 FILTERS
                    item {
                        Column {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text(if (isHindi) "आयोजन खोजें..." else "Search Events...") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentGolden) },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextWhite)
                                        }
                                    }
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = SurfaceDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            val filterOptions = listOf(
                                EventCategories.ALL to "सभी",
                                "UPCOMING" to "आगामी",
                                "ACTIVE" to "सक्रिय",
                                "PAST" to "समाप्त",
                                "IMPORTANT" to "विशेष",
                                EventCategories.CHOWKI to "चौकी",
                                EventCategories.BHANDARA to "भंडारा",
                                EventCategories.UTSAV to "उत्सव",
                                EventCategories.ANNIVERSARY to "वार्षिकोत्सव"
                            )

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(filterOptions) { (key, label) ->
                                    val isSelected = selectedFilterCategory == key
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedFilterCategory = key },
                                        label = { Text(label, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentGolden, containerColor = SurfaceDark)
                                    )
                                }
                            }
                        }
                    }

                    // 2.4 EVENT CARDS LIST
                    items(filteredEvents) { item ->
                        AdminEventCard(
                            event = item,
                            isHindi = isHindi,
                            todayStr = today,
                            haptic = haptic,
                            onToggleActive = {
                                EventManager.toggleActive(item.id)
                            },
                            onEdit = {
                                editingEventItem = item
                                titleHindiInput = item.title
                                titleEngInput = item.titleEnglish
                                dateInput = item.dateStr
                                timeInput = item.startTime
                                locationInput = item.location
                                categoryInput = item.categoryTag
                                descInput = item.description
                                imageUrlInput = item.imageUrl
                                isImportantInput = item.isImportant
                                isActiveInput = item.isActive
                                selectedTab = 1
                            },
                            onDelete = {
                                pendingDeleteId = item.id
                                showGuardDialog = true
                            }
                        )
                    }
                }
            } else {
                // TAB 1: ADD / EDIT EVENT FORM WITH LIVE PREVIEW
                val formScrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .verticalScroll(formScrollState)
                        .padding(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (isHindi) "SECTION A — मूल जानकारी (Basic Info)" else "SECTION A — Basic Info",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AccentGolden
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = titleHindiInput,
                                onValueChange = { titleHindiInput = it },
                                label = { Text(if (isHindi) "आयोजन शीर्षक (हिंदी) *" else "Event Title (Hindi) *") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = AccentGolden) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = titleEngInput,
                                onValueChange = { titleEngInput = it },
                                label = { Text(if (isHindi) "आयोजन शीर्षक (अंग्रेज़ी)" else "Event Title (English)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = descInput,
                                onValueChange = { descInput = it },
                                label = { Text(if (isHindi) "आयोजन विवरण (Description)" else "Description") },
                                modifier = Modifier.fillMaxWidth().height(90.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (isHindi) "SECTION B — दिनांक एवं स्थान (Schedule & Location)" else "SECTION B — Schedule & Location",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AccentGolden
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = dateInput,
                                    onValueChange = { dateInput = it },
                                    label = { Text("Date (YYYY-MM-DD)") },
                                    modifier = Modifier.weight(1f),
                                    leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = AccentGolden) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                                )

                                OutlinedTextField(
                                    value = timeInput,
                                    onValueChange = { timeInput = it },
                                    label = { Text("Time (e.g. 07:00 PM)") },
                                    modifier = Modifier.weight(1f),
                                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = AccentGolden) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = locationInput,
                                onValueChange = { locationInput = it },
                                label = { Text(if (isHindi) "स्थान (Location)" else "Location") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = AccentGolden) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (isHindi) "SECTION C — श्रेणी एवं मीडिया (Category & Media)" else "SECTION C — Category & Media",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AccentGolden
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(if (isHindi) "श्रेणी *" else "Category *", fontSize = 11.sp, color = AccentGolden, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                EventCategories.adminList.forEach { catKey ->
                                    val isSelected = categoryInput.equals(catKey, ignoreCase = true)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { categoryInput = catKey },
                                        label = { Text(EventCategories.getDisplayName(catKey, isHindi), fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentGolden, containerColor = BackgroundDark)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = imageUrlInput,
                                onValueChange = { imageUrlInput = it },
                                label = { Text(if (isHindi) "इमेज यूआरएल (Optional)" else "Image URL (Optional)") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Image, contentDescription = null, tint = AccentGolden) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                            )

                            if (imageUrlInput.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth().height(120.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    AsyncImage(
                                        model = imageUrlInput,
                                        contentDescription = "Preview",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(if (isHindi) "विशेष आयोजन (Important)" else "Important Event", color = TextWhite, fontSize = 13.sp)
                                Switch(
                                    checked = isImportantInput,
                                    onCheckedChange = { isImportantInput = it }
                                )
                            }

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
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2.9 LIVE FORM PREVIEW CARD
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = BorderStroke(1.dp, AccentGolden.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (isHindi) "🔍 लाइव फ़ॉर्म पूर्वावलोकन (Live Preview)" else "🔍 Live Form Preview",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentGolden
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = titleHindiInput.ifEmpty { "आयोजन शीर्षक पूर्वावलोकन" },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextWhite
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "दिनांक: $dateInput • समय: $timeInput", fontSize = 11.sp, color = AccentGolden)
                            Text(text = "स्थान: $locationInput • श्रेणी: ${EventCategories.getDisplayName(categoryInput, isHindi)}", fontSize = 11.sp, color = TextMuted)

                            if (descInput.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = descInput, fontSize = 11.sp, color = TextMuted, maxLines = 2)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    AdminSaveButton(
                        text = if (isHindi) "सेव आयोजन" else "Save Event",
                        textEnglish = "Save Event",
                        isEnabled = titleHindiInput.trim().isNotEmpty() && dateInput.trim().isNotEmpty(),
                        isHindi = isHindi,
                        onSaveClick = {
                            haptic.playPremiumHaptic()
                            showSaveGuardDialog = true
                        }
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // SAVE GUARD DIALOG
    AdminActionGuardDialog(
        state = GuardDialogState(
            isVisible = showSaveGuardDialog,
            context = GuardActionContext.PUBLISH_MAJOR_CHANGE,
            title = if (isHindi) "आयोजन सेव करें" else "Confirm Save Event",
            message = if (isHindi) "क्या आप वाकई में इस आयोजन को सेव और प्रकाशित करना चाहते हैं?" else "Are you sure you want to save and publish this event?",
            onConfirm = {
                showSaveGuardDialog = false
                val newItem = EventItem(
                    id = editingEventItem?.id ?: "",
                    title = titleHindiInput.trim(),
                    titleEnglish = titleEngInput.trim(),
                    dateStr = dateInput.trim(),
                    displayDateStr = "${dateInput.trim()}, ${if (isHindi) "आयोजन" else "Event"}",
                    startTime = timeInput.trim(),
                    location = locationInput.trim(),
                    categoryTag = categoryInput,
                    description = descInput.trim(),
                    imageUrl = imageUrlInput.trim(),
                    isImportant = isImportantInput,
                    isActive = isActiveInput,
                    createdAt = editingEventItem?.createdAt ?: 0L
                )
                EventManager.addOrUpdateEvent(newItem)
                statusMessage = if (isHindi) "आयोजन सफलतापूर्वक सेव एवं प्रकाशित हो गया! ✓" else "Event saved and published successfully! ✓"
                selectedTab = 0
            },
            onDismiss = { showSaveGuardDialog = false }
        ),
        isHindi = isHindi
    )

    // DELETE GUARD DIALOG
    AdminActionGuardDialog(
        state = GuardDialogState(
            isVisible = showGuardDialog,
            context = GuardActionContext.DELETE_ITEM,
            title = if (isHindi) "आयोजन हटाने की पुष्टि" else "Confirm Event Deletion",
            message = if (isHindi) "क्या आप वाकई में इस आयोजन को हटाना चाहते हैं?" else "Are you sure you want to delete this event?",
            onConfirm = {
                showGuardDialog = false
                if (pendingDeleteId != null) {
                    EventManager.deleteEvent(pendingDeleteId!!)
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
private fun AdminSummaryStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = AccentGolden)
        Text(text = label, fontSize = 11.sp, color = TextMuted)
    }
}

@Composable
private fun AdminEventCard(
    event: EventItem,
    isHindi: Boolean,
    todayStr: String,
    haptic: com.example.miyank.HapticManager,
    onToggleActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isPast = event.dateStr < todayStr
    val statusText = if (!event.isActive) "INACTIVE" else if (isPast) "PAST" else "UPCOMING"
    val statusColor = if (!event.isActive) TextMuted else if (isPast) TextMuted else Color(0xFF81C784)

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
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.title,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = TextWhite
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = statusText,
                            color = statusColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    if (event.isImportant) {
                        Icon(Icons.Default.Star, contentDescription = "Important", tint = AccentGolden, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Switch(
                        checked = event.isActive,
                        onCheckedChange = {
                            haptic.playPremiumHaptic()
                            onToggleActive()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "दिनांक: ${event.displayDateStr} • समय: ${event.startTime}", fontSize = 12.sp, color = AccentGolden, fontWeight = FontWeight.Bold)
            Text(text = "स्थान: ${event.location} • श्रेणी: ${EventCategories.getDisplayName(event.categoryTag, isHindi)}", fontSize = 11.sp, color = TextMuted)

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
