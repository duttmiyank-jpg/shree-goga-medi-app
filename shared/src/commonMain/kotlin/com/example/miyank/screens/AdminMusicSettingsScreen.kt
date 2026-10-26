package com.example.miyank.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyank.AudioTrimmer
import com.example.miyank.MusicManager
import com.example.miyank.rememberAudioPlayer
import com.example.miyank.rememberAudioTrimmer
import com.example.miyank.rememberFilePickerLauncher
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*

data class PendingAudioCrop(
    val isAarti: Boolean,
    val inputPath: String,
    val originalName: String,
    val totalDurationMs: Long,
    val originalSizeBytes: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMusicSettingsScreen(
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val audioTrimmer = rememberAudioTrimmer()
    val currentBgSettings by MusicManager.settings.collectAsState()
    val currentAartiSettings by MusicManager.aartiSettings.collectAsState()

    var bgMusicName by remember { mutableStateOf(currentBgSettings.musicName) }
    var aartiName by remember { mutableStateOf(currentAartiSettings.aartiName) }
    var isAutoPlay by remember { mutableStateOf(currentBgSettings.isAutoPlay) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var pendingCrop by remember { mutableStateOf<PendingAudioCrop?>(null) }

    var showDeleteBgConfirm by remember { mutableStateOf(false) }
    var showDeleteAartiConfirm by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var showSaveConfirmDialog by remember { mutableStateOf(false) }

    val hasChanges = bgMusicName != currentBgSettings.musicName ||
            aartiName != currentAartiSettings.aartiName ||
            isAutoPlay != currentBgSettings.isAutoPlay

    // Pause app background music immediately when entering Admin Music Setup, and resume when leaving
    DisposableEffect(Unit) {
        MusicManager.setOtherMediaPlaying(true)
        onDispose {
            MusicManager.setOtherMediaPlaying(false)
        }
    }

    // File picker launcher for Background Music
    val bgFilePicker = rememberFilePickerLauncher("audio/*") { filePath, fileName, fileSize ->
        if (filePath != null) {
            val meta = audioTrimmer.getAudioMetadata(filePath)
            val durationMs = meta?.durationMs ?: 60000L
            val sizeBytes = if ((meta?.sizeBytes ?: 0L) > 0L) meta!!.sizeBytes else fileSize

            pendingCrop = PendingAudioCrop(
                isAarti = false,
                inputPath = filePath,
                originalName = fileName ?: bgMusicName,
                totalDurationMs = durationMs,
                originalSizeBytes = sizeBytes
            )
        }
    }

    // File picker launcher for Aarti Song
    val aartiFilePicker = rememberFilePickerLauncher("audio/*") { filePath, fileName, fileSize ->
        if (filePath != null) {
            val meta = audioTrimmer.getAudioMetadata(filePath)
            val durationMs = meta?.durationMs ?: 60000L
            val sizeBytes = if ((meta?.sizeBytes ?: 0L) > 0L) meta!!.sizeBytes else fileSize

            pendingCrop = PendingAudioCrop(
                isAarti = true,
                inputPath = filePath,
                originalName = fileName ?: aartiName,
                totalDurationMs = durationMs,
                originalSizeBytes = sizeBytes
            )
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Music & Aarti Setup", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasChanges) {
                            showUnsavedDialog = true
                        } else {
                            onBackClick()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark,
                    titleContentColor = TextWhite,
                    navigationIconContentColor = TextWhite
                )
            )
        },
        containerColor = BackgroundDark,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    haptic.playPremiumHaptic()
                    showSaveConfirmDialog = true
                },
                containerColor = AccentGolden,
                contentColor = BackgroundDark,
                icon = { Icon(Icons.Default.Save, contentDescription = null) },
                text = { Text("Save Settings") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            // BLOCK 1: BACKGROUND BHAJAN
            Text(
                text = "1. Background Bhajan Setup (Max 1 Min / 1.5MB)",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AccentGolden
            )
            Text(
                text = "Plays continuously across all app screens. Auto-cropped to 1 minute (Max 1.5 MB).",
                fontSize = 13.sp,
                color = TextMuted,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = bgMusicName,
                onValueChange = { bgMusicName = it },
                label = { Text("Background Bhajan Title") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentGolden,
                    unfocusedBorderColor = SurfaceDark,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    haptic.playPremiumHaptic()
                    bgFilePicker()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, tint = BackgroundDark)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Select & Crop Background Audio", fontWeight = FontWeight.Bold, color = BackgroundDark, fontSize = 13.sp)
                        Text("Max Duration: 1 Min • Max File Size: 1.5 MB", fontSize = 11.sp, color = BackgroundDark.copy(alpha = 0.85f))
                    }
                }
            }

            if (currentBgSettings.hasCustomFile) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        haptic.playPremiumHaptic()
                        showDeleteBgConfirm = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Background MP3 (Reset to Default)")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(color = SurfaceDark, thickness = 2.dp)
            Spacer(modifier = Modifier.height(24.dp))

            // BLOCK 2: VIRTUAL AARTI SONG SETUP
            Text(
                text = "2. Virtual Aarti Song Setup (Max 1 Min / 1.5MB)",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = WarmGold
            )
            Text(
                text = "Plays when the user taps 'Start Aarti' in Virtual Pooja. Auto-cropped to 1 minute (Max 1.5 MB).",
                fontSize = 13.sp,
                color = TextMuted,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = aartiName,
                onValueChange = { aartiName = it },
                label = { Text("Aarti Bhajan Title") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WarmGold,
                    unfocusedBorderColor = SurfaceDark,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    haptic.playPremiumHaptic()
                    aartiFilePicker()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WarmGold)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = BackgroundDark)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Select & Crop Aarti Audio", fontWeight = FontWeight.Bold, color = BackgroundDark, fontSize = 13.sp)
                        Text("Max Duration: 1 Min • Max File Size: 1.5 MB", fontSize = 11.sp, color = BackgroundDark.copy(alpha = 0.85f))
                    }
                }
            }

            if (currentAartiSettings.hasCustomFile) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        haptic.playPremiumHaptic()
                        showDeleteAartiConfirm = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Custom Aarti MP3")
                }
            }

            if (statusMessage != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Surface(
                    color = AccentGolden.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = statusMessage!!,
                        color = AccentGolden,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Auto Play Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Auto Play Background Music", fontWeight = FontWeight.Bold, color = TextWhite)
                    Text("Start background bhajan automatically when app opens", fontSize = 12.sp, color = TextMuted)
                }
                Switch(
                    checked = isAutoPlay,
                    onCheckedChange = { isAutoPlay = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AccentGolden,
                        checkedTrackColor = AccentGolden.copy(alpha = 0.5f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        // Crop Dialog
        pendingCrop?.let { cropInfo ->
            AudioCropDialog(
                cropInfo = cropInfo,
                audioTrimmer = audioTrimmer,
                onDismiss = { pendingCrop = null },
                onCroppedAndSaved = { trimmedPath, trimmedSizeStr ->
                    if (cropInfo.isAarti) {
                        MusicManager.updateAartiSettings(
                            currentAartiSettings.copy(
                                aartiName = cropInfo.originalName,
                                localFilePath = trimmedPath,
                                fileSizeFormatted = trimmedSizeStr,
                                hasCustomFile = true
                            )
                        )
                        statusMessage = "Aarti Audio cropped ($trimmedSizeStr) & applied!"
                    } else {
                        MusicManager.updateSettings(
                            currentBgSettings.copy(
                                musicName = cropInfo.originalName,
                                localFilePath = trimmedPath,
                                fileSizeFormatted = trimmedSizeStr,
                                hasCustomFile = true
                            )
                        )
                        statusMessage = "Background Bhajan cropped ($trimmedSizeStr) & applied!"
                    }
                    pendingCrop = null
                }
            )
        }

        // DELETE CONFIRMATION DIALOG BACKGROUND MUSIC
        if (showDeleteBgConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteBgConfirm = false },
                containerColor = SurfaceDark,
                titleContentColor = Color.Red,
                textContentColor = TextWhite,
                title = {
                    Text("Confirm Deletion", fontWeight = FontWeight.ExtraBold, color = Color.Red, fontSize = 18.sp)
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "⚠️ WARNING: Are you really sure you want to permanently delete the custom background MP3? This will reset background audio to Default Bhajan.",
                            color = Color.Red,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("This action cannot be undone.", color = TextMuted, fontSize = 12.sp)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteBgConfirm = false
                            MusicManager.deleteCustomMusic()
                            statusMessage = "Custom Background MP3 deleted. Default Bhajan active!"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Yes, Delete", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteBgConfirm = false }) {
                        Text("Cancel", color = TextMuted)
                    }
                }
            )
        }

        // DELETE CONFIRMATION DIALOG AARTI MUSIC
        if (showDeleteAartiConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteAartiConfirm = false },
                containerColor = SurfaceDark,
                titleContentColor = Color.Red,
                textContentColor = TextWhite,
                title = {
                    Text("Confirm Deletion", fontWeight = FontWeight.ExtraBold, color = Color.Red, fontSize = 18.sp)
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "⚠️ WARNING: Are you really sure you want to permanently delete the custom Aarti MP3?",
                            color = Color.Red,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("This action cannot be undone.", color = TextMuted, fontSize = 12.sp)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteAartiConfirm = false
                            MusicManager.deleteCustomAarti()
                            statusMessage = "Custom Aarti MP3 deleted. Default Aarti active!"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Yes, Delete", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteAartiConfirm = false }) {
                        Text("Cancel", color = TextMuted)
                    }
                }
            )
        }

        // UNSAVED CHANGES GUARD DIALOG
        if (showUnsavedDialog) {
            AlertDialog(
                onDismissRequest = { showUnsavedDialog = false },
                containerColor = SurfaceDark,
                titleContentColor = TextWhite,
                textContentColor = TextWhite,
                title = {
                    Text("Unsaved Changes", fontWeight = FontWeight.ExtraBold, color = AccentGolden, fontSize = 17.sp)
                },
                text = {
                    Text(
                        text = "⚠️ You have unsaved music settings! Do you really want to discard your changes and leave?",
                        color = TextWhite,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showUnsavedDialog = false
                            onBackClick()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Discard & Leave", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showUnsavedDialog = false }) {
                        Text("Stay & Save", color = AccentGolden, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // CONFIRM SAVE & APPLY SETTINGS DIALOG
        if (showSaveConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showSaveConfirmDialog = false },
                containerColor = SurfaceDark,
                titleContentColor = TextWhite,
                textContentColor = TextWhite,
                title = {
                    Text("Confirm Save Settings", fontWeight = FontWeight.ExtraBold, color = AccentGolden, fontSize = 17.sp)
                },
                text = {
                    Text(
                        text = "Are you really sure you want to save and apply all music & audio settings to the app?",
                        color = TextWhite,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showSaveConfirmDialog = false
                            MusicManager.updateSettings(
                                currentBgSettings.copy(
                                    musicName = bgMusicName,
                                    isAutoPlay = isAutoPlay
                                )
                            )
                            MusicManager.updateAartiSettings(
                                currentAartiSettings.copy(
                                    aartiName = aartiName
                                )
                            )
                            statusMessage = "All Audio Settings Saved!"
                            onBackClick()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                    ) {
                        Text("Yes, Save & Apply", color = BackgroundDark, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSaveConfirmDialog = false }) {
                        Text("Cancel", color = TextMuted)
                    }
                }
            )
        }
    }
}

@Composable
fun AudioCropDialog(
    cropInfo: PendingAudioCrop,
    audioTrimmer: AudioTrimmer,
    onDismiss: () -> Unit,
    onCroppedAndSaved: (trimmedPath: String, trimmedSizeStr: String) -> Unit
) {
    val totalMs = cropInfo.totalDurationMs.toFloat()
    val initialMaxMs = minOf(totalMs, 60000f)

    var sliderRange by remember { mutableStateOf(0f..initialMaxMs) }
    var isZoomedIn by remember { mutableStateOf(false) }
    var isPreviewPlaying by remember { mutableStateOf(false) }
    var cropError by remember { mutableStateOf<String?>(null) }
    var isTrimming by remember { mutableStateOf(false) }

    val previewPlayer = rememberAudioPlayer()

    val startPosMs = sliderRange.start
    val endPosMs = sliderRange.endInclusive
    val selectedDurationSec = ((endPosMs - startPosMs) / 1000f).toInt()
    val isDurationValid = (endPosMs - startPosMs) <= 60050f

    // Determine Slider View Bounds (1x Overview vs 5x Deep Zoom Focus)
    val sliderMin: Float
    val sliderMax: Float

    if (isZoomedIn) {
        val windowWidth = maxOf(120000f, (endPosMs - startPosMs) + 60000f) // 2-min focused window
        val center = (startPosMs + endPosMs) / 2f
        sliderMin = maxOf(0f, center - windowWidth / 2f)
        sliderMax = minOf(totalMs, center + windowWidth / 2f)
    } else {
        sliderMin = 0f
        sliderMax = totalMs
    }

    DisposableEffect(Unit) {
        onDispose {
            previewPlayer.stop()
        }
    }

    val formatTime = { ms: Long ->
        val totalSec = ms / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        "${min.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}"
    }

    AlertDialog(
        onDismissRequest = {
            previewPlayer.stop()
            onDismiss()
        },
        containerColor = SurfaceDark,
        titleContentColor = TextWhite,
        textContentColor = TextWhite,
        title = {
            Text(
                text = if (cropInfo.isAarti) "Crop Aarti Music (Max 1 Min)" else "Crop Background Music (Max 1 Min)",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = AccentGolden
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎵 ${cropInfo.originalName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextWhite
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Duration: ${formatTime(cropInfo.totalDurationMs)}",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Text(
                        text = "Size: ${(cropInfo.originalSizeBytes / (1024f * 1024f)).toString().take(3)} MB",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Deep Zoom Mode Toggle Button
                OutlinedButton(
                    onClick = {
                        isZoomedIn = !isZoomedIn
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isZoomedIn) AccentGolden.copy(alpha = 0.2f) else Color.Transparent,
                        contentColor = AccentGolden
                    )
                ) {
                    Icon(
                        imageVector = if (isZoomedIn) Icons.Default.ZoomOut else Icons.Default.ZoomIn,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isZoomedIn) "🔍 Zoom Out (Full Track View)" else "🔎 Deep Zoom Mode (5x Precision)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isZoomedIn) "🔍 Deep Zoom Focus Range Slider:" else "Adjust Dual Sliders (Start & End Position):",
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = AccentGolden
                )

                Spacer(modifier = Modifier.height(4.dp))

                RangeSlider(
                    value = sliderRange,
                    onValueChange = { range ->
                        val duration = range.endInclusive - range.start
                        if (duration <= 60000f) {
                            sliderRange = range
                            cropError = null
                        } else {
                            if (range.start != sliderRange.start) {
                                sliderRange = range.start..(range.start + 60000f)
                            } else {
                                sliderRange = (range.endInclusive - 60000f)..range.endInclusive
                            }
                            cropError = "⚠️ Maximum duration allowed is 60 seconds (1 Min)!"
                        }
                        if (isPreviewPlaying) {
                            previewPlayer.stop()
                            isPreviewPlaying = false
                        }
                    },
                    valueRange = sliderMin..sliderMax,
                    colors = SliderDefaults.colors(
                        thumbColor = AccentGolden,
                        activeTrackColor = AccentGolden,
                        inactiveTrackColor = SurfaceDark
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Start: ${formatTime(startPosMs.toLong())}",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Text(
                        text = if (isDurationValid) "✓ Selected: ${selectedDurationSec}s / 60s" else "❌ Selected: ${selectedDurationSec}s (> 60s Limit)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDurationValid) AccentGolden else Color.Red
                    )
                    Text(
                        text = "End: ${formatTime(endPosMs.toLong())}",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Micro Nudge Fine Tuning Buttons for Start Point
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Start Time:", fontSize = 11.sp, color = TextMuted)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        SmallNudgeButton("-5s") {
                            val newStart = maxOf(0f, startPosMs - 5000f)
                            val newEnd = minOf(endPosMs, newStart + 60000f)
                            sliderRange = newStart..newEnd
                            cropError = null
                        }
                        SmallNudgeButton("-1s") {
                            val newStart = maxOf(0f, startPosMs - 1000f)
                            val newEnd = minOf(endPosMs, newStart + 60000f)
                            sliderRange = newStart..newEnd
                            cropError = null
                        }
                        SmallNudgeButton("+1s") {
                            val newStart = minOf(endPosMs - 1000f, startPosMs + 1000f)
                            sliderRange = newStart..endPosMs
                            cropError = null
                        }
                        SmallNudgeButton("+5s") {
                            val newStart = minOf(endPosMs - 1000f, startPosMs + 5000f)
                            sliderRange = newStart..endPosMs
                            cropError = null
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Micro Nudge Fine Tuning Buttons for End Point
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("End Time:", fontSize = 11.sp, color = TextMuted)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        SmallNudgeButton("-5s") {
                            val newEnd = maxOf(startPosMs + 1000f, endPosMs - 5000f)
                            sliderRange = startPosMs..newEnd
                            cropError = null
                        }
                        SmallNudgeButton("-1s") {
                            val newEnd = maxOf(startPosMs + 1000f, endPosMs - 1000f)
                            sliderRange = startPosMs..newEnd
                            cropError = null
                        }
                        SmallNudgeButton("+1s") {
                            val newEnd = minOf(minOf(cropInfo.totalDurationMs.toFloat(), startPosMs + 60000f), endPosMs + 1000f)
                            sliderRange = startPosMs..newEnd
                            cropError = null
                        }
                        SmallNudgeButton("+5s") {
                            val newEnd = minOf(minOf(cropInfo.totalDurationMs.toFloat(), startPosMs + 60000f), endPosMs + 5000f)
                            sliderRange = startPosMs..newEnd
                            cropError = null
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Preview Button
                OutlinedButton(
                    onClick = {
                        if (isPreviewPlaying) {
                            previewPlayer.pause()
                            isPreviewPlaying = false
                        } else {
                            previewPlayer.playSegment(
                                url = cropInfo.inputPath,
                                startMs = startPosMs.toLong(),
                                endMs = endPosMs.toLong()
                            )
                            isPreviewPlaying = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentGolden)
                ) {
                    Icon(
                        if (isPreviewPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isPreviewPlaying) "Pause Preview" else "Play Segment Preview (${selectedDurationSec}s)")
                }

                if (cropError != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = cropError!!,
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val durationMs = (endPosMs - startPosMs).toLong()
                    if (durationMs > 60050L) {
                        cropError = "❌ Audio duration (${durationMs / 1000}s) exceeds 60 seconds limit! Please reduce segment to 60s or less."
                        return@Button
                    }

                    previewPlayer.stop()
                    isPreviewPlaying = false
                    isTrimming = true

                    val outputName = if (cropInfo.isAarti) "custom_aarti_trimmed.mp3" else "custom_bg_bhajan_trimmed.mp3"
                    val trimmedPath = audioTrimmer.trimAudio(
                        inputUriOrPath = cropInfo.inputPath,
                        outputFileName = outputName,
                        startMs = startPosMs.toLong(),
                        endMs = endPosMs.toLong()
                    )

                    if (trimmedPath != null) {
                        val meta = audioTrimmer.getAudioMetadata(trimmedPath)
                        val trimmedSizeBytes = meta?.sizeBytes ?: 0L
                        val trimmedSizeMb = trimmedSizeBytes / (1024f * 1024f)

                        if (trimmedSizeMb > 1.5f) {
                            cropError = "❌ Trimmed file size (${trimmedSizeMb.toString().take(3)} MB) exceeds 1.5 MB limit! Please crop a shorter segment."
                            isTrimming = false
                        } else {
                            onCroppedAndSaved(trimmedPath, "${trimmedSizeMb.toString().take(3)} MB")
                        }
                    } else {
                        cropError = "❌ Failed to crop audio file. Please try another MP3 file."
                        isTrimming = false
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentGolden),
                enabled = !isTrimming && isDurationValid
            ) {
                if (isTrimming) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = BackgroundDark)
                } else {
                    Text("Crop & Apply Audio", fontWeight = FontWeight.Bold, color = BackgroundDark)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    previewPlayer.stop()
                    onDismiss()
                }
            ) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}

@Composable
fun SmallNudgeButton(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = SurfaceDark,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AccentGolden.copy(alpha = 0.5f))
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AccentGolden,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
