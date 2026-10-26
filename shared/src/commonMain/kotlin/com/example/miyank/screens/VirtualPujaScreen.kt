package com.example.miyank.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.miyank.LocalStrings
import com.example.miyank.MusicManager
import com.example.miyank.rememberAudioPlayer
import com.example.miyank.rememberBellSoundPlayer
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VirtualPujaScreen(
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val strings = LocalStrings.current
    val aartiPlayer = rememberAudioPlayer()
    val bellPlayer = rememberBellSoundPlayer()
    val aartiSettings by MusicManager.aartiSettings.collectAsState()

    var isAartiStarted by remember { mutableStateOf(false) }
    var isBellRung by remember { mutableStateOf(false) }
    var showPushpanjali by remember { mutableStateOf(false) }
    var showPrasad by remember { mutableStateOf(false) }
    var showBlessing by remember { mutableStateOf(false) }
    var showChawar by remember { mutableStateOf(false) }

    // Aarti Song Playback & Background Music Control
    LaunchedEffect(isAartiStarted) {
        MusicManager.setOtherMediaPlaying(isAartiStarted)
        if (isAartiStarted) {
            aartiPlayer.play(aartiSettings.localFilePath ?: "")
        } else {
            aartiPlayer.pause()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            MusicManager.setOtherMediaPlaying(false)
            aartiPlayer.stop()
            bellPlayer.stop()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pooja_animations")

    // Circular Gol-Gol Aarti Angle Animation (0 to 360 degrees)
    val aartiAngleDegree by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "aarti_circle"
    )

    val aartiRad = (aartiAngleDegree * kotlin.math.PI / 180f).toFloat()
    val aartiOffsetX = (cos(aartiRad) * 55f).dp
    val aartiOffsetY = (sin(aartiRad) * 25f).dp

    // Flame & Divine Light Glow Scale
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glow"
    )

    // Mor Pankh Feather Seva Waving Angle
    val morPankhAngle by infiniteTransition.animateFloat(
        initialValue = -35f,
        targetValue = 35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "mor_pankh"
    )

    // Bell Swing Angle
    val bellSwing by infiniteTransition.animateFloat(
        initialValue = -18f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(280, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "bell"
    )

    val scrollState = rememberScrollState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
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
                    text = strings.virtualPujaTitle,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentGolden,
                    modifier = Modifier.padding(start = 8.dp),
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // SACRED TEMPLE BACKDROP WITH REAL PHOTO & DIVINE ILLUMINATION
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .height(420.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // FULL UNCROPPED TEMPLE PHOTO
                    AsyncImage(
                        model = "file:///android_asset/temple_pooja.jpg",
                        contentDescription = "Shree Goga Madi Darbar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    // Layer 1: Divine Golden Ambient Glow
                    if (isAartiStarted || isBellRung || showPushpanjali) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            LuxuryGold.copy(alpha = 0.45f * glowScale),
                                            SaffronAccent.copy(alpha = 0.30f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }

                    // Layer 2: AASHIRWAD DIVINE LIGHT FLOOD (दिव्य प्रकाश & PURE DEVOTIONAL VIBES)
                    if (showBlessing) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            WarmGold.copy(alpha = 0.65f * glowScale),
                                            LuxuryGold.copy(alpha = 0.50f),
                                            SaffronAccent.copy(alpha = 0.35f)
                                        )
                                    )
                                )
                        )
                    }

                    // Layer 3: Circular Gol-Gol Rotating Aarti Thali
                    if (isAartiStarted) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .offset(x = aartiOffsetX, y = (-40).dp + aartiOffsetY)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .scale(glowScale)
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(WarmGold, SaffronAccent, Color.Transparent)
                                            ),
                                            CircleShape
                                        )
                                )
                                Text(
                                    text = "🪔",
                                    fontSize = 72.sp,
                                    modifier = Modifier.offset(y = (-26).dp)
                                )
                            }
                        }
                    }

                    // Layer 4: Mor Pankh Feather Seva Waving Across Temple
                    if (showChawar) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .rotate(morPankhAngle)
                        ) {
                            Text(text = "🪶", fontSize = 80.sp)
                        }
                    }

                    // Layer 5: Falling Pure Flower Shower (Pushpanjali)
                    if (showPushpanjali) {
                        FallingFlowersAnimation()
                    }

                    // Layer 6: Animated Ringing Temple Bell
                    if (isBellRung) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 16.dp)
                                .rotate(bellSwing)
                        ) {
                            Text(
                                text = "🔔",
                                fontSize = 64.sp
                            )
                        }
                    }

                    // Temple Title Overlay Badge
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp),
                        color = Color.Black.copy(alpha = 0.70f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "🛕 श्री गोगा माड़ी दरबार",
                            color = LuxuryGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Active Message Banners (Prasad / Blessings)
            AnimatedVisibility(visible = showPrasad) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(bottom = 8.dp),
                    color = AccentGolden.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, AccentGolden)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🥮", fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "बाबा जी को शुद्ध देशी घी के लड्डू एवं बर्फी का भोग अर्पित हुआ! 🙏",
                            color = TextWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            AnimatedVisibility(visible = showBlessing) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(bottom = 8.dp),
                    color = WarmGold.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, WarmGold)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "✨", fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "सत् गुरु बाबा सोम दत्त जी एवं बाबा गोगा जी का पावन आशीर्वाद आपके साथ है! 🙏",
                            color = WarmGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // DEVOTIONAL ACTION CONTROLS (3x2 Grid)
            Column(
                modifier = Modifier.fillMaxWidth(0.92f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Button 1: Start/Stop Aarti
                    DevotionalCard(
                        modifier = Modifier.weight(1f),
                        title = if (isAartiStarted) strings.stopAarti else strings.startAarti,
                        icon = "🪔",
                        isActive = isAartiStarted,
                        activeColor = AccentGolden,
                        onClick = {
                            haptic.playPremiumHaptic()
                            isAartiStarted = !isAartiStarted
                        }
                    )

                    // Button 2: Ring Temple Bell
                    DevotionalCard(
                        modifier = Modifier.weight(1f),
                        title = "घंटी बजाएं",
                        icon = "🔔",
                        isActive = isBellRung,
                        activeColor = WarmGold,
                        onClick = {
                            haptic.playPremiumHaptic()
                            bellPlayer.playBellSound()
                            isBellRung = true
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Button 3: Pushpanjali / Flower Shower
                    DevotionalCard(
                        modifier = Modifier.weight(1f),
                        title = "फूल बरसाएं",
                        icon = "🌸",
                        isActive = showPushpanjali,
                        activeColor = SaffronAccent,
                        onClick = {
                            haptic.playPremiumHaptic()
                            showPushpanjali = true
                        }
                    )

                    // Button 4: Offer Prasad (Laddoo / Barfi)
                    DevotionalCard(
                        modifier = Modifier.weight(1f),
                        title = "प्रसाद चढ़ाएं",
                        icon = "🥮",
                        isActive = showPrasad,
                        activeColor = AccentGolden,
                        onClick = {
                            haptic.playPremiumHaptic()
                            showPrasad = true
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Button 5: Take Divine Blessings
                    DevotionalCard(
                        modifier = Modifier.weight(1f),
                        title = "आशीर्वाद लें",
                        icon = "✨",
                        isActive = showBlessing,
                        activeColor = WarmGold,
                        onClick = {
                            haptic.playPremiumHaptic()
                            showBlessing = true
                        }
                    )

                    // Button 6: Mor Pankh Feather Seva
                    DevotionalCard(
                        modifier = Modifier.weight(1f),
                        title = "सेवा",
                        icon = "🪶",
                        isActive = showChawar,
                        activeColor = LuxuryGold,
                        onClick = {
                            haptic.playPremiumHaptic()
                            showChawar = !showChawar
                        }
                    )
                }
            }

            // Auto-reset timers for transient actions
            LaunchedEffect(isBellRung) {
                if (isBellRung) {
                    bellPlayer.playBellSound()
                    kotlinx.coroutines.delay(2500)
                    isBellRung = false
                }
            }

            LaunchedEffect(showPushpanjali) {
                if (showPushpanjali) {
                    kotlinx.coroutines.delay(3500)
                    showPushpanjali = false
                }
            }

            LaunchedEffect(showPrasad) {
                if (showPrasad) {
                    kotlinx.coroutines.delay(4000)
                    showPrasad = false
                }
            }

            LaunchedEffect(showBlessing) {
                if (showBlessing) {
                    kotlinx.coroutines.delay(4500)
                    showBlessing = false
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = strings.pujaHomeNote,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                textAlign = TextAlign.Center,
                color = TextMuted,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun DevotionalCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) activeColor.copy(alpha = 0.25f) else SurfaceDark
        ),
        border = BorderStroke(
            if (isActive) 2.dp else 1.dp,
            if (isActive) activeColor else SurfaceDark
        ),
        elevation = CardDefaults.cardElevation(if (isActive) 6.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 32.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) activeColor else TextWhite,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun FallingFlowersAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "flowers")
    val flowerProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "flower_fall"
    )

    val flowers = listOf("🌸", "🌺", "🌼", "🌹", "🌸", "🌺", "🌼", "🌹")

    Box(modifier = Modifier.fillMaxSize()) {
        flowers.forEachIndexed { index, flower ->
            val xOffset = (index * 42 - 130).dp
            val startY = (-30).dp
            val endY = 330.dp
            val currentY = startY + (endY - startY) * ((flowerProgress + index * 0.14f) % 1f)

            Text(
                text = flower,
                fontSize = (22 + index % 3 * 4).sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(x = xOffset, y = currentY)
                    .alpha(1f - (currentY / endY).coerceIn(0f, 1f))
            )
        }
    }
}
