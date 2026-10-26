package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
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
import com.example.miyank.GuardActionContext
import com.example.miyank.GuardDialogState
import com.example.miyank.LanguageManager
import com.example.miyank.MandirCalendarEvent
import com.example.miyank.MandirCalendarManager
import com.example.miyank.PanchangProvider
import com.example.miyank.formatDisplayDate
import com.example.miyank.formatEpochToDateString
import com.example.miyank.getEpochMillis
import com.example.miyank.getTodayDateString
import com.example.miyank.rememberHapticManager
import com.example.miyank.rememberImageOptimizer
import com.example.miyank.rememberMultipleFilePickerLauncher
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMandirCalendarScreen(
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val imageOptimizer = rememberImageOptimizer()
    val isHindi = LanguageManager.currentLanguage == com.example.miyank.Language.HINDI

    val eventsState by MandirCalendarManager.events.collectAsState()

    var editableEvents by remember(eventsState) {
        mutableStateOf(eventsState.toMutableList())
    }

    var currentYear by remember { mutableStateOf(2026) }
    var currentMonthIndex by remember { mutableStateOf(9) } // 1-12 (September)
    var selectedDateStr by remember { mutableStateOf(getTodayDateString()) }

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<MandirCalendarEvent?>(null) }
    var deletingItem by remember { mutableStateOf<MandirCalendarEvent?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var showSaveConfirmDialog by remember { mutableStateOf(false) }

    val hasUnsavedChanges = editableEvents != eventsState

    val monthEvents = remember(editableEvents, currentYear, currentMonthIndex) {
        val monthPrefix = "${currentYear}-${currentMonthIndex.toString().padStart(2, '0')}"
        editableEvents.filter { it.dateStr.startsWith(monthPrefix) }
    }

    val selectedDateEvents = remember(editableEvents, selectedDateStr) {
        editableEvents.filter { it.dateStr == selectedDateStr }
    }

    val monthNamesHindi = arrayOf("", "जनवरी", "फरवरी", "मार्च", "अप्रैल", "मई", "जून", "जुलाई", "अगस्त", "सितम्बर", "अक्टूबर", "नवम्बर", "दिसम्बर")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isHindi) "मंदिर कैलेंडर प्रबंधन (Admin)" else "Manage Mandir Calendar", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasUnsavedChanges) {
                            showUnsavedDialog = true
                        } else {
                            onBackClick()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark,
                    titleContentColor = TextWhite,
                    navigationIconContentColor = TextWhite
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    haptic.playPremiumHaptic()
                    editingItem = MandirCalendarEvent(dateStr = selectedDateStr)
                    showAddEditDialog = true
                },
                containerColor = AccentGolden,
                contentColor = BackgroundDark,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(if (isHindi) "कार्यक्रम जोड़ें" else "Add Event") }
            )
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Stats & Save Action Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isHindi) "कैलेंडर डैशबोर्ड" else "Calendar Dashboard",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextWhite
                            )
                            Text(
                                text = if (isHindi) "इस महीने के कार्यक्रम: ${monthEvents.size}" else "Events in Month: ${monthEvents.size}",
                                fontSize = 12.sp,
                                color = AccentGolden,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                haptic.playPremiumHaptic()
                                showSaveConfirmDialog = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isHindi) "सेव करें" else "Save Settings", color = BackgroundDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            if (statusMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = AccentGolden.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AccentGolden.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = statusMessage!!,
                        color = TextWhite,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // MONTH SELECTOR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    haptic.playPremiumHaptic()
                    currentMonthIndex--
                    if (currentMonthIndex < 1) {
                        currentMonthIndex = 12
                        currentYear--
                    }
                }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Month", tint = AccentGolden)
                }

                Text(
                    text = "${monthNamesHindi.getOrElse(currentMonthIndex) { "सितम्बर" }} $currentYear",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentGolden
                )

                IconButton(onClick = {
                    haptic.playPremiumHaptic()
                    currentMonthIndex++
                    if (currentMonthIndex > 12) {
                        currentMonthIndex = 1
                        currentYear++
                    }
                }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Month", tint = AccentGolden)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // MONTHLY CALENDAR GRID
            AdminCalendarGrid(
                year = currentYear,
                month = currentMonthIndex,
                selectedDateStr = selectedDateStr,
                events = monthEvents,
                isHindi = isHindi,
                onDateSelected = { date ->
                    haptic.playPremiumHaptic()
                    selectedDateStr = date
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // SELECTED DATE EVENTS LIST
            Text(
                text = "${formatDisplayDate(selectedDateStr)} के कार्यक्रम (${selectedDateEvents.size})",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = AccentGolden
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (selectedDateEvents.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SurfaceDark,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = if (isHindi) "इस तारीख पर कोई कार्यक्रम निर्धारित नहीं है। '+ कार्यक्रम जोड़ें' पर क्लिक करें।" else "No events scheduled on this date. Click '+ Add Event' to add item.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    selectedDateEvents.forEach { event ->
                        AdminCalendarEventCard(
                            event = event,
                            isHindi = isHindi,
                            onToggleStatus = {
                                val idx = editableEvents.indexOfFirst { it.id == event.id }
                                if (idx >= 0) {
                                    val updated = editableEvents.toMutableList()
                                    updated[idx] = event.copy(isActive = !event.isActive)
                                    editableEvents = updated
                                }
                            },
                            onEdit = {
                                editingItem = event
                                showAddEditDialog = true
                            },
                            onDelete = {
                                deletingItem = event
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // ADD / EDIT EVENT DIALOG
    if (showAddEditDialog) {
        AddEditCalendarEventDialog(
            existingItem = editingItem,
            defaultDateStr = selectedDateStr,
            isHindi = isHindi,
            imageOptimizer = imageOptimizer,
            onDismiss = { showAddEditDialog = false },
            onSave = { updatedItem ->
                val current = editableEvents.toMutableList()
                val idx = current.indexOfFirst { it.id == updatedItem.id }
                if (idx >= 0) {
                    current[idx] = updatedItem
                } else {
                    current.add(0, updatedItem)
                }
                editableEvents = current
                statusMessage = if (isHindi) "कार्यक्रम ड्राफ्ट में अपडेट हुआ! 'सेव करें' दबाएँ ✓" else "Event draft updated! Tap 'Save Settings' to apply ✓"
                showAddEditDialog = false
            }
        )
    }

    // CONFIRM SAVE SETTINGS DIALOG USING GUARD
    AdminActionGuardDialog(
        state = GuardDialogState(
            isVisible = showSaveConfirmDialog,
            context = GuardActionContext.PUBLISH_MAJOR_CHANGE,
            title = if (isHindi) "कैलेंडर बदलाव सेव करें" else "Save Calendar Changes",
            message = if (isHindi) "क्या आप वाकई में मंदिर कैलेंडर के इन बदलावों को सेव और प्रकाशित करना चाहते हैं?" else "Are you sure you want to save and publish these calendar updates?",
            onConfirm = {
                showSaveConfirmDialog = false
                haptic.playPremiumHaptic()
                MandirCalendarManager.saveEvents(editableEvents)
                statusMessage = if (isHindi) "💾 मंदिर कैलेंडर सफलतापूर्वक सेव एवं अपडेट हो गया! ✓" else "💾 Mandir Calendar Saved Successfully! ✓"
            },
            onDismiss = { showSaveConfirmDialog = false }
        ),
        isHindi = isHindi
    )

    // UNSAVED CHANGES GUARD DIALOG
    AdminActionGuardDialog(
        state = GuardDialogState(
            isVisible = showUnsavedDialog,
            context = GuardActionContext.UNSAVED_CHANGES,
            onConfirm = {
                showUnsavedDialog = false
                MandirCalendarManager.saveEvents(editableEvents)
                onBackClick()
            },
            onDiscard = {
                showUnsavedDialog = false
                onBackClick()
            },
            onDismiss = { showUnsavedDialog = false }
        ),
        isHindi = isHindi
    )

    // DELETE CONFIRMATION DIALOG WITH PREVIEW
    deletingItem?.let { eventToDelete ->
        AdminActionGuardDialog(
            state = GuardDialogState(
                isVisible = true,
                context = GuardActionContext.DELETE_ITEM,
                itemTitle = eventToDelete.title,
                itemCategory = "🗓️ ${formatDisplayDate(eventToDelete.dateStr)}",
                itemImageUrl = eventToDelete.imageUrl,
                onConfirm = {
                    val id = eventToDelete.id
                    deletingItem = null
                    editableEvents = editableEvents.filter { it.id != id }.toMutableList()
                    statusMessage = if (isHindi) "कार्यक्रम हटाया गया! 'सेव करें' दबाएँ।" else "Event removed! Tap 'Save Settings' to confirm."
                },
                onDismiss = { deletingItem = null }
            ),
            isHindi = isHindi
        )
    }
}

@Composable
fun AdminCalendarGrid(
    year: Int,
    month: Int,
    selectedDateStr: String,
    events: List<MandirCalendarEvent>,
    isHindi: Boolean,
    onDateSelected: (String) -> Unit
) {
    val daysOfWeek = if (isHindi) listOf("सोम", "मंगल", "बुध", "गुरु", "शुक्र", "शनि", "रवि")
    else listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    val totalDays = PanchangProvider.getDaysInMonth(year, month)

    Column(modifier = Modifier.fillMaxWidth()) {
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
                    val dayEvents = events.filter { it.dateStr == dayDateStr }

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) AccentGolden.copy(alpha = 0.25f)
                                else if (dayEvents.isNotEmpty()) WarmGold.copy(alpha = 0.15f)
                                else Color.Transparent
                            )
                            .border(
                                1.dp,
                                if (isSelected) AccentGolden else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onDateSelected(dayDateStr) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = dayNum.toString(),
                                fontSize = 13.sp,
                                fontWeight = if (isSelected || dayEvents.isNotEmpty()) FontWeight.ExtraBold else FontWeight.Normal,
                                color = if (isSelected) AccentGolden else TextWhite
                            )
                            if (dayEvents.isNotEmpty()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    dayEvents.take(3).forEach {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(AccentGolden)
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
fun AdminCalendarEventCard(
    event: MandirCalendarEvent,
    isHindi: Boolean,
    onToggleStatus: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = AccentGolden.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = event.categoryTag,
                            color = AccentGolden,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (event.startTime.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(event.startTime, fontSize = 10.sp, color = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isHindi || event.titleEnglish.isEmpty()) event.title else event.titleEnglish,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Switch(
                    checked = event.isActive,
                    onCheckedChange = { onToggleStatus() },
                    colors = SwitchDefaults.colors(checkedThumbColor = AccentGolden)
                )
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextWhite, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCalendarEventDialog(
    existingItem: MandirCalendarEvent?,
    defaultDateStr: String,
    isHindi: Boolean,
    imageOptimizer: com.example.miyank.ImageOptimizer,
    onDismiss: () -> Unit,
    onSave: (MandirCalendarEvent) -> Unit
) {
    var titleInput by remember { mutableStateOf(existingItem?.title ?: "") }
    var titleEngInput by remember { mutableStateOf(existingItem?.titleEnglish ?: "") }
    var dateInput by remember { mutableStateOf(existingItem?.dateStr ?: defaultDateStr) }
    var startTimeInput by remember { mutableStateOf(existingItem?.startTime ?: "07:00 AM") }
    var endTimeInput by remember { mutableStateOf(existingItem?.endTime ?: "08:30 PM") }
    var categoryInput by remember { mutableStateOf(existingItem?.categoryTag ?: "FESTIVAL") }
    var descriptionInput by remember { mutableStateOf(existingItem?.description ?: "") }
    var imageUrlInput by remember { mutableStateOf(existingItem?.imageUrl ?: "") }
    var isImportantInput by remember { mutableStateOf(existingItem?.isImportant ?: false) }
    var isActiveInput by remember { mutableStateOf(existingItem?.isActive ?: true) }

    var errorText by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val filePicker = rememberMultipleFilePickerLauncher("image/*") { pickedFiles ->
        if (pickedFiles.isNotEmpty()) {
            val (path, _, size) = pickedFiles.first()
            val sizeMb = size / (1024f * 1024f)
            if (sizeMb > 10.0f) {
                errorText = if (isHindi) "❌ फोटो का आकार 10 MB सीमा से अधिक है!" else "❌ Image size exceeds 10 MB limit!"
            } else {
                val optResult = imageOptimizer.processAndOptimizeImage(path)
                imageUrlInput = optResult?.optimizedFullImagePath ?: path
                errorText = null
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        titleContentColor = TextWhite,
        textContentColor = TextWhite,
        title = {
            Text(
                text = if (existingItem == null || existingItem.id.isEmpty()) (if (isHindi) "नया कार्यक्रम जोड़ें" else "New Calendar Event") else (if (isHindi) "कार्यक्रम संपादित करें" else "Edit Event"),
                color = AccentGolden,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Title Hindi
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text(if (isHindi) "कार्यक्रम नाम (हिन्दी)" else "Event Title (Hindi)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Date Field with Calendar Picker
                OutlinedTextField(
                    value = dateInput,
                    onValueChange = { dateInput = it },
                    label = { Text("Event Date (YYYY-MM-DD)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Pick Date", tint = AccentGolden)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTimeInput,
                        onValueChange = { startTimeInput = it },
                        label = { Text("Start Time") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = endTimeInput,
                        onValueChange = { endTimeInput = it },
                        label = { Text("End Time") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                OutlinedTextField(
                    value = descriptionInput,
                    onValueChange = { descriptionInput = it },
                    label = { Text(if (isHindi) "विवरण (Description)" else "Description") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Image Upload
                Button(
                    onClick = { filePicker() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = BackgroundDark)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (imageUrlInput.isEmpty()) (if (isHindi) "फोटो जोड़ें (Optional)" else "Attach Photo (Optional)") else (if (isHindi) "फोटो बदलें ✓" else "Change Photo ✓"),
                        color = BackgroundDark,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Active Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(if (isHindi) "कैलेंडर में एक्टिव रखें" else "Active in Calendar", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 13.sp)
                    Switch(
                        checked = isActiveInput,
                        onCheckedChange = { isActiveInput = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = AccentGolden)
                    )
                }

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorText!!, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            com.example.miyank.AdminSaveButton(
                text = "कार्यक्रम सहेजें",
                textEnglish = "Save Calendar Event",
                isEnabled = titleInput.trim().isNotEmpty(),
                onSaveClick = {
                    if (titleInput.trim().isEmpty()) {
                        errorText = if (isHindi) "❌ कृपया कार्यक्रम नाम दर्ज करें!" else "❌ Please enter title!"
                        return@AdminSaveButton
                    }

                    val item = MandirCalendarEvent(
                        id = existingItem?.id ?: "",
                        title = titleInput.trim(),
                        titleEnglish = titleEngInput.trim(),
                        dateStr = dateInput.trim(),
                        startTime = startTimeInput.trim(),
                        endTime = endTimeInput.trim(),
                        categoryTag = categoryInput,
                        description = descriptionInput.trim(),
                        imageUrl = imageUrlInput,
                        isImportant = isImportantInput,
                        isActive = isActiveInput,
                        createdAt = existingItem?.createdAt ?: getEpochMillis()
                    )
                    onSave(item)
                },
                onCancelClick = onDismiss
            )
        }
    )

    // MATERIAL 3 INTERACTIVE CALENDAR DATE PICKER
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            dateInput = formatEpochToDateString(selectedMillis)
                        }
                        showDatePicker = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                ) {
                    Text("Select Date", color = BackgroundDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = SurfaceDark)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = SurfaceDark,
                    titleContentColor = AccentGolden,
                    headlineContentColor = TextWhite,
                    weekdayContentColor = AccentGolden,
                    subheadContentColor = TextWhite,
                    yearContentColor = TextWhite,
                    currentYearContentColor = AccentGolden,
                    selectedYearContentColor = BackgroundDark,
                    selectedYearContainerColor = AccentGolden,
                    dayContentColor = TextWhite,
                    disabledDayContentColor = TextMuted,
                    selectedDayContentColor = BackgroundDark,
                    selectedDayContainerColor = AccentGolden,
                    todayContentColor = AccentGolden,
                    todayDateBorderColor = AccentGolden
                )
            )
        }
    }
}
