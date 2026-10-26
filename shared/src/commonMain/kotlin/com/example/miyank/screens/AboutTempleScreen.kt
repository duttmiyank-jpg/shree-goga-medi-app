package com.example.miyank.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.miyank.LocalStrings
import com.example.miyank.ui.theme.*

@Composable
fun AboutTempleScreen(
    onBackClick: () -> Unit
) {
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
                text = strings.aboutTitle,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Hero Image
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(32.dp),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            AsyncImage(
                model = "https://images.unsplash.com/photo-1621244249243-436b79b5eea8?q=80&w=1000",
                contentDescription = "Mandir Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // History Section
        AboutSectionLuxury(
            title = strings.historyTitle,
            icon = Icons.Default.Info,
            content = strings.historyContent
        )

        // Significance Section
        AboutSectionLuxury(
            title = strings.significanceTitle,
            icon = Icons.Default.Info,
            content = strings.significanceContent
        )

        // Location Section
        AboutSectionLuxury(
            title = strings.locationTitle,
            icon = Icons.Default.LocationOn,
            content = strings.locationContent
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun AboutSectionLuxury(title: String, icon: ImageVector, content: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AccentGolden.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = AccentGolden)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = content,
                fontSize = 15.sp,
                color = TextMuted,
                lineHeight = 24.sp,
                textAlign = TextAlign.Justify
            )
        }
    }
}
