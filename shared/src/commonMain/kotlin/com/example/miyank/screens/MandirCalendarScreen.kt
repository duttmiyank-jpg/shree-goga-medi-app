package com.example.miyank.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
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
import com.example.miyank.premiumHaptic
import com.example.miyank.rememberHapticManager

data class MandirEvent(
    val day: Int,
    val title: String,
    val type: EventType
)

enum class EventType {
    FESTIVAL, AARTI, FAST
}

@Composable
fun MandirCalendarScreen(
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    var currentMonth by remember { mutableStateOf("अगस्त 2026") }
    
    val events = listOf(
        MandirEvent(11, "सावन सोमवार", EventType.FAST),
        MandirEvent(15, "विशेष भजन", EventType.AARTI),
        MandirEvent(23, "पुत्रदा एकादशी", EventType.FAST),
        MandirEvent(25, "बाबा गोगा जी उत्सव", EventType.FESTIVAL),
        MandirEvent(28, "रक्षा बंधन / पूर्णिमा", EventType.FESTIVAL)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBF2))
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
                    tint = Color(0xFF8B4500)
                )
            }
            Text(
                text = "मंदिर कैलेंडर",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8B4500),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Month Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { haptic.playPremiumHaptic() }) {
                Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = null, tint = Color(0xFFB85C00))
            }
            Text(
                text = currentMonth,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF5D4037)
            )
            IconButton(onClick = { haptic.playPremiumHaptic() }) {
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFB85C00))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Calendar Grid
        CalendarGrid(events, haptic)

        Spacer(modifier = Modifier.height(24.dp))

        // Event List for the month
        Text(
            text = "आगामी पर्व एवं उत्सव",
            modifier = Modifier.padding(horizontal = 20.dp),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF8B4500)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(events) { event ->
                EventListItem(event)
            }
        }
    }
}

@Composable
fun CalendarGrid(events: List<MandirEvent>, haptic: com.example.miyank.HapticManager) {
    val daysOfWeek = listOf("सोम", "मंगल", "बुध", "गुरु", "शुक्र", "शनि", "रवि")
    
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Simple grid for 31 days (starting Mon for example)
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(240.dp),
            userScrollEnabled = false
        ) {
            items(31) { index ->
                val day = index + 1
                val event = events.find { it.day == day }
                
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(2.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (event != null) getEventColor(event.type).copy(alpha = 0.2f) else Color.Transparent)
                        .clickable { haptic.playPremiumHaptic() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = day.toString(),
                            fontSize = 14.sp,
                            fontWeight = if (event != null) FontWeight.Bold else FontWeight.Normal,
                            color = if (event != null) getEventColor(event.type) else Color.DarkGray
                        )
                        if (event != null) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(getEventColor(event.type))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventListItem(event: MandirEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(45.dp),
                shape = RoundedCornerShape(12.dp),
                color = getEventColor(event.type).copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = event.day.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = getEventColor(event.type)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = event.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5D4037)
                )
                Text(
                    text = when(event.type) {
                        EventType.FESTIVAL -> "पर्व एवं उत्सव"
                        EventType.AARTI -> "विशेष आरती"
                        EventType.FAST -> "व्रत एवं उपवास"
                    },
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

fun getEventColor(type: EventType): Color {
    return when(type) {
        EventType.FESTIVAL -> Color(0xFFD32F2F)
        EventType.AARTI -> Color(0xFFF57C00)
        EventType.FAST -> Color(0xFF388E3C)
    }
}
