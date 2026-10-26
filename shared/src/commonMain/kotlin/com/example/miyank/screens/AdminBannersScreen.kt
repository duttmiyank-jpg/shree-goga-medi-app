package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.miyank.BannerCategories
import com.example.miyank.BannerItem
import com.example.miyank.BannersManager
import com.example.miyank.LanguageManager
import com.example.miyank.LocalStrings
import com.example.miyank.TargetRoutes
import com.example.miyank.getEpochMillis
import com.example.miyank.rememberHapticManager
import com.example.miyank.rememberImageOptimizer
import com.example.miyank.rememberMultipleFilePickerLauncher
import com.example.miyank.rememberSuvicharMediaHandler
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBannersScreen(
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val imageOptimizer = rememberImageOptimizer()
    val isHindi = LanguageManager.currentLanguage == com.example.miyank.Language.HINDI

    val bannersState by BannersManager.banners.collectAsState()

    var editableBanners by remember(bannersState) {
        mutableStateOf(bannersState.toMutableList())
    }

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<BannerItem?>(null) }
    var previewingItem by remember { mutableStateOf<BannerItem?>(null) }
    var deletingItem by remember { mutableStateOf<BannerItem?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var showSaveConfirmDialog by remember { mutableStateOf(false) }

    val hasUnsavedChanges = editableBanners != bannersState

    val maxBanners = 20
    val maxImageSizeMb = 10.0f

    // Single & Bulk Banner Image Upload
    val bulkFilePicker = rememberMultipleFilePickerLauncher("image/*") { pickedFiles ->
        if (pickedFiles.isNotEmpty()) {
            var addedCount = 0
            var errorCount = 0

            for ((path, _, size) in pickedFiles) {
                val sizeMb = size / (1024f * 1024f)
                if (sizeMb > maxImageSizeMb) {
                    errorCount++
                } else {
                    val optResult = imageOptimizer.processAndOptimizeImage(path)
                    val optimizedPath = optResult?.optimizedFullImagePath ?: path

                    val nextSort = if (editableBanners.isEmpty()) 1 else (editableBanners.maxOfOrNull { it.sortOrder } ?: 0) + 1
                    val newBanner = BannerItem(
                        id = "banner_${getEpochMillis()}_${addedCount}",
                        imageUrl = optimizedPath,
                        categoryTag = BannerCategories.JAGRAN,
                        title = if (isHindi) "श्री जामनीवाला दरबार विशेष आयोजन" else "Shree Jamniwala Darbar Special Event",
                        titleEnglish = "Shree Jamniwala Darbar Special Event",
                        targetRoute = TargetRoutes.EVENTS,
                        isActive = true,
                        sortOrder = nextSort,
                        createdAt = getEpochMillis() + addedCount
                    )
                    editableBanners = (editableBanners + newBanner).toMutableList()
                    addedCount++
                }
            }

            statusMessage = when {
                addedCount > 0 && errorCount > 0 -> if (isHindi) "📷 $addedCount बैनर प्रोसेस होकर जुड़े ($errorCount बैनर 10MB सीमा से बड़े होने के कारण खारिज) ✓" else "📷 $addedCount Banners added ($errorCount skipped for >10MB limit) ✓"
                addedCount > 0 -> if (isHindi) "📷 $addedCount नए बैनर कंप्रेस व प्रोसेस होकर जुड़े (सेव करने के लिए 'Save Settings' दबाएँ) ✓" else "📷 $addedCount Banners added & optimized (Tap 'Save Settings' to confirm) ✓"
                else -> if (isHindi) "❌ चुनी गई फोटो 10 MB सीमा से अधिक होने के कारण खारिज!" else "❌ Selected images exceeded 10 MB limit!"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isHindi) "होम बैनर प्रबंधन (Admin)" else "Manage Home Banners", fontWeight = FontWeight.ExtraBold) },
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
                text = { Text(if (isHindi) "नया बैनर जोड़ें" else "Add Banner") }
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
            // Header Action Card
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
                        Text(
                            text = if (isHindi) "होम स्क्रीन बैनर लिस्ट" else "Current Home Banners",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite
                        )
                        Surface(
                            color = AccentGolden.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${editableBanners.size} / $maxBanners Banners",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentGolden,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Bulk Upload Button
                        Button(
                            onClick = {
                                haptic.playPremiumHaptic()
                                if (editableBanners.size >= maxBanners) {
                                    statusMessage = if (isHindi) "❌ बैनर सीमा ($maxBanners) पूरी हो चुकी है!" else "❌ Banner limit reached!"
                                } else {
                                    bulkFilePicker()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isHindi) "बैनर अपलोड करें" else "Upload Banners",
                                color = BackgroundDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        // Save Settings Button
                        Button(
                            onClick = {
                                haptic.playPremiumHaptic()
                                showSaveConfirmDialog = true
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                            border = BorderStroke(1.dp, AccentGolden)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, tint = AccentGolden, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isHindi) "सेव सेटिंग्स" else "Save Settings",
                                color = AccentGolden,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
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

            if (editableBanners.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isHindi) "🌸 कोई बैनर सेट नहीं है।\n'बैनर अपलोड करें' पर टैप करें।" else "🌸 No banners configured.\nTap 'Upload Banners' to add items.",
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    editableBanners.forEachIndexed { index, banner ->
                        AdminBannerCard(
                            banner = banner,
                            isHindi = isHindi,
                            onToggleStatus = {
                                val updated = editableBanners.toMutableList()
                                updated[index] = banner.copy(isActive = !banner.isActive)
                                editableBanners = updated
                            },
                            onPreview = { previewingItem = banner },
                            onEdit = {
                                editingItem = banner
                                showAddEditDialog = true
                            },
                            onDelete = {
                                haptic.playPremiumHaptic()
                                deletingItem = banner
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // ADD / EDIT BANNER DIALOG
    if (showAddEditDialog) {
        AddEditBannerDialog(
            existingItem = editingItem,
            isHindi = isHindi,
            imageOptimizer = imageOptimizer,
            onDismiss = { showAddEditDialog = false },
            onSave = { updatedBanner ->
                val current = editableBanners.toMutableList()
                val idx = current.indexOfFirst { it.id == updatedBanner.id }
                if (idx >= 0) {
                    current[idx] = updatedBanner
                } else {
                    current.add(updatedBanner)
                }
                editableBanners = current
                statusMessage = if (isHindi) "बैनर ड्राफ्ट में अपडेट हुआ! 'Save Settings' दबाएँ ✓" else "Banner draft updated! Tap 'Save Settings' to apply ✓"
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
                    text = if (isHindi) "बैनर सेटिंग्स सेव करने की पुष्टि करें" else "Confirm Save Banner Settings",
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentGolden,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = if (isHindi) "क्या आप वाकई में होम स्क्रीन बैनर सेटिंग्स को सेव और पब्लिश करना चाहते हैं?" else "Are you sure you want to save and publish these home banner settings?",
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
                        BannersManager.saveBanners(editableBanners)
                        statusMessage = if (isHindi) "💾 बैनर सेटिंग्स सफलतापूर्वक सेव हो गई! ✓" else "💾 Banner Settings Saved Successfully! ✓"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                ) {
                    Text(
                        text = if (isHindi) "हाँ, सेव करें" else "Yes, Save Settings",
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
                    text = if (isHindi) "बिना सेव किए बाहर निकलें?" else "Unsaved Banner Changes",
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentGolden,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = if (isHindi) "⚠️ आपने बैनर में बदलाव किए हैं जो अभी सेव नहीं हुए हैं! बिना सेव किए जाने पर नए बैनर होम स्क्रीन पर नहीं दिखेंगे।" else "⚠️ You have unsaved banner changes! If you leave without tapping 'Save Settings', changes will be lost.",
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
                        text = if (isHindi) "बिना सेव किए बाहर जाएँ" else "Discard & Leave",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showUnsavedDialog = false
                        BannersManager.saveBanners(editableBanners)
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                ) {
                    Text(
                        text = if (isHindi) "सेव करके बाहर जाएँ" else "Save & Leave",
                        color = BackgroundDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    // DELETE CONFIRMATION DIALOG WITH RED WARNING
    deletingItem?.let { bannerToDelete ->
        AlertDialog(
            onDismissRequest = { deletingItem = null },
            containerColor = SurfaceDark,
            titleContentColor = Color.Red,
            textContentColor = TextWhite,
            title = {
                Text(
                    text = if (isHindi) "बैनर हटाने की पुष्टि करें" else "Confirm Banner Deletion",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Red,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isHindi) "⚠️ क्या आप वाकई में इस बैनर को हटाना चाहते हैं?" else "⚠️ Are you sure you want to delete this banner?",
                        color = Color.Red,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
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
                            model = bannerToDelete.imageUrl,
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
                        val targetId = bannerToDelete.id
                        deletingItem = null
                        editableBanners = editableBanners.filter { it.id != targetId }.toMutableList()
                        statusMessage = if (isHindi) "बैनर हटाया गया! 'Save Settings' दबाएँ।" else "Banner removed! Tap 'Save Settings' to confirm."
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
    previewingItem?.let { banner ->
        AlertDialog(
            onDismissRequest = { previewingItem = null },
            containerColor = SurfaceDark,
            title = {
                Text(
                    text = if (isHindi) "होम बैनर लाइव प्रिव्यू" else "Live Banner Preview",
                    color = AccentGolden,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.5f))
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = banner.imageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.88f)),
                                            startY = 120f
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                            ) {
                                Surface(
                                    color = AccentGolden,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = BannerCategories.getDisplayName(banner.categoryTag, isHindi),
                                        color = BackgroundDark,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isHindi || banner.titleEnglish.isEmpty()) banner.title else banner.titleEnglish,
                                    color = TextWhite,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Target Route: ${TargetRoutes.getDisplayName(banner.targetRoute, isHindi)}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { previewingItem = null },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                ) {
                    Text(if (isHindi) "बंद करें" else "Close", color = BackgroundDark, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun AdminBannerCard(
    banner: BannerItem,
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
        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                AsyncImage(
                    model = banner.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.4f), Color.Black.copy(alpha = 0.85f))
                            )
                        )
                )

                // Category Tag Pill
                Surface(
                    color = AccentGolden,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = BannerCategories.getDisplayName(banner.categoryTag, isHindi),
                        color = BackgroundDark,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Quick Action Buttons Top Right
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onPreview,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = "Preview", tint = AccentGolden, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextWhite, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                    }
                }

                // Banner Title on Image
                Text(
                    text = if (isHindi || banner.titleEnglish.isEmpty()) banner.title else banner.titleEnglish,
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                )
            }

            // Bottom Bar: Status Toggle & Target Route
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Route: ${TargetRoutes.getDisplayName(banner.targetRoute, isHindi)}",
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (banner.isActive) "🟢 Active" else "⚪ Inactive",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (banner.isActive) Color(0xFF2ECC71) else Color.Gray,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Switch(
                        checked = banner.isActive,
                        onCheckedChange = { onToggleStatus() },
                        colors = SwitchDefaults.colors(checkedThumbColor = AccentGolden)
                    )
                }
            }
        }
    }
}

@Composable
fun AddEditBannerDialog(
    existingItem: BannerItem?,
    isHindi: Boolean,
    imageOptimizer: com.example.miyank.ImageOptimizer,
    onDismiss: () -> Unit,
    onSave: (BannerItem) -> Unit
) {
    var titleInput by remember { mutableStateOf(existingItem?.title ?: "") }
    var titleEngInput by remember { mutableStateOf(existingItem?.titleEnglish ?: "") }
    var categoryInput by remember { mutableStateOf(existingItem?.categoryTag ?: BannerCategories.JAGRAN) }
    var routeInput by remember { mutableStateOf(existingItem?.targetRoute ?: TargetRoutes.EVENTS) }
    var imageUrlInput by remember { mutableStateOf(existingItem?.imageUrl ?: "") }
    var isActiveInput by remember { mutableStateOf(existingItem?.isActive ?: true) }
    var sortOrderInput by remember { mutableStateOf((existingItem?.sortOrder ?: 1).toString()) }

    var errorText by remember { mutableStateOf<String?>(null) }
    var rawPickedPath by remember { mutableStateOf<String?>(null) }

    val filePicker = rememberMultipleFilePickerLauncher("image/*") { pickedFiles ->
        if (pickedFiles.isNotEmpty()) {
            val (path, _, size) = pickedFiles.first()
            val sizeMb = size / (1024f * 1024f)
            if (sizeMb > 10.0f) {
                errorText = if (isHindi) "❌ फोटो का आकार 10 MB सीमा से अधिक है!" else "❌ Image size exceeds 10 MB limit!"
            } else {
                rawPickedPath = path
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
                text = if (existingItem == null) (if (isHindi) "नया होम बैनर जोड़ें" else "Add New Home Banner") else (if (isHindi) "बैनर संपादित करें" else "Edit Banner Details"),
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
                // Select Image Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { filePicker() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = BackgroundDark)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (imageUrlInput.isEmpty()) (if (isHindi) "फोटो चुनें" else "Select Photo") else (if (isHindi) "फोटो बदलें ✓" else "Change Photo ✓"),
                            color = BackgroundDark,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (imageUrlInput.isNotEmpty()) {
                        OutlinedButton(
                            onClick = {
                                rawPickedPath = imageUrlInput
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, AccentGolden)
                        ) {
                            Icon(Icons.Default.Crop, contentDescription = "Crop", tint = AccentGolden)
                        }
                    }
                }

                if (imageUrlInput.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
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

                // Title Hindi
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text(if (isHindi) "बैनर शीर्षक (हिन्दी)" else "Banner Title (Hindi)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Title English
                OutlinedTextField(
                    value = titleEngInput,
                    onValueChange = { titleEngInput = it },
                    label = { Text(if (isHindi) "बैनर शीर्षक (अंग्रेज़ी)" else "Banner Title (English)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category Tag Pills
                Text(if (isHindi) "कैटेगरी टैग चुनें:" else "Select Category Tag:", fontSize = 12.sp, color = AccentGolden, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(BannerCategories.list) { catTag ->
                        val isSelected = categoryInput == catTag
                        val displayName = BannerCategories.getDisplayName(catTag, isHindi)
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { categoryInput = catTag },
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

                // Target Navigation Route Pills
                Text(if (isHindi) "टैप करने पर कौन सी स्क्रीन खुले:" else "Navigation Target Route:", fontSize = 12.sp, color = AccentGolden, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(TargetRoutes.list) { routeKey ->
                        val isSelected = routeInput == routeKey
                        val displayName = TargetRoutes.getDisplayName(routeKey, isHindi)
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { routeInput = routeKey },
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

                // Active Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(if (isHindi) "होम स्क्रीन पर एक्टिव रखें" else "Active on Home Screen", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 13.sp)
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
                text = "बैनर सहेजें",
                textEnglish = "Save Banner",
                isEnabled = imageUrlInput.isNotEmpty() && titleInput.trim().isNotEmpty(),
                onSaveClick = {
                    if (imageUrlInput.isEmpty()) {
                        errorText = if (isHindi) "❌ कृपया बैनर फोटो अपलोड करें!" else "❌ Please select banner photo!"
                        return@AdminSaveButton
                    }
                    if (titleInput.trim().isEmpty()) {
                        errorText = if (isHindi) "❌ कृपया बैनर शीर्षक दर्ज करें!" else "❌ Please enter banner title!"
                        return@AdminSaveButton
                    }

                    val item = BannerItem(
                        id = existingItem?.id ?: "",
                        imageUrl = imageUrlInput,
                        categoryTag = categoryInput,
                        title = titleInput.trim(),
                        titleEnglish = titleEngInput.trim(),
                        targetRoute = routeInput,
                        isActive = isActiveInput,
                        sortOrder = sortOrderInput.toIntOrNull() ?: 1,
                        createdAt = existingItem?.createdAt ?: getEpochMillis()
                    )
                    onSave(item)
                },
                onCancelClick = onDismiss
            )
        }
    )

    // CROPPER DIALOG FOR BANNER
    rawPickedPath?.let { rawPath ->
        BannerImageCropperDialog(
            rawPath = rawPath,
            isHindi = isHindi,
            onDismiss = { rawPickedPath = null },
            onCropped = { finalPath ->
                imageUrlInput = finalPath
                rawPickedPath = null
                errorText = null
            }
        )
    }
}

@Composable
fun BannerImageCropperDialog(
    rawPath: String,
    isHindi: Boolean,
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
                    text = if (isHindi) "बैनर क्रॉप एवं सेट करें" else "Crop & Adjust Banner Image",
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentGolden,
                    fontSize = 16.sp
                )
                Surface(
                    color = AccentGolden.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("2.1:1 Frame", color = AccentGolden, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isHindi) "होम स्क्रीन बैनर (2.1:1) फ्रेम में सेट करने के लिए फोटो को ड्रैग या ज़ूम करें:" else "Drag or pinch to adjust photo inside the 2.1:1 banner frame:",
                    fontSize = 12.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // BANNER 2.1:1 ASPECT RATIO CROP CONTAINER WITH 3x3 GRID OVERLAY
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2.1f)
                        .clip(RoundedCornerShape(18.dp))
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1.0f, 3.5f)
                                offsetX = (offsetX + pan.x).coerceIn(-300f, 300f)
                                offsetY = (offsetY + pan.y).coerceIn(-300f, 300f)
                            }
                        },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
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

                        // Golden Border Frame
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(2.5.dp, WarmGold, RoundedCornerShape(18.dp))
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
                        Text(if (isHindi) "रिसेट करें" else "Reset Frame", fontSize = 11.sp)
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
                        offsetY = offsetY,
                        targetWidth = 1200,
                        targetHeight = 570
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
                    Text(if (isHindi) "क्रॉप व अप्लाई करें" else "Crop & Apply Banner", color = BackgroundDark, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isHindi) "रद्द करें" else "Cancel", color = TextMuted)
            }
        }
    )
}
