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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.miyank.BlogCategories
import com.example.miyank.BlogItem
import com.example.miyank.BlogManager
import com.example.miyank.LocalStrings
import com.example.miyank.getTodayDateString
import com.example.miyank.rememberFilePickerLauncher
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*

data class EmbeddedImageInfo(
    val fullTag: String,
    val url: String,
    val size: String
)

fun extractEmbeddedImages(text: String): List<EmbeddedImageInfo> {
    val list = mutableListOf<EmbeddedImageInfo>()
    val parts = text.split("[IMAGE:")
    for (i in 1 until parts.size) {
        val raw = parts[i].substringBefore("]")
        val fullTag = "[IMAGE:$raw]"
        val (url, size) = parseImageSpec(raw)
        if (url.isNotEmpty()) {
            list.add(EmbeddedImageInfo(fullTag, url, size))
        }
    }
    return list
}

fun updateEmbeddedImageSize(text: String, oldTag: String, url: String, newSize: String): String {
    val newTag = "[IMAGE:$url|size:$newSize]"
    return text.replace(oldTag, newTag)
}

fun deleteEmbeddedImage(text: String, tag: String): String {
    return text.replace(tag, "").replace("\n\n\n", "\n\n")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBlogScreen(
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val strings = LocalStrings.current
    val blogs by BlogManager.blogs.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<BlogItem?>(null) }
    var previewingItem by remember { mutableStateOf<BlogItem?>(null) }
    var deletingItem by remember { mutableStateOf<BlogItem?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var showUnsavedDialog by remember { mutableStateOf(false) }

    val publishedCount = blogs.count { it.isPublished }
    val featuredCount = blogs.count { it.isFeatured }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.manageAboutDarbarTitle, fontWeight = FontWeight.ExtraBold) },
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
                text = { Text("Add Article") }
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
            // Stats Header Card
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
                        Text("📖", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${blogs.size} Total Articles",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "Published: $publishedCount | Featured: $featuredCount",
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
                text = "About Darbar Articles List",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AccentGolden
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (blogs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No articles created yet.\nTap '+ Add Article' to add content.",
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
                    items(blogs) { item ->
                        AdminBlogCard(
                            item = item,
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
                            },
                            onTogglePublish = {
                                haptic.playPremiumHaptic()
                                BlogManager.togglePublishStatus(item.id)
                                statusMessage = "Publish status updated for '${item.title}'."
                            },
                            onToggleFeatured = {
                                haptic.playPremiumHaptic()
                                BlogManager.toggleFeaturedStatus(item.id)
                                statusMessage = "Featured status updated for '${item.title}'."
                            }
                        )
                    }
                }
            }
        }
    }

    // ADD / EDIT DIALOG
    if (showAddDialog) {
        AddEditBlogDialog(
            existingItem = editingItem,
            onDismiss = { showAddDialog = false },
            onSaveConfirmed = { newItem ->
                BlogManager.addOrUpdateBlog(newItem)
                statusMessage = "Article '${newItem.title}' saved successfully!"
                showAddDialog = false
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
                    text = "⚠️ You have unsaved changes in article setup! Do you really want to discard your changes and leave?",
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
                        text = "⚠️ WARNING: Are you really sure you want to permanently delete this article '${itemToDelete.title}'?",
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
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = itemToDelete.id
                        val title = itemToDelete.title
                        deletingItem = null
                        BlogManager.deleteBlog(id)
                        statusMessage = "Article '$title' permanently deleted!"
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
                    text = "Article Preview",
                    color = AccentGolden,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        AsyncImage(
                            model = previewItem.coverImageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = previewItem.title, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextWhite)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = BlogCategories.getDisplayName(previewItem.category, isHindi = true), fontSize = 12.sp, color = AccentGolden, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    RenderRichArticleContent(
                        rawContent = previewItem.content,
                        secondaryImageUrl = previewItem.contentImageUrl
                    )
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
fun AdminBlogCard(
    item: BlogItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPreview: () -> Unit,
    onTogglePublish: () -> Unit,
    onToggleFeatured: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Main Info Row (Thumbnail, Title, Category, Action Buttons)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    modifier = Modifier.size(72.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    AsyncImage(
                        model = item.coverImageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = TextWhite,
                        maxLines = 2,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = BlogCategories.getDisplayName(item.category, isHindi = true),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGolden
                    )
                }

                // Action Buttons (Preview, Edit, Delete)
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(onClick = onPreview, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Visibility, contentDescription = "Preview", tint = AccentGolden, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextWhite, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BackgroundDark, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // SYSTEMATIC HORIZONTAL STATUS & FEATURED TOGGLES
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Publish Status Badge / Toggle
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTogglePublish() },
                    color = if (item.isPublished) Color(0xFF2ECC71).copy(alpha = 0.18f) else Color.Gray.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (item.isPublished) Color(0xFF2ECC71) else Color.Gray)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (item.isPublished) "🟢 Published" else "⚪ Draft",
                            color = if (item.isPublished) Color(0xFF2ECC71) else Color.LightGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Featured Section Badge / Toggle (Horizontal layout for any section)
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onToggleFeatured() },
                    color = if (item.isFeatured) AccentGolden.copy(alpha = 0.2f) else BackgroundDark,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (item.isFeatured) AccentGolden else WarmGold.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (item.isFeatured) "⭐ Featured Section" else "☆ Set Featured",
                            color = if (item.isFeatured) AccentGolden else TextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmbeddedImagesManager(
    text: String,
    onUpdateText: (String) -> Unit
) {
    val images = extractEmbeddedImages(text)
    if (images.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(14.dp))
            .border(1.dp, WarmGold.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🖼️ Embedded Images in Article (${images.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AccentGolden
            )
            Text(
                text = "Tap Size to Resize",
                fontSize = 10.sp,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        images.forEachIndexed { index, imgInfo ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                color = BackgroundDark,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    // Header Row: Thumbnail + Title + Delete Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Card(
                                modifier = Modifier.size(44.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                AsyncImage(
                                    model = imgInfo.url,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = "Photo #${index + 1}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }

                        // Delete Action Button
                        IconButton(
                            onClick = {
                                val updated = deleteEmbeddedImage(text, imgInfo.fullTag)
                                onUpdateText(updated)
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Red.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete Image",
                                tint = Color.Red,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Full-Width Symmetrical Size Selector Row (Small | Medium | Large)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val currentSize = imgInfo.size.lowercase()

                        // Small Option
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    val updated = updateEmbeddedImageSize(text, imgInfo.fullTag, imgInfo.url, "small")
                                    onUpdateText(updated)
                                },
                            color = if (currentSize == "small") AccentGolden else SurfaceDark,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (currentSize == "small") AccentGolden else WarmGold.copy(alpha = 0.3f))
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Small",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentSize == "small") BackgroundDark else TextWhite
                                )
                            }
                        }

                        // Medium Option
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    val updated = updateEmbeddedImageSize(text, imgInfo.fullTag, imgInfo.url, "medium")
                                    onUpdateText(updated)
                                },
                            color = if (currentSize == "medium") AccentGolden else SurfaceDark,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (currentSize == "medium") AccentGolden else WarmGold.copy(alpha = 0.3f))
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Medium",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentSize == "medium") BackgroundDark else TextWhite
                                )
                            }
                        }

                        // Large Option
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    val updated = updateEmbeddedImageSize(text, imgInfo.fullTag, imgInfo.url, "large")
                                    onUpdateText(updated)
                                },
                            color = if (currentSize == "large" || currentSize == "full") AccentGolden else SurfaceDark,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (currentSize == "large" || currentSize == "full") AccentGolden else WarmGold.copy(alpha = 0.3f))
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Large",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentSize == "large" || currentSize == "full") BackgroundDark else TextWhite
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditBlogDialog(
    existingItem: BlogItem?,
    onDismiss: () -> Unit,
    onSaveConfirmed: (BlogItem) -> Unit
) {
    var titleInput by remember { mutableStateOf(existingItem?.title ?: "") }
    var titleEnglishInput by remember { mutableStateOf(existingItem?.titleEnglish ?: "") }
    var categoryInput by remember { mutableStateOf(existingItem?.category ?: BlogCategories.HISTORY) }
    var shortDescInput by remember { mutableStateOf(existingItem?.shortDescription ?: "") }
    var shortDescEnglishInput by remember { mutableStateOf(existingItem?.shortDescriptionEnglish ?: "") }

    var contentValue by remember {
        mutableStateOf(TextFieldValue(text = existingItem?.content ?: ""))
    }
    var contentEnglishValue by remember {
        mutableStateOf(TextFieldValue(text = existingItem?.contentEnglish ?: ""))
    }

    var coverUrlInput by remember { mutableStateOf(existingItem?.coverImageUrl ?: "file:///android_asset/temple_pooja.jpg") }
    var contentUrlInput by remember { mutableStateOf(existingItem?.contentImageUrl ?: "") }
    var authorInput by remember { mutableStateOf(existingItem?.author ?: "श्री जामनीवाला दरबार") }
    var isPublishedInput by remember { mutableStateOf(existingItem?.isPublished ?: true) }
    var isFeaturedInput by remember { mutableStateOf(existingItem?.isFeatured ?: false) }
    var sortOrderInput by remember { mutableStateOf((existingItem?.sortOrder ?: 0).toString()) }

    var errorText by remember { mutableStateOf<String?>(null) }
    var pendingSaveItem by remember { mutableStateOf<BlogItem?>(null) }

    // Cover Image Picker
    val coverFilePicker = rememberFilePickerLauncher("image/*") { filePath, _, fileSize ->
        if (filePath != null) {
            val sizeMb = fileSize / (1024f * 1024f)
            if (sizeMb > 10.0f) {
                errorText = "❌ Image size exceeds 10.0 MB limit!"
            } else {
                coverUrlInput = filePath
                errorText = null
            }
        }
    }

    // In-Content Inline Image Picker for Hindi
    val hindiInlineImagePicker = rememberFilePickerLauncher("image/*") { filePath, _, fileSize ->
        if (filePath != null) {
            val sizeMb = fileSize / (1024f * 1024f)
            if (sizeMb > 10.0f) {
                errorText = "❌ Image size exceeds 10.0 MB limit!"
            } else {
                val cursor = contentValue.selection.start.coerceIn(0, contentValue.text.length)
                val tag = "\n[IMAGE:$filePath|size:medium]\n"
                val newText = StringBuilder(contentValue.text).insert(cursor, tag).toString()
                contentValue = TextFieldValue(
                    text = newText,
                    selection = TextRange(cursor + tag.length)
                )
                errorText = null
            }
        }
    }

    // In-Content Inline Image Picker for English
    val englishInlineImagePicker = rememberFilePickerLauncher("image/*") { filePath, _, fileSize ->
        if (filePath != null) {
            val sizeMb = fileSize / (1024f * 1024f)
            if (sizeMb > 10.0f) {
                errorText = "❌ Image size exceeds 10.0 MB limit!"
            } else {
                val cursor = contentEnglishValue.selection.start.coerceIn(0, contentEnglishValue.text.length)
                val tag = "\n[IMAGE:$filePath|size:medium]\n"
                val newText = StringBuilder(contentEnglishValue.text).insert(cursor, tag).toString()
                contentEnglishValue = TextFieldValue(
                    text = newText,
                    selection = TextRange(cursor + tag.length)
                )
                errorText = null
            }
        }
    }

    val categoryOptions = remember {
        BlogCategories.list.filter { it != BlogCategories.ALL }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        titleContentColor = TextWhite,
        textContentColor = TextWhite,
        title = {
            Text(
                text = if (existingItem == null) "Add About Darbar Article" else "Edit Article",
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
                // HINDI TITLE INPUT
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("Article Title (Hindi)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGolden,
                        unfocusedBorderColor = BackgroundDark,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // ENGLISH TITLE INPUT
                OutlinedTextField(
                    value = titleEnglishInput,
                    onValueChange = { titleEnglishInput = it },
                    label = { Text("Article Title (English Optional)") },
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

                // CATEGORY SELECTOR PILLS
                Text("Select Category:", fontSize = 12.sp, color = AccentGolden, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categoryOptions) { catKey ->
                        val isSelected = categoryInput == catKey
                        val displayName = BlogCategories.getDisplayName(catKey, isHindi = true)
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { categoryInput = catKey },
                            color = if (isSelected) AccentGolden else BackgroundDark,
                            shape = RoundedCornerShape(12.dp)
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

                Spacer(modifier = Modifier.height(12.dp))

                // MAIN COVER IMAGE UPLOAD BUTTON
                Button(
                    onClick = { coverFilePicker() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, tint = BackgroundDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (coverUrlInput.isEmpty()) "Upload Main Cover Photo" else "Change Main Cover Photo ✓",
                        color = BackgroundDark,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (coverUrlInput.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        AsyncImage(
                            model = coverUrlInput,
                            contentDescription = "Cover Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // SHORT DESCRIPTION (HINDI & ENGLISH)
                OutlinedTextField(
                    value = shortDescInput,
                    onValueChange = { shortDescInput = it },
                    label = { Text("Short Summary (Hindi)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGolden,
                        unfocusedBorderColor = BackgroundDark,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = shortDescEnglishInput,
                    onValueChange = { shortDescEnglishInput = it },
                    label = { Text("Short Summary (English Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGolden,
                        unfocusedBorderColor = BackgroundDark,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // HINDI FULL CONTENT FIELD WITH CURSOR-POSITION IMAGE UPLOAD & RESIZE MANAGER
                OutlinedTextField(
                    value = contentValue,
                    onValueChange = { contentValue = it },
                    label = { Text("Full Article Content (Hindi)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 140.dp),
                    maxLines = 12,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGolden,
                        unfocusedBorderColor = BackgroundDark,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = { hindiInlineImagePicker() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentGolden),
                    border = BorderStroke(1.dp, AccentGolden)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = AccentGolden)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("📷 Insert Image at Cursor (Hindi Text)", color = AccentGolden, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                EmbeddedImagesManager(
                    text = contentValue.text,
                    onUpdateText = { newText ->
                        contentValue = TextFieldValue(text = newText)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ENGLISH FULL CONTENT FIELD WITH CURSOR-POSITION IMAGE UPLOAD & RESIZE MANAGER
                OutlinedTextField(
                    value = contentEnglishValue,
                    onValueChange = { contentEnglishValue = it },
                    label = { Text("Full Article Content (English Optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    maxLines = 10,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGolden,
                        unfocusedBorderColor = BackgroundDark,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = { englishInlineImagePicker() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentGolden),
                    border = BorderStroke(1.dp, AccentGolden)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = AccentGolden)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("📷 Insert Image at Cursor (English Text)", color = AccentGolden, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                EmbeddedImagesManager(
                    text = contentEnglishValue.text,
                    onUpdateText = { newText ->
                        contentEnglishValue = TextFieldValue(text = newText)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // AUTHOR
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

                // PUBLISHED TOGGLE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Publish Immediately", fontWeight = FontWeight.Bold, color = TextWhite)
                    Switch(
                        checked = isPublishedInput,
                        onCheckedChange = { isPublishedInput = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = AccentGolden)
                    )
                }

                // FEATURED TOGGLE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Mark as Featured Article", fontWeight = FontWeight.Bold, color = TextWhite)
                    Switch(
                        checked = isFeaturedInput,
                        onCheckedChange = { isFeaturedInput = it },
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
            Button(
                onClick = {
                    if (titleInput.trim().isEmpty()) {
                        errorText = "❌ Please enter an article title!"
                        return@Button
                    }
                    if (contentValue.text.trim().isEmpty()) {
                        errorText = "❌ Please enter full article content!"
                        return@Button
                    }

                    pendingSaveItem = BlogItem(
                        id = existingItem?.id ?: "",
                        title = titleInput.trim(),
                        titleEnglish = titleEnglishInput.trim(),
                        category = categoryInput,
                        shortDescription = shortDescInput.trim(),
                        shortDescriptionEnglish = shortDescEnglishInput.trim(),
                        content = contentValue.text.trim(),
                        contentEnglish = contentEnglishValue.text.trim(),
                        coverImageUrl = coverUrlInput,
                        contentImageUrl = contentUrlInput,
                        author = authorInput.trim(),
                        publishedDate = existingItem?.publishedDate ?: getTodayDateString(),
                        updatedDate = getTodayDateString(),
                        isPublished = isPublishedInput,
                        isFeatured = isFeaturedInput,
                        sortOrder = sortOrderInput.toIntOrNull() ?: 0
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
            ) {
                Text("Save Article", color = BackgroundDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )

    // FINAL SAVE CONFIRMATION DIALOG
    pendingSaveItem?.let { saveItem ->
        AlertDialog(
            onDismissRequest = { pendingSaveItem = null },
            containerColor = SurfaceDark,
            titleContentColor = TextWhite,
            textContentColor = TextWhite,
            title = {
                Text(
                    text = "Confirm Article Publication",
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentGolden,
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    text = "Are you really sure you want to save and publish '${saveItem.title}'?",
                    color = TextWhite,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
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
                    Text("Yes, Save & Publish", color = BackgroundDark, fontWeight = FontWeight.Bold)
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