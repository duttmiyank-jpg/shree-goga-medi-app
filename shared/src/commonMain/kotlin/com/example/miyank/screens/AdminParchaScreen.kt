package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.miyank.AdminActionGuardDialog
import com.example.miyank.AdminSaveButton
import com.example.miyank.GuardActionContext
import com.example.miyank.GuardDialogState
import com.example.miyank.HapticManager
import com.example.miyank.LanguageManager
import com.example.miyank.LocalStrings
import com.example.miyank.ParchaItem
import com.example.miyank.ParchaManager
import com.example.miyank.ParchaRecipientType
import com.example.miyank.UserQueryManager
import com.example.miyank.formatDisplayDate
import com.example.miyank.formatEpochToDateString
import com.example.miyank.rememberFilePickerLauncher
import com.example.miyank.rememberHapticManager
import com.example.miyank.rememberImageOptimizer
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminParchaScreen(
    prefillQueryId: String = "",
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val isHindi = LanguageManager.currentLanguage == com.example.miyank.Language.HINDI
    val strings = LocalStrings.current

    val allParchas by ParchaManager.parchas.collectAsState()
    val allQueries by UserQueryManager.queries.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingParcha by remember { mutableStateOf<ParchaItem?>(null) }

    // Guard Dialog State
    var guardState by remember { mutableStateOf(GuardDialogState()) }

    // Pre-fill query handling if launched from query
    LaunchedEffect(prefillQueryId) {
        if (prefillQueryId.isNotEmpty()) {
            val q = allQueries.find { it.id == prefillQueryId }
            if (q != null) {
                editingParcha = ParchaItem(
                    recipientType = ParchaRecipientType.SPECIFIC_USER,
                    recipientUserId = q.phone.ifEmpty { q.id },
                    recipientUserName = q.name,
                    recipientPhoneNumber = q.phone,
                    title = "आपकी समस्या के संबंध में दरबार मार्गदर्शन",
                    problem = q.message
                )
                showEditDialog = true
            }
        }
    }

    val filteredParchas = remember(allParchas, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) allParchas
        else allParchas.filter { p ->
            p.title.lowercase().contains(q) ||
                    (p.recipientUserName?.lowercase()?.contains(q) == true) ||
                    (p.recipientPhoneNumber?.contains(q) == true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = strings.parchaManagement,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = if (isHindi) "दरबार पर्चा प्रेषण व प्रबंधन" else "Dispatch & Manage Darbar Parchas",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.playPremiumHaptic()
                        onBackClick()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    haptic.playPremiumHaptic()
                    editingParcha = ParchaItem(
                        recipientType = ParchaRecipientType.SPECIFIC_USER
                    )
                    showEditDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null, tint = BackgroundDark) },
                text = { Text(if (isHindi) "नया पर्चा भेजें" else "Send New Parcha", color = BackgroundDark, fontWeight = FontWeight.Bold) },
                containerColor = WarmGold,
                shape = RoundedCornerShape(16.dp)
            )
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (isHindi) "पर्चा या श्रद्धालु खोजें..." else "Search parcha or devotee...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentGolden) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = TextMuted)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WarmGold,
                    unfocusedBorderColor = TextMuted.copy(alpha = 0.5f),
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark
                ),
                shape = RoundedCornerShape(12.dp)
            )

            if (filteredParchas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isHindi) "कोई पर्चा नहीं मिला" else "No Parchas found",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredParchas, key = { it.id }) { item ->
                        AdminParchaCard(
                            item = item,
                            isHindi = isHindi,
                            haptic = haptic,
                            onEdit = {
                                editingParcha = item
                                showEditDialog = true
                            },
                            onDelete = {
                                guardState = GuardDialogState(
                                    isVisible = true,
                                    context = GuardActionContext.DELETE_ITEM,
                                    title = if (isHindi) "पर्चा हटाएं?" else "Delete Parcha?",
                                    message = if (isHindi) "क्या आप वाकई इस पर्चा को हटाना चाहते हैं?" else "Are you sure you want to delete this Parcha?",
                                    itemTitle = item.title,
                                    onConfirm = {
                                        ParchaManager.deleteParcha(item.id)
                                        guardState = GuardDialogState()
                                    },
                                    onDismiss = { guardState = GuardDialogState() }
                                )
                            },
                            onToggleActive = {
                                ParchaManager.toggleActive(item.id)
                            }
                        )
                    }
                }
            }
        }
    }

    // Edit/Create Dialog
    if (showEditDialog && editingParcha != null) {
        AdminParchaFormDialog(
            initialParcha = editingParcha!!,
            isHindi = isHindi,
            haptic = haptic,
            onDismiss = { showEditDialog = false },
            onSave = { updated ->
                if (updated.id.isEmpty()) {
                    ParchaManager.addParcha(updated)
                } else {
                    ParchaManager.updateParcha(updated)
                }
                showEditDialog = false
            }
        )
    }

    // Guard Dialog
    AdminActionGuardDialog(state = guardState, isHindi = isHindi)
}

