package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyank.LanguageManager
import com.example.miyank.LocalStrings
import com.example.miyank.NotificationCategories
import com.example.miyank.NotificationItem
import com.example.miyank.NotificationManager
import com.example.miyank.TargetRoutes
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationHistoryScreen(
    onBackClick: () -> Unit,
    onNavigateTarget: (targetRoute: String, targetId: String) -> Unit = { _, _ -> }
) {
    val haptic = rememberHapticManager()
    val strings = LocalStrings.current
    val isHindi = LanguageManager.currentLanguage == com.example.miyank.Language.HINDI

    val notificationsList by NotificationManager.notifications.collectAsState()

    var selectedTab by remember { mutableStateOf(NotificationCategories.ALL) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val filteredNotifications = remember(notificationsList, selectedTab, selectedCategoryFilter) {
        notificationsList.filter { item ->
            val matchesTab = when (selectedTab) {
                NotificationCategories.ALL -> true
                NotificationCategories.UNREAD -> !item.isRead
                NotificationCategories.URGENT -> item.isUrgent
                NotificationCategories.GENERAL -> !item.isUrgent
                else -> true
            }
            val matchesCategory = selectedCategoryFilter == null ||
                    item.categoryTag.equals(selectedCategoryFilter, ignoreCase = true)

            matchesTab && matchesCategory
        }
    }

    val unreadCount = NotificationManager.getUnreadCount()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isHindi) "🔔 मंदिर सूचनाएँ" else "🔔 Notifications",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextWhite
                            )
                            if (unreadCount > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = AccentGolden,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "$unreadCount New",
                                        color = BackgroundDark,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (isHindi) "श्री जामनीवाला दरबार की नवीनतम जानकारी" else "Latest updates from Shree Jamniwala Darbar",
                            fontSize = 11.sp,
                            color = AccentGolden,
                            fontWeight = FontWeight.Medium
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
            // QUICK ACTIONS & TABS BAR
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mark All as Read Button
                    TextButton(
                        onClick = {
                            haptic.playPremiumHaptic()
                            NotificationManager.markAllAsRead()
                        },
                        enabled = unreadCount > 0
                    ) {
                        Icon(Icons.Default.DoneAll, contentDescription = null, tint = if (unreadCount > 0) AccentGolden else TextMuted, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isHindi) "सभी पढ़े गए" else "Mark All Read",
                            color = if (unreadCount > 0) AccentGolden else TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Clear History Button
                    TextButton(
                        onClick = {
                            haptic.playPremiumHaptic()
                            showClearConfirmDialog = true
                        },
                        enabled = notificationsList.isNotEmpty()
                    ) {
                        Icon(Icons.Default.CleaningServices, contentDescription = null, tint = if (notificationsList.isNotEmpty()) Color.Red else TextMuted, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isHindi) "इतिहास साफ़ करें" else "Clear History",
                            color = if (notificationsList.isNotEmpty()) Color.Red else TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Main Filter Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NotificationCategories.filterTabs.forEach { tabKey ->
                        val isSelected = selectedTab == tabKey
                        val displayName = NotificationCategories.getDisplayName(tabKey, isHindi)

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    haptic.playPremiumHaptic()
                                    selectedTab = tabKey
                                },
                            color = if (isSelected) AccentGolden else BackgroundDark,
                            border = BorderStroke(1.dp, if (isSelected) AccentGolden else WarmGold.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = displayName,
                                color = if (isSelected) BackgroundDark else TextWhite,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Devotional Categories Horizontal Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    item {
                        val isAllSelected = selectedCategoryFilter == null
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    haptic.playPremiumHaptic()
                                    selectedCategoryFilter = null
                                },
                            color = if (isAllSelected) AccentGolden.copy(alpha = 0.25f) else BackgroundDark,
                            border = BorderStroke(1.dp, if (isAllSelected) AccentGolden else WarmGold.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isHindi) "सभी कैटेगरी" else "All Categories",
                                color = if (isAllSelected) AccentGolden else TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    items(NotificationCategories.categoriesList) { catKey ->
                        val isSelected = selectedCategoryFilter == catKey
                        val displayName = NotificationCategories.getDisplayName(catKey, isHindi)

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    haptic.playPremiumHaptic()
                                    selectedCategoryFilter = if (isSelected) null else catKey
                                },
                            color = if (isSelected) AccentGolden.copy(alpha = 0.25f) else BackgroundDark,
                            border = BorderStroke(1.dp, if (isSelected) AccentGolden else WarmGold.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = displayName,
                                color = if (isSelected) AccentGolden else TextWhite,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // NOTIFICATIONS LIST
            if (filteredNotifications.isEmpty()) {
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
                            Text("🔕", fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isHindi) "अभी कोई नई सूचना नहीं" else "No Notifications Yet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentGolden,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isHindi) "मंदिर की नवीनतम सूचनाएँ एवं दिव्य अपडेट यहाँ दिखाई देंगे।" else "Latest temple notices and updates will appear here.",
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredNotifications, key = { "${it.id}_${it.createdAt}" }) { item ->
                        MandirNotificationCard(
                            item = item,
                            isHindi = isHindi,
                            onClick = {
                                haptic.playPremiumHaptic()
                                NotificationManager.markAsRead(item.id)
                                onNavigateTarget(item.targetRoute, item.targetId)
                            }
                        )
                    }
                }
            }
        }
    }

    // CLEAR HISTORY CONFIRMATION DIALOG
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            containerColor = SurfaceDark,
            titleContentColor = Color.Red,
            textContentColor = TextWhite,
            title = {
                Text(
                    text = if (isHindi) "इतिहास साफ़ करने की पुष्टि" else "Confirm Clear History",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Red,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = if (isHindi) "क्या आप सभी मंदिर सूचनाओं का इतिहास साफ़ करना चाहते हैं?" else "Are you sure you want to clear all notification history?",
                    color = TextWhite,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirmDialog = false
                        haptic.playPremiumHaptic()
                        NotificationManager.clearHistory()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(
                        text = if (isHindi) "इतिहास साफ़ करें" else "Clear History",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text(text = if (isHindi) "रद्द करें" else "Cancel", color = TextMuted)
                }
            }
        )
    }
}

