package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MenuBook
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
import com.example.miyank.DevotionalCategories
import com.example.miyank.DevotionalContentItem
import com.example.miyank.DevotionalContentManager
import com.example.miyank.Language
import com.example.miyank.LanguageManager
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChalisaStotramScreen(
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val isHindi = LanguageManager.currentLanguage == Language.HINDI

    val allItems by DevotionalContentManager.items.collectAsState()
    val activeItems = remember(allItems) {
        allItems.filter { it.isActive }
    }

    var selectedCategory by remember { mutableStateOf(DevotionalCategories.CHALISA) }
    val filteredItems = remember(activeItems, selectedCategory) {
        activeItems.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    var selectedItemForReading by remember { mutableStateOf<DevotionalContentItem?>(null) }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(SurfaceDark)) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = if (isHindi) "चालीसा, स्तोत्रम एवं कथाएं" else "Chalisa, Stotram & Kathayein",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextWhite
                            )
                            Text(
                                text = if (isHindi) "पावन धार्मिक साहित्य संग्रह" else "Sacred Devotional Literature",
                                fontSize = 11.sp,
                                color = WarmGold
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
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SurfaceDark,
                        titleContentColor = TextWhite,
                        navigationIconContentColor = TextWhite
                    )
                )

                // Category Filter Scrollable Row
                ScrollableTabRow(
                    selectedTabIndex = DevotionalCategories.list.indexOf(selectedCategory).coerceAtLeast(0),
                    containerColor = SurfaceDark,
                    contentColor = WarmGold,
                    edgePadding = 16.dp,
                    divider = {}
                ) {
                    DevotionalCategories.list.forEach { catKey ->
                        val isSelected = selectedCategory == catKey
                        Tab(
                            selected = isSelected,
                            onClick = {
                                haptic.playPremiumHaptic()
                                selectedCategory = catKey
                            },
                            text = {
                                Text(
                                    text = DevotionalCategories.getDisplayName(catKey, isHindi),
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) WarmGold else TextMuted,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }
                }
            }
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (filteredItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isHindi) "इस श्रेणी में कोई सामग्री उपलब्ध नहीं है।" else "No content available in this category.",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredItems) { item ->
                        DevotionalContentCardUser(
                            item = item,
                            isHindi = isHindi,
                            haptic = haptic,
                            onReadClick = {
                                selectedItemForReading = item
                            }
                        )
                    }
                }
            }
        }

        // FULL SCREEN READING DIALOG
        if (selectedItemForReading != null) {
            DevotionalReadingDialog(
                item = selectedItemForReading!!,
                isHindi = isHindi,
                onDismiss = { selectedItemForReading = null }
            )
        }
    }
}

@Composable
private fun DevotionalContentCardUser(
    item: DevotionalContentItem,
    isHindi: Boolean,
    haptic: com.example.miyank.HapticManager,
    onReadClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(WarmGold.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = WarmGold, modifier = Modifier.size(24.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = DevotionalCategories.getDisplayName(item.category, isHindi) + " • " + item.author,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    haptic.playPremiumHaptic()
                    onReadClick()
                },
                modifier = Modifier.fillMaxWidth().height(42.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WarmGold)
            ) {
                Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFF160C05), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isHindi) "पढ़ें (Read Full Text)" else "Read Content", color = Color(0xFF160C05), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DevotionalReadingDialog(
    item: DevotionalContentItem,
    isHindi: Boolean,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = item.title, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite)
                    Text(text = DevotionalCategories.getDisplayName(item.category, isHindi), fontSize = 11.sp, color = WarmGold)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextWhite)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                val contentText = if (isHindi || item.contentEnglish.isEmpty()) item.contentHindi else item.contentEnglish
                Text(
                    text = contentText,
                    fontSize = 15.sp,
                    color = TextWhite,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = WarmGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isHindi) "बंद करें" else "Close", color = Color(0xFF160C05), fontWeight = FontWeight.Bold)
            }
        },
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(20.dp)
    )
}
