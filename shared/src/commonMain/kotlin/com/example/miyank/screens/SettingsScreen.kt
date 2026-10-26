package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyank.Language
import com.example.miyank.LanguageManager
import com.example.miyank.LocalStrings
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val scrollState = rememberScrollState()
    val strings = LocalStrings.current

    val initialLang = LanguageManager.currentLanguage
    var pendingLanguage by remember { mutableStateOf(initialLang) }

    var notificationsEnabled by remember { mutableStateOf(true) }
    var eventRemindersEnabled by remember { mutableStateOf(true) }

    var showLanguageBottomSheet by remember { mutableStateOf(false) }
    var selectedLanguageInSheet by remember { mutableStateOf(pendingLanguage) }

    val hasChanges = pendingLanguage != initialLang || !notificationsEnabled || !eventRemindersEnabled

    var showSaveConfirmDialog by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val isHindi = pendingLanguage == Language.HINDI

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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            Text(
                text = if (isHindi) "एप्प सेटिंग्स" else "App Settings",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        SettingsSectionHeader(if (isHindi) "सूचनाएं" else "Notifications")

        SettingsToggleItem(
            icon = Icons.Default.Notifications,
            title = if (isHindi) "पुश नोटिफिकेशन" else "Push Notifications",
            subtitle = if (isHindi) "मंदिर के अपडेट और सूचनाएं प्राप्त करें" else "Receive temple updates & alerts",
            checked = notificationsEnabled,
            onCheckedChange = {
                haptic.playPremiumHaptic()
                notificationsEnabled = it
            }
        )

        SettingsToggleItem(
            icon = Icons.Default.Notifications,
            title = if (isHindi) "आयोजन रिमाइंडर" else "Event Reminders",
            subtitle = if (isHindi) "आगामी आयोजनों के लिए अलर्ट" else "Alerts for upcoming events",
            checked = eventRemindersEnabled,
            onCheckedChange = {
                haptic.playPremiumHaptic()
                eventRemindersEnabled = it
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSectionHeader(if (isHindi) "सामान्य" else "General")

        SettingsNavigationItem(
            icon = Icons.Default.Language,
            title = if (isHindi) "भाषा" else "Language",
            subtitle = if (pendingLanguage == Language.HINDI) "हिन्दी (Hindi)" else "English",
            onClick = {
                haptic.playPremiumHaptic()
                selectedLanguageInSheet = pendingLanguage
                showLanguageBottomSheet = true
            }
        )

        SettingsNavigationItem(
            icon = Icons.Default.Palette,
            title = if (isHindi) "थीम" else "Theme",
            subtitle = if (isHindi) "डार्क थीम" else "Dark Theme",
            onClick = {
                haptic.playPremiumHaptic()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSectionHeader(if (isHindi) "सुरक्षा एवं अन्य" else "Security & More")

        SettingsNavigationItem(
            icon = Icons.Default.Security,
            title = if (isHindi) "गोपनीयता नीति" else "Privacy Policy",
            onClick = { haptic.playPremiumHaptic() }
        )

        if (statusMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = AccentGolden.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = statusMessage!!,
                    color = AccentGolden,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (hasChanges) {
            Button(
                onClick = {
                    haptic.playPremiumHaptic()
                    showSaveConfirmDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = BackgroundDark)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isHindi) "सेटिंग्स सेव करें" else "Save & Apply Settings", fontWeight = FontWeight.Bold, color = BackgroundDark)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "App Version 1.0.0 (Beta)",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = "© 2026 श्री जामनीवाला दरबार",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    // MODAL BOTTOM SHEET FOR LANGUAGE SELECTION & CONFIRMATION
    if (showLanguageBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLanguageBottomSheet = false },
            containerColor = SurfaceDark,
            contentColor = TextWhite,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(WarmGold.copy(alpha = 0.5f))
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp, top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = AccentGolden,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHindi) "भाषा चुनें (Select Language)" else "Select App Language",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentGolden
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isHindi) "कृपया अपनी पसंदीदा भाषा चुनें और पुष्टि करें:" else "Choose your preferred language and confirm:",
                    fontSize = 12.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Option 1: Hindi
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            haptic.playPremiumHaptic()
                            selectedLanguageInSheet = Language.HINDI
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedLanguageInSheet == Language.HINDI) AccentGolden.copy(alpha = 0.15f) else BackgroundDark
                    ),
                    border = BorderStroke(
                        1.5.dp,
                        if (selectedLanguageInSheet == Language.HINDI) AccentGolden else WarmGold.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🇮🇳", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("हिन्दी (Hindi)", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = TextWhite)
                                Text("श्री जामनीवाला दरबार हिन्दी भाषा में", fontSize = 11.sp, color = TextMuted)
                            }
                        }

                        RadioButton(
                            selected = selectedLanguageInSheet == Language.HINDI,
                            onClick = {
                                haptic.playPremiumHaptic()
                                selectedLanguageInSheet = Language.HINDI
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = AccentGolden)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Option 2: English
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            haptic.playPremiumHaptic()
                            selectedLanguageInSheet = Language.ENGLISH
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedLanguageInSheet == Language.ENGLISH) AccentGolden.copy(alpha = 0.15f) else BackgroundDark
                    ),
                    border = BorderStroke(
                        1.5.dp,
                        if (selectedLanguageInSheet == Language.ENGLISH) AccentGolden else WarmGold.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🌐", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("English", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = TextWhite)
                                Text("Shree Jamniwala Darbar in English", fontSize = 11.sp, color = TextMuted)
                            }
                        }

                        RadioButton(
                            selected = selectedLanguageInSheet == Language.ENGLISH,
                            onClick = {
                                haptic.playPremiumHaptic()
                                selectedLanguageInSheet = Language.ENGLISH
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = AccentGolden)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Confirm Apply Button
                Button(
                    onClick = {
                        haptic.playPremiumHaptic()
                        LanguageManager.selectLanguage(selectedLanguageInSheet)
                        pendingLanguage = selectedLanguageInSheet
                        showLanguageBottomSheet = false
                        statusMessage = if (selectedLanguageInSheet == Language.HINDI) "भाषा बदलकर हिन्दी कर दी गई! ✓" else "Language changed to English! ✓"
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                ) {
                    Text(
                        text = if (selectedLanguageInSheet == Language.HINDI) "भाषा लागू करें (Apply Language)" else "Apply Language",
                        color = BackgroundDark,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    // SAVE CONFIRMATION DIALOG USING UNIVERSAL GUARD
    com.example.miyank.AdminActionGuardDialog(
        state = com.example.miyank.GuardDialogState(
            isVisible = showSaveConfirmDialog,
            context = com.example.miyank.GuardActionContext.PUBLISH_MAJOR_CHANGE,
            title = if (isHindi) "सेटिंग्स अपडेट की पुष्टि करें" else "Confirm Settings Update",
            message = if (isHindi) "क्या आप वाकई में इन सेटिंग्स/भाषा बदलावों को सेव और लागू करना चाहते हैं?" else "Are you sure you want to save and apply these settings/language changes?",
            onConfirm = {
                showSaveConfirmDialog = false
                LanguageManager.selectLanguage(pendingLanguage)
                statusMessage = if (pendingLanguage == Language.HINDI) "सेटिंग्स और भाषा सफलतापूर्वक अपडेट हो गई! ✓" else "Settings & Language updated successfully! ✓"
            },
            onDismiss = { showSaveConfirmDialog = false }
        ),
        isHindi = isHindi
    )

    // UNSAVED CHANGES GUARD DIALOG USING UNIVERSAL GUARD
    com.example.miyank.AdminActionGuardDialog(
        state = com.example.miyank.GuardDialogState(
            isVisible = showUnsavedDialog,
            context = com.example.miyank.GuardActionContext.UNSAVED_CHANGES,
            onConfirm = {
                showUnsavedDialog = false
                LanguageManager.selectLanguage(pendingLanguage)
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

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = AccentGolden
    )
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = AccentGolden)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextWhite)
                Text(text = subtitle, fontSize = 12.sp, color = TextMuted)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = BackgroundDark,
                    checkedTrackColor = AccentGolden,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = SurfaceDark
                )
            )
        }
    }
}

@Composable
fun SettingsNavigationItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = AccentGolden)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextWhite)
                if (subtitle != null) {
                    Text(text = subtitle, fontSize = 12.sp, color = TextMuted)
                }
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
        }
    }
}
