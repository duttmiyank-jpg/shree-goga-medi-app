package com.example.miyank.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
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
import com.example.miyank.LanguageManager
import com.example.miyank.LocalStrings
import com.example.miyank.SuvicharManager
import com.example.miyank.formatDisplayDate
import com.example.miyank.rememberHapticManager
import com.example.miyank.rememberSuvicharMediaHandler
import com.example.miyank.ui.theme.*

@Composable
fun SuvicharPreviewScreen(
    suvicharId: String = "",
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val strings = LocalStrings.current
    val isHindi = LanguageManager.currentLanguage == com.example.miyank.Language.HINDI
    val mediaHandler = rememberSuvicharMediaHandler()

    val suvichars by SuvicharManager.suvichars.collectAsState()
    val suvicharItem = suvichars.firstOrNull { it.id == suvicharId } ?: SuvicharManager.getTodaySuvichar()

    var statusMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundDark,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
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
                    text = if (isHindi) "आज का दैनिक सुविचार" else "Today's Suvichar",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentGolden,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (suvicharItem != null) {
                // ELEGANT TRANSLUCENT DATE BADGE AT TOP
                Surface(
                    color = SurfaceDark,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = if (isHindi) "दैनिक सुविचार  |  ${formatDisplayDate(suvicharItem.date)}" else "Daily Suvichar  |  ${formatDisplayDate(suvicharItem.date)}",
                        color = AccentGolden,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // DEVOTIONAL 4:5 PORTRAIT POSTER CARD
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.80f), // 4:5 Aspect Ratio
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    elevation = CardDefaults.cardElevation(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                1.dp,
                                Brush.verticalGradient(listOf(WarmGold, AccentGolden)),
                                RoundedCornerShape(28.dp)
                            )
                    ) {
                        AsyncImage(
                            model = suvicharItem.imageUrl,
                            contentDescription = strings.todaySuvicharTitle,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Smooth Cinematic Dark Gradient Overlay for Text Readability
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.5f),
                                            Color.Black.copy(alpha = 0.92f)
                                        ),
                                        startY = 200f
                                    )
                                )
                        )

                        // Content Overlay: Quote, Author & Temple Signature Branding
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 20.dp, vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (suvicharItem.quoteText.isNotEmpty()) {
                                Text(
                                    text = "“${suvicharItem.quoteText}”",
                                    color = TextWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 24.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            if (suvicharItem.author.isNotEmpty()) {
                                Text(
                                    text = "— ${suvicharItem.author}",
                                    color = AccentGolden,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                            }

                            HorizontalDivider(color = WarmGold.copy(alpha = 0.3f), thickness = 1.dp)

                            Spacer(modifier = Modifier.height(10.dp))

                            // EMOJI-FREE NOBLE TEMPLE BRANDING WATERMARK
                            Text(
                                text = "जय श्री गोगा जी महाराज",
                                color = AccentGolden,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "श्री जामनीवाला दरबार - गोगामेड़ी मोबाइल एप",
                                color = TextWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ACTION BUTTONS: DOWNLOAD & SHARE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Button(
                        onClick = {
                            haptic.playPremiumHaptic()
                            mediaHandler.saveToGallery(
                                imageUrlOrPath = suvicharItem.imageUrl,
                                dateStr = suvicharItem.date,
                                quoteText = suvicharItem.quoteText,
                                author = suvicharItem.author
                            ) { success, _ ->
                                statusMessage = if (success) {
                                    if (isHindi) "सुविचार आपकी Gallery में सुरक्षित है ✓" else "Suvichar saved to Gallery ✓"
                                } else {
                                    if (isHindi) "❌ सेव करने में त्रुटि!" else "❌ Error saving photo!"
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = BackgroundDark,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isHindi) "डाउनलोड करें" else "Download",
                            fontWeight = FontWeight.Bold,
                            color = BackgroundDark,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = {
                            haptic.playPremiumHaptic()
                            mediaHandler.shareSuvichar(
                                imageUrlOrPath = suvicharItem.imageUrl,
                                dateStr = suvicharItem.date,
                                quoteText = suvicharItem.quoteText,
                                author = suvicharItem.author,
                                shareText = com.example.miyank.ShareManager.formatSuvicharShareMessage(suvicharItem.quoteText, suvicharItem.author)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                        border = BorderStroke(1.dp, AccentGolden)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = AccentGolden,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isHindi) "शेयर करें" else "Share",
                            fontWeight = FontWeight.Bold,
                            color = AccentGolden,
                            fontSize = 14.sp
                        )
                    }
                }

                // STATUS TOAST FEEDBACK
                AnimatedVisibility(
                    visible = statusMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    statusMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            color = AccentGolden,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = msg,
                                color = BackgroundDark,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            } else {
                // DEVOTIONAL EMPTY STATE
                Spacer(modifier = Modifier.height(40.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🛕", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isHindi) "आज का सुविचार अभी निर्धारित नहीं है" else "Today's Suvichar is not scheduled yet",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentGolden,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isHindi) "“हर दिन प्रभु का एक नया संदेश आपके जीवन में शांति और प्रकाश लाता है।”" else "“Every day a divine message brings peace and light to your life.”",
                            fontSize = 13.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
