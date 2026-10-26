package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyank.AdminActionGuardDialog
import com.example.miyank.AdminSaveButton
import com.example.miyank.ContactUsSettings
import com.example.miyank.GuardActionContext
import com.example.miyank.GuardDialogState
import com.example.miyank.HapticManager
import com.example.miyank.LanguageManager
import com.example.miyank.QuerySenderType
import com.example.miyank.QueryStatus
import com.example.miyank.UserQuery
import com.example.miyank.UserQueryManager
import com.example.miyank.getEpochMillis
import com.example.miyank.openExternalUrl
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserQueriesScreen(
    onSendAsParcha: ((queryId: String) -> Unit)? = null,
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val isHindi = LanguageManager.currentLanguage == com.example.miyank.Language.HINDI
    val queriesState by UserQueryManager.queries.collectAsState()
    val currentContactSettings by UserQueryManager.contactSettings.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Queries, 1: Contact Settings

    // Form states for Contact Settings Tab
    var helplineInput by remember { mutableStateOf(currentContactSettings.helplinePhone) }
    var whatsappInput by remember { mutableStateOf(currentContactSettings.whatsappPhone) }
    var emailInput by remember { mutableStateOf(currentContactSettings.email) }
    var addressInput by remember { mutableStateOf(currentContactSettings.address) }

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var showSaveConfirmDialog by remember { mutableStateOf(false) }

    val unreadCount = remember(queriesState) { UserQueryManager.getUnreadCountForAdmin() }

    val hasContactChanges = helplineInput != currentContactSettings.helplinePhone ||
            whatsappInput != currentContactSettings.whatsappPhone ||
            emailInput != currentContactSettings.email ||
            addressInput != currentContactSettings.address

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(SurfaceDark)) {
                TopAppBar(
                    title = { Text(if (isHindi) "संपर्क करें व प्रश्न प्रबंधन" else "Contact Us & Queries", fontWeight = FontWeight.ExtraBold) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextWhite)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SurfaceDark,
                        titleContentColor = TextWhite,
                        navigationIconContentColor = TextWhite
                    )
                )

                // Tab Selector
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SurfaceDark,
                    contentColor = AccentGolden,
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            haptic.playPremiumHaptic()
                            selectedTab = 0
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isHindi) "📩 प्रश्न (${queriesState.size})" else "Queries (${queriesState.size})",
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTab == 0) AccentGolden else TextMuted
                                )
                                if (unreadCount > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Badge(containerColor = Color.Red) {
                                        Text("$unreadCount", color = Color.White, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            haptic.playPremiumHaptic()
                            selectedTab = 1
                        },
                        text = {
                            Text(
                                text = if (isHindi) "⚙️ संपर्क सेटिंग्स" else "Contact Settings",
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == 1) AccentGolden else TextMuted
                            )
                        }
                    )
                }
            }
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (selectedTab == 0) {
                // TAB 0: USER QUERIES LIST
                if (queriesState.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isHindi) "अभी कोई प्रश्न या संदेश दर्ज नहीं हुआ है।" else "No user queries found.",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize().imePadding()
                    ) {
                        items(queriesState) { query ->
                            QueryCard(
                                query = query,
                                haptic = haptic,
                                isHindi = isHindi,
                                onResolve = {
                                    haptic.playPremiumHaptic()
                                    UserQueryManager.markResolved(query.id)
                                },
                                onDelete = {
                                    haptic.playPremiumHaptic()
                                    UserQueryManager.deleteQuery(query.id)
                                },
                                onSendReply = { replyMsg ->
                                    haptic.playPremiumHaptic()
                                    UserQueryManager.addReply(query.id, replyMsg, QuerySenderType.ADMIN)
                                },
                                onSendAsParcha = if (onSendAsParcha != null) {
                                    { onSendAsParcha(query.id) }
                                } else null
                            )
                        }
                    }
                }
            } else {
                // TAB 1: CONTACT US SETTINGS FORM WITH KEYBOARD IME PADDING FIX
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    if (statusMessage != null) {
                        Surface(
                            color = AccentGolden.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, AccentGolden.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
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

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = if (isHindi) "आधिकारिक संपर्क विवरण अपडेट करें" else "Edit Official Contact Info",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AccentGolden
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = helplineInput,
                                onValueChange = { helplineInput = it },
                                label = { Text(if (isHindi) "हेल्पलाइन फ़ोन नंबर" else "Helpline Phone") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = AccentGolden) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentGolden,
                                    unfocusedBorderColor = BackgroundDark,
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = whatsappInput,
                                onValueChange = { whatsappInput = it },
                                label = { Text(if (isHindi) "WhatsApp नंबर (Country code के साथ)" else "WhatsApp Number") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = AccentGolden) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentGolden,
                                    unfocusedBorderColor = BackgroundDark,
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text(if (isHindi) "आधिकारिक ईमेल (Email)" else "Official Email") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = AccentGolden) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentGolden,
                                    unfocusedBorderColor = BackgroundDark,
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = addressInput,
                                onValueChange = { addressInput = it },
                                label = { Text(if (isHindi) "मंदिर पता (Temple Address)" else "Temple Address") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = AccentGolden) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentGolden,
                                    unfocusedBorderColor = BackgroundDark,
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite
                                ),
                                shape = RoundedCornerShape(12.dp),
                                minLines = 2
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    AdminSaveButton(
                        text = if (isHindi) "सेव संपर्क सेटिंग्स" else "Save Contact Settings",
                        textEnglish = "Save Contact Settings",
                        isEnabled = hasContactChanges,
                        isHindi = isHindi,
                        onSaveClick = {
                            haptic.playPremiumHaptic()
                            showSaveConfirmDialog = true
                        }
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // SAVE CONTACT SETTINGS GUARD DIALOG
    AdminActionGuardDialog(
        state = GuardDialogState(
            isVisible = showSaveConfirmDialog,
            context = GuardActionContext.PUBLISH_MAJOR_CHANGE,
            title = if (isHindi) "💾 सेव संपर्क सेटिंग्स" else "Save Contact Settings",
            message = if (isHindi) "क्या आप वाकई में आधिकारिक संपर्क जानकारी अपडेट करना चाहते हैं?" else "Are you sure you want to update official contact settings?",
            onConfirm = {
                showSaveConfirmDialog = false
                val newSettings = ContactUsSettings(
                    helplinePhone = helplineInput.trim(),
                    whatsappPhone = whatsappInput.trim(),
                    email = emailInput.trim(),
                    address = addressInput.trim(),
                    updatedAt = getEpochMillis()
                )
                UserQueryManager.updateContactSettings(newSettings)
                statusMessage = if (isHindi) "💾 आधिकारिक संपर्क विवरण सफलतापूर्वक सेव हो गया! ✓" else "💾 Contact info saved successfully! ✓"
            },
            onDismiss = { showSaveConfirmDialog = false }
        ),
        isHindi = isHindi
    )
}

@Composable
fun QueryCard(
    query: UserQuery,
    haptic: HapticManager,
    isHindi: Boolean,
    onResolve: () -> Unit,
    onDelete: () -> Unit,
    onSendReply: (replyMsg: String) -> Unit,
    onSendAsParcha: (() -> Unit)? = null
) {
    var showReplyInput by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }

    val statusBadgeText = when (query.status) {
        QueryStatus.NEW -> "🔴 नई"
        QueryStatus.IN_PROGRESS -> "⏳ प्रगति पर"
        QueryStatus.REPLIED -> "✅ उत्तर भेजा गया"
        QueryStatus.CLOSED -> "🔒 बंद"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = AccentGolden, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = query.name, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = TextWhite)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = AccentGolden.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = query.category,
                            color = AccentGolden,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        color = if (query.status == QueryStatus.REPLIED) Color(0xFF1B5E20).copy(alpha = 0.3f) else BackgroundDark,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = statusBadgeText,
                            color = if (query.status == QueryStatus.REPLIED) Color(0xFF81C784) else TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = TextMuted, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = query.phone, fontSize = 12.sp, color = TextMuted)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "• ${query.date}", fontSize = 11.sp, color = TextMuted)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = BackgroundDark,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.15f))
            ) {
                Text(
                    text = query.message,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 13.sp,
                    color = TextWhite,
                    lineHeight = 18.sp
                )
            }

            // ADMIN REPLIES CONVERSATION
            if (query.replies.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    query.replies.forEach { reply ->
                        Surface(
                            color = AccentGolden.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, AccentGolden.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "🛕 ${reply.senderName}:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentGolden
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = reply.message,
                                    fontSize = 12.sp,
                                    color = TextWhite,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // INLINE REPLY INPUT FORM
            if (showReplyInput) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    label = { Text(if (isHindi) "अपना उत्तर लिखें..." else "Type reply...") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGolden,
                        unfocusedBorderColor = BackgroundDark,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    shape = RoundedCornerShape(10.dp),
                    trailingIcon = {
                        IconButton(onClick = {
                            if (replyText.trim().isNotEmpty()) {
                                onSendReply(replyText.trim())
                                replyText = ""
                                showReplyInput = false
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = AccentGolden)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onSendAsParcha != null) {
                        TextButton(onClick = {
                            haptic.playPremiumHaptic()
                            onSendAsParcha()
                        }) {
                            Text(if (isHindi) "📜 पर्चा भेजें" else "📜 Send Parcha", color = WarmGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    TextButton(onClick = {
                        showReplyInput = !showReplyInput
                    }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Reply, contentDescription = null, tint = AccentGolden, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (showReplyInput) (if (isHindi) "बंद करें" else "Cancel") else (if (isHindi) "उत्तर भेजें" else "Reply"), color = AccentGolden, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    TextButton(onClick = {
                        haptic.playPremiumHaptic()
                        val cleanPhone = query.phone.replace(" ", "").replace("-", "")
                        openExternalUrl("tel:$cleanPhone")
                    }) {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = TextMuted, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isHindi) "कॉल करें" else "Call", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    if (!query.isResolved) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = onResolve,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGolden),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(if (isHindi) "बंद करें" else "Close", fontSize = 12.sp, color = BackgroundDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
