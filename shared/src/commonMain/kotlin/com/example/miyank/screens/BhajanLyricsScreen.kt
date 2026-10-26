package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyank.BhajanCategories
import com.example.miyank.BhajanItem
import com.example.miyank.BhajanManager
import com.example.miyank.LanguageManager
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BhajanLyricsScreen(
    onBhajanClick: (BhajanItem) -> Unit,
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val isHindi = LanguageManager.currentLanguage == com.example.miyank.Language.HINDI

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf(BhajanCategories.ALL) }

    // Dynamic Bhajan Items from Admin Persistent Manager
    val bhajansState by BhajanManager.bhajans.collectAsState()

    val filteredBhajans = remember(bhajansState, searchQuery, selectedCategoryFilter) {
        bhajansState.filter { item ->
            val matchesCategory = when (selectedCategoryFilter) {
                BhajanCategories.ALL -> true
                else -> item.categoryTag.equals(selectedCategoryFilter, ignoreCase = true)
            }

            val matchesSearch = searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.titleEnglish.contains(searchQuery, ignoreCase = true) ||
                    item.lyricsText.contains(searchQuery, ignoreCase = true)

            item.isActive && item.lyricsText.isNotEmpty() && matchesCategory && matchesSearch
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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextWhite
                )
            }
            Text(
                text = if (isHindi) "भजन, आरती, चालीसा एवं स्तुति संग्रह" else "Bhajan, Aarti & Stuti Lyrics",
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text(if (isHindi) "भजन, आरती, चालीसा या स्तुति खोजें..." else "Search Bhajan, Aarti, Stuti...", color = TextMuted, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentGolden) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentGolden,
                unfocusedBorderColor = WarmGold.copy(alpha = 0.3f),
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Pills
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(BhajanCategories.adminList) { catKey ->
                val isSelected = selectedCategoryFilter == catKey
                val displayName = BhajanCategories.getDisplayName(catKey, isHindi)

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            haptic.playPremiumHaptic()
                            selectedCategoryFilter = if (isSelected) BhajanCategories.ALL else catKey
                        },
                    color = if (isSelected) AccentGolden else SurfaceDark,
                    border = BorderStroke(1.dp, if (isSelected) AccentGolden else WarmGold.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = displayName,
                        color = if (isSelected) BackgroundDark else TextWhite,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (filteredBhajans.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isHindi) "🌸 कोई लिरिक्स या लिखाई उपलब्ध नहीं है।" else "🌸 No lyrics found.",
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredBhajans) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                haptic.playPremiumHaptic()
                                onBhajanClick(item)
                            },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(AccentGolden.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = AccentGolden,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    color = AccentGolden.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = BhajanCategories.getDisplayName(item.categoryTag, isHindi),
                                        color = AccentGolden,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = if (isHindi || item.titleEnglish.isEmpty()) item.title else item.titleEnglish,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextWhite
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.lyricsText.take(70) + "...",
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                    maxLines = 1
                                )
                            }

                            Text(
                                text = "पढ़ें ➔",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentGolden
                            )
                        }
                    }
                }
            }
        }
    }
}
