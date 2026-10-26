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
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MusicNote
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
import com.example.miyank.AdminActionGuardDialog
import com.example.miyank.AdminSaveButton
import com.example.miyank.BhajanCategories
import com.example.miyank.BhajanItem
import com.example.miyank.BhajanManager
import com.example.miyank.GuardActionContext
import com.example.miyank.GuardDialogState
import com.example.miyank.LanguageManager
import com.example.miyank.getEpochMillis
import com.example.miyank.rememberHapticManager
import com.example.miyank.rememberImageOptimizer
import com.example.miyank.rememberMultipleFilePickerLauncher
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBhajansScreen(
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val imageOptimizer = rememberImageOptimizer()
    val isHindi = LanguageManager.currentLanguage == com.example.miyank.Language.HINDI

    val bhajansState by BhajanManager.bhajans.collectAsState()

    var editableBhajans by remember(bhajansState) {
        mutableStateOf(bhajansState.toMutableList())
    }

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<BhajanItem?>(null) }
    var deletingItem by remember { mutableStateOf<BhajanItem?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var showSaveConfirmDialog by remember { mutableStateOf(false) }

    val hasUnsavedChanges = editableBhajans != bhajansState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isHindi) "भजन एवं लिरिक्स प्रबंधन (Admin)" else "Manage Bhajans & Lyrics", fontWeight = FontWeight.ExtraBold) },
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
                text = { Text(if (isHindi) "नया भजन / आरती जोड़ें" else "Add Bhajan") }
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
            // Dashboard Stats Card
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
                                text = if (isHindi) "भजन एवं लिरिक्स डैशबोर्ड" else "Bhajans Dashboard",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextWhite
                            )
                            Text(
                                text = if (isHindi) "कुल भजन: ${editableBhajans.size} | एक्टिव: ${editableBhajans.count { it.isActive }}" else "Total: ${editableBhajans.size} | Active: ${editableBhajans.count { it.isActive }}",
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

            if (editableBhajans.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isHindi) "🌸 कोई भजन मौजूद नहीं है।\n'नया भजन जोड़ें' पर टैप करें।" else "🌸 No bhajans available.\nTap 'Add Bhajan' to create items.",
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    editableBhajans.forEachIndexed { index, bhajan ->
                        AdminBhajanCardItem(
                            bhajan = bhajan,
                            isHindi = isHindi,
                            onToggleStatus = {
                                val updated = editableBhajans.toMutableList()
                                updated[index] = bhajan.copy(isActive = !bhajan.isActive)
                                editableBhajans = updated
                            },
                            onEdit = {
                                editingItem = bhajan
                                showAddEditDialog = true
                            },
                            onDelete = {
                                haptic.playPremiumHaptic()
                                deletingItem = bhajan
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
        AddEditBhajanDialog(
            existingItem = editingItem,
            isHindi = isHindi,
            imageOptimizer = imageOptimizer,
            onDismiss = { showAddEditDialog = false },
            onSave = { updatedItem ->
                val current = editableBhajans.toMutableList()
                val idx = current.indexOfFirst { it.id == updatedItem.id }
                if (idx >= 0) {
                    current[idx] = updatedItem
                } else {
                    current.add(0, updatedItem)
                }
                editableBhajans = current
                statusMessage = if (isHindi) "भजन ड्राफ्ट में अपडेट हुआ! 'सेव करें' दबाएँ ✓" else "Bhajan draft updated! Tap 'Save Settings' to apply ✓"
                showAddEditDialog = false
            }
        )
    }

    // CONFIRM SAVE DIALOG USING GUARD
    AdminActionGuardDialog(
        state = GuardDialogState(
            isVisible = showSaveConfirmDialog,
            context = GuardActionContext.PUBLISH_MAJOR_CHANGE,
            title = if (isHindi) "भजन सेटिंग्स सेव करें" else "Save Bhajan Settings",
            message = if (isHindi) "क्या आप वाकई में भजन एवं लिरिक्स सेटिंग्स को सेव और पब्लिश करना चाहते हैं?" else "Are you sure you want to save and publish these bhajan updates?",
            onConfirm = {
                showSaveConfirmDialog = false
                haptic.playPremiumHaptic()
                BhajanManager.saveBhajans(editableBhajans)
                statusMessage = if (isHindi) "💾 भजन एवं लिरिक्स सेटिंग्स सफलतापूर्वक सेव हो गई! ✓" else "💾 Bhajan Settings Saved Successfully! ✓"
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
                BhajanManager.saveBhajans(editableBhajans)
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
    deletingItem?.let { itemToDelete ->
        AdminActionGuardDialog(
            state = GuardDialogState(
                isVisible = true,
                context = GuardActionContext.DELETE_ITEM,
                itemTitle = itemToDelete.title,
                itemCategory = BhajanCategories.getDisplayName(itemToDelete.categoryTag, isHindi),
                itemImageUrl = itemToDelete.thumbnailUrl,
                onConfirm = {
                    val id = itemToDelete.id
                    deletingItem = null
                    editableBhajans = editableBhajans.filter { it.id != id }.toMutableList()
                    statusMessage = if (isHindi) "भजन हटाया गया! 'सेव करें' दबाएँ।" else "Bhajan removed! Tap 'Save Settings' to confirm."
                },
                onDismiss = { deletingItem = null }
            ),
            isHindi = isHindi
        )
    }
}

@Composable
fun AdminBhajanCardItem(
    bhajan: BhajanItem,
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
            if (bhajan.thumbnailUrl.isNotEmpty()) {
                Card(
                    modifier = Modifier.size(50.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    AsyncImage(
                        model = bhajan.thumbnailUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = AccentGolden.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = BhajanCategories.getDisplayName(bhajan.categoryTag, isHindi),
                            color = AccentGolden,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (bhajan.duration.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(bhajan.duration, fontSize = 10.sp, color = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isHindi || bhajan.titleEnglish.isEmpty()) bhajan.title else bhajan.titleEnglish,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    text = if (isHindi || bhajan.artistEnglish.isEmpty()) bhajan.artist else bhajan.artistEnglish,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Switch(
                    checked = bhajan.isActive,
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
fun AddEditBhajanDialog(
    existingItem: BhajanItem?,
    isHindi: Boolean,
    imageOptimizer: com.example.miyank.ImageOptimizer,
    onDismiss: () -> Unit,
    onSave: (BhajanItem) -> Unit
) {
    var titleInput by remember { mutableStateOf(existingItem?.title ?: "") }
    var titleEngInput by remember { mutableStateOf(existingItem?.titleEnglish ?: "") }
    var artistInput by remember { mutableStateOf(existingItem?.artist ?: "श्री जामनीवाला दरबार") }
    var durationInput by remember { mutableStateOf(existingItem?.duration ?: "05:20") }
    var categoryInput by remember { mutableStateOf(existingItem?.categoryTag ?: BhajanCategories.BHAJAN) }
    var audioUrlInput by remember { mutableStateOf(existingItem?.audioUrl ?: "") }
    var lyricsInput by remember { mutableStateOf(existingItem?.lyricsText ?: "") }
    var imageUrlInput by remember { mutableStateOf(existingItem?.thumbnailUrl ?: "") }
    var isActiveInput by remember { mutableStateOf(existingItem?.isActive ?: true) }

    var errorText by remember { mutableStateOf<String?>(null) }

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

    val audioPicker = rememberMultipleFilePickerLauncher("audio/*") { pickedFiles ->
        if (pickedFiles.isNotEmpty()) {
            val (path, _, _) = pickedFiles.first()
            audioUrlInput = path
            errorText = null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        titleContentColor = TextWhite,
        textContentColor = TextWhite,
        title = {
            Text(
                text = if (existingItem == null) (if (isHindi) "नया भजन / आरती जोड़ें" else "Add New Bhajan") else (if (isHindi) "भजन संपादित करें" else "Edit Bhajan"),
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
                    label = { Text(if (isHindi) "भजन शीर्षक (हिन्दी)" else "Bhajan Title (Hindi)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Artist Input
                OutlinedTextField(
                    value = artistInput,
                    onValueChange = { artistInput = it },
                    label = { Text(if (isHindi) "गायक / कलाकार" else "Artist / Singer") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category Selection Pills
                Text(if (isHindi) "कैटेगरी चुनें:" else "Select Category:", fontSize = 12.sp, color = AccentGolden, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(BhajanCategories.adminList) { catKey ->
                        val isSelected = categoryInput == catKey
                        val displayName = BhajanCategories.getDisplayName(catKey, isHindi)
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

                // Audio File Selection
                Button(
                    onClick = { audioPicker() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                ) {
                    Icon(Icons.Default.AudioFile, contentDescription = null, tint = BackgroundDark)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (audioUrlInput.isEmpty()) (if (isHindi) "ऑडियो फ़ाइल चुनें (Optional)" else "Attach Audio File") else (if (isHindi) "ऑडियो फ़ाइल लगी है ✓" else "Audio File Attached ✓"),
                        color = BackgroundDark,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Lyrics Plain Text Editor
                OutlinedTextField(
                    value = lyricsInput,
                    onValueChange = { lyricsInput = it },
                    label = { Text(if (isHindi) "भजन लिरिक्स (Plain Text)" else "Bhajan Lyrics (Text)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Cover Photo Upload
                Button(
                    onClick = { filePicker() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, AccentGolden)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = AccentGolden)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (imageUrlInput.isEmpty()) (if (isHindi) "कवर फोटो जोड़ें" else "Attach Cover Photo") else (if (isHindi) "कवर फोटो लगी है ✓" else "Cover Photo Attached ✓"),
                        color = AccentGolden,
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
            AdminSaveButton(
                text = "भजन सेव करें",
                textEnglish = "Save Bhajan",
                isEnabled = titleInput.trim().isNotEmpty(),
                isHindi = isHindi,
                onSaveClick = {
                    if (titleInput.trim().isEmpty()) {
                        errorText = if (isHindi) "❌ कृपया भजन शीर्षक दर्ज करें!" else "❌ Please enter title!"
                        return@AdminSaveButton
                    }

                    val item = BhajanItem(
                        id = existingItem?.id ?: "",
                        title = titleInput.trim(),
                        titleEnglish = titleEngInput.trim(),
                        artist = artistInput.trim(),
                        duration = durationInput.trim(),
                        categoryTag = categoryInput,
                        audioUrl = audioUrlInput,
                        thumbnailUrl = imageUrlInput,
                        lyricsText = lyricsInput.trim(),
                        isActive = isActiveInput,
                        sortOrder = existingItem?.sortOrder ?: 1,
                        createdAt = existingItem?.createdAt ?: getEpochMillis()
                    )
                    onSave(item)
                },
                onCancelClick = onDismiss
            )
        }
    )
}
