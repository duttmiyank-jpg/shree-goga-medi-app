package com.example.miyank.screens

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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.miyank.AppStrings
import com.example.miyank.HapticManager
import com.example.miyank.LocalStrings
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*

@Composable
fun EventsScreen(
    onBackClick: () -> Unit = {}
) {
    val haptic = rememberHapticManager()
    val scrollState = rememberScrollState()
    val strings = LocalStrings.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(scrollState)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
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
                text = strings.eventsTitle,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // 1. Shubh Muhurat Section
        SectionHeaderLuxury(strings.auspiciousDates)
        MuhuratSectionLuxury(haptic, strings)

        Spacer(modifier = Modifier.height(32.dp))

        // 2. Upcoming Events (This Month)
        SectionHeaderLuxury(strings.upcomingEvents)

        EventDetailCardLuxury(
            title = strings.bhajanKirtan,
            date = strings.everySunday,
            time = "4:00 PM ${strings.onwards}",
            location = strings.welcomeTitle,
            description = strings.bhajanDesc,
            imageUrl = "file:///android_asset/temple_pooja.jpg",
            haptic = haptic
        )

        EventDetailCardLuxury(
            title = strings.specialAarti,
            date = strings.everyTuesday,
            time = "8:00 PM",
            location = strings.mainDarbar,
            description = strings.aartiDesc,
            imageUrl = "file:///android_asset/temple_pooja.jpg",
            haptic = haptic
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 3. Next Month Section
        SectionHeaderLuxury(strings.comingSoon)

        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Text(
                text = strings.comingSoonNote,
                modifier = Modifier.padding(24.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun SectionHeaderLuxury(title: String) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.ExtraBold,
        color = TextWhite,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
    )
}

@Composable
fun MuhuratSectionLuxury(haptic: HapticManager, strings: AppStrings) {
    val muhurats = listOf(
        (if (strings.currentLanguageName == "English") "Ekadashi" else "एकादशी") to "Aug 23",
        (if (strings.currentLanguageName == "English") "Purnima" else "पूर्णिमा") to "Aug 28",
        (if (strings.currentLanguageName == "English") "Amavasya" else "अमावस्या") to "Aug 14",
        (if (strings.currentLanguageName == "English") "Chouki" else "चौकी") to strings.everyTuesday
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(muhurats) { muhurat ->
            Card(
                modifier = Modifier.width(110.dp).clickable { haptic.playPremiumHaptic() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = muhurat.first, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccentGolden)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = muhurat.second, fontSize = 12.sp, color = TextMuted)
                }
            }
        }
    }
}

@Composable
fun EventDetailCardLuxury(
    title: String,
    date: String,
    time: String,
    location: String,
    description: String,
    imageUrl: String,
    haptic: HapticManager
) {
    var isReminded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().height(180.dp)
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Reminder Toggle
                Surface(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopEnd)
                        .clickable {
                            haptic.playPremiumHaptic()
                            isReminded = !isReminded
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isReminded) AccentGolden else Color.Black.copy(alpha = 0.6f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(8.dp).size(20.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(text = title, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite)

                Spacer(modifier = Modifier.height(16.dp))

                EventLuxuryInfoItem(Icons.Default.CalendarMonth, date)
                Spacer(modifier = Modifier.height(8.dp))
                EventLuxuryInfoItem(Icons.Default.Schedule, time)
                Spacer(modifier = Modifier.height(8.dp))
                EventLuxuryInfoItem(Icons.Default.LocationOn, location)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = TextMuted,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
fun EventLuxuryInfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentGolden,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextWhite
        )
    }
}
