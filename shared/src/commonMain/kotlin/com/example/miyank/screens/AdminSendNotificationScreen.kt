package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyank.LanguageManager
import com.example.miyank.NotificationCategories
import com.example.miyank.NotificationItem
import com.example.miyank.NotificationManager
import com.example.miyank.TargetRoutes
import com.example.miyank.getEpochMillis
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSendNotificationScreen(
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val isHindi = LanguageManager.currentLanguage == com.example.miyank.Language.HINDI

    val notificationsState by NotificationManager.notifications.collectAsState()

    var titleHindiInput by remember { mutableStateOf("") }
    var titleEngInput by remember { mutableStateOf("") }
    var messageHindiInput by remember { mutableStateOf("") }
    var messageEngInput by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf(NotificationCategories.MANDIR) }
    var targetRouteInput by remember { mutableStateOf(TargetRoutes.HOME) }
    var isUrgentInput by remember { mutableStateOf(false) }

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var showSendConfirmDialog by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var deletingNotification by remember { mutableStateOf<NotificationItem?>(null) }

    val hasDraftData = titleHindiInput.isNotEmpty() || messageHindiInput.isNotEmpty()

    val totalSent = notificationsState.size
    val urgentSent = notificationsState.count { it.isUrgent }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isHindi) "मंदिर सूचना ब्रॉडकास्ट (Admin)" else "Send Mandir Notification", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasDraftData) {
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
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Stats Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isHindi) "नोटिफिकेशन डैशबोर्ड" else "Notification Dashboard",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite
                        )
                        Text(
                            text = if (isHindi) "कुल भेजी गई: $totalSent | अति आवश्यक: $urgentSent" else "Total Sent: $totalSent | Urgent: $urgentSent",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(AccentGolden.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = AccentGolden, modifier = Modifier.size(20.dp))
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

            Spacer(modifier = Modifier.height(18.dp))

            // SECTION A: LIVE NOTIFICATION PREVIEW BOX
            Text(
                text = if (isHindi) "📱 लाइव पुश नोटिफिकेशन प्रिव्यू (Real-Time Preview)" else "📱 Live Notification Preview",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AccentGolden
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.5.dp, if (isUrgentInput) Color.Red else AccentGolden)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AccentGolden.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isUrgentInput) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                            contentDescription = null,
                            tint = if (isUrgentInput) Color.Red else AccentGolden,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔔 " + (if (isHindi) "श्री जामनीवाला दरबार" else "Shree Jamniwala Darbar"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentGolden
                            )
                            Text("अभी", fontSize = 10.sp, color = TextMuted)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = titleHindiInput.ifEmpty { if (isHindi) "सूचना शीर्षक यहाँ दिखेगा" else "Notification Title Preview" },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = messageHindiInput.ifEmpty { if (isHindi) "सूचना का संदेश यहाँ दिखेगा..." else "Notification message will appear here..." },
                            fontSize = 12.sp,
                            color = TextMuted,
                            maxLines = 2
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION B: BROADCAST FORM FIELDS
            Text(
                text = if (isHindi) "नयी सूचना तैयार करें" else "Create Broadcast Message",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AccentGolden
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Title Hindi
            OutlinedTextField(
                value = titleHindiInput,
                onValueChange = { titleHindiInput = it },
                label = { Text(if (isHindi) "सूचना शीर्षक (हिन्दी)" else "Notification Title (Hindi)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Title English
            OutlinedTextField(
                value = titleEngInput,
                onValueChange = { titleEngInput = it },
                label = { Text(if (isHindi) "शीर्षक (अंग्रेज़ी - Optional)" else "Title (English)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Message Hindi
            OutlinedTextField(
                value = messageHindiInput,
                onValueChange = { messageHindiInput = it },
                label = { Text(if (isHindi) "सूचना का पूरा संदेश (हिन्दी)" else "Notification Message (Hindi)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Message English
            OutlinedTextField(
                value = messageEngInput,
                onValueChange = { messageEngInput = it },
                label = { Text(if (isHindi) "संदेश (अंग्रेज़ी - Optional)" else "Message (English)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Selection Pills
            Text(if (isHindi) "कैटेगरी (Category) चुनें:" else "Select Category:", fontSize = 12.sp, color = AccentGolden, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(NotificationCategories.categoriesList) { catKey ->
                    val isSelected = categoryInput == catKey
                    val displayName = NotificationCategories.getDisplayName(catKey, isHindi)
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { categoryInput = catKey },
                        color = if (isSelected) AccentGolden else SurfaceDark,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = displayName,
                            color = if (isSelected) BackgroundDark else TextWhite,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Target Route Pills
            Text(if (isHindi) "टैप करने पर कौन सी स्क्रीन खुले:" else "Navigation Target Route:", fontSize = 12.sp, color = AccentGolden, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(TargetRoutes.list) { routeKey ->
                    val isSelected = targetRouteInput == routeKey
                    val displayName = TargetRoutes.getDisplayName(routeKey, isHindi)
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { targetRouteInput = routeKey },
                        color = if (isSelected) AccentGolden else SurfaceDark,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = displayName,
                            color = if (isSelected) BackgroundDark else TextWhite,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Urgent Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(if (isHindi) "🚨 अति आवश्यक सूचना (High Priority Alert)" else "🚨 High Priority Push Notification", fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 13.sp)
                Switch(
                    checked = isUrgentInput,
                    onCheckedChange = { isUrgentInput = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.Red)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Broadcast Button using AdminSaveButton Component
            com.example.miyank.AdminSaveButton(
                text = "सभी श्रद्धालुओं को सूचना भेजें (Broadcast)",
                textEnglish = "Broadcast Push Notification",
                isEnabled = titleHindiInput.trim().isNotEmpty() && messageHindiInput.trim().isNotEmpty(),
                isHindi = isHindi,
                onSaveClick = {
                    if (titleHindiInput.trim().isEmpty() || messageHindiInput.trim().isEmpty()) {
                        statusMessage = if (isHindi) "❌ कृपया सूचना शीर्षक एवं संदेश दर्ज करें!" else "❌ Please enter title and message!"
                        return@AdminSaveButton
                    }
                    haptic.playPremiumHaptic()
                    showSendConfirmDialog = true
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // SECTION C: SENT HISTORY LOG
            Text(
                text = if (isHindi) "पूर्व में भेजी गई सूचनाएँ (Sent History Log)" else "Sent Notification History",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AccentGolden
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (notificationsState.isEmpty()) {
                Text(
                    text = if (isHindi) "कोई नोटिफिकेशन हिस्ट्री नहीं है।" else "No notification history.",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    notificationsState.forEach { sentItem ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.25f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = AccentGolden.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = NotificationCategories.getDisplayName(sentItem.categoryTag, isHindi),
                                                color = AccentGolden,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(sentItem.timestampStr, fontSize = 10.sp, color = TextMuted)
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = sentItem.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    // Resend / Copy to Form Button
                                    IconButton(
                                        onClick = {
                                            haptic.playPremiumHaptic()
                                            titleHindiInput = sentItem.title
                                            titleEngInput = sentItem.titleEnglish
                                            messageHindiInput = sentItem.message
                                            messageEngInput = sentItem.messageEnglish
                                            categoryInput = sentItem.categoryTag
                                            targetRouteInput = sentItem.targetRoute
                                            isUrgentInput = sentItem.isUrgent
                                            statusMessage = if (isHindi) "नोटिफिकेशन डेटा री-सेंड फॉर्म में लोड हो गया ✓" else "Data loaded into send form ✓"
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Resend", tint = AccentGolden, modifier = Modifier.size(16.dp))
                                    }

                                    // Delete Button
                                    IconButton(
                                        onClick = {
                                            haptic.playPremiumHaptic()
                                            deletingNotification = sentItem
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // CONFIRM BROADCAST DIALOG
    if (showSendConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSendConfirmDialog = false },
            containerColor = SurfaceDark,
            titleContentColor = AccentGolden,
            textContentColor = TextWhite,
            title = {
                Text(
                    text = if (isHindi) "ब्रॉडकास्ट की पुष्टि करें" else "Confirm Notification Broadcast",
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentGolden,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isHindi) "क्या आप वाकई में यह सूचना सभी श्रद्धालुओं को भेजना चाहते हैं?" else "Are you sure you want to broadcast this notification to all users?",
                        color = TextWhite,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = BackgroundDark)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(titleHindiInput, fontWeight = FontWeight.Bold, color = AccentGolden, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(messageHindiInput, color = TextWhite, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSendConfirmDialog = false
                        haptic.playPremiumHaptic()

                        val newNotif = NotificationItem(
                            id = "notif_${getEpochMillis()}",
                            title = titleHindiInput.trim(),
                            titleEnglish = titleEngInput.trim(),
                            message = messageHindiInput.trim(),
                            messageEnglish = messageEngInput.trim(),
                            timestampStr = "अभी",
                            categoryTag = categoryInput,
                            targetRoute = targetRouteInput,
                            isRead = false,
                            isUrgent = isUrgentInput,
                            createdAt = getEpochMillis()
                        )

                        NotificationManager.addNotification(newNotif)

                        // Clear Inputs
                        titleHindiInput = ""
                        titleEngInput = ""
                        messageHindiInput = ""
                        messageEngInput = ""
                        statusMessage = if (isHindi) "🚀 नोटिफिकेशन सभी श्रद्धालुओं को सफलतापूर्वक ब्रॉडकास्ट हो गया! ✓" else "🚀 Notification Broadcast Successfully! ✓"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                ) {
                    Text(
                        text = if (isHindi) "हाँ, ब्रॉडकास्ट करें" else "Yes, Broadcast Now",
                        color = BackgroundDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showSendConfirmDialog = false }) {
                    Text(text = if (isHindi) "रद्द करें" else "Cancel", color = TextMuted)
                }
            }
        )
    }

    // UNSAVED CHANGES GUARD DIALOG
    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            containerColor = SurfaceDark,
            titleContentColor = AccentGolden,
            textContentColor = TextWhite,
            title = {
                Text(
                    text = if (isHindi) "बदलाव सुरक्षित नहीं हैं" else "Unsaved Draft Changes",
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentGolden,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = if (isHindi) "आपके द्वारा लिखा गया ड्राफ्ट संदेश अभी भेजा नहीं गया है। क्या आप बाहर जाना चाहते हैं?" else "Your draft message has not been sent. Do you want to leave?",
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
                    Text(
                        text = if (isHindi) "बाहर जाएँ" else "Discard & Leave",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnsavedDialog = false }) {
                    Text(text = if (isHindi) "यहीं रहें" else "Stay Here", color = AccentGolden, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // DELETE CONFIRMATION DIALOG
    deletingNotification?.let { notif ->
        AlertDialog(
            onDismissRequest = { deletingNotification = null },
            containerColor = SurfaceDark,
            titleContentColor = Color.Red,
            textContentColor = TextWhite,
            title = {
                Text(
                    text = if (isHindi) "नोटिफिकेशन हटाने की पुष्टि" else "Confirm Notification Deletion",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Red,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = if (isHindi) "क्या आप इस नोटिफिकेशन को हिस्ट्री से permanently हटाना चाहते हैं?" else "Are you sure you want to delete this notification?",
                    color = TextWhite,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = notif.id
                        deletingNotification = null
                        NotificationManager.deleteNotification(id)
                        statusMessage = if (isHindi) "नोटिफिकेशन हिस्ट्री से हटाया गया।" else "Notification deleted from history."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(if (isHindi) "हाँ, हटाएँ" else "Yes, Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingNotification = null }) {
                    Text(text = if (isHindi) "रद्द करें" else "Cancel", color = TextMuted)
                }
            }
        )
    }
}
