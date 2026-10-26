package com.example.miyank.screens

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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.miyank.LanguageManager
import com.example.miyank.LiveDarshanManager
import com.example.miyank.LivePlatform
import com.example.miyank.LivePlatformType
import com.example.miyank.MusicManager
import com.example.miyank.openExternalUrl
import com.example.miyank.rememberHapticManager
import com.example.miyank.rememberSuvicharMediaHandler
import com.example.miyank.ui.components.OfficialYouTubeLogo
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveDarshanScreen(
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val mediaHandler = rememberSuvicharMediaHandler()
    val uriHandler = LocalUriHandler.current
    val isHindi = LanguageManager.currentLanguage == com.example.miyank.Language.HINDI
    val scrollState = rememberScrollState()

    val settings by LiveDarshanManager.settings.collectAsState()
    val activePlatforms = remember(settings.platforms, settings.streamUrl) {
        LiveDarshanManager.getActivePlatforms()
    }
    val isAnyLive = remember(activePlatforms, settings.isLiveNow) {
        LiveDarshanManager.getActualPlayableStatus() || activePlatforms.any { it.isLive }
    }

    val primaryVideoId = remember(settings.streamUrl) {
        LiveDarshanManager.extractYouTubeVideoId(settings.streamUrl)
    }

    var launchErrorMessage by remember { mutableStateOf<String?>(null) }

    // PAUSE BACKGROUND BHAJAN / DEFAULT MUSIC WHEN LIVE SCREEN OPENS
    DisposableEffect(Unit) {
        MusicManager.setOtherMediaPlaying(true)
        onDispose {
            MusicManager.setOtherMediaPlaying(false)
        }
    }

    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    fun launchPlatform(platformUrl: String) {
        haptic.playPremiumHaptic()
        launchErrorMessage = null
        val targetUrl = LiveDarshanManager.getValidLaunchUrl(platformUrl)
        if (targetUrl.isNotEmpty()) {
            val success = openExternalUrl(targetUrl)
            if (!success) {
                try {
                    uriHandler.openUri(targetUrl)
                } catch (e: Exception) {
                    launchErrorMessage = if (isHindi) "लिंक खोलने में समस्या हुई। कृपया कुछ देर बाद पुनः प्रयास करें।" else "Unable to open live link. Please try again later."
                }
            }
        } else {
            launchErrorMessage = if (isHindi) "लाइव दर्शन लिंक अभी उपलब्ध नहीं है।" else "Live Stream URL is currently unavailable."
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isHindi) "🔴 लाइव दर्शन" else "🔴 Live Darshan",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite
                        )
                        Text(
                            text = if (isHindi) "श्री जामनीवाला दरबार से सीधे लाइव दर्शन एवं आरती" else "Direct Live Darshan from Shree Jamniwala Darbar",
                            fontSize = 11.sp,
                            color = AccentGolden,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // HERO PREVIEW & CTA CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // HEADER ROW WITH LIVE BADGE
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isAnyLive) {
                            Surface(
                                color = Color.Red.copy(alpha = pulseAlpha),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color.White))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "🔴 LIVE NOW",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        } else {
                            Surface(
                                color = BackgroundDark,
                                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "⏸ OFFLINE",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = "श्री जामनीवाला दरबार",
                            color = AccentGolden,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // PREVIEW THUMBNAIL CONTAINER
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black)
                            .clickable {
                                if (activePlatforms.isNotEmpty()) {
                                    launchPlatform(activePlatforms.first().url)
                                }
                            }
                    ) {
                        val thumbUrl = if (primaryVideoId != null) LiveDarshanManager.getYoutubeThumbnailUrl(primaryVideoId) else ""
                        if (thumbUrl.isNotEmpty()) {
                            AsyncImage(
                                model = thumbUrl,
                                contentDescription = "Live Darshan Poster",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            AsyncImage(
                                model = "file:///android_asset/temple_pooja.jpg",
                                contentDescription = "Temple Fallback",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Gradient overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                                    )
                                )
                        )

                        if (isAnyLive) {
                            Surface(
                                color = Color.Red.copy(alpha = 0.95f),
                                shape = CircleShape,
                                modifier = Modifier.align(Alignment.Center).size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play Live",
                                        tint = Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // STREAM TITLE & DESCRIPTION
                    Text(
                        text = if (isHindi || settings.titleEnglish.isEmpty()) settings.title else settings.titleEnglish,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isAnyLive) (if (isHindi) "अभी लाइव दर्शन चल रहा है" else "Live Darshan is currently active")
                               else (if (isHindi) "अभी लाइव दर्शन उपलब्ध नहीं है। अगले लाइव दर्शन की जानकारी जल्द उपलब्ध होगी।" else "Live Darshan is currently offline. Please check back during Aarti timings."),
                        fontSize = 13.sp,
                        color = if (isAnyLive) AccentGolden else TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // MULTI-PLATFORM SELECTION BUTTONS SECTION
            Text(
                text = if (isHindi) "📡 लाइव दर्शन के विकल्प" else "📡 Live Stream Options",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AccentGolden
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (activePlatforms.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = AccentGolden, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isHindi) "अभी कोई लाइव प्लेटफॉर्म उपलब्ध नहीं है।" else "No live streaming platform available right now.",
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    activePlatforms.forEach { platform ->
                        LivePlatformButton(
                            platform = platform,
                            isHindi = isHindi,
                            onClick = { launchPlatform(platform.url) }
                        )
                    }
                }
            }

            if (launchErrorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = launchErrorMessage!!,
                    color = Color.Red,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SHARE BUTTON WITH ALL ENABLED PLATFORM LINKS
            Button(
                onClick = {
                    haptic.playPremiumHaptic()
                    val shareImg = if (settings.shareCardImage.isNotEmpty()) settings.shareCardImage else "file:///android_asset/temple_pooja.jpg"
                    val customMsg = if (isHindi || settings.shareMessageEnglish.isEmpty()) settings.shareMessageHindi else settings.shareMessageEnglish
                    val baseText = if (customMsg.isNotBlank()) customMsg else (if (isHindi) "🔴 लाइव दर्शन\n\nश्री जामनीवाला दरबार से सीधे लाइव दर्शन एवं आरती देखें।" else "🔴 Live Darshan\n\nWatch direct Live Darshan and Aarti from Shree Jamniwala Darbar.")

                    val linksBuilder = StringBuilder()
                    activePlatforms.forEach { p ->
                        val platformName = when (p.platformType) {
                            LivePlatformType.YOUTUBE -> "YouTube"
                            LivePlatformType.INSTAGRAM -> "Instagram"
                            LivePlatformType.FACEBOOK -> "Facebook"
                            LivePlatformType.OTHER -> p.name
                        }
                        linksBuilder.append("\n$platformName:\n${LiveDarshanManager.getValidLaunchUrl(p.url)}")
                    }

                    val shareText = "$baseText$linksBuilder\n\n📱 श्री जामनीवाला दरबार मोबाइल एप डाउनलोड करें:\n${com.example.miyank.AppShareConfig.getOfficialAppLink()}"

                    mediaHandler.shareSuvichar(
                        imageUrlOrPath = shareImg,
                        dateStr = "",
                        quoteText = settings.shareTitle.ifEmpty { settings.title },
                        author = "श्री जामनीवाला दरबार",
                        shareText = shareText
                    )
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, AccentGolden.copy(alpha = 0.6f))
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = AccentGolden, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isHindi) "लाइव दर्शन लिंक शेयर करें" else "Share Live Darshan Links",
                    color = AccentGolden,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // DYNAMIC DAILY AARTI TIMINGS CARD
            val activeAartis = remember(settings.aartiTimings) {
                settings.aartiTimings.filter { it.isActive }.sortedBy { it.sortOrder }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (isHindi) "🕉️ दैनिक आरती समय" else "🕉️ Daily Aarti Timings",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentGolden
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (activeAartis.isEmpty()) {
                        Text(
                            text = if (isHindi) "आरती समय की जानकारी जल्द उपलब्ध होगी।" else "Aarti timing details will be available soon.",
                            color = TextMuted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        activeAartis.forEachIndexed { index, aarti ->
                            if (index > 0) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = WarmGold.copy(alpha = 0.2f))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isHindi || aarti.nameEnglish.isEmpty()) aarti.nameHindi else aarti.nameEnglish,
                                        color = TextWhite,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (aarti.description.isNotEmpty()) {
                                        Text(
                                            text = aarti.description,
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                Text(
                                    text = aarti.time,
                                    color = AccentGolden,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun LivePlatformButton(
    platform: LivePlatform,
    isHindi: Boolean,
    onClick: () -> Unit
) {
    val buttonColor = when (platform.platformType) {
        LivePlatformType.YOUTUBE -> Color.Red
        LivePlatformType.INSTAGRAM -> Color(0xFFE1306C)
        LivePlatformType.FACEBOOK -> Color(0xFF1877F2)
        LivePlatformType.OTHER -> SurfaceDark
    }

    val buttonText = when (platform.platformType) {
        LivePlatformType.YOUTUBE -> if (isHindi) "▶ YouTube पर लाइव दर्शन देखें" else "▶ Watch Live on YouTube"
        LivePlatformType.INSTAGRAM -> if (isHindi) "◎ Instagram पर लाइव दर्शन देखें" else "◎ Watch Live on Instagram"
        LivePlatformType.FACEBOOK -> if (isHindi) "f Facebook पर लाइव दर्शन देखें" else "f Watch Live on Facebook"
        LivePlatformType.OTHER -> if (isHindi) "${platform.name} पर लाइव देखें" else "Watch Live on ${platform.name}"
    }

    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (platform.platformType == LivePlatformType.YOUTUBE) {
                    OfficialYouTubeLogo(20.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = buttonText,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            }

            if (platform.isLive) {
                Surface(
                    color = Color.White.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "🔴 LIVE",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
