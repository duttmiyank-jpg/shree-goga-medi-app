package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyank.AdminActionGuardDialog
import com.example.miyank.AdminSaveButton
import com.example.miyank.GuardActionContext
import com.example.miyank.GuardDialogState
import com.example.miyank.LanguageManager
import com.example.miyank.PanchangLocationConfig
import com.example.miyank.PanchangProvider
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAppSettingsScreen(
    onBackClick: () -> Unit
) {
    val hapticManager = rememberHapticManager()
    val scrollState = rememberScrollState()
    val isHindi = LanguageManager.currentLanguage == com.example.miyank.Language.HINDI

    val initialConfig = remember { PanchangProvider.locationConfig.value }

    var templeNameInput by remember { mutableStateOf(initialConfig.templeName) }
    var cityInput by remember { mutableStateOf(initialConfig.city) }
    var stateInput by remember { mutableStateOf(initialConfig.state) }
    var countryInput by remember { mutableStateOf(initialConfig.country) }
    var latInput by remember { mutableStateOf(initialConfig.latitude.toString()) }
    var longInput by remember { mutableStateOf(initialConfig.longitude.toString()) }
    var timezoneInput by remember { mutableStateOf(initialConfig.timezone) }

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var showSaveConfirmDialog by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }

    val hasChanges = templeNameInput != initialConfig.templeName ||
            cityInput != initialConfig.city ||
            stateInput != initialConfig.state ||
            countryInput != initialConfig.country ||
            latInput != initialConfig.latitude.toString() ||
            longInput != initialConfig.longitude.toString() ||
            timezoneInput != initialConfig.timezone

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isHindi) "पंचांग लोकेशन सेटिंग्स" else "Panchang Location Settings", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasChanges) {
                            showUnsavedDialog = true
                        } else {
                            onBackClick()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhite
                        )
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
                .padding(20.dp)
        ) {
            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = AccentGolden)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isHindi) "मंदिर लोकेशन एवं टाइमज़ोन सेटिंग्स" else "Temple Location & Timezone",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isHindi) "पंचांग गणना और लोकेशन डिस्प्ले इसी कॉन्फ़िगरेशन से काम करेगी।" else "Panchang calculations & user display use this exact configuration.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
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

            // FORM FIELDS
            OutlinedTextField(
                value = templeNameInput,
                onValueChange = { templeNameInput = it },
                label = { Text(if (isHindi) "मंदिर / संस्थान नाम" else "Temple Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = cityInput,
                onValueChange = { cityInput = it },
                label = { Text(if (isHindi) "शहर / स्थान" else "City") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = stateInput,
                    onValueChange = { stateInput = it },
                    label = { Text(if (isHindi) "राज्य" else "State") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = countryInput,
                    onValueChange = { countryInput = it },
                    label = { Text(if (isHindi) "देश" else "Country") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = latInput,
                    onValueChange = { latInput = it },
                    label = { Text("Latitude (° N)") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = longInput,
                    onValueChange = { longInput = it },
                    label = { Text("Longitude (° E)") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = timezoneInput,
                onValueChange = { timezoneInput = it },
                label = { Text("Timezone (e.g. Asia/Kolkata)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            AdminSaveButton(
                text = "लोकेशन सेटिंग्स सेव करें",
                textEnglish = "Save Location Settings",
                isEnabled = hasChanges && templeNameInput.isNotEmpty() && cityInput.isNotEmpty(),
                isHindi = isHindi,
                onSaveClick = {
                    hapticManager.playPremiumHaptic()
                    showSaveConfirmDialog = true
                }
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // CONFIRM SAVE DIALOG
    AdminActionGuardDialog(
        state = GuardDialogState(
            isVisible = showSaveConfirmDialog,
            context = GuardActionContext.PUBLISH_MAJOR_CHANGE,
            title = if (isHindi) "लोकेशन सेटिंग्स सेव करें" else "Confirm Save Location",
            message = if (isHindi) "क्या आप वाकई में पंचांग लोकेशन एवं कोऑर्डिनेट्स को अपडेट करना चाहते हैं?" else "Are you sure you want to update Panchang location and coordinates?",
            onConfirm = {
                showSaveConfirmDialog = false
                val newConfig = PanchangLocationConfig(
                    templeName = templeNameInput.trim(),
                    city = cityInput.trim(),
                    state = stateInput.trim(),
                    country = countryInput.trim(),
                    latitude = latInput.toDoubleOrNull() ?: 30.45,
                    longitude = longInput.toDoubleOrNull() ?: 77.62,
                    timezone = timezoneInput.trim()
                )
                PanchangProvider.saveLocationConfig(newConfig)
                statusMessage = if (isHindi) "💾 पंचांग लोकेशन एवं कोऑर्डिनेट्स सफलतापूर्वक सेव हो गए! ✓" else "💾 Panchang Location Settings Saved Successfully! ✓"
            },
            onDismiss = { showSaveConfirmDialog = false }
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
                val newConfig = PanchangLocationConfig(
                    templeName = templeNameInput.trim(),
                    city = cityInput.trim(),
                    state = stateInput.trim(),
                    country = countryInput.trim(),
                    latitude = latInput.toDoubleOrNull() ?: 30.45,
                    longitude = longInput.toDoubleOrNull() ?: 77.62,
                    timezone = timezoneInput.trim()
                )
                PanchangProvider.saveLocationConfig(newConfig)
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