@Composable
private fun AdminParchaCard(
    item: ParchaItem,
    isHindi: Boolean,
    haptic: HapticManager,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (item.recipientType == ParchaRecipientType.ALL_USERS) AccentGolden.copy(alpha = 0.15f) else WarmGold.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (item.recipientType == ParchaRecipientType.ALL_USERS) {
                            if (isHindi) "सभी श्रद्धालु" else "All Devotees"
                        } else {
                            "${item.recipientUserName ?: (if (isHindi) "विशेष श्रद्धालु" else "Specific Devotee")} (${item.recipientPhoneNumber ?: ""})".trim()
                        },
                        color = AccentGolden,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = formatDisplayDate(formatEpochToDateString(item.createdAt)),
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Text(
                text = item.title.ifEmpty { "दरबार पर्चा" },
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = item.guidance,
                fontSize = 12.sp,
                color = TextMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    haptic.playPremiumHaptic()
                    onToggleActive()
                }) {
                    Icon(
                        imageVector = if (item.isActive) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Active",
                        tint = if (item.isActive) Color(0xFF81C784) else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = {
                    haptic.playPremiumHaptic()
                    onEdit()
                }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = WarmGold, modifier = Modifier.size(20.dp))
                }

                IconButton(onClick = {
                    haptic.playPremiumHaptic()
                    onDelete()
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminParchaFormDialog(
    initialParcha: ParchaItem,
    isHindi: Boolean,
    haptic: HapticManager,
    onDismiss: () -> Unit,
    onSave: (ParchaItem) -> Unit
) {
    val scrollState = rememberScrollState()

    var recipientType by remember { mutableStateOf(initialParcha.recipientType) }
    var recipientUserId by remember { mutableStateOf(initialParcha.recipientUserId ?: "") }
    var recipientUserName by remember { mutableStateOf(initialParcha.recipientUserName ?: "") }
    var recipientPhoneNumber by remember { mutableStateOf(initialParcha.recipientPhoneNumber ?: "") }

    var titleInput by remember { mutableStateOf(initialParcha.title) }
    var problemInput by remember { mutableStateOf(initialParcha.problem ?: "") }
    var guidanceInput by remember { mutableStateOf(initialParcha.guidance) }
    var upayInput by remember { mutableStateOf(initialParcha.upay ?: "") }
    var karamInput by remember { mutableStateOf(initialParcha.karamSeva ?: "") }
    var additionalInput by remember { mutableStateOf(initialParcha.additionalMessage ?: "") }
    var imageUrl by remember { mutableStateOf(initialParcha.imageUrl ?: "") }

    var formError by remember { mutableStateOf("") }
    var hasUnsavedChanges by remember { mutableStateOf(false) }
    var showUnsavedGuard by remember { mutableStateOf(false) }

    val imageOptimizer = rememberImageOptimizer()
    val photoPickerLauncher = rememberFilePickerLauncher("image/*") { filePath, _, _ ->
        if (!filePath.isNullOrBlank()) {
            val optResult = imageOptimizer.processAndOptimizeImage(filePath)
            val finalPath = optResult?.optimizedFullImagePath ?: filePath
            imageUrl = finalPath
            hasUnsavedChanges = true
        }
    }

    val userQueries by UserQueryManager.queries.collectAsState()

    val checkCancel = {
        if (hasUnsavedChanges) {
            showUnsavedGuard = true
        } else {
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = { checkCancel() },
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialParcha.id.isEmpty()) {
                        if (isHindi) "नया पर्चा लिखें" else "Send New Parcha"
                    } else {
                        if (isHindi) "पर्चा संपादित करें" else "Edit Parcha"
                    },
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    fontSize = 18.sp
                )
                IconButton(onClick = { checkCancel() }) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (formError.isNotEmpty()) {
                    Text(formError, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Recipient Selection
                Text(if (isHindi) "प्राप्तकर्ता चुनें:" else "Select Recipient:", color = AccentGolden, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = recipientType == ParchaRecipientType.SPECIFIC_USER,
                        onClick = {
                            recipientType = ParchaRecipientType.SPECIFIC_USER
                            hasUnsavedChanges = true
                        },
                        label = { Text(if (isHindi) "विशेष श्रद्धालु" else "Specific Devotee") },
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = recipientType == ParchaRecipientType.ALL_USERS,
                        onClick = {
                            recipientType = ParchaRecipientType.ALL_USERS
                            hasUnsavedChanges = true
                        },
                        label = { Text(if (isHindi) "सभी श्रद्धालु" else "All Devotees") },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (recipientType == ParchaRecipientType.SPECIFIC_USER) {
                    OutlinedTextField(
                        value = recipientUserName,
                        onValueChange = { recipientUserName = it; hasUnsavedChanges = true },
                        label = { Text(if (isHindi) "श्रद्धालु का नाम" else "Devotee Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )

                    OutlinedTextField(
                        value = recipientPhoneNumber,
                        onValueChange = {
                            recipientPhoneNumber = it
                            recipientUserId = it
                            hasUnsavedChanges = true
                        },
                        label = { Text(if (isHindi) "पंजीकृत मोबाइल नंबर" else "Registered Mobile Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )

                    if (userQueries.isNotEmpty()) {
                        Text(if (isHindi) "या प्रश्नों से श्रद्धालु चुनें:" else "Or Select Devotee from Queries:", color = TextMuted, fontSize = 11.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            userQueries.take(3).forEach { q ->
                                SuggestionChip(
                                    onClick = {
                                        recipientUserName = q.name
                                        recipientPhoneNumber = q.phone
                                        recipientUserId = q.phone.ifEmpty { q.id }
                                        hasUnsavedChanges = true
                                    },
                                    label = { Text(q.name, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = WarmGold.copy(alpha = 0.2f))

                // Single Hindi Content Inputs
                Text(if (isHindi) "विषय (Hindi Content):" else "Subject / विषय (Hindi):", color = AccentGolden, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it; hasUnsavedChanges = true },
                    label = { Text(if (isHindi) "विषय (हिंदी)" else "Subject (Hindi)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                )

                Text(if (isHindi) "श्रद्धालु समस्या (Optional):" else "Devotee Concern / समस्या (Optional):", color = AccentGolden, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = problemInput,
                    onValueChange = { problemInput = it; hasUnsavedChanges = true },
                    label = { Text(if (isHindi) "समस्या (हिंदी)" else "Concern (Hindi)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                )

                Text(if (isHindi) "दरबार मार्गदर्शन (Required):" else "Darbar Guidance / मार्गदर्शन (Required):", color = AccentGolden, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = guidanceInput,
                    onValueChange = { guidanceInput = it; hasUnsavedChanges = true },
                    label = { Text(if (isHindi) "मार्गदर्शन (हिंदी)" else "Guidance (Hindi)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                )

                Text(if (isHindi) "उपाय (Optional):" else "Upay / उपाय (Optional):", color = AccentGolden, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = upayInput,
                    onValueChange = { upayInput = it; hasUnsavedChanges = true },
                    label = { Text(if (isHindi) "उपाय (हिंदी)" else "Upay (Hindi)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                )

                Text(if (isHindi) "कर्म / सेवा (Optional):" else "Karam / Seva / कर्म-सेवा (Optional):", color = AccentGolden, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = karamInput,
                    onValueChange = { karamInput = it; hasUnsavedChanges = true },
                    label = { Text(if (isHindi) "कर्म/सेवा (हिंदी)" else "Karam/Seva (Hindi)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                )

                Text(if (isHindi) "अतिरिक्त मार्गदर्शन (Optional):" else "Additional Guidance (Optional):", color = AccentGolden, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = additionalInput,
                    onValueChange = { additionalInput = it; hasUnsavedChanges = true },
                    label = { Text(if (isHindi) "अतिरिक्त संदेश (हिंदी)" else "Additional Guidance (Hindi)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                )

                // Image Upload Section
                Text(if (isHindi) "फोटो / पर्चा संलग्नक (Optional):" else "Parcha Photo / Attachment (Optional):", color = AccentGolden, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                if (imageUrl.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.5f))
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Parcha Attachment",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = {
                                    imageUrl = ""
                                    hasUnsavedChanges = true
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                                    .size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            haptic.playPremiumHaptic()
                            photoPickerLauncher()
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, WarmGold)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = WarmGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isHindi) "गैलरी से फोटो अपलोड करें" else "Upload Photo from Gallery", color = WarmGold, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            AdminSaveButton(
                onSaveClick = {
                    val g = guidanceInput.trim()
                    if (g.isEmpty()) {
                        formError = if (isHindi) "मार्गदर्शन क्षेत्र भरना आवश्यक है!" else "Guidance field (Hindi) is required!"
                        return@AdminSaveButton
                    }
                    val t = titleInput.trim().ifEmpty { "दरबार पर्चा" }

                    val updated = initialParcha.copy(
                        recipientType = recipientType,
                        recipientUserId = if (recipientType == ParchaRecipientType.SPECIFIC_USER) recipientUserId.ifEmpty { "guest_user" } else null,
                        recipientPhoneNumber = if (recipientType == ParchaRecipientType.SPECIFIC_USER) recipientPhoneNumber else null,
                        recipientUserName = if (recipientType == ParchaRecipientType.SPECIFIC_USER) recipientUserName.ifEmpty { "श्रद्धालु" } else null,
                        title = t,
                        problem = problemInput.ifEmpty { null },
                        guidance = g,
                        upay = upayInput.ifEmpty { null },
                        karamSeva = karamInput.ifEmpty { null },
                        additionalMessage = additionalInput.ifEmpty { null },
                        imageUrl = imageUrl.ifEmpty { null }
                    )
                    onSave(updated)
                },
                isHindi = isHindi
            )
        },
        dismissButton = {
            TextButton(onClick = { checkCancel() }) {
                Text(if (isHindi) "रद्द करें" else "Cancel", color = TextMuted)
            }
        }
    )

    if (showUnsavedGuard) {
        AdminActionGuardDialog(
            state = GuardDialogState(
                isVisible = true,
                context = GuardActionContext.UNSAVED_CHANGES,
                title = if (isHindi) "बिना सेव किए निकलें?" else "Discard Unsaved Changes?",
                message = if (isHindi) "आपके पर्चा ड्राफ्ट में बिना सेव किए बदलाव हैं।" else "You have unsaved edits in your Parcha draft.",
                onConfirm = {
                    showUnsavedGuard = false
                    onDismiss()
                },
                onDiscard = {
                    showUnsavedGuard = false
                    onDismiss()
                },
                onDismiss = { showUnsavedGuard = false }
            ),
            isHindi = isHindi
        )
    }
}
