package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyank.AdminActionGuardDialog
import com.example.miyank.DarbariCategories
import com.example.miyank.DarbariItem
import com.example.miyank.DarbariManager
import com.example.miyank.DarbariMessage
import com.example.miyank.GuardActionContext
import com.example.miyank.GuardDialogState
import com.example.miyank.LanguageManager
import com.example.miyank.QuerySenderType
import com.example.miyank.openExternalUrl
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDarbarisScreen(
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val isHindi = LanguageManager.currentLanguage == com.example.miyank.Language.HINDI

    val allDarbaris by DarbariManager.darbaris.collectAsState()
    val allMessages by DarbariManager.messages.collectAsState()
    val unreadMessageCount = remember(allMessages) { DarbariManager.getUnreadCountForAdmin() }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Darbaris List, 1: Messages

    // Add/Edit Dialog State
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<DarbariItem?>(null) }
    var nameInput by remember { mutableStateOf("") }
    var mobileInput by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf(DarbariCategories.DARBARIS) }
    var descInput by remember { mutableStateOf("") }
    var isActiveInput by remember { mutableStateOf(true) }

    // Guard Dialog State
    var showGuardDialog by remember { mutableStateOf(false) }
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(SurfaceDark)) {
                TopAppBar(
                    title = {
                        Text(
                            text = if (isHindi) "दरबारी व पुजारी प्रबंधन" else "Manage Darbaris & Pujaris",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite
                        )
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
                    actions = {
                        if (selectedTab == 0) {
                            IconButton(onClick = {
                                editingItem = null
                                nameInput = ""
                                mobileInput = ""
                                categoryInput = DarbariCategories.DARBARIS
                                descInput = ""
                                isActiveInput = true
                                showAddDialog = true
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Add Darbari", tint = AccentGolden)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SurfaceDark,
                        titleContentColor = TextWhite,
                        navigationIconContentColor = TextWhite
                    )
                )

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
                            Text(
                                text = if (isHindi) "👥 दरबारी सूची (${allDarbaris.size})" else "Darbaris (${allDarbaris.size})",
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == 0) AccentGolden else TextMuted
                            )
                        }
                    )

                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            haptic.playPremiumHaptic()
                            selectedTab = 1
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isHindi) "💬 संदेश (${allMessages.size})" else "Messages (${allMessages.size})",
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTab == 1) AccentGolden else TextMuted
                                )
                                if (unreadMessageCount > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Badge(containerColor = Color.Red) {
                                        Text("$unreadMessageCount", color = Color.White, fontSize = 10.sp)
                                    }
                                }
                            }
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
            if (statusMessage != null) {
                Surface(
                    color = AccentGolden.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, AccentGolden.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text(
                        text = statusMessage!!,
                        color = TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            if (selectedTab == 0) {
                // TAB 0: DARBARIS MANAGEMENT LIST
                if (allDarbaris.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isHindi) "कोई दरबारी नहीं जोड़ा गया है।" else "No Darbaris found.",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(allDarbaris) { item ->
                            AdminDarbariCard(
                                item = item,
                                isHindi = isHindi,
                                haptic = haptic,
                                onToggleActive = {
                                    DarbariManager.toggleActive(item.id)
                                },
                                onEdit = {
                                    editingItem = item
                                    nameInput = item.name
                                    mobileInput = item.mobileNumber
                                    categoryInput = item.category
                                    descInput = item.description
                                    isActiveInput = item.isActive
                                    showAddDialog = true
                                },
                                onDelete = {
                                    pendingDeleteId = item.id
                                    showGuardDialog = true
                                }
                            )
                        }
                    }
                }
            } else {
                // TAB 2: DARBARIS MESSAGES
                if (allMessages.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isHindi) "कोई दरबारी संदेश नहीं है।" else "No Darbari messages.",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize().imePadding()
                    ) {
                        items(allMessages) { msg ->
                            AdminDarbariMessageCard(
                                msg = msg,
                                isHindi = isHindi,
                                haptic = haptic,
                                onSendReply = { replyText ->
                                    DarbariManager.sendMessage(
                                        darbariId = msg.darbariId,
                                        darbariName = msg.darbariName,
                                        userName = msg.userName,
                                        userMobile = msg.userMobile,
                                        messageText = replyText,
                                        senderType = QuerySenderType.ADMIN
                                    )
                                    statusMessage = if (isHindi) "उत्तर सफलतापूर्वक भेजा गया! ✓" else "Reply sent successfully! ✓"
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // ADD / EDIT DARBARI DIALOG
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = if (editingItem != null) (if (isHindi) "दरबारी/पुजारी संपादित करें" else "Edit Darbari") else (if (isHindi) "नया दरबारी/पुजारी जोड़ें" else "Add Darbari"),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextWhite
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text(if (isHindi) "नाम *" else "Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = mobileInput,
                        onValueChange = { mobileInput = it },
                        label = { Text(if (isHindi) "मोबाइल नंबर" else "Mobile Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(if (isHindi) "श्रेणी *" else "Category *", fontSize = 12.sp, color = AccentGolden, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DarbariCategories.adminList.forEach { catKey ->
                            val isSelected = categoryInput.equals(catKey, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { categoryInput = catKey },
                                label = { Text(DarbariCategories.getDisplayName(catKey, isHindi), fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentGolden, containerColor = BackgroundDark)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        label = { Text(if (isHindi) "विवरण / सेवा पद" else "Description / Role") },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isHindi) "सक्रिय (Active)" else "Active Status", color = TextWhite, fontSize = 13.sp)
                        Switch(
                            checked = isActiveInput,
                            onCheckedChange = { isActiveInput = it }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.trim().isNotEmpty()) {
                            haptic.playPremiumHaptic()
                            val newItem = DarbariItem(
                                id = editingItem?.id ?: "",
                                name = nameInput.trim(),
                                mobileNumber = mobileInput.trim(),
                                category = categoryInput,
                                description = descInput.trim(),
                                isActive = isActiveInput,
                                sortOrder = editingItem?.sortOrder ?: 0,
                                createdAt = editingItem?.createdAt ?: 0L
                            )
                            DarbariManager.addOrUpdateDarbari(newItem)
                            showAddDialog = false
                            statusMessage = if (isHindi) "सफलतापूर्वक सेव हो गया! ✓" else "Saved successfully! ✓"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                ) {
                    Text(if (isHindi) "सेव करें" else "Save", color = BackgroundDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(if (isHindi) "रद्द करें" else "Cancel")
                }
            },
            containerColor = SurfaceDark
        )
    }

    // DELETE GUARD DIALOG
    AdminActionGuardDialog(
        state = GuardDialogState(
            isVisible = showGuardDialog,
            context = GuardActionContext.DELETE_ITEM,
            title = if (isHindi) "हटाने की पुष्टि करें" else "Confirm Deletion",
            message = if (isHindi) "क्या आप वाकई में इस दरबारी/सेवादार को सूची से हटाना चाहते हैं?" else "Are you sure you want to remove this person?",
            onConfirm = {
                showGuardDialog = false
                if (pendingDeleteId != null) {
                    DarbariManager.deleteDarbari(pendingDeleteId!!)
                    pendingDeleteId = null
                    statusMessage = if (isHindi) "सफलतापूर्वक हटा दिया गया! ✓" else "Removed successfully! ✓"
                }
            },
            onDismiss = { showGuardDialog = false }
        ),
        isHindi = isHindi
    )
}

@Composable
private fun AdminDarbariCard(
    item: DarbariItem,
    isHindi: Boolean,
    haptic: com.example.miyank.HapticManager,
    onToggleActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(38.dp).clip(CircleShape).background(AccentGolden.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = AccentGolden, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = item.name, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = TextWhite)
                        Text(text = item.mobileNumber, fontSize = 11.sp, color = TextMuted)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = AccentGolden.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = DarbariCategories.getDisplayName(item.category, isHindi),
                            color = AccentGolden,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Switch(
                        checked = item.isActive,
                        onCheckedChange = {
                            haptic.playPremiumHaptic()
                            onToggleActive()
                        }
                    )
                }
            }

            if (item.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = item.description, fontSize = 12.sp, color = TextWhite)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AccentGolden, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun AdminDarbariMessageCard(
    msg: DarbariMessage,
    isHindi: Boolean,
    haptic: com.example.miyank.HapticManager,
    onSendReply: (replyText: String) -> Unit
) {
    var showReplyBox by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🛕 ${msg.darbariName}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AccentGolden)
                Text(text = msg.userName + " (" + msg.userMobile + ")", fontSize = 11.sp, color = TextMuted)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                color = BackgroundDark,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = msg.message, modifier = Modifier.padding(10.dp), fontSize = 12.sp, color = TextWhite)
            }

            if (showReplyBox) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    label = { Text(if (isHindi) "उत्तर लिखें..." else "Type reply...") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGolden, unfocusedBorderColor = BackgroundDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    shape = RoundedCornerShape(10.dp),
                    trailingIcon = {
                        IconButton(onClick = {
                            if (replyText.trim().isNotEmpty()) {
                                haptic.playPremiumHaptic()
                                onSendReply(replyText.trim())
                                replyText = ""
                                showReplyBox = false
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = AccentGolden)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = {
                    val cleanPhone = msg.userMobile.replace(" ", "").replace("-", "")
                    openExternalUrl("tel:$cleanPhone")
                }) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = AccentGolden, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isHindi) "कॉल करें" else "Call", color = AccentGolden, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                TextButton(onClick = { showReplyBox = !showReplyBox }) {
                    Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (showReplyBox) (if (isHindi) "रद्द करें" else "Cancel") else (if (isHindi) "उत्तर दें" else "Reply"), color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
