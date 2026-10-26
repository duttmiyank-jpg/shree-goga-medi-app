package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyank.DarbariCategories
import com.example.miyank.DarbariItem
import com.example.miyank.DarbariManager
import com.example.miyank.LanguageManager
import com.example.miyank.QuerySenderType
import com.example.miyank.openExternalUrl
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DarbarisScreen(
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val isHindi = LanguageManager.currentLanguage == com.example.miyank.Language.HINDI

    val allDarbaris by DarbariManager.darbaris.collectAsState()
    val activeDarbaris = remember(allDarbaris) {
        allDarbaris.filter { it.isActive }.sortedBy { it.sortOrder }
    }

    var selectedTabCategory by remember { mutableStateOf(DarbariCategories.ALL) }
    val filteredDarbaris = remember(activeDarbaris, selectedTabCategory) {
        if (selectedTabCategory == DarbariCategories.ALL) {
            activeDarbaris
        } else {
            activeDarbaris.filter { it.category.equals(selectedTabCategory, ignoreCase = true) }
        }
    }

    var selectedDarbariForChat by remember { mutableStateOf<DarbariItem?>(null) }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(SurfaceDark)) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = if (isHindi) "दरबारी" else "Darbaris",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextWhite
                            )
                            Text(
                                text = if (isHindi) "पुजारी, सेवादार एवं दरबारी" else "Priests, Volunteers & Devotees",
                                fontSize = 11.sp,
                                color = AccentGolden
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
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SurfaceDark,
                        titleContentColor = TextWhite,
                        navigationIconContentColor = TextWhite
                    )
                )

                // Category Filter Scrollable Row
                ScrollableTabRow(
                    selectedTabIndex = DarbariCategories.filterList.indexOf(selectedTabCategory).coerceAtLeast(0),
                    containerColor = SurfaceDark,
                    contentColor = AccentGolden,
                    edgePadding = 16.dp,
                    divider = {}
                ) {
                    DarbariCategories.filterList.forEach { catKey ->
                        val isSelected = selectedTabCategory == catKey
                        Tab(
                            selected = isSelected,
                            onClick = {
                                haptic.playPremiumHaptic()
                                selectedTabCategory = catKey
                            },
                            text = {
                                Text(
                                    text = DarbariCategories.getDisplayName(catKey, isHindi),
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) AccentGolden else TextMuted,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }
                }
            }
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (filteredDarbaris.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isHindi) "कोई दरबारी/सेवादार सूची में उपलब्ध नहीं है।" else "No Darbaris found in this category.",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredDarbaris) { darbari ->
                        DarbariCardUser(
                            darbari = darbari,
                            isHindi = isHindi,
                            haptic = haptic,
                            onCall = {
                                val cleanPhone = darbari.mobileNumber.replace(" ", "").replace("-", "")
                                openExternalUrl("tel:$cleanPhone")
                            },
                            onMessage = {
                                selectedDarbariForChat = darbari
                            }
                        )
                    }
                }
            }
        }

        // CONVERSATION DIALOG/SHEET FOR SELECTED DARBARI
        if (selectedDarbariForChat != null) {
            DarbariConversationSheet(
                darbari = selectedDarbariForChat!!,
                isHindi = isHindi,
                onDismiss = { selectedDarbariForChat = null }
            )
        }
    }
}

@Composable
private fun DarbariCardUser(
    darbari: DarbariItem,
    isHindi: Boolean,
    haptic: com.example.miyank.HapticManager,
    onCall: () -> Unit,
    onMessage: () -> Unit
) {
    val categoryDisplay = DarbariCategories.getDisplayName(darbari.category, isHindi)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(AccentGolden.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = AccentGolden,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = darbari.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite
                        )

                        Surface(
                            color = AccentGolden.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = categoryDisplay,
                                color = AccentGolden,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (darbari.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = darbari.description,
                            fontSize = 12.sp,
                            color = TextMuted,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Contact Action Buttons: Call & Message
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        haptic.playPremiumHaptic()
                        onCall()
                    },
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, AccentGolden.copy(alpha = 0.5f))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = AccentGolden, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isHindi) "कॉल करें" else "Call", color = AccentGolden, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        haptic.playPremiumHaptic()
                        onMessage()
                    },
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isHindi) "मैसेज करें" else "Message", color = BackgroundDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DarbariConversationSheet(
    darbari: DarbariItem,
    isHindi: Boolean,
    onDismiss: () -> Unit
) {
    val haptic = rememberHapticManager()
    val allMessages by DarbariManager.messages.collectAsState()
    val conversationMessages = remember(allMessages, darbari.id) {
        allMessages.filter { it.darbariId == darbari.id }.sortedBy { it.createdAt }
    }

    var messageInput by remember { mutableStateOf("") }
    var userMobileInput by remember { mutableStateOf("") }
    var userNameInput by remember { mutableStateOf("") }
    var sentSuccess by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = darbari.name, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite)
                    Text(text = DarbariCategories.getDisplayName(darbari.category, isHindi), fontSize = 11.sp, color = AccentGolden)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextWhite)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
            ) {
                if (conversationMessages.isNotEmpty()) {
                    Text(if (isHindi) "बातचीत इतिहास:" else "Conversation History:", fontSize = 11.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 160.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(conversationMessages) { msg ->
                            val isUser = msg.senderType == QuerySenderType.USER
                            Surface(
                                color = if (isUser) AccentGolden.copy(alpha = 0.15f) else BackgroundDark,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, if (isUser) AccentGolden.copy(alpha = 0.4f) else WarmGold.copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = if (isUser) "आप:" else "🛕 ${darbari.name}:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUser) TextWhite else AccentGolden
                                    )
                                    Text(text = msg.message, fontSize = 12.sp, color = TextWhite)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (sentSuccess) {
                    Surface(
                        color = Color(0xFF1B5E20).copy(alpha = 0.3f),
                        border = BorderStroke(1.dp, Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isHindi) "संदेश सफलतापूर्वक भेजा गया! 🙏" else "Message sent successfully! 🙏", fontSize = 11.sp, color = Color(0xFF81C784))
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                OutlinedTextField(
                    value = userNameInput,
                    onValueChange = { userNameInput = it },
                    label = { Text(if (isHindi) "आपका नाम" else "Your Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = userMobileInput,
                    onValueChange = { userMobileInput = it },
                    label = { Text(if (isHindi) "मोबाइल नंबर" else "Mobile Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    label = { Text(if (isHindi) "अपना संदेश लिखें..." else "Type your message...") },
                    modifier = Modifier.fillMaxWidth().height(90.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (messageInput.trim().isNotEmpty()) {
                        haptic.playPremiumHaptic()
                        DarbariManager.sendMessage(
                            darbariId = darbari.id,
                            darbariName = darbari.name,
                            userName = userNameInput.trim(),
                            userMobile = userMobileInput.trim(),
                            messageText = messageInput.trim(),
                            senderType = QuerySenderType.USER
                        )
                        messageInput = ""
                        sentSuccess = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isHindi) "संदेश भेजें" else "Send Message", color = BackgroundDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isHindi) "बंद करें" else "Close")
            }
        },
        containerColor = SurfaceDark
    )
}
