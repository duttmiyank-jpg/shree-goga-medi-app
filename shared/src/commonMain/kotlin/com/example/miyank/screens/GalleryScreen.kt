package com.example.miyank.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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
import com.example.miyank.GalleryCategories
import com.example.miyank.GalleryItem
import com.example.miyank.GalleryManager
import com.example.miyank.HapticManager
import com.example.miyank.Language
import com.example.miyank.LanguageManager
import com.example.miyank.LocalStrings
import com.example.miyank.getTodayDateString
import com.example.miyank.rememberHapticManager
import com.example.miyank.rememberSuvicharMediaHandler
import com.example.miyank.ui.theme.*

@Composable
fun GalleryScreen(
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val strings = LocalStrings.current
    val isHindi = LanguageManager.currentLanguage == Language.HINDI
    val galleryItems by GalleryManager.galleryItems.collectAsState()

    val categoriesList = GalleryCategories.defaultList

    var selectedCategoryKey by remember { mutableStateOf(GalleryCategories.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var sortNewestFirst by remember { mutableStateOf(true) }

    var selectedImageIndex by remember { mutableStateOf<Int?>(null) }
    var currentPage by remember { mutableIntStateOf(1) }
    val pageSize = 24

    var isLoading by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    // Featured Gallery Items
    val featuredItems = remember(galleryItems) {
        GalleryManager.getFeaturedItems()
    }

    // Paged Items (Infinite Scroll / Cursor pagination)
    val displayItems = remember(galleryItems, selectedCategoryKey, searchQuery, sortNewestFirst, currentPage) {
        GalleryManager.getGalleryItemsPaged(
            categoryKey = selectedCategoryKey,
            searchQuery = searchQuery,
            sortNewestFirst = sortNewestFirst,
            page = 1,
            pageSize = currentPage * pageSize
        )
    }

    val gridState = rememberLazyGridState()

    // Detect when user scrolls near the end of grid to trigger next page load
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && lastIndex >= displayItems.size - 4 && displayItems.size >= currentPage * pageSize) {
                    currentPage++
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
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
                    text = strings.galleryTitle,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextWhite,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // Sort Order Button
            IconButton(
                onClick = {
                    haptic.playPremiumHaptic()
                    sortNewestFirst = !sortNewestFirst
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(SurfaceDark)
            ) {
                Icon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = "Sort",
                    tint = AccentGolden
                )
            }
        }

        // Search Bar
        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    currentPage = 1
                },
                placeholder = { Text(if (isHindi) "दिव्य फोटो खोजें..." else "Search photos...", fontSize = 13.sp, color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentGolden) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentGolden,
                    unfocusedBorderColor = SurfaceDark,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark
                ),
                shape = RoundedCornerShape(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Categories list
        ScrollableTabRow(
            selectedTabIndex = categoriesList.indexOf(selectedCategoryKey).coerceAtLeast(0),
            containerColor = Color.Transparent,
            contentColor = AccentGolden,
            edgePadding = 20.dp,
            divider = {}
        ) {
            categoriesList.forEach { catKey ->
                val displayName = GalleryCategories.getDisplayName(catKey, isHindi)
                Tab(
                    selected = selectedCategoryKey == catKey,
                    onClick = {
                        haptic.playPremiumHaptic()
                        selectedCategoryKey = catKey
                        currentPage = 1
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

        Spacer(modifier = Modifier.height(12.dp))

        // FEATURED GALLERY SECTION (TOP HIGHLIGHT)
        if (selectedCategoryKey == GalleryCategories.ALL && searchQuery.isEmpty() && featuredItems.isNotEmpty()) {
            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = AccentGolden, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isHindi) "मुख्य अलौकिक दर्शन (Featured)" else "Featured Divine Darshan",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentGolden
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(featuredItems) { item ->
                        FeaturedGalleryCard(
                            item = item,
                            isHindi = isHindi,
                            onClick = {
                                haptic.playPremiumHaptic()
                                val index = displayItems.indexOfFirst { it.id == item.id }
                                if (index >= 0) selectedImageIndex = index else selectedImageIndex = 0
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        // MAIN GALLERY GRID WITH LOADING, SHIMMER & EMPTY / RETRY STATES
        if (isError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⚠️", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isHindi) "फोटो लोड करने में समस्या आई।" else "Error loading photos.",
                        fontSize = 14.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            isError = false
                            isLoading = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = BackgroundDark)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isHindi) "पुनः प्रयास करें" else "Retry", color = BackgroundDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else if (displayItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isHindi) "🌸 इस श्रेणी में कोई फोटो उपलब्ध नहीं है।" else "🌸 No photos found in this category.",
                    fontSize = 14.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Responsive 2 Column Grid
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(displayItems) { index, item ->
                    GalleryImageItemLuxury(
                        item = item,
                        isHindi = isHindi,
                        haptic = haptic
                    ) {
                        selectedImageIndex = index
                    }
                }
            }
        }
    }

    // Native Phone Gallery Style Full-Screen Viewer (Swipable Pager, Zoom, Save, Share)
    selectedImageIndex?.let { startIndex ->
        FullScreenImageDialogLuxury(
            items = displayItems,
            initialIndex = startIndex,
            isHindi = isHindi,
            onDismiss = { selectedImageIndex = null }
        )
    }
}

@Composable
fun FeaturedGalleryCard(
    item: GalleryItem,
    isHindi: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.5f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = if (item.thumbnailUrl.isNotEmpty()) item.thumbnailUrl else item.optimizedImageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
            )

            Text(
                text = if (isHindi || item.titleEnglish.isEmpty()) item.title else item.titleEnglish,
                color = TextWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            )
        }
    }
}

@Composable
fun GalleryImageItemLuxury(
    item: GalleryItem,
    isHindi: Boolean,
    haptic: HapticManager,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clickable {
                haptic.playPremiumHaptic()
                onClick()
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.25f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Thumbnail First Loading
            AsyncImage(
                model = if (item.thumbnailUrl.isNotEmpty()) item.thumbnailUrl else item.optimizedImageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
                    .padding(10.dp)
            ) {
                Text(
                    text = if (isHindi || item.titleEnglish.isEmpty()) item.title else item.titleEnglish,
                    color = TextWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun FullScreenImageDialogLuxury(
    items: List<GalleryItem>,
    initialIndex: Int,
    isHindi: Boolean,
    onDismiss: () -> Unit
) {
    val haptic = rememberHapticManager()
    val mediaHandler = rememberSuvicharMediaHandler()
    var statusToast by remember { mutableStateOf<String?>(null) }

    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0)),
        pageCount = { items.size }
    )

    var isPagerScrollEnabled by remember { mutableStateOf(true) }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Horizontal Pager for smooth, 1-swipe photo sliding (Phone Gallery Style)
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = isPagerScrollEnabled,
                modifier = Modifier.fillMaxSize()
            ) { pageIndex ->
                val item = items[pageIndex]
                val fullUrl = if (item.optimizedImageUrl.isNotEmpty()) item.optimizedImageUrl else item.thumbnailUrl

                ZoomableSwipableImage(
                    imageUrl = fullUrl,
                    onZoomChanged = { isZoomed ->
                        isPagerScrollEnabled = !isZoomed
                    }
                )
            }

            // Top Control Overlay (Close Button, Photo Counter, Save, Share)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Close / Back Button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Current Photo Index Indicator (e.g. 3 / 12)
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1} / ${items.size}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                // Actions: Save & Share Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Save / Download Button
                    IconButton(
                        onClick = {
                            haptic.playPremiumHaptic()
                            val currentItem = items.getOrNull(pagerState.currentPage) ?: return@IconButton
                            val targetPath = if (currentItem.optimizedImageUrl.isNotEmpty()) currentItem.optimizedImageUrl else currentItem.thumbnailUrl
                            mediaHandler.saveToGallery(
                                imageUrlOrPath = targetPath,
                                dateStr = currentItem.uploadDate,
                                quoteText = "Shree Jamniwala Darbar Gallery",
                                author = "श्री जामनीवाला दरबार",
                                onResult = { success, _ ->
                                    statusToast = if (success) {
                                        if (isHindi) "फोटो गैलरी में सेव हो गई" else "Photo saved to Gallery"
                                    } else {
                                        if (isHindi) "सेव करने में त्रुटि" else "Error saving photo"
                                    }
                                }
                            )
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Save Photo",
                            tint = AccentGolden,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Share Button
                    IconButton(
                        onClick = {
                            haptic.playPremiumHaptic()
                            val currentItem = items.getOrNull(pagerState.currentPage) ?: return@IconButton
                            val targetPath = if (currentItem.optimizedImageUrl.isNotEmpty()) currentItem.optimizedImageUrl else currentItem.thumbnailUrl
                            mediaHandler.shareSuvichar(
                                imageUrlOrPath = targetPath,
                                dateStr = currentItem.uploadDate,
                                quoteText = "Shree Jamniwala Darbar Gallery",
                                author = "श्री जामनीवाला दरबार",
                                shareText = com.example.miyank.ShareManager.formatGalleryShareMessage(currentItem.title, currentItem.category)
                            )
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Photo",
                            tint = AccentGolden,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Bottom Item Details Overlay
            val currentItem = items.getOrNull(pagerState.currentPage)
            if (currentItem != null && (currentItem.title.isNotEmpty() || currentItem.description.isNotEmpty())) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                            )
                        )
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    Column {
                        Text(
                            text = if (isHindi || currentItem.titleEnglish.isEmpty()) currentItem.title else currentItem.titleEnglish,
                            color = AccentGolden,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (currentItem.description.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isHindi || currentItem.descriptionEnglish.isEmpty()) currentItem.description else currentItem.descriptionEnglish,
                                color = TextWhite,
                                fontSize = 13.sp,
                                maxLines = 2
                            )
                        }
                    }
                }
            }

            // Status Toast Bar
            if (statusToast != null) {
                Surface(
                    color = AccentGolden,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 70.dp)
                ) {
                    Text(
                        text = statusToast!!,
                        color = BackgroundDark,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ZoomableSwipableImage(
    imageUrl: String,
    onZoomChanged: (Boolean) -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val animateScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    LaunchedEffect(scale) {
        onZoomChanged(scale > 1.05f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { tapOffset ->
                        if (scale > 1.2f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 2.5f
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f
                            offset = Offset(
                                (centerX - tapOffset.x) * 1.5f,
                                (centerY - tapOffset.y) * 1.5f
                            )
                        }
                    }
                )
            }
            .then(
                if (scale > 1.05f) {
                    Modifier.pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newScale = (scale * zoom).coerceIn(1f, 4f)
                            if (newScale <= 1.05f) {
                                scale = 1f
                                offset = Offset.Zero
                            } else {
                                scale = newScale
                                offset += pan
                            }
                        }
                    }
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Gallery Photo",
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(
                    scaleX = animateScale,
                    scaleY = animateScale,
                    translationX = offset.x,
                    translationY = offset.y
                ),
            contentScale = ContentScale.Fit
        )
    }
}
