package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.miyank.LocalStrings
import com.example.miyank.SuvicharItem
import com.example.miyank.SuvicharManager
import com.example.miyank.formatDisplayDate
import com.example.miyank.formatEpochToDateString
import com.example.miyank.getTodayDateString
import com.example.miyank.rememberFilePickerLauncher
import com.example.miyank.rememberHapticManager
import com.example.miyank.rememberSuvicharMediaHandler
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSuvicharScreen(
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val strings = LocalStrings.current
    val suvichars by SuvicharManager.suvichars.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<SuvicharItem?>(null) }
    var previewingItem by remember { mutableStateOf<SuvicharItem?>(null) }
    var deletingItem by remember { mutableStateOf<SuvicharItem?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var showUnsavedDialog by remember { mutableStateOf(false) }

    val todayDateStr = getTodayDateString()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.manageSuvicharTitle, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (showAddDialog) {
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
                    editingItem = null
                    showAddDialog = true
                },
                containerColor = AccentGolden,
                contentColor = BackgroundDark,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(strings.scheduleSuvichar) }
            )
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
        ) {
            // Header Stats Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(AccentGolden.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = AccentGolden)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${suvichars.size} Suvichars Scheduled",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "Today: ${formatDisplayDate(todayDateStr)}",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
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
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Scheduled Daily Suvichar List",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AccentGolden
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (suvichars.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Suvichars scheduled yet.\nTap '+ Schedule Suvichar' to add content.",
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(suvichars) { item ->
                        AdminSuvicharCard(
                            item = item,
                            todayDateStr = todayDateStr,
                            onEdit = {
                                editingItem = item
                                showAddDialog = true
                            },
                            onDelete = {
                                haptic.playPremiumHaptic()
                                deletingItem = item
                            },
                            onPreview = {
                                previewingItem = item
                            }
                        )
                    }
                }
            }
        }
    }

    // ADD / EDIT DIALOG
    if (showAddDialog) {
        AddEditSuvicharDialog(
            existingItem = editingItem,
            todayDateStr = todayDateStr,
            onDismiss = { showAddDialog = false },
            onSaveConfirmed = { newItem ->
                if (SuvicharManager.isDateAlreadyScheduled(newItem.date, newItem.id)) {
                    statusMessage = "इस तारीख (${newItem.date}) के लिए सुविचार पहले से मौजूद है।"
                } else {
                    SuvicharManager.addOrUpdateSuvichar(newItem)
                    statusMessage = "Suvichar for ${newItem.date} saved & scheduled!"
                    showAddDialog = false
                }
            }
        )
    }

    // UNSAVED CHANGES GUARD DIALOG
    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            containerColor = SurfaceDark,
            titleContentColor = TextWhite,
            textContentColor = TextWhite,
            title = {
                Text(
                    text = "Unsaved Changes",
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentGolden,
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    text = "⚠️ You have unsaved changes in Suvichar setup! Do you really want to discard your changes and leave?",
                    color = TextWhite,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUnsavedDialog = false
                        showAddDialog = false
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Discard & Leave", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnsavedDialog = false }) {
                    Text("Stay & Edit", color = AccentGolden, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // DELETE CONFIRMATION DIALOG WITH RED LINE WARNING
    deletingItem?.let { itemToDelete ->
        AlertDialog(
            onDismissRequest = { deletingItem = null },
            containerColor = SurfaceDark,
            titleContentColor = Color.Red,
            textContentColor = TextWhite,
            title = {
                Text(
                    text = "Confirm Deletion",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Red,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "⚠️ WARNING: Are you really sure you want to permanently delete this Suvichar item for ${itemToDelete.date}?",
                        color = Color.Red,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "This action cannot be undone.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        AsyncImage(
                            model = itemToDelete.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = itemToDelete.id
                        val date = itemToDelete.date
                        deletingItem = null
                        SuvicharManager.deleteSuvichar(id)
                        statusMessage = "Suvichar for $date permanently deleted!"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Yes, Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingItem = null }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    // PREVIEW DIALOG
    previewingItem?.let { previewItem ->
        AlertDialog(
            onDismissRequest = { previewingItem = null },
            containerColor = SurfaceDark,
            title = {
                Text(
                    text = "Suvichar Preview (${formatDisplayDate(previewItem.date)})",
                    color = AccentGolden,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.80f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = previewItem.imageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                            if (previewItem.quoteText.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.BottomCenter)
                                        .background(Color.Black.copy(alpha = 0.8f))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = previewItem.quoteText,
                                        color = TextWhite,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { previewingItem = null },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                ) {
                    Text("Close", color = BackgroundDark, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun AdminSuvicharCard(
    item: SuvicharItem,
    todayDateStr: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPreview: () -> Unit
) {
    val isToday = item.date == todayDateStr
    val isFuture = item.date > todayDateStr

    val statusColor = when {
        !item.isActive -> Color.Gray
        isToday -> Color(0xFF2ECC71)
        isFuture -> Color(0xFF3498DB)
        else -> Color.LightGray
    }

    val statusText = when {
        !item.isActive -> "⚪ Disabled"
        isToday -> "🟢 Published (Today)"
        isFuture -> "🔵 Scheduled"
        else -> "⚪ Past"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier.size(70.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🗓️ ${formatDisplayDate(item.date)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = TextWhite
                )

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                if (item.quoteText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.quoteText,
                        fontSize = 11.sp,
                        color = TextMuted,
                        maxLines = 1
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onPreview, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Visibility, contentDescription = "Preview", tint = AccentGolden, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextWhite, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSuvicharDialog(
    existingItem: SuvicharItem?,
    todayDateStr: String,
    onDismiss: () -> Unit,
    onSaveConfirmed: (SuvicharItem) -> Unit
) {
    var dateInput by remember { mutableStateOf(existingItem?.date ?: todayDateStr) }
    var imageUrlInput by remember { mutableStateOf(existingItem?.imageUrl ?: "") }
    var quoteInput by remember { mutableStateOf(existingItem?.quoteText ?: "") }
    var authorInput by remember { mutableStateOf(existingItem?.author ?: "श्री जामनीवाला दरबार") }
    var isActiveInput by remember { mutableStateOf(existingItem?.isActive ?: true) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var pendingSaveItem by remember { mutableStateOf<SuvicharItem?>(null) }
    var rawPickedPath by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val filePicker = rememberFilePickerLauncher("image/*") { filePath, _, fileSize ->
        if (filePath != null) {
            val lower = filePath.lowercase()
            val isImage = lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
                          lower.endsWith(".png") || lower.endsWith(".webp") ||
                          lower.endsWith(".bmp") || lower.endsWith(".gif")

            if (!isImage) {
                errorText = "❌ File type not supported for Suvichar! Please select a valid Image file (JPG, PNG, WEBP)."
                imageUrlInput = ""
            } else {
                val sizeMb = fileSize / (1024f * 1024f)
                if (sizeMb > 10.0f) {
                    errorText = "❌ Image size (${sizeMb.toString().take(3)} MB) exceeds 10.0 MB limit!"
                    imageUrlInput = ""
                } else {
                    rawPickedPath = filePath
                    errorText = null
                }
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
                text = if (existingItem == null) "Schedule Daily Suvichar" else "Edit Suvichar (${existingItem.date})",
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
                // INTERACTIVE DATE SELECTION FIELD WITH CALENDAR PICKER
                OutlinedTextField(
                    value = dateInput,
                    onValueChange = { dateInput = it },
                    label = { Text("Publication Date (YYYY-MM-DD)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Pick Date", tint = AccentGolden)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGolden,
                        unfocusedBorderColor = BackgroundDark,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // IMAGE UPLOAD BUTTON
                Button(
                    onClick = { filePicker() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, tint = BackgroundDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (imageUrlInput.isEmpty()) "Upload Suvichar Image (Images Only)" else "Change Image ✓",
                        color = BackgroundDark,
                        fontWeight = FontWeight.Bold
                    )
                }

                // LIVE IMAGE PREVIEW
                if (imageUrlInput.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.80f),
                        shape = RoundedCornerShape(16.dp)
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

                // QUOTE INPUT
                OutlinedTextField(
                    value = quoteInput,
                    onValueChange = { quoteInput = it },
                    label = { Text("Devotional Quote (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGolden,
                        unfocusedBorderColor = BackgroundDark,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // AUTHOR INPUT
                OutlinedTextField(
                    value = authorInput,
                    onValueChange = { authorInput = it },
                    label = { Text("Author / Source") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGolden,
                        unfocusedBorderColor = BackgroundDark,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ACTIVE TOGGLE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Enable & Publish", fontWeight = FontWeight.Bold, color = TextWhite)
                    Switch(
                        checked = isActiveInput,
                        onCheckedChange = { isActiveInput = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = AccentGolden)
                    )
                }

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorText!!,
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            com.example.miyank.AdminSaveButton(
                text = "सुविचार सहेजें",
                textEnglish = "Save Suvichar",
                isEnabled = dateInput.trim().isNotEmpty() && imageUrlInput.trim().isNotEmpty(),
                onSaveClick = {
                    if (dateInput.trim().isEmpty() || !dateInput.contains("-")) {
                        errorText = "❌ Please enter date in YYYY-MM-DD format (e.g. 2026-09-05)"
                        return@AdminSaveButton
                    }
                    if (imageUrlInput.trim().isEmpty()) {
                        errorText = "❌ Please select/upload a valid Suvichar image!"
                        return@AdminSaveButton
                    }

                    pendingSaveItem = SuvicharItem(
                        id = existingItem?.id ?: "",
                        date = dateInput.trim(),
                        imageUrl = imageUrlInput,
                        quoteText = quoteInput.trim(),
                        author = authorInput.trim(),
                        isActive = isActiveInput
                    )
                },
                onCancelClick = onDismiss
            )
        }
    )

    // MATERIAL 3 INTERACTIVE CALENDAR DATE PICKER DIALOG
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

    // IMAGE CROPPER DIALOG
    rawPickedPath?.let { rawPath ->
        ImageCropperDialog(
            rawPath = rawPath,
            onDismiss = { rawPickedPath = null },
            onCropped = { finalPath ->
                imageUrlInput = finalPath
                rawPickedPath = null
                errorText = null
            }
        )
    }

    // FINAL CONFIRMATION DIALOG
    pendingSaveItem?.let { saveItem ->
        AlertDialog(
            onDismissRequest = { pendingSaveItem = null },
            containerColor = SurfaceDark,
            titleContentColor = TextWhite,
            textContentColor = TextWhite,
            title = {
                Text(
                    text = "Confirm Suvichar Publication",
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentGolden,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Are you really sure you want to add and schedule this image for Daily Suvichar on ${saveItem.date}?",
                        color = TextWhite,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.80f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        AsyncImage(
                            model = saveItem.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toSave = saveItem
                        pendingSaveItem = null
                        onSaveConfirmed(toSave)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                ) {
                    Text("Yes, Publish / Schedule", color = BackgroundDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingSaveItem = null }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

@Composable
fun ImageCropperDialog(
    rawPath: String,
    onDismiss: () -> Unit,
    onCropped: (String) -> Unit
) {
    var scale by remember { mutableStateOf(1.0f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var isCropping by remember { mutableStateOf(false) }

    val mediaHandler = rememberSuvicharMediaHandler()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BackgroundDark,
        titleContentColor = TextWhite,
        textContentColor = TextWhite,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Crop & Adjust Image",
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentGolden,
                    fontSize = 16.sp
                )
                Surface(
                    color = AccentGolden.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("4:5 Frame", color = AccentGolden, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Drag or pinch to adjust photo inside the 4:5 crop grid:",
                    fontSize = 12.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 4:5 CROP CONTAINER WITH 3x3 GRID OVERLAY
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.80f)
                        .clip(RoundedCornerShape(20.dp))
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1.0f, 3.5f)
                                offsetX = (offsetX + pan.x).coerceIn(-300f, 300f)
                                offsetY = (offsetY + pan.y).coerceIn(-300f, 300f)
                            }
                        },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Transformable Image
                        AsyncImage(
                            model = rawPath,
                            contentDescription = "Crop View",
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offsetX,
                                    translationY = offsetY
                                ),
                            contentScale = ContentScale.Crop
                        )

                        // 3x3 Grid Overlay
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                Box(modifier = Modifier.weight(1f).fillMaxHeight().border(0.5.dp, Color.White.copy(alpha = 0.4f)))
                                Box(modifier = Modifier.weight(1f).fillMaxHeight().border(0.5.dp, Color.White.copy(alpha = 0.4f)))
                                Box(modifier = Modifier.weight(1f).fillMaxHeight().border(0.5.dp, Color.White.copy(alpha = 0.4f)))
                            }
                            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                Box(modifier = Modifier.weight(1f).fillMaxHeight().border(0.5.dp, Color.White.copy(alpha = 0.4f)))
                                Box(modifier = Modifier.weight(1f).fillMaxHeight().border(0.5.dp, Color.White.copy(alpha = 0.4f)))
                                Box(modifier = Modifier.weight(1f).fillMaxHeight().border(0.5.dp, Color.White.copy(alpha = 0.4f)))
                            }
                            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                Box(modifier = Modifier.weight(1f).fillMaxHeight().border(0.5.dp, Color.White.copy(alpha = 0.4f)))
                                Box(modifier = Modifier.weight(1f).fillMaxHeight().border(0.5.dp, Color.White.copy(alpha = 0.4f)))
                                Box(modifier = Modifier.weight(1f).fillMaxHeight().border(0.5.dp, Color.White.copy(alpha = 0.4f)))
                            }
                        }

                        // Golden Corner Handles
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(3.dp, WarmGold, RoundedCornerShape(20.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            scale = 1.0f
                            offsetX = 0f
                            offsetY = 0f
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentGolden)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset Frame", fontSize = 11.sp)
                    }

                    Text("Zoom: ${(scale * 100).toInt()}%", fontSize = 12.sp, color = AccentGolden, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isCropping = true
                    val croppedPath = mediaHandler.cropAndCompressBitmap(
                        rawPath = rawPath,
                        scale = scale,
                        offsetX = offsetX,
                        offsetY = offsetY
                    )
                    isCropping = false

                    if (croppedPath != null) {
                        onCropped(croppedPath)
                    } else {
                        onCropped(rawPath)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentGolden),
                enabled = !isCropping
            ) {
                if (isCropping) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BackgroundDark)
                } else {
                    Text("Crop & Apply Image", color = BackgroundDark, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}
