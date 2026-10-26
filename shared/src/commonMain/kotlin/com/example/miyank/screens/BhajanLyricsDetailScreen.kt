package com.example.miyank.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyank.rememberHapticManager

@Composable
fun BhajanLyricsDetailScreen(
    title: String,
    lyrics: String,
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val scrollState = rememberScrollState()

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
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8B4500),
                modifier = Modifier.padding(start = 8.dp),
                maxLines = 1
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = lyrics,
                        fontSize = 18.sp,
                        lineHeight = 32.sp,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF5D4037),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                val mediaHandler = com.example.miyank.rememberSuvicharMediaHandler()
                Button(
                    onClick = {
                        haptic.playPremiumHaptic()
                        val formattedMsg = com.example.miyank.ShareManager.formatGeneralShareMessage(title, lyrics.take(200))
                        mediaHandler.shareSuvichar(
                            imageUrlOrPath = "file:///android_asset/temple_pooja.jpg",
                            dateStr = "",
                            quoteText = title,
                            author = "श्री जामनीवाला दरबार",
                            shareText = formattedMsg
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.miyank.ui.theme.AccentGolden),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = com.example.miyank.ui.theme.BackgroundDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share Bhajan", color = com.example.miyank.ui.theme.BackgroundDark, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