@Composable
fun MandirNotificationCard(
    item: NotificationItem,
    isHindi: Boolean,
    onClick: () -> Unit
) {
    val displayTitle = if (isHindi || item.titleEnglish.isEmpty()) item.title else item.titleEnglish
    val displayMessage = if (isHindi || item.messageEnglish.isEmpty()) item.message else item.messageEnglish

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!item.isRead) SurfaceDark else SurfaceDark.copy(alpha = 0.75f)
        ),
        border = BorderStroke(
            1.dp,
            when {
                item.isUrgent -> Color.Red.copy(alpha = 0.6f)
                !item.isRead -> AccentGolden.copy(alpha = 0.5f)
                else -> WarmGold.copy(alpha = 0.25f)
            }
        ),
        elevation = CardDefaults.cardElevation(if (!item.isRead) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Golden Glowing Icon Badge
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (!item.isRead) AccentGolden.copy(alpha = 0.20f) else WarmGold.copy(alpha = 0.10f)
                    )
                    .border(
                        1.dp,
                        if (!item.isRead) AccentGolden else WarmGold.copy(alpha = 0.3f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.isUrgent) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                    contentDescription = null,
                    tint = if (item.isUrgent) Color.Red else AccentGolden,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Header Row: Category Badge, Unread Dot, Timestamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = AccentGolden.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = NotificationCategories.getDisplayName(item.categoryTag, isHindi),
                                color = AccentGolden,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (!item.isRead) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(AccentGolden)
                            )
                        }
                    }

                    Text(
                        text = item.timestampStr,
                        fontSize = 10.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Title
                Text(
                    text = displayTitle,
                    fontSize = 14.sp,
                    fontWeight = if (!item.isRead) FontWeight.ExtraBold else FontWeight.Bold,
                    color = TextWhite,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Body Message
                Text(
                    text = displayMessage,
                    fontSize = 12.sp,
                    color = TextWhite.copy(alpha = 0.85f),
                    lineHeight = 18.sp
                )

                if (item.targetRoute.isNotEmpty() && item.targetRoute != TargetRoutes.HOME) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "देखने के लिए टैप करें ➔",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGolden
                    )
                }
            }
        }
    }
}
