package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
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
import com.example.miyank.AnnouncementCategories
import com.example.miyank.AnnouncementItem
import com.example.miyank.AnnouncementManager
import com.example.miyank.LanguageManager
import com.example.miyank.LocalStrings
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
fun AdminAnnouncementsScreen(
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val imageOptimizer = rememberImageOptimizer()
    val isHindi = LanguageManager.currentLanguage == com.example.miyank.Language.HINDI

    val announcementsState by AnnouncementManager.announcements.collectAsState()

    var editableAnnouncements by remember(announcementsState) {
        mutableStateOf(announcementsState.toMutableList())
    }

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<AnnouncementItem?>(null) }
    var previewingItem by remember { mutableStateOf<AnnouncementItem?>(null) }
    var deletingItem by remember { mutableStateOf<AnnouncementItem?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var showSaveConfirmDialog by remember { mutableStateOf(false) }

    val hasUnsavedChanges = editableAnnouncements != announcementsState

    val maxAnnouncements = 50
    val urgentCount = editableAnnouncements.count { it.isUrgent && it.isActive }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isHindi) "घोषणाएं प्रबंधन (Admin)" else "Manage Announcements", fontWeight = FontWeight.ExtraBold) },
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
                    editingItem = null
                    showAddEditDialog = true
                },
                containerColor = AccentGolden,
                contentColor = BackgroundDark,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(if (isHindi) "नयी घोषणा जोड़ें" else "New Announcement") }
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
            // Header Stats & Action Card
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
                                text = if (isHindi) "कुल घोषणाएं" else "Total Announcements",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextWhite
                            )
                            Text(
                                text = if (isHindi) "अति आवश्यक (Urgent): $urgentCount" else "Urgent Active: $urgentCount",
                                fontSize = 12.sp,
                                color = if (urgentCount > 0) Color.Red else TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            color = AccentGolden.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${editableAnnouncements.size} Items",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentGolden,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            haptic.playPremiumHaptic()
                            showSaveConfirmDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isHindi) "घोषणा सेटिंग्स सेव करें" else "Save Announcement Settings",
                            color = BackgroundDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
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
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (editableAnnouncements.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isHindi) "🌸 कोई घोषणा मौजूद नहीं है।\n'नयी घोषणा जोड़ें' पर टैप करें।" else "🌸 No announcements available.\nTap 'New Announcement' to create items.",
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    editableAnnouncements.forEachIndexed { index, item ->
                        AdminAnnouncementCard(
                            item = item,
                            isHindi = isHindi,
                            onToggleStatus = {
                                val updated = editableAnnouncements.toMutableList()
                                updated[index] = item.copy(isActive = !item.isActive)
                                editableAnnouncements = updated
                            },
                            onPreview = { previewingItem = item },
                            onEdit = {
                                editingItem = item
                                showAddEditDialog = true
                            },
                            onDelete = {
                                haptic.playPremiumHaptic()
                                deletingItem = item
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // ADD / EDIT DIALOG
    if (showAddEditDialog) {
        AddEditAnnouncementDialog(
            existingItem = editingItem,
            isHindi = isHindi,
            imageOptimizer = imageOptimizer,
            onDismiss = { showAddEditDialog = false },
            onSave = { updatedItem ->
                val current = editableAnnouncements.toMutableList()
                val idx = current.indexOfFirst { it.id == updatedItem.id }
                if (idx >= 0) {
                    current[idx] = updatedItem
                } else {
                    current.add(0, updatedItem)
                }
                editableAnnouncements = current
                statusMessage = if (isHindi) "घोषणा ड्राफ्ट में अपडेट हुई! 'घोषणा सेटिंग्स सेव करें' दबाएँ ✓" else "Announcement draft updated! Tap 'Save Settings' to apply ✓"
                showAddEditDialog = false
            }
        )
    }

    // CONFIRM SAVE SETTINGS DIALOG
    if (showSaveConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSaveConfirmDialog = false },
            containerColor = SurfaceDark,
            titleContentColor = AccentGolden,
            textContentColor = TextWhite,
            title = {
                Text(
                    text = if (isHindi) "घोषणाएं सेव करने की पुष्टि" else "Confirm Save Announcements",
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentGolden,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = if (isHindi) "क्या आप वाकई में इन घोषणाओं को श्रद्धालुओं के लिए पब्लिश और सेव करना चाहते हैं?" else "Are you sure you want to save and publish these announcements?",
                    color = TextWhite,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSaveConfirmDialog = false
                        haptic.playPremiumHaptic()
                        AnnouncementManager.saveAnnouncements(editableAnnouncements)
                        statusMessage = if (isHindi) "💾 घोषणाएं सफलतापूर्वक पब्लिश एवं सेव हो गईं! ✓" else "💾 Announcements Saved Successfully! ✓"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                ) {
                    Text(
                        text = if (isHindi) "हाँ, पब्लिश करें" else "Yes, Publish Settings",
                        color = BackgroundDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveConfirmDialog = false }) {
                    Text(text = if (isHindi) "रद्द करें" else "Cancel", color = TextMuted)
                }
            }
        )
    }

    // UNSAVED CHANGES GUARD DIALOG
    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            containerColor = SurfaceDark,
            titleContentColor = AccentGolden,
            textContentColor = TextWhite,
            title = {
                Text(
                    text = if (isHindi) "बिना सेव किए बाहर निकलें?" else "Unsaved Changes",
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentGolden,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = if (isHindi) "⚠️ आपने घोषणाओं में बदलाव किए हैं जो अभी पब्लिश नहीं हुए हैं!" else "⚠️ You have unsaved changes in announcements!",
                    color = TextWhite,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUnsavedDialog = false
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(
                        text = if (isHindi) "बिना सेव किए जाएँ" else "Discard & Leave",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showUnsavedDialog = false
                        AnnouncementManager.saveAnnouncements(editableAnnouncements)
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                ) {
                    Text(
                        text = if (isHindi) "सेव करके जाएँ" else "Save & Leave",
                        color = BackgroundDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    // DELETE CONFIRMATION DIALOG
    deletingItem?.let { itemToDelete ->
        AlertDialog(
            onDismissRequest = { deletingItem = null },
            containerColor = SurfaceDark,
            titleContentColor = Color.Red,
            textContentColor = TextWhite,
            title = {
                Text(
                    text = if (isHindi) "घोषणा हटाने की पुष्टि" else "Confirm Deletion",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Red,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isHindi) "⚠️ क्या आप वाकई में इस घोषणा को permanently हटाना चाहते हैं?" else "⚠️ Are you sure you want to delete this announcement?",
                        color = Color.Red,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val targetId = itemToDelete.id
                        deletingItem = null
                        editableAnnouncements = editableAnnouncements.filter { it.id != targetId }.toMutableList()
                        statusMessage = if (isHindi) "घोषणा हटाई गई! 'घोषणा सेटिंग्स सेव करें' दबाएँ।" else "Announcement removed! Tap 'Save Settings' to confirm."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(
                        text = if (isHindi) "हाँ, हटाएँ" else "Yes, Delete",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingItem = null }) {
                    Text(text = if (isHindi) "रद्द करें" else "Cancel", color = TextMuted)
                }
            }
        )
    }

    // PREVIEW DIALOG
    previewingItem?.let { detail ->
        AnnouncementDetailDialog(
            item = detail,
            isHindi = isHindi,
            onDismiss = { previewingItem = null }
        )
    }
}

@Composable
fun AdminAnnouncementCard(
    item: AnnouncementItem,
    isHindi: Boolean,
    onToggleStatus: () -> Unit,
    onPreview: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(
            1.dp,
            if (item.isUrgent) Color.Red.copy(alpha = 0.6f) else WarmGold.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.isUrgent) {
                        Surface(
                            color = Color.Red.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text("🔴 अति आवश्यक", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Surface(
                        color = AccentGolden.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = AnnouncementCategories.getDisplayName(item.categoryTag, isHindi),
                            color = AccentGolden,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onPreview, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Visibility, contentDescription = "Preview", tint = AccentGolden, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextWhite, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isHindi || item.titleEnglish.isEmpty()) item.title else item.titleEnglish,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite
            )

            if (item.content.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isHindi || item.contentEnglish.isEmpty()) item.content else item.contentEnglish,
                    fontSize = 12.sp,
                    color = TextMuted,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🗓️ ${formatDisplayDate(item.dateStr)}",
                    fontSize = 11.sp,
                    color = TextMuted
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (item.isActive) "🟢 Active" else "⚪ Archived",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isActive) Color(0xFF2ECC71) else Color.Gray,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Switch(
                        checked = item.isActive,
                        onCheckedChange = { onToggleStatus() },
                        colors = SwitchDefaults.colors(checkedThumbColor = AccentGolden)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAnnouncementDialog(
    existingItem: AnnouncementItem?,
    isHindi: Boolean,
    imageOptimizer: com.example.miyank.ImageOptimizer,
    onDismiss: () -> Unit,
    onSave: (AnnouncementItem) -> Unit
) {
    var titleInput by remember { mutableStateOf(existingItem?.title ?: "") }
    var titleEngInput by remember { mutableStateOf(existingItem?.titleEnglish ?: "") }
    var dateInput by remember { mutableStateOf(existingItem?.dateStr ?: getTodayDateString()) }
    var contentInput by remember { mutableStateOf(existingItem?.content ?: "") }
    var contentEngInput by remember { mutableStateOf(existingItem?.contentEnglish ?: "") }
    var categoryInput by remember { mutableStateOf(existingItem?.categoryTag ?: AnnouncementCategories.GENERAL) }
    var imageUrlInput by remember { mutableStateOf(existingItem?.imageUrl ?: "") }
    var isUrgentInput by remember { mutableStateOf(existingItem?.isUrgent ?: false) }
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
                text = if (existingItem == null) (if (isHindi) "नयी घोषणा पब्लिश करें" else "New Announcement") else (if (isHindi) "घोषणा संपादित करें" else "Edit Announcement"),
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
                    label = { Text(if (isHindi) "घोषणा शीर्षक (हिन्दी)" else "Announcement Title (Hindi)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Title English
                OutlinedTextField(
                    value = titleEngInput,
                    onValueChange = { titleEngInput = it },
                    label = { Text(if (isHindi) "घोषणा शीर्षक (अंग्रेज़ी)" else "Announcement Title (English)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Date Field with Calendar Picker
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
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category Selection Pills
                Text(if (isHindi) "श्रेणी (Category) चुनें:" else "Select Category:", fontSize = 12.sp, color = AccentGolden, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(AnnouncementCategories.adminList) { catKey ->
                        val isSelected = categoryInput == catKey
                        val displayName = AnnouncementCategories.getDisplayName(catKey, isHindi)
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { categoryInput = catKey },
                            color = if (isSelected) AccentGolden else BackgroundDark,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = displayName,
                                color = if (isSelected) BackgroundDark else TextWhite,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Description Hindi
                OutlinedTextField(
                    value = contentInput,
                    onValueChange = { contentInput = it },
                    label = { Text(if (isHindi) "सूचना का विस्तृत विवरण (हिन्दी)" else "Announcement Description (Hindi)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description English
                OutlinedTextField(
                    value = contentEngInput,
                    onValueChange = { contentEngInput = it },
                    label = { Text(if (isHindi) "विवरण (अंग्रेज़ी)" else "Description (English)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Photo Selection
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

                if (imageUrlInput.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        AsyncImage(
                            model = imageUrlInput,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Urgent Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(if (isHindi) "🚨 अति आवश्यक घोषित करें (Urgent Alert)" else "🚨 Mark as Urgent Notice", fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 13.sp)
                    Switch(
                        checked = isUrgentInput,
                        onCheckedChange = { isUrgentInput = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.Red)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Active Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(if (isHindi) "ऐप में एक्टिव रखें" else "Active on App", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 13.sp)
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
                text = "घोषणा सहेजें",
                textEnglish = "Save Announcement",
                isEnabled = titleInput.trim().isNotEmpty() && contentInput.trim().isNotEmpty(),
                onSaveClick = {
                    if (titleInput.trim().isEmpty()) {
                        errorText = if (isHindi) "❌ कृपया घोषणा शीर्षक दर्ज करें!" else "❌ Please enter title!"
                        return@AdminSaveButton
                    }
                    if (contentInput.trim().isEmpty()) {
                        errorText = if (isHindi) "❌ कृपया सूचना का विवरण दर्ज करें!" else "❌ Please enter description!"
                        return@AdminSaveButton
                    }

                    val item = AnnouncementItem(
                        id = existingItem?.id ?: "",
                        title = titleInput.trim(),
                        titleEnglish = titleEngInput.trim(),
                        dateStr = dateInput.trim(),
                        content = contentInput.trim(),
                        contentEnglish = contentEngInput.trim(),
                        categoryTag = categoryInput,
                        imageUrl = imageUrlInput,
                        isUrgent = isUrgentInput,
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
