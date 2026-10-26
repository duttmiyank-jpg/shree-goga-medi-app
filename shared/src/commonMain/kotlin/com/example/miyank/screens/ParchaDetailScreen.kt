package com.example.miyank.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.example.miyank.HapticManager
import com.example.miyank.Language
import com.example.miyank.LanguageManager
import com.example.miyank.LocalStrings
import com.example.miyank.ParchaItem
import com.example.miyank.ParchaManager
import com.example.miyank.UserProfileManager
import com.example.miyank.formatDisplayDate
import com.example.miyank.formatEpochToDateString
import com.example.miyank.rememberHapticManager
import com.example.miyank.rememberSuvicharMediaHandler
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParchaDetailScreen(
    parchaId: String,
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val strings = LocalStrings.current
    val isHindi = LanguageManager.currentLanguage == Language.HINDI

    val verifiedMobileNumber by ParchaManager.verifiedMobileNumber.collectAsState()
    val userProfile by UserProfileManager.userProfile.collectAsState()
    val userId = verifiedMobileNumber.ifEmpty { userProfile.mobileNumber.ifEmpty { "guest_user" } }

    val parcha = remember(parchaId) {
        ParchaManager.getParchaById(parchaId)
    }

    var showFullscreenImage by remember { mutableStateOf(false) }

    LaunchedEffect(parchaId) {
        if (parchaId.isNotEmpty()) {
            ParchaManager.markAsReadByUser(parchaId, userId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.parcha,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.playPremiumHaptic()
                        onBackClick()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark
                )
            )
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        if (parcha == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = strings.unableToLoadParcha,
                    color = TextMuted,
                    fontSize = 14.sp
                )
            }
        } else {
            // Sacred Parcha Content is ALWAYS Hindi
            val title = parcha.title.ifBlank { "दरबार पर्चा" }
            val problem = parcha.problem
            val guidance = parcha.guidance
            val upay = parcha.upay
            val karamSeva = parcha.karamSeva
            val additional = parcha.additionalMessage

            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = AccentGolden.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = AccentGolden,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isHindi) "श्री जामनीवाला दरबार" else "Shree Jamniwala Darbar",
                                        color = AccentGolden,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = formatDisplayDate(formatEpochToDateString(parcha.createdAt)),
                                color = TextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Text(
                            text = title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite,
                            lineHeight = 26.sp
                        )

                        if (!parcha.recipientUserName.isNullOrBlank() || !parcha.recipientPhoneNumber.isNullOrBlank()) {
                            Text(
                                text = "${if (isHindi) "श्रद्धालु" else "Devotee"}: ${parcha.recipientUserName ?: ""} ${parcha.recipientPhoneNumber ?: ""}".trim(),
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                }

                // Clean Image Attachment with Tap-to-Zoom Fullscreen Viewer
                if (!parcha.imageUrl.isNullOrBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clickable {
                                haptic.playPremiumHaptic()
                                showFullscreenImage = true
                            },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.4f))
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = parcha.imageUrl,
                                contentDescription = "Parcha Attachment",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                // 1. Devotee Concern / Problem Section (Hindi content)
                if (!problem.isNullOrBlank()) {
                    ParchaDetailSectionCard(
                        headerTitle = strings.parchaProblem,
                        content = problem,
                        accentColor = AccentGolden
                    )
                }

                // 2. Darbar Guidance Section (Hindi content - Required)
                ParchaDetailSectionCard(
                    headerTitle = strings.parchaGuidance,
                    content = guidance,
                    accentColor = WarmGold,
                    isHighlight = true
                )

                // 3. Upay Section (Hindi content)
                if (!upay.isNullOrBlank()) {
                    ParchaDetailSectionCard(
                        headerTitle = strings.parchaUpay,
                        content = upay,
                        accentColor = SaffronAccent
                    )
                }

                // 4. Karam / Seva Section (Hindi content)
                if (!karamSeva.isNullOrBlank()) {
                    ParchaDetailSectionCard(
                        headerTitle = strings.parchaKaram,
                        content = karamSeva,
                        accentColor = AccentGolden
                    )
                }

                // 5. Additional Guidance (Hindi content)
                if (!additional.isNullOrBlank()) {
                    ParchaDetailSectionCard(
                        headerTitle = strings.parchaAdditional,
                        content = additional,
                        accentColor = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Fullscreen Zoomable Image Viewer Dialog
            if (showFullscreenImage && !parcha.imageUrl.isNullOrBlank()) {
                ParchaFullscreenImageViewer(
                    imageUrl = parcha.imageUrl!!,
                    title = title,
                    isHindi = isHindi,
                    onDismiss = { showFullscreenImage = false }
                )
            }
        }
    }
}

@Composable
private fun ParchaDetailSectionCard(
    headerTitle: String,
    content: String,
    accentColor: Color,
    isHighlight: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlight) SurfaceDark else BackgroundDark
        ),
        border = BorderStroke(
            if (isHighlight) 1.5.dp else 1.dp,
            accentColor.copy(alpha = if (isHighlight) 0.6f else 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = headerTitle.uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor,
                    letterSpacing = 0.5.sp
                )
            }

            Text(
                text = content,
                fontSize = 15.sp,
                fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
                color = TextWhite,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
fun ParchaFullscreenImageViewer(
    imageUrl: String,
    title: String,
    isHindi: Boolean,
    onDismiss: () -> Unit
) {
    val haptic = rememberHapticManager()
    val mediaHandler = rememberSuvicharMediaHandler()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Interactive Pinch-to-Zoom & Pan Image
            ParchaZoomableImage(imageUrl = imageUrl)

            // Top Bar Overlay (Close, Title, Share)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        haptic.playPremiumHaptic()
                        onDismiss()
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Text(
                    text = title,
                    color = AccentGolden,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                )

                IconButton(
                    onClick = {
                        haptic.playPremiumHaptic()
                        mediaHandler.shareCleanImage(imageUrl)
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Image",
                        tint = AccentGolden,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ParchaZoomableImage(imageUrl: String) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val animateScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { tapOffset ->
                        if (scale > 1.2f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 2.5f
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f
                            offset = Offset(
                                (centerX - tapOffset.x) * 1.5f,
                                (centerY - tapOffset.y) * 1.5f
                            )
                        }
                    }
                )
            }
            .then(
                if (scale > 1.05f) {
                    Modifier.pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newScale = (scale * zoom).coerceIn(1f, 4.5f)
                            if (newScale <= 1.05f) {
                                scale = 1f
                                offset = Offset.Zero
                            } else {
                                scale = newScale
                                offset += pan
                            }
                        }
                    }
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Parcha Image Fullscreen",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = animateScale,
                    scaleY = animateScale,
                    translationX = offset.x,
                    translationY = offset.y
                ),
            contentScale = ContentScale.Fit
        )
    }
}
