package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.miyank.AartiTimingItem
import com.example.miyank.AdminActionGuardDialog
import com.example.miyank.AdminSaveButton
import com.example.miyank.GuardActionContext
import com.example.miyank.GuardDialogState
import com.example.miyank.LanguageManager
import com.example.miyank.LiveDarshanManager
import com.example.miyank.LiveDarshanSettings
import com.example.miyank.LivePlatform
import com.example.miyank.LivePlatformType
import com.example.miyank.formatDisplayDate
import com.example.miyank.getDefaultLivePlatforms
import com.example.miyank.getEpochMillis
import com.example.miyank.openExternalUrl
import com.example.miyank.rememberFilePickerLauncher
import com.example.miyank.rememberHapticManager
import com.example.miyank.rememberImageOptimizer
import com.example.miyank.ui.components.OfficialYouTubeLogo
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLiveStreamScreen(
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val scrollState = rememberScrollState()
    val isHindi = LanguageManager.currentLanguage == com.example.miyank.Language.HINDI
    val imageOptimizer = rememberImageOptimizer()

    val currentSettings by LiveDarshanManager.settings.collectAsState()

    var streamUrlInput by remember { mutableStateOf(currentSettings.streamUrl) }
    var isLiveInput by remember { mutableStateOf(currentSettings.isLiveNow) }
    var titleInput by remember { mutableStateOf(currentSettings.title) }
    var titleEngInput by remember { mutableStateOf(currentSettings.titleEnglish) }
    var descInput by remember { mutableStateOf(currentSettings.description) }

    // Multi-Platform List Input
    var platformsInput by remember(currentSettings.platforms) {
        mutableStateOf(
            if (currentSettings.platforms.isNotEmpty()) currentSettings.platforms
            else getDefaultLivePlatforms(currentSettings.streamUrl, currentSettings.isLiveNow)
        )
    }

    // Share Card inputs
    var shareCardImageInput by remember { mutableStateOf(currentSettings.shareCardImage) }
    var shareTitleInput by remember { mutableStateOf(currentSettings.shareTitle) }
    var shareDescInput by remember { mutableStateOf(currentSettings.shareDescription) }
    var shareMsgHindiInput by remember { mutableStateOf(currentSettings.shareMessageHindi) }
    var shareMsgEngInput by remember { mutableStateOf(currentSettings.shareMessageEnglish) }

    // Darshan Windows inputs
    var morningOpeningInput by remember { mutableStateOf(currentSettings.morningDarshanOpening) }
    var morningClosingInput by remember { mutableStateOf(currentSettings.morningDarshanClosing) }
    var eveningOpeningInput by remember { mutableStateOf(currentSettings.eveningDarshanOpening) }
    var eveningClosingInput by remember { mutableStateOf(currentSettings.eveningDarshanClosing) }

    // Aarti Timings list
    var aartiTimingsInput by remember { mutableStateOf(currentSettings.aartiTimings) }

    // Aarti Dialog State
    var showAartiDialog by remember { mutableStateOf(false) }
    var editingAartiItem by remember { mutableStateOf<AartiTimingItem?>(null) }
    var aartiNameHindiInput by remember { mutableStateOf("") }
    var aartiNameEngInput by remember { mutableStateOf("") }
    var aartiTimeInput by remember { mutableStateOf("") }
    var aartiDescInput by remember { mutableStateOf("") }
    var aartiIsActiveInput by remember { mutableStateOf(true) }

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var showSaveConfirmDialog by remember { mutableStateOf(false) }
    var showEndLiveConfirmDialog by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }

    val hasChanges = streamUrlInput != currentSettings.streamUrl ||
            isLiveInput != currentSettings.isLiveNow ||
            titleInput != currentSettings.title ||
            titleEngInput != currentSettings.titleEnglish ||
            descInput != currentSettings.description ||
            platformsInput != currentSettings.platforms ||
            shareCardImageInput != currentSettings.shareCardImage ||
            shareTitleInput != currentSettings.shareTitle ||
            shareDescInput != currentSettings.shareDescription ||
            shareMsgHindiInput != currentSettings.shareMessageHindi ||
            shareMsgEngInput != currentSettings.shareMessageEnglish ||
            morningOpeningInput != currentSettings.morningDarshanOpening ||
            morningClosingInput != currentSettings.morningDarshanClosing ||
            eveningOpeningInput != currentSettings.eveningDarshanOpening ||
            eveningClosingInput != currentSettings.eveningDarshanClosing ||
            aartiTimingsInput != currentSettings.aartiTimings

    // Image Picker Launcher for Share Card
    val photoPickerLauncher = rememberFilePickerLauncher("image/*") { path, _, size ->
        if (path != null) {
            val sizeMb = size / (1024f * 1024f)
            if (sizeMb > 10f) {
                errorText = if (isHindi) "❌ फोटो साइज़ 10MB से कम होना चाहिए!" else "❌ Image size must be under 10MB!"
            } else {
                val optResult = imageOptimizer.processAndOptimizeImage(path)
                val fullPath = optResult?.optimizedFullImagePath ?: path
                shareCardImageInput = fullPath
                errorText = null
                statusMessage = if (isHindi) "📷 शेयर कार्ड फोटो चुनी गई! ✓" else "📷 Share Card photo selected! ✓"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isHindi) "लाइव स्ट्रीम एवं प्लेटफॉर्म प्रबंधन" else "Manage Live Streams", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasChanges) {
                            showUnsavedDialog = true
                        } else {
                            onBackClick()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark,
                    titleContentColor = TextWhite,
                    navigationIconContentColor = TextWhite
                )
            )
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Dashboard Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isHindi) "वर्तमान लाइव स्थिति:" else "Current Stream Status:",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val isAnyActiveLive = platformsInput.any { it.isEnabled && it.isLive && it.url.isNotBlank() } || isLiveInput
                            if (isAnyActiveLive) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Red))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("🔴 LIVE NOW", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                }
                            } else {
                                Text("⏸ OFFLINE", color = AccentGolden, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                            }
                        }

                        if (currentSettings.updatedAt > 0L) {
                            Text(
                                text = (if (isHindi) "अंतिम अपडेट: " else "Updated: ") + formatDisplayDate(currentSettings.updatedAt.toString()),
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    // QUICK ACTION BUTTONS: GO LIVE / END LIVE
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                isLiveInput = true
                                val updatedList = platformsInput.map {
                                    if (it.platformType == LivePlatformType.YOUTUBE) it.copy(isEnabled = true, isLive = true) else it
                                }
                                platformsInput = updatedList
                                errorText = null
                                showSaveConfirmDialog = true
                            },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("GO LIVE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                showEndLiveConfirmDialog = true
                            },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.StopCircle, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("END LIVE", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            if (statusMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = AccentGolden.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AccentGolden.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = statusMessage!!,
                        color = TextWhite,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. MULTI-PLATFORM CONFIGURATION SECTION
            Text(if (isHindi) "1. 📡 लाइव प्लेटफॉर्म प्रबंधन (YouTube, Instagram, Facebook)" else "1. Live Stream Platforms", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = AccentGolden)
            Spacer(modifier = Modifier.height(10.dp))

            platformsInput.forEachIndexed { index, platform ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                when (platform.platformType) {
                                    LivePlatformType.YOUTUBE -> OfficialYouTubeLogo(22.dp)
                                    LivePlatformType.INSTAGRAM -> Text("◎", color = Color(0xFFE1306C), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                                    LivePlatformType.FACEBOOK -> Text("f", color = Color(0xFF1877F2), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                                    else -> Icon(Icons.Default.Link, contentDescription = null, tint = AccentGolden, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(platform.name, fontWeight = FontWeight.ExtraBold, color = TextWhite, fontSize = 14.sp)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (platform.isEnabled) "सक्रिय" else "निष्क्रिय", fontSize = 11.sp, color = if (platform.isEnabled) AccentGolden else TextMuted)
                                Spacer(modifier = Modifier.width(4.dp))
                                Switch(
                                    checked = platform.isEnabled,
                                    onCheckedChange = { enabled ->
                                        val updated = platformsInput.toMutableList()
                                        updated[index] = platform.copy(isEnabled = enabled)
                                        platformsInput = updated
                                    },
                                    modifier = Modifier.scale(0.8f)
                                )
                            }
                        }

                        if (platform.isEnabled) {
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(if (isHindi) "लाइव स्टेटस (Live Now):" else "Live Status:", fontSize = 12.sp, color = TextMuted)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (platform.isLive) {
                                        Text("🔴 LIVE", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    } else {
                                        Text("OFFLINE", color = TextMuted, fontSize = 11.sp)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Switch(
                                        checked = platform.isLive,
                                        onCheckedChange = { isLive ->
                                            val updated = platformsInput.toMutableList()
                                            updated[index] = platform.copy(isLive = isLive)
                                            platformsInput = updated
                                            if (platform.platformType == LivePlatformType.YOUTUBE) {
                                                isLiveInput = isLive
                                            }
                                        },
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color.Red),
                                        modifier = Modifier.scale(0.8f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = platform.url,
                                onValueChange = { newUrl ->
                                    val updated = platformsInput.toMutableList()
                                    updated[index] = platform.copy(url = newUrl)
                                    platformsInput = updated
                                    if (platform.platformType == LivePlatformType.YOUTUBE) {
                                        streamUrlInput = newUrl
                                    }
                                },
                                label = { Text("${platform.name} URL") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = AccentGolden) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            if (platform.url.isNotBlank() && !LiveDarshanManager.validatePlatformUrl(platform.platformType, platform.url)) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("⚠️ कृपया सही ${platform.name} URL दर्ज करें", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            if (platform.url.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(
                                    onClick = {
                                        val launchUrl = LiveDarshanManager.getValidLaunchUrl(platform.url)
                                        openExternalUrl(launchUrl)
                                    },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(if (isHindi) "टेस्ट खोलें" else "Test Link", color = AccentGolden, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = AccentGolden, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // STREAM GENERAL DETAILS
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(if (isHindi) "मुख्य लाइव विवरण (General Stream Title & Description)" else "Stream Details", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text(if (isHindi) "स्ट्रीम शीर्षक (हिन्दी)" else "Stream Title (Hindi)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        label = { Text(if (isHindi) "विवरण (Description)" else "Description") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. LIVE DARSHAN SHARE CARD SECTION
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(if (isHindi) "2. 🖼️ शेयर कार्ड प्रबंधन (Live Darshan Share Card)" else "2. Share Card Management", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = AccentGolden)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(if (isHindi) "शेयर फोटो/पोस्टर:" else "Share Card Image:", fontSize = 13.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    val activePreviewImg = if (shareCardImageInput.isNotEmpty()) shareCardImageInput else "file:///android_asset/temple_pooja.jpg"

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.91f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BackgroundDark)
                            .border(1.dp, WarmGold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = activePreviewImg,
                            contentDescription = "Share Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { photoPickerLauncher() },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isHindi) "नई फोटो चुनें" else "Change Photo", color = BackgroundDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        if (shareCardImageInput.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { shareCardImageInput = "" },
                                modifier = Modifier.height(40.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.6f))
                            ) {
                                Text(if (isHindi) "हटाएं" else "Remove", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = shareTitleInput,
                        onValueChange = { shareTitleInput = it },
                        label = { Text(if (isHindi) "शेयर शीर्षक (Share Title)" else "Share Title") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = shareMsgHindiInput,
                        onValueChange = { shareMsgHindiInput = it },
                        label = { Text(if (isHindi) "शेयर मैसेज (हिन्दी)" else "Share Message (Hindi)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 3
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. DARSHAN OPENING/CLOSING WINDOWS SECTION
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(if (isHindi) "3. 🏛️ दरबार द्वार खुलने व बंद होने का समय" else "3. Temple Opening & Closing Hours", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = AccentGolden)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = morningOpeningInput,
                            onValueChange = { morningOpeningInput = it },
                            label = { Text(if (isHindi) "प्रातः खुलना" else "Morning Open") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )
                        OutlinedTextField(
                            value = morningClosingInput,
                            onValueChange = { morningClosingInput = it },
                            label = { Text(if (isHindi) "प्रातः बंद" else "Morning Close") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = eveningOpeningInput,
                            onValueChange = { eveningOpeningInput = it },
                            label = { Text(if (isHindi) "सायं खुलना" else "Evening Open") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )
                        OutlinedTextField(
                            value = eveningClosingInput,
                            onValueChange = { eveningClosingInput = it },
                            label = { Text(if (isHindi) "सायं बंद" else "Evening Close") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. DAILY AARTI TIMINGS MANAGEMENT SECTION
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isHindi) "4. 🕉️ दैनिक आरती समय प्रबंधन" else "4. Daily Aarti Management", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = AccentGolden)
                        Button(
                            onClick = {
                                editingAartiItem = null
                                aartiNameHindiInput = ""
                                aartiNameEngInput = ""
                                aartiTimeInput = "06:00 AM"
                                aartiDescInput = ""
                                aartiIsActiveInput = true
                                showAartiDialog = true
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGolden),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isHindi) "+ नई आरती" else "+ Add Aarti", color = BackgroundDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (aartiTimingsInput.isEmpty()) {
                        Text(
                            text = if (isHindi) "कोई आरती समय नहीं जोड़ा गया है।" else "No Aarti timings added.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    } else {
                        aartiTimingsInput.sortedBy { it.sortOrder }.forEach { aarti ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = BackgroundDark),
                                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = aarti.nameHindi.ifEmpty { aarti.nameEnglish },
                                            color = TextWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "समय: ${aarti.time}",
                                            color = AccentGolden,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Switch(
                                            checked = aarti.isActive,
                                            onCheckedChange = { active ->
                                                val updatedList = aartiTimingsInput.map {
                                                    if (it.id == aarti.id) it.copy(isActive = active) else it
                                                }
                                                aartiTimingsInput = updatedList
                                            },
                                            modifier = Modifier.scale(0.8f)
                                        )

                                        IconButton(onClick = {
                                            editingAartiItem = aarti
                                            aartiNameHindiInput = aarti.nameHindi
                                            aartiNameEngInput = aarti.nameEnglish
                                            aartiTimeInput = aarti.time
                                            aartiDescInput = aarti.description
                                            aartiIsActiveInput = aarti.isActive
                                            showAartiDialog = true
                                        }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AccentGolden, modifier = Modifier.size(16.dp))
                                        }

                                        IconButton(onClick = {
                                            val updatedList = aartiTimingsInput.filter { it.id != aarti.id }
                                            aartiTimingsInput = updatedList
                                        }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (errorText != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(errorText!!, color = Color.Red, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }

            Spacer(modifier = Modifier.height(28.dp))

            AdminSaveButton(
                text = if (isLiveInput || platformsInput.any { it.isEnabled && it.isLive }) "GO LIVE (लाइव करें)" else "सेव सेटिंग्स",
                textEnglish = if (isLiveInput || platformsInput.any { it.isEnabled && it.isLive }) "GO LIVE" else "Save Settings",
                isEnabled = hasChanges,
                isHindi = isHindi,
                onSaveClick = {
                    haptic.playPremiumHaptic()
                    showSaveConfirmDialog = true
                }
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // ADD / EDIT AARTI DIALOG
    if (showAartiDialog) {
        AlertDialog(
            onDismissRequest = { showAartiDialog = false },
            title = {
                Text(
                    text = if (editingAartiItem != null) (if (isHindi) "आरती एडिट करें" else "Edit Aarti") else (if (isHindi) "नई आरती जोड़ें" else "Add New Aarti"),
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentGolden
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = aartiNameHindiInput,
                        onValueChange = { aartiNameHindiInput = it },
                        label = { Text(if (isHindi) "आरती नाम (हिन्दी)" else "Aarti Name (Hindi)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = aartiNameEngInput,
                        onValueChange = { aartiNameEngInput = it },
                        label = { Text(if (isHindi) "आरती नाम (अंग्रेज़ी)" else "Aarti Name (English)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = aartiTimeInput,
                        onValueChange = { aartiTimeInput = it },
                        label = { Text(if (isHindi) "समय (Time, e.g. 06:00 AM)" else "Time") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = aartiDescInput,
                        onValueChange = { aartiDescInput = it },
                        label = { Text(if (isHindi) "विवरण (Optional)" else "Description") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isHindi) "सक्रिय रखें (Active)" else "Active")
                        Switch(
                            checked = aartiIsActiveInput,
                            onCheckedChange = { aartiIsActiveInput = it }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (aartiNameHindiInput.isBlank() || aartiTimeInput.isBlank()) return@Button
                        val now = getEpochMillis()
                        val currentList = aartiTimingsInput.toMutableList()

                        if (editingAartiItem != null) {
                            val idx = currentList.indexOfFirst { it.id == editingAartiItem!!.id }
                            if (idx >= 0) {
                                currentList[idx] = editingAartiItem!!.copy(
                                    nameHindi = aartiNameHindiInput.trim(),
                                    nameEnglish = aartiNameEngInput.trim(),
                                    time = aartiTimeInput.trim(),
                                    description = aartiDescInput.trim(),
                                    isActive = aartiIsActiveInput,
                                    updatedAt = now
                                )
                            }
                        } else {
                            val newItem = AartiTimingItem(
                                id = "aarti_${now}",
                                nameHindi = aartiNameHindiInput.trim(),
                                nameEnglish = aartiNameEngInput.trim(),
                                time = aartiTimeInput.trim(),
                                description = aartiDescInput.trim(),
                                isActive = aartiIsActiveInput,
                                sortOrder = currentList.size + 1,
                                createdAt = now,
                                updatedAt = now
                            )
                            currentList.add(newItem)
                        }

                        aartiTimingsInput = currentList
                        showAartiDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                ) {
                    Text(if (isHindi) "सेव करें" else "Save", color = BackgroundDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAartiDialog = false }) {
                    Text(if (isHindi) "रद्द करें" else "Cancel")
                }
            },
            containerColor = SurfaceDark
        )
    }

    // SAVE CONFIRM DIALOG
    AdminActionGuardDialog(
        state = GuardDialogState(
            isVisible = showSaveConfirmDialog,
            context = GuardActionContext.PUBLISH_MAJOR_CHANGE,
            title = if (isLiveInput || platformsInput.any { it.isEnabled && it.isLive }) (if (isHindi) "🔴 लाइव दर्शन प्रारंभ करें" else "Start Live Stream") else (if (isHindi) "💾 सेव सेटिंग्स" else "Save Settings"),
            message = if (isLiveInput || platformsInput.any { it.isEnabled && it.isLive }) (if (isHindi) "क्या आप वाकई में लाइव दर्शन प्रारंभ करना चाहते हैं? सभी श्रद्धालुओं को नोटिफिकेशन भेजा जाएगा।" else "Are you sure you want to go live? Notifications will be sent to all users.")
                      else (if (isHindi) "क्या आप इन बदलावों को सेव करना चाहते हैं?" else "Are you sure you want to save these changes?"),
            onConfirm = {
                showSaveConfirmDialog = false
                val ytPlatform = platformsInput.find { it.platformType == LivePlatformType.YOUTUBE }
                val activeYtUrl = ytPlatform?.url ?: streamUrlInput
                val normalizedUrl = LiveDarshanManager.normalizeYouTubeUrl(activeYtUrl)

                val newSettings = LiveDarshanSettings(
                    streamUrl = normalizedUrl,
                    isLiveNow = isLiveInput || (ytPlatform?.isLive == true && ytPlatform.isEnabled),
                    title = titleInput.trim(),
                    titleEnglish = titleEngInput.trim(),
                    description = descInput.trim(),
                    shareCardImage = shareCardImageInput.trim(),
                    shareTitle = shareTitleInput.trim(),
                    shareDescription = shareDescInput.trim(),
                    shareMessageHindi = shareMsgHindiInput.trim(),
                    shareMessageEnglish = shareMsgEngInput.trim(),
                    morningDarshanOpening = morningOpeningInput.trim(),
                    morningDarshanClosing = morningClosingInput.trim(),
                    eveningDarshanOpening = eveningOpeningInput.trim(),
                    eveningDarshanClosing = eveningClosingInput.trim(),
                    aartiTimings = aartiTimingsInput,
                    platforms = platformsInput,
                    updatedAt = getEpochMillis()
                )
                LiveDarshanManager.updateSettings(newSettings)
                statusMessage = if (isLiveInput || platformsInput.any { it.isEnabled && it.isLive }) (if (isHindi) "🚀 लाइव प्लेटफॉर्म सेटिंग्स सफलतापूर्वक सहेजी गईं! ✓" else "🚀 Live Platforms Settings Saved Successfully! ✓")
                                else (if (isHindi) "💾 लाइव दर्शन व आरती सेटिंग्स सेव हो गईं! ✓" else "💾 Live Darshan Settings Saved! ✓")
            },
            onDismiss = { showSaveConfirmDialog = false }
        ),
        isHindi = isHindi
    )

    // END LIVE CONFIRMATION DIALOG
    AdminActionGuardDialog(
        state = GuardDialogState(
            isVisible = showEndLiveConfirmDialog,
            context = GuardActionContext.PUBLISH_MAJOR_CHANGE,
            title = if (isHindi) "⏹ लाइव दर्शन समाप्त करें" else "End Live Stream",
            message = if (isHindi) "क्या आप वाकई में सभी प्लेटफॉर्म्स के लाइव दर्शन समाप्त (OFFLINE) करना चाहते हैं?" else "Are you sure you want to end all live streams?",
            onConfirm = {
                showEndLiveConfirmDialog = false
                isLiveInput = false
                val updatedPlatforms = platformsInput.map { it.copy(isLive = false) }
                platformsInput = updatedPlatforms

                val newSettings = currentSettings.copy(
                    isLiveNow = false,
                    platforms = updatedPlatforms,
                    updatedAt = getEpochMillis()
                )
                LiveDarshanManager.updateSettings(newSettings)
                statusMessage = if (isHindi) "⏸ लाइव दर्शन समाप्त कर दिया गया है। ✓" else "⏸ Live stream ended successfully. ✓"
            },
            onDismiss = { showEndLiveConfirmDialog = false }
        ),
        isHindi = isHindi
    )

    // UNSAVED CHANGES GUARD DIALOG
    AdminActionGuardDialog(
        state = GuardDialogState(
            isVisible = showUnsavedDialog,
            context = GuardActionContext.UNSAVED_CHANGES,
            onConfirm = {
                showUnsavedDialog = false
                val ytPlatform = platformsInput.find { it.platformType == LivePlatformType.YOUTUBE }
                val activeYtUrl = ytPlatform?.url ?: streamUrlInput
                val normalizedUrl = LiveDarshanManager.normalizeYouTubeUrl(activeYtUrl)

                val newSettings = LiveDarshanSettings(
                    streamUrl = normalizedUrl,
                    isLiveNow = isLiveInput || (ytPlatform?.isLive == true && ytPlatform.isEnabled),
                    title = titleInput.trim(),
                    titleEnglish = titleEngInput.trim(),
                    description = descInput.trim(),
                    shareCardImage = shareCardImageInput.trim(),
                    shareTitle = shareTitleInput.trim(),
                    shareDescription = shareDescInput.trim(),
                    shareMessageHindi = shareMsgHindiInput.trim(),
                    shareMessageEnglish = shareMsgEngInput.trim(),
                    morningDarshanOpening = morningOpeningInput.trim(),
                    morningDarshanClosing = morningClosingInput.trim(),
                    eveningDarshanOpening = eveningOpeningInput.trim(),
                    eveningDarshanClosing = eveningClosingInput.trim(),
                    aartiTimings = aartiTimingsInput,
                    platforms = platformsInput,
                    updatedAt = getEpochMillis()
                )
                LiveDarshanManager.updateSettings(newSettings)
                onBackClick()
            },
            onDiscard = {
                showUnsavedDialog = false
                onBackClick()
            },
            onDismiss = { showUnsavedDialog = false }
        ),
        isHindi = isHindi
    )
}
