package com.example.miyank.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.miyank.rememberHapticManager
import com.example.miyank.rememberSuvicharMediaHandler
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementsScreen(
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val strings = LocalStrings.current
    val isHindi = LanguageManager.currentLanguage == com.example.miyank.Language.HINDI

    val announcementsList by AnnouncementManager.announcements.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(AnnouncementCategories.ALL) }
    var selectedDetailAnnouncement by remember { mutableStateOf<AnnouncementItem?>(null) }

    val filteredAnnouncements = remember(announcementsList, searchQuery, selectedCategory) {
        announcementsList.filter { item ->
            val matchesCategory = when (selectedCategory) {
                AnnouncementCategories.ALL -> true
                AnnouncementCategories.URGENT -> item.isUrgent
                else -> item.categoryTag.equals(selectedCategory, ignoreCase = true)
            }
            val matchesSearch = searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.titleEnglish.contains(searchQuery, ignoreCase = true) ||
                    item.content.contains(searchQuery, ignoreCase = true) ||
                    item.contentEnglish.contains(searchQuery, ignoreCase = true)

            item.isActive && matchesCategory && matchesSearch
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = AccentGolden,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isHindi) "महत्वपूर्ण घोषणाएं" else "Important Announcements",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // SEARCH BAR & CATEGORY FILTER BAR
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Search Input Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (isHindi) "घोषणा खोजें (Search Notices)..." else "Search Announcements...", color = TextMuted, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentGolden, modifier = Modifier.size(20.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGolden,
                        unfocusedBorderColor = WarmGold.copy(alpha = 0.3f),
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category Filter Pills
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(AnnouncementCategories.filterList) { catKey ->
                        val isSelected = selectedCategory == catKey
                        val displayName = AnnouncementCategories.getDisplayName(catKey, isHindi)

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    haptic.playPremiumHaptic()
                                    selectedCategory = catKey
                                },
                            color = if (isSelected) AccentGolden else SurfaceDark,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) AccentGolden else WarmGold.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = displayName,
                                color = if (isSelected) BackgroundDark else TextWhite,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // ANNOUNCEMENTS LIST
            if (filteredAnnouncements.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🛕", fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isHindi) "महत्वपूर्ण घोषणाएं" else "Temple Announcements",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentGolden,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isHindi) "दरबार की ओर से इस समय कोई नई सूचना उपलब्ध नहीं है।" else "There are no new announcements available from the temple at this time.",
                                fontSize = 13.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredAnnouncements) { item ->
                        AnnouncementCard(
                            item = item,
                            isHindi = isHindi,
                            onClick = {
                                haptic.playPremiumHaptic()
                                selectedDetailAnnouncement = item
                            }
                        )
                    }
                }
            }
        }
    }

    // IMMERSIVE ANNOUNCEMENT DETAILS DIALOG
    selectedDetailAnnouncement?.let { detail ->
        AnnouncementDetailDialog(
            item = detail,
            isHindi = isHindi,
            onDismiss = { selectedDetailAnnouncement = null }
        )
    }
}

@Composable
fun AnnouncementCard(
    item: AnnouncementItem,
    isHindi: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(
            1.dp,
            if (item.isUrgent) Color.Red.copy(alpha = 0.6f) else WarmGold.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Category Tag, Urgent Badge & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.isUrgent) {
                        Surface(
                            color = Color.Red.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Color.Red),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = if (isHindi) "🔴 अति आवश्यक" else "🔴 URGENT",
                                color = Color.Red,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Surface(
                        color = AccentGolden.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = AnnouncementCategories.getDisplayName(item.categoryTag, isHindi),
                            color = AccentGolden,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = formatDisplayDate(item.dateStr),
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body: Optional Small Thumbnail + Title & Text Preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                if (item.imageUrl.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .size(75.dp)
                            .padding(end = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
                    ) {
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isHindi || item.titleEnglish.isEmpty()) item.title else item.titleEnglish,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextWhite,
                        lineHeight = 22.sp
                    )

                    if (item.content.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isHindi || item.contentEnglish.isEmpty()) item.content else item.contentEnglish,
                            fontSize = 12.sp,
                            color = TextMuted,
                            maxLines = 2,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = WarmGold.copy(alpha = 0.2f), thickness = 1.dp)

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Right Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isHindi) "पूरी सूचना देखें ➔" else "View Full Info ➔",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentGolden
                )
            }
        }
    }
}

@Composable
fun AnnouncementDetailDialog(
    item: AnnouncementItem,
    isHindi: Boolean,
    onDismiss: () -> Unit
) {
    val mediaHandler = rememberSuvicharMediaHandler()
    val displayTitle = if (isHindi || item.titleEnglish.isEmpty()) item.title else item.titleEnglish
    val displayContent = if (isHindi || item.contentEnglish.isEmpty()) item.content else item.contentEnglish

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = AccentGolden.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = AnnouncementCategories.getDisplayName(item.categoryTag, isHindi),
                        color = AccentGolden,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = formatDisplayDate(item.dateStr),
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (item.isUrgent) {
                    Surface(
                        color = Color.Red.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color.Red),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isHindi) "🚨 अति आवश्यक सूचना (Urgent Announcement)" else "🚨 Urgent Announcement",
                            color = Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (item.imageUrl.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.4f))
                    ) {
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = displayTitle,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                Text(
                    text = displayTitle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextWhite,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = displayContent,
                    fontSize = 14.sp,
                    color = TextWhite.copy(alpha = 0.9f),
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // WhatsApp Share Action Button
                Button(
                    onClick = {
                        val formattedMsg = com.example.miyank.ShareManager.formatAnnouncementShareMessage(
                            title = displayTitle,
                            dateStr = formatDisplayDate(item.dateStr),
                            content = displayContent
                        )
                        val targetImage = if (item.imageUrl.isNotEmpty()) item.imageUrl else "file:///android_asset/temple_pooja.jpg"
                        mediaHandler.shareSuvichar(
                            imageUrlOrPath = targetImage,
                            dateStr = item.dateStr,
                            quoteText = displayTitle,
                            author = "श्री जामनीवाला दरबार",
                            shareText = formattedMsg
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isHindi) "WhatsApp पर शेयर करें" else "Share Announcement",
                        color = BackgroundDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isHindi) "बंद करें" else "Close", color = AccentGolden, fontWeight = FontWeight.Bold)
            }
        }
    )
}
