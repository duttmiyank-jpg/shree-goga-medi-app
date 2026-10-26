package com.example.miyank.screens.donation

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.miyank.AdminActionGuardDialog
import com.example.miyank.AdminSaveButton
import com.example.miyank.GuardActionContext
import com.example.miyank.GuardDialogState
import com.example.miyank.Language
import com.example.miyank.LanguageManager
import com.example.miyank.donation.*
import com.example.miyank.rememberFilePickerLauncher
import com.example.miyank.rememberHapticManager
import com.example.miyank.rememberImageOptimizer
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDonationScreen(
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val imageOptimizer = rememberImageOptimizer()
    val isHindi = LanguageManager.currentLanguage == Language.HINDI

    val causes by DonationCauseManager.causes.collectAsState()
    val bankSettings by DonationCauseManager.bankSettings.collectAsState()
    val transactions by DonationCauseManager.transactions.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Causes, 1: Bank & UPI, 2: Records & Verification, 3: Analytics
    var showCauseDialog by remember { mutableStateOf(false) }
    var editingCause by remember { mutableStateOf<DonationCause?>(null) }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var causeToDelete by remember { mutableStateOf<DonationCause?>(null) }

    var txnFilter by remember { mutableStateOf<DonationStatus?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isHindi) "दान एवं सेवा प्रबंधन" else "Donation & Seva Admin",
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark
                )
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = {
                        haptic.playPremiumHaptic()
                        editingCause = DonationCause()
                        showCauseDialog = true
                    },
                    containerColor = WarmGold,
                    contentColor = Color(0xFF160C05)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Cause")
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
            // Admin Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfaceDark,
                contentColor = WarmGold,
                divider = { HorizontalDivider(color = TextMuted.copy(alpha = 0.2f)) }
            ) {
                Tab(selected = selectedTab == 0, onClick = { haptic.playPremiumHaptic(); selectedTab = 0 }, text = { Text(if (isHindi) "सेवा क्षेत्र" else "Causes", fontSize = 12.sp, fontWeight = FontWeight.Bold) })
                Tab(selected = selectedTab == 1, onClick = { haptic.playPremiumHaptic(); selectedTab = 1 }, text = { Text(if (isHindi) "बैंक व UPI" else "Bank/UPI", fontSize = 12.sp, fontWeight = FontWeight.Bold) })
                Tab(selected = selectedTab == 2, onClick = { haptic.playPremiumHaptic(); selectedTab = 2 }, text = { Text(if (isHindi) "सत्यापन (${transactions.count { it.paymentStatus == DonationStatus.PENDING }})" else "Records", fontSize = 12.sp, fontWeight = FontWeight.Bold) })
                Tab(selected = selectedTab == 3, onClick = { haptic.playPremiumHaptic(); selectedTab = 3 }, text = { Text(if (isHindi) "एनालिटिक्स" else "Analytics", fontSize = 12.sp, fontWeight = FontWeight.Bold) })
            }

            when (selectedTab) {
                0 -> {
                    // TAB 0: SEVA CAUSES CRUD
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(causes) { cause ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                border = BorderStroke(0.5.dp, WarmGold.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(cause.icon, fontSize = 28.sp)
                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(cause.titleHindi, fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 15.sp)
                                        Text("अनुशंसित दान: ₹${cause.recommendedAmount.toInt()} • स्थिति: ${if (cause.isActive) "सक्रिय" else "निष्क्रिय"}", color = WarmGold, fontSize = 12.sp)
                                    }

                                    IconButton(onClick = {
                                        haptic.playPremiumHaptic()
                                        editingCause = cause
                                        showCauseDialog = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = WarmGold)
                                    }

                                    IconButton(onClick = {
                                        haptic.playPremiumHaptic()
                                        causeToDelete = cause
                                        showDeleteConfirmDialog = true
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE57373))
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: BANK & UPI SETTINGS WITH GALLERY SCANNER PICKER
                    var trustName by remember { mutableStateOf(bankSettings.trustName) }
                    var bankName by remember { mutableStateOf(bankSettings.bankName) }
                    var accNo by remember { mutableStateOf(bankSettings.accountNumber) }
                    var ifsc by remember { mutableStateOf(bankSettings.ifscCode) }
                    var upiId by remember { mutableStateOf(bankSettings.upiId) }
                    var qrUrl by remember { mutableStateOf(bankSettings.qrCodeUrl) }

                    // Scanner Image Gallery Picker Launcher
                    val qrPickerLauncher = rememberFilePickerLauncher("image/*") { filePath, _, _ ->
                        if (!filePath.isNullOrBlank()) {
                            val optResult = imageOptimizer.processAndOptimizeImage(filePath)
                            qrUrl = optResult?.optimizedFullImagePath ?: filePath
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(if (isHindi) "मंदिर ट्रस्ट बैंक खाता व UPI सेटिंग्स" else "Bank & UPI Settings", fontWeight = FontWeight.Bold, color = AccentGolden, fontSize = 16.sp)

                        OutlinedTextField(value = trustName, onValueChange = { trustName = it }, label = { Text(if (isHindi) "ट्रस्ट का नाम" else "Trust Name") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite))
                        OutlinedTextField(value = bankName, onValueChange = { bankName = it }, label = { Text(if (isHindi) "बैंक का नाम" else "Bank Name") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite))
                        OutlinedTextField(value = accNo, onValueChange = { accNo = it }, label = { Text(if (isHindi) "खाता संख्या (Account No)" else "Account Number") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite))
                        OutlinedTextField(value = ifsc, onValueChange = { ifsc = it }, label = { Text("IFSC Code") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite))
                        OutlinedTextField(value = upiId, onValueChange = { upiId = it }, label = { Text("UPI ID") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite))

                        // GALLERY SCANNER PHOTO SELECTION & PREVIEW CARD
                        Text(
                            text = if (isHindi) "QR कोड / स्कैनर फोटो" else "QR Code / Scanner Image",
                            fontWeight = FontWeight.Bold,
                            color = AccentGolden,
                            fontSize = 14.sp
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            border = BorderStroke(1.5.dp, WarmGold.copy(alpha = 0.4f))
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (qrUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = qrUrl,
                                        contentDescription = "QR Scanner Preview",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(10.dp)
                                    )
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = WarmGold, modifier = Modifier.size(44.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = if (isHindi) "कोई स्कैनर फोटो नहीं चुनी गई" else "No Scanner Image Selected",
                                            color = TextMuted,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    haptic.playPremiumHaptic()
                                    qrPickerLauncher()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = WarmGold),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).height(44.dp)
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color(0xFF160C05), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isHindi) "गैलरी से स्कैनर फोटो चुनें" else "Pick Scanner Photo",
                                    color = Color(0xFF160C05),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            if (qrUrl.isNotBlank()) {
                                OutlinedButton(
                                    onClick = {
                                        haptic.playPremiumHaptic()
                                        qrUrl = ""
                                    },
                                    border = BorderStroke(1.dp, Color(0xFFE57373)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(44.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove Scanner", tint = Color(0xFFE57373), modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        // Compact text field backup for path display / manual entry
                        OutlinedTextField(
                            value = qrUrl,
                            onValueChange = { qrUrl = it },
                            label = { Text(if (isHindi) "स्कैनर फ़ाइल पाथ / URL" else "Scanner File Path / URL") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = WarmGold,
                                unfocusedBorderColor = TextMuted,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        AdminSaveButton(
                            isHindi = isHindi,
                            onSaveClick = {
                                val newSettings = bankSettings.copy(
                                    trustName = trustName,
                                    bankName = bankName,
                                    accountNumber = accNo,
                                    ifscCode = ifsc,
                                    upiId = upiId,
                                    qrCodeUrl = qrUrl
                                )
                                DonationCauseManager.saveBankSettings(newSettings)
                            }
                        )
                    }
                }

                2 -> {
                    // TAB 2: RECORDS & PAYMENT VERIFICATION
                    val filteredTxns = if (txnFilter != null) {
                        transactions.filter { it.paymentStatus == txnFilter }
                    } else transactions

                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                                FilterChip(selected = txnFilter == null, onClick = { txnFilter = null }, label = { Text("सभी") })
                                FilterChip(selected = txnFilter == DonationStatus.PENDING, onClick = { txnFilter = DonationStatus.PENDING }, label = { Text("लंबित (${transactions.count { it.paymentStatus == DonationStatus.PENDING }})") })
                                FilterChip(selected = txnFilter == DonationStatus.SUCCESS, onClick = { txnFilter = DonationStatus.SUCCESS }, label = { Text("सत्यापित") })
                            }
                        }

                        items(filteredTxns) { txn ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                border = BorderStroke(0.5.dp, WarmGold.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("#${txn.receiptNumber}", fontWeight = FontWeight.ExtraBold, color = WarmGold, fontSize = 14.sp)
                                        Box(
                                            modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(if (txn.isVerified()) Color(0xFF2E7D32) else Color(0xFFE65100)).padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(txn.paymentStatus.hindiTitle, fontSize = 11.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Text("दाता: ${txn.donorName} (${txn.mobileNumber})", color = TextWhite, fontSize = 13.sp)
                                    Text("सेवा: ${txn.sevaCauseName} • राशि: ₹${txn.amount.toInt()}", color = WarmGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                                    if (txn.utrNumber.isNotBlank()) {
                                        Text("📍 UTR/संदर्भ: ${txn.utrNumber}", color = Color(0xFF81C784), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    if (!txn.isVerified()) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                            Button(
                                                onClick = {
                                                    haptic.playPremiumHaptic()
                                                    DonationCauseManager.verifyAndApprovePayment(txn.id)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                                modifier = Modifier.weight(1f).height(36.dp),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("सत्यापित करें (Approve)", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    haptic.playPremiumHaptic()
                                                    DonationCauseManager.rejectPayment(txn.id)
                                                },
                                                border = BorderStroke(1.dp, Color(0xFFE57373)),
                                                modifier = Modifier.weight(1f).height(36.dp),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("अस्वीकृत करें", color = Color(0xFFE57373), fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // TAB 3: ANALYTICS
                    val totalAmt = DonationCauseManager.calculateTotalVerifiedAmount()
                    val verifiedCount = DonationCauseManager.calculateVerifiedCount()
                    val pendingCount = DonationCauseManager.calculatePendingCount()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(if (isHindi) "दान एवं सेवा समरी एनालिटिक्स" else "Donation Analytics", fontWeight = FontWeight.Bold, color = AccentGolden, fontSize = 18.sp)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            border = BorderStroke(1.5.dp, WarmGold),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(if (isHindi) "कुल सत्यापित दान राशि" else "Total Verified Donations", fontSize = 13.sp, color = TextMuted)
                                Text("₹${totalAmt.toInt()}", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = WarmGold)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("सत्यापित रसीदें", fontSize = 11.sp, color = TextMuted)
                                    Text("$verifiedCount", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF81C784))
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("सत्यापन लंबित", fontSize = 11.sp, color = TextMuted)
                                    Text("$pendingCount", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB74D))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Seva Cause Dialog
    if (showCauseDialog && editingCause != null) {
        val c = editingCause!!
        var titleHindi by remember { mutableStateOf(c.titleHindi) }
        var titleEnglish by remember { mutableStateOf(c.titleEnglish) }
        var descHindi by remember { mutableStateOf(c.descriptionHindi) }
        var icon by remember { mutableStateOf(c.icon.ifBlank { "🛕" }) }
        var amountStr by remember { mutableStateOf(c.recommendedAmount.toInt().toString()) }
        var isActive by remember { mutableStateOf(c.isActive) }

        AlertDialog(
            onDismissRequest = { showCauseDialog = false },
            confirmButton = {
                AdminSaveButton(
                    isHindi = isHindi,
                    onSaveClick = {
                        val updated = c.copy(
                            titleHindi = titleHindi.ifBlank { "सेवा क्षेत्र" },
                            titleEnglish = titleEnglish.ifBlank { "Seva Cause" },
                            descriptionHindi = descHindi,
                            icon = icon,
                            recommendedAmount = amountStr.toDoubleOrNull() ?: 501.0,
                            isActive = isActive
                        )
                        DonationCauseManager.saveCause(updated)
                        showCauseDialog = false
                    },
                    onCancelClick = { showCauseDialog = false }
                )
            },
            title = {
                Text(if (isHindi) "सेवा क्षेत्र विवरण दर्ज करें" else "Seva Cause Details", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(value = titleHindi, onValueChange = { titleHindi = it }, label = { Text(if (isHindi) "शीर्षक (हिंदी)" else "Title Hindi") }, singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite))
                    OutlinedTextField(value = titleEnglish, onValueChange = { titleEnglish = it }, label = { Text(if (isHindi) "शीर्षक (अंग्रेजी)" else "Title English") }, singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite))
                    OutlinedTextField(value = icon, onValueChange = { icon = it }, label = { Text(if (isHindi) "इमोजी आइकॉन (Emoji)" else "Icon Emoji") }, singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite))
                    OutlinedTextField(value = amountStr, onValueChange = { amountStr = it }, label = { Text(if (isHindi) "अनुशंसित दान (₹)" else "Recommended Amount ₹") }, singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite))
                    OutlinedTextField(value = descHindi, onValueChange = { descHindi = it }, label = { Text(if (isHindi) "विवरण" else "Description") }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isActive, onCheckedChange = { isActive = it }, colors = CheckboxDefaults.colors(checkedColor = WarmGold))
                        Text(if (isHindi) "सक्रिय (Show to Devotees)" else "Active Cause", color = TextWhite, fontSize = 13.sp)
                    }
                }
            },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Delete Cause Guard Dialog
    if (showDeleteConfirmDialog && causeToDelete != null) {
        AdminActionGuardDialog(
            state = GuardDialogState(
                isVisible = true,
                context = GuardActionContext.DELETE_ITEM,
                title = if (isHindi) "सेवा क्षेत्र हटाएं?" else "Delete Seva Cause?",
                message = if (isHindi) "क्या आप वाकई '${causeToDelete?.titleHindi}' को हटाना चाहते हैं?" else "Are you sure you want to delete '${causeToDelete?.titleHindi}'?",
                itemTitle = causeToDelete?.titleHindi ?: "",
                onConfirm = {
                    DonationCauseManager.deleteCause(causeToDelete!!.id)
                    showDeleteConfirmDialog = false
                },
                onDismiss = { showDeleteConfirmDialog = false }
            ),
            isHindi = isHindi
        )
    }
}
