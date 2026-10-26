package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
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
import com.example.miyank.GalleryCategories
import com.example.miyank.GalleryItem
import com.example.miyank.GalleryManager
import com.example.miyank.LanguageManager
import com.example.miyank.getEpochMillis
import com.example.miyank.getTodayDateString
import com.example.miyank.rememberHapticManager
import com.example.miyank.rememberImageOptimizer
import com.example.miyank.rememberMultipleFilePickerLauncher
import com.example.miyank.ui.theme.*

@Composable
fun AdminGalleryScreen(
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val imageOptimizer = rememberImageOptimizer()
    val isHindi = LanguageManager.currentLanguage == com.example.miyank.Language.HINDI

    val galleryItems by GalleryManager.galleryItems.collectAsState()
    val customCategories by GalleryManager.customCategories.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryKey by remember { mutableStateOf(GalleryCategories.ALL) }
    var sortNewestFirst by remember { mutableStateOf(true) }

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var editingItem by remember { mutableStateOf<GalleryItem?>(null) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var deletingItem by remember { mutableStateOf<GalleryItem?>(null) }
    var previewingItem by remember { mutableStateOf<GalleryItem?>(null) }
    var showCategoryDialog by remember { mutableStateOf(false) }

    val categoriesList = remember(customCategories) {
        GalleryManager.getAllCategories()
    }

    val maxPhotosLimit = 5000
    val maxImageSizeMb = 10.0f

    val filteredItems = remember(galleryItems, selectedCategoryKey, searchQuery, sortNewestFirst) {
        GalleryManager.getGalleryItemsPaged(
            categoryKey = selectedCategoryKey,
            searchQuery = searchQuery,
            sortNewestFirst = sortNewestFirst,
            page = 1,
            pageSize = 1000
        )
    }

    // Single / Multiple Image Picker with 10 MB Validation & Intelligent WebP Optimization Pipeline
    val photoPickerLauncher = rememberMultipleFilePickerLauncher("image/*") { pickedFiles ->
        if (pickedFiles.isNotEmpty()) {
            var addedCount = 0
            var errorCount = 0

            for ((path, _, size) in pickedFiles) {
                val sizeMb = size / (1024f * 1024f)
                if (sizeMb > maxImageSizeMb) {
                    errorCount++
                } else {
                    val optResult = imageOptimizer.processAndOptimizeImage(path)
                    val fullPath = optResult?.optimizedFullImagePath ?: path
                    val thumbPath = optResult?.thumbnailImagePath ?: path

                    val newItem = GalleryItem(
                        id = "gallery_${getEpochMillis()}_${addedCount}",
                        title = if (isHindi) "श्री जामनीवाला दरबार फोटो" else "Shree Jamniwala Darbar Photo",
                        titleEnglish = "Shree Jamniwala Darbar Photo",
                        category = if (selectedCategoryKey == GalleryCategories.ALL) GalleryCategories.TEMPLE else selectedCategoryKey,
                        thumbnailUrl = thumbPath,
                        optimizedImageUrl = fullPath,
                        originalImageUrl = path,
                        createdAt = getEpochMillis() + addedCount,
                        uploadDate = getTodayDateString(),
                        isFeatured = false
                    )
                    GalleryManager.addOrUpdateGalleryItem(newItem)
                    addedCount++
                }
            }

            statusMessage = when {
                addedCount > 0 && errorCount > 0 -> if (isHindi) "📷 $addedCount फोटो प्रोसेस और ऑप्टिमाइज़ होकर जोड़ी गईं ($errorCount फोटो 10MB सीमा से बड़ी होने के कारण खारिज) ✓" else "📷 $addedCount Photos optimized & added ($errorCount rejected for >10MB limit) ✓"
                addedCount > 0 -> if (isHindi) "📷 $addedCount फोटो प्रोसेस, कंप्रेस और ऑप्टिमाइज़ होकर सफलतापूर्वक जोड़ी गईं ✓" else "📷 $addedCount Photos optimized & added successfully ✓"
                else -> if (isHindi) "❌ चुनी गई फोटो 10 MB सीमा से अधिक होने के कारण खारिज कर दी गईं!" else "❌ Selected images exceeded the 10 MB limit!"
            }
        }
    }

    val pageScrollState = rememberScrollState()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhite
                        )
                    }
                    Text(
                        text = if (isHindi) "गैलरी प्रबंधन (Admin)" else "Gallery Management (Admin)",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextWhite,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                // Add Category Button
                IconButton(onClick = { showCategoryDialog = true }) {
                    Icon(Icons.Default.Category, contentDescription = "Category Manager", tint = AccentGolden)
                }
            }
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
                text = { Text(if (isHindi) "फोटो जोड़ें" else "Add Photo") }
            )
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(pageScrollState)
        ) {
            // CATEGORY FILTER TABS
            ScrollableTabRow(
                selectedTabIndex = categoriesList.indexOf(selectedCategoryKey).coerceAtLeast(0),
                containerColor = SurfaceDark,
                contentColor = AccentGolden,
                edgePadding = 16.dp,
                divider = {}
            ) {
                categoriesList.forEach { catKey ->
                    val displayName = GalleryCategories.getDisplayName(catKey, isHindi)
                    Tab(
                        selected = selectedCategoryKey == catKey,
                        onClick = {
                            haptic.playPremiumHaptic()
                            selectedCategoryKey = catKey
                        },
                        text = {
                            Text(
                                text = displayName,
                                fontWeight = if (selectedCategoryKey == catKey) FontWeight.ExtraBold else FontWeight.Normal,
                                fontSize = 13.sp,
                                color = if (selectedCategoryKey == catKey) AccentGolden else TextMuted
                            )
                        }
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Header Info & Quick Controls
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
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
                                text = GalleryCategories.getDisplayName(selectedCategoryKey, isHindi),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextWhite
                            )
                            Surface(
                                color = AccentGolden.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${filteredItems.size} Photos",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentGolden,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Search Field & Sort Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text(if (isHindi) "फोटो खोजें..." else "Search photos...", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentGolden) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentGolden,
                                    unfocusedBorderColor = BackgroundDark,
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Sort Order Toggle Button
                            IconButton(
                                onClick = {
                                    haptic.playPremiumHaptic()
                                    sortNewestFirst = !sortNewestFirst
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceDark)
                            ) {
                                Icon(
                                    Icons.Default.Sort,
                                    contentDescription = "Sort",
                                    tint = AccentGolden
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Bulk Upload Button
                        Button(
                            onClick = {
                                haptic.playPremiumHaptic()
                                photoPickerLauncher()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isHindi) "फोटो अपलोड करें (मैक्स 10MB)" else "Upload Photos (Max 10MB)",
                                color = BackgroundDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                if (statusMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
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

                Spacer(modifier = Modifier.height(16.dp))

                if (filteredItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isHindi) "🌸 अभी कोई फोटो उपलब्ध नहीं है।\n'फोटो अपलोड करें' पर टैप करके नई फोटो जोड़ें।" else "🌸 No photos found.\nTap 'Upload Photos' to add images.",
                            color = TextMuted,
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    // Continuous Grid Rows
                    val photoRows = filteredItems.chunked(2)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        photoRows.forEach { rowItems ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                rowItems.forEach { item ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        AdminGalleryItemCard(
                                            item = item,
                                            isHindi = isHindi,
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
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // ADD / EDIT PHOTO DIALOG
    if (showAddEditDialog) {
        AddEditGalleryPhotoDialog(
            existingItem = editingItem,
            categoriesList = categoriesList,
            isHindi = isHindi,
            imageOptimizer = imageOptimizer,
            onDismiss = { showAddEditDialog = false },
            onSave = { newItem ->
                GalleryManager.addOrUpdateGalleryItem(newItem)
                statusMessage = if (isHindi) "फोटो सफलतापूर्वक अपडेट हो गई! ✓" else "Photo updated successfully! ✓"
                showAddEditDialog = false
            }
        )
    }

    // CATEGORY MANAGER DIALOG
    if (showCategoryDialog) {
        CategoryManagerDialog(
            isHindi = isHindi,
            categoriesList = categoriesList,
            onDismiss = { showCategoryDialog = false },
            onAddCategory = { newCat ->
                GalleryManager.addCustomCategory(newCat)
            }
        )
    }

    // PREVIEW DIALOG
    previewingItem?.let { item ->
        AlertDialog(
            onDismissRequest = { previewingItem = null },
            containerColor = SurfaceDark,
            title = {
                Text(
                    text = if (isHindi || item.titleEnglish.isEmpty()) item.title else item.titleEnglish,
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
                            .height(220.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        AsyncImage(
                            model = if (item.optimizedImageUrl.isNotEmpty()) item.optimizedImageUrl else item.thumbnailUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isHindi || item.descriptionEnglish.isEmpty()) item.description else item.descriptionEnglish,
                        fontSize = 13.sp,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Category: ${GalleryCategories.getDisplayName(item.category, isHindi)} | Date: ${item.uploadDate}",
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

    // DELETE CONFIRMATION DIALOG
    deletingItem?.let { itemToDelete ->
        AlertDialog(
            onDismissRequest = { deletingItem = null },
            containerColor = SurfaceDark,
            titleContentColor = Color.Red,
            textContentColor = TextWhite,
            title = {
                Text(
                    text = if (isHindi) "फोटो हटाने की पुष्टि करें" else "Confirm Photo Deletion",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Red,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isHindi) "⚠️ क्या आप वाकई में इस फोटो को हटाना चाहते हैं?" else "⚠️ Are you sure you want to permanently delete this photo?",
                        color = Color.Red,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        AsyncImage(
                            model = if (itemToDelete.thumbnailUrl.isNotEmpty()) itemToDelete.thumbnailUrl else itemToDelete.optimizedImageUrl,
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
                        val targetId = itemToDelete.id
                        deletingItem = null
                        GalleryManager.deleteGalleryItem(targetId)
                        statusMessage = if (isHindi) "फोटो गैलरी से हटा दी गई!" else "Photo deleted from gallery!"
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
}

@Composable
fun AdminGalleryItemCard(
    item: GalleryItem,
    isHindi: Boolean,
    onPreview: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.9f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = if (item.thumbnailUrl.isNotEmpty()) item.thumbnailUrl else item.optimizedImageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Featured Tag Badge
            if (item.isFeatured) {
                Surface(
                    color = AccentGolden,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Featured",
                        tint = BackgroundDark,
                        modifier = Modifier
                            .padding(4.dp)
                            .size(14.dp)
                    )
                }
            }

            // Quick Actions Bar at Top Right
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = onPreview,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = "Preview", tint = AccentGolden, modifier = Modifier.size(15.dp))
                }
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextWhite, modifier = Modifier.size(15.dp))
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(15.dp))
                }
            }

            // Title & Category Gradient Bar at Bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.75f))
                    .padding(8.dp)
            ) {
                Column {
                    Text(
                        text = if (isHindi || item.titleEnglish.isEmpty()) item.title else item.titleEnglish,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        maxLines = 1
                    )
                    Text(
                        text = GalleryCategories.getDisplayName(item.category, isHindi),
                        fontSize = 9.sp,
                        color = AccentGolden
                    )
                }
            }
        }
    }
}

@Composable
fun AddEditGalleryPhotoDialog(
    existingItem: GalleryItem?,
    categoriesList: List<String>,
    isHindi: Boolean,
    imageOptimizer: com.example.miyank.ImageOptimizer,
    onDismiss: () -> Unit,
    onSave: (GalleryItem) -> Unit
) {
    var titleInput by remember { mutableStateOf(existingItem?.title ?: "") }
    var titleEngInput by remember { mutableStateOf(existingItem?.titleEnglish ?: "") }
    var descInput by remember { mutableStateOf(existingItem?.description ?: "") }
    var descEngInput by remember { mutableStateOf(existingItem?.descriptionEnglish ?: "") }
    var categoryInput by remember { mutableStateOf(existingItem?.category ?: GalleryCategories.TEMPLE) }
    var uploadDateInput by remember { mutableStateOf(existingItem?.uploadDate ?: getTodayDateString()) }
    var isFeaturedInput by remember { mutableStateOf(existingItem?.isFeatured ?: false) }
    var imagePathInput by remember { mutableStateOf(existingItem?.optimizedImageUrl ?: "") }
    var thumbPathInput by remember { mutableStateOf(existingItem?.thumbnailUrl ?: "") }

    var errorText by remember { mutableStateOf<String?>(null) }

    val singlePicker = rememberMultipleFilePickerLauncher("image/*") { pickedFiles ->
        if (pickedFiles.isNotEmpty()) {
            val (path, _, size) = pickedFiles.first()
            val sizeMb = size / (1024f * 1024f)
            if (sizeMb > 10.0f) {
                errorText = if (isHindi) "❌ फोटो का साइज 10 MB सीमा से अधिक है!" else "❌ Selected image exceeds 10 MB limit!"
            } else {
                val optResult = imageOptimizer.processAndOptimizeImage(path)
                imagePathInput = optResult?.optimizedFullImagePath ?: path
                thumbPathInput = optResult?.thumbnailImagePath ?: path
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
                text = if (existingItem == null) (if (isHindi) "नई फोटो जोड़ें" else "Add New Photo") else (if (isHindi) "फोटो संपादित करें" else "Edit Photo Details"),
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
                // Upload / Change Image
                Button(
                    onClick = { singlePicker() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGolden),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = BackgroundDark)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (imagePathInput.isEmpty()) (if (isHindi) "फोटो चुनें (मैक्स 10MB)" else "Select Photo (Max 10MB)") else (if (isHindi) "फोटो बदलें ✓" else "Change Photo ✓"),
                        color = BackgroundDark,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (imagePathInput.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        AsyncImage(
                            model = if (thumbPathInput.isNotEmpty()) thumbPathInput else imagePathInput,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Title Hindi
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text(if (isHindi) "फोटो शीर्षक (हिन्दी)" else "Photo Title (Hindi)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Title English
                OutlinedTextField(
                    value = titleEngInput,
                    onValueChange = { titleEngInput = it },
                    label = { Text(if (isHindi) "फोटो शीर्षक (अंग्रेज़ी)" else "Photo Title (English)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Selection Pills
                Text(if (isHindi) "श्रेणी चुनें:" else "Select Category:", fontSize = 12.sp, color = AccentGolden, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categoriesList.filter { it != GalleryCategories.ALL }) { catKey ->
                        val isSelected = categoryInput == catKey
                        val displayName = GalleryCategories.getDisplayName(catKey, isHindi)
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { categoryInput = catKey },
                            color = if (isSelected) AccentGolden else BackgroundDark,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = displayName,
                                color = if (isSelected) BackgroundDark else TextWhite,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Description Hindi
                OutlinedTextField(
                    value = descInput,
                    onValueChange = { descInput = it },
                    label = { Text(if (isHindi) "विवरण (हिन्दी)" else "Description (Hindi)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description English
                OutlinedTextField(
                    value = descEngInput,
                    onValueChange = { descEngInput = it },
                    label = { Text(if (isHindi) "विवरण (अंग्रेज़ी)" else "Description (English)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Upload Date Input
                OutlinedTextField(
                    value = uploadDateInput,
                    onValueChange = { uploadDateInput = it },
                    label = { Text(if (isHindi) "अपलोड दिनांक (YYYY-MM-DD)" else "Upload Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Featured Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(if (isHindi) "मुख्य फोटो (Featured) मार्क करें" else "Mark as Featured Photo", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 13.sp)
                    Switch(
                        checked = isFeaturedInput,
                        onCheckedChange = { isFeaturedInput = it },
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
            Button(
                onClick = {
                    if (imagePathInput.isEmpty()) {
                        errorText = if (isHindi) "❌ कृपया फोटो अपलोड करें!" else "❌ Please select an image!"
                        return@Button
                    }
                    if (titleInput.trim().isEmpty()) {
                        errorText = if (isHindi) "❌ कृपया फोटो शीर्षक दर्ज करें!" else "❌ Please enter photo title!"
                        return@Button
                    }

                    val item = GalleryItem(
                        id = existingItem?.id ?: "",
                        title = titleInput.trim(),
                        titleEnglish = titleEngInput.trim(),
                        description = descInput.trim(),
                        descriptionEnglish = descEngInput.trim(),
                        category = categoryInput,
                        thumbnailUrl = if (thumbPathInput.isNotEmpty()) thumbPathInput else imagePathInput,
                        optimizedImageUrl = imagePathInput,
                        originalImageUrl = imagePathInput,
                        createdAt = existingItem?.createdAt ?: getEpochMillis(),
                        uploadDate = uploadDateInput.trim(),
                        isFeatured = isFeaturedInput
                    )
                    onSave(item)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
            ) {
                Text(if (isHindi) "सेव करें" else "Save Photo", color = BackgroundDark, fontWeight = FontWeight.Bold)
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
fun CategoryManagerDialog(
    isHindi: Boolean,
    categoriesList: List<String>,
    onDismiss: () -> Unit,
    onAddCategory: (String) -> Unit
) {
    var newCatName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        titleContentColor = TextWhite,
        textContentColor = TextWhite,
        title = {
            Text(
                text = if (isHindi) "गैलरी श्रेणियाँ (Categories)" else "Manage Gallery Categories",
                color = AccentGolden,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(if (isHindi) "वर्तमान श्रेणियाँ:" else "Existing Categories:", fontSize = 12.sp, color = AccentGolden, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categoriesList) { catKey ->
                        Surface(
                            color = BackgroundDark,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = GalleryCategories.getDisplayName(catKey, isHindi),
                                color = TextWhite,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = newCatName,
                    onValueChange = { newCatName = it },
                    label = { Text(if (isHindi) "नई श्रेणी का नाम" else "New Category Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newCatName.trim().isNotEmpty()) {
                        onAddCategory(newCatName.trim())
                        newCatName = ""
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
            ) {
                Text(if (isHindi) "श्रेणी जोड़ें" else "Add Category", color = BackgroundDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isHindi) "बंद करें" else "Close", color = TextMuted)
            }
        }
    )
}
