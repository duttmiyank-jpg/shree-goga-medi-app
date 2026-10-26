package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.miyank.Language
import com.example.miyank.LanguageManager
import com.example.miyank.donation.*
import com.example.miyank.rememberHapticManager
import com.example.miyank.rememberPaymentManager
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationScreen(
    onMyReceiptsClick: () -> Unit,
    onReceiptGeneratedClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val paymentManager = rememberPaymentManager()
    val clipboardManager = LocalClipboardManager.current
    val isHindi = LanguageManager.currentLanguage == Language.HINDI

    val causes by DonationCauseManager.causes.collectAsState()
    val activeCauses = causes.filter { it.isActive }.sortedBy { it.sortOrder }
    val bankSettings by DonationCauseManager.bankSettings.collectAsState()

    var selectedCause by remember { mutableStateOf<DonationCause?>(null) }
    var showDonateDialog by remember { mutableStateOf(false) }
    var showEnlargedQrDialog by remember { mutableStateOf(false) }
    var copiedNotice by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isHindi) "दान एवं सेवा" else "Donation & Seva",
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
                actions = {
                    TextButton(onClick = {
                        haptic.playPremiumHaptic()
                        onMyReceiptsClick()
                    }) {
                        Icon(Icons.Default.Receipt, contentDescription = null, tint = WarmGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isHindi) "मेरी रसीदें" else "My Receipts",
                            color = WarmGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero Appeal Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(GoldenGradientStart, WarmGold))),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(GoldenGradientStart, GoldenGradientEnd))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🛕", fontSize = 28.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isHindi) "सेवा में आपका पवित्र सहयोग" else "Your Sacred Contribution",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isHindi) "आपका सहयोग मंदिर सेवा, लंगर, गौ सेवा एवं जनकल्याणकारी कार्यों में समर्पित किया जाता है।" else "Your donation supports temple maintenance, langar, gau seva, and welfare.",
                        fontSize = 13.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }

            // Section 1: Seva Causes List
            Text(
                text = if (isHindi) "सेवा क्षेत्र चुनें" else "Select Seva Cause",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AccentGolden
            )

            activeCauses.forEach { cause ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            haptic.playPremiumHaptic()
                            selectedCause = cause
                            showDonateDialog = true
                        },
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(0.5.dp, WarmGold.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(WarmGold.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(cause.icon, fontSize = 24.sp)
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isHindi) cause.titleHindi else cause.titleEnglish,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isHindi) cause.descriptionHindi else cause.descriptionEnglish,
                                fontSize = 12.sp,
                                color = TextMuted,
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                haptic.playPremiumHaptic()
                                selectedCause = cause
                                showDonateDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WarmGold),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = if (isHindi) "दान करें" else "Donate",
                                color = Color(0xFF160C05),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // Section 2: Bank & UPI Details Card (Matching Mock Layout)
            Text(
                text = if (isHindi) "🏦 बैंक विवरण" else "🏦 Bank Details",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AccentGolden
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Trust Name
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (isHindi) "ट्रस्ट का नाम (Trust Name)" else "Trust Name",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = bankSettings.trustName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite
                        )
                    }

                    HorizontalDivider(color = TextMuted.copy(alpha = 0.2f))

                    // Bank Name
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (isHindi) "बैंक का नाम (Bank Name)" else "Bank Name",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = bankSettings.bankName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }

                    // Account Number
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (isHindi) "खाता संख्या (Account Number)" else "Account Number",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = bankSettings.accountNumber,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarmGold
                            )
                            IconButton(
                                onClick = {
                                    haptic.playPremiumHaptic()
                                    clipboardManager.setText(AnnotatedString(bankSettings.accountNumber))
                                    copiedNotice = if (isHindi) "खाता संख्या कॉपी हुई! ✓" else "Account number copied! ✓"
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Account No", tint = WarmGold, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // IFSC Code
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (isHindi) "IFSC कोड" else "IFSC Code",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = bankSettings.ifscCode,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }

                    HorizontalDivider(color = TextMuted.copy(alpha = 0.2f))

                    // UPI ID
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "UPI ID",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = bankSettings.upiId,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WarmGold
                        )
                    }

                    // [ UPI ID कॉपी करें ] Button
                    Button(
                        onClick = {
                            haptic.playPremiumHaptic()
                            clipboardManager.setText(AnnotatedString(bankSettings.upiId))
                            copiedNotice = if (isHindi) "UPI ID सफलतापूर्वक कॉपी हुई! ✓" else "UPI ID copied successfully! ✓"
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WarmGold),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF160C05), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isHindi) "UPI ID कॉपी करें" else "Copy UPI ID",
                            color = Color(0xFF160C05),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }

                    if (copiedNotice != null) {
                        Text(
                            text = copiedNotice!!,
                            color = Color(0xFF81C784),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    HorizontalDivider(color = TextMuted.copy(alpha = 0.2f))

                    // QR CODE DISPLAY & ZOOM
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (isHindi) "स्कैन करके दान करें (Scan QR Code)" else "Scan QR Code to Donate",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentGolden
                        )

                        Card(
                            modifier = Modifier
                                .size(190.dp)
                                .clickable {
                                    haptic.playPremiumHaptic()
                                    showEnlargedQrDialog = true
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(2.dp, WarmGold)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                AsyncImage(
                                    model = bankSettings.qrCodeUrl.ifBlank { "file:///android_asset/mandir_qr.jpg" },
                                    contentDescription = "Temple QR Code",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(10.dp)
                                )
                            }
                        }

                        // [ QR बड़ा करें ] Button
                        OutlinedButton(
                            onClick = {
                                haptic.playPremiumHaptic()
                                showEnlargedQrDialog = true
                            },
                            border = BorderStroke(1.dp, WarmGold),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Icon(Icons.Default.ZoomIn, contentDescription = null, tint = WarmGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isHindi) "QR बड़ा करें" else "Enlarge QR",
                                color = WarmGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Enlarged QR Code Dialog
    if (showEnlargedQrDialog) {
        AlertDialog(
            onDismissRequest = { showEnlargedQrDialog = false },
            confirmButton = {
                TextButton(onClick = { showEnlargedQrDialog = false }) {
                    Text(if (isHindi) "बंद करें" else "Close", color = WarmGold, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text(
                    text = if (isHindi) "श्री जामनीवाला दरबार QR कोड" else "Temple QR Code",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .size(250.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(2.dp, WarmGold)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            AsyncImage(
                                model = bankSettings.qrCodeUrl.ifBlank { "file:///android_asset/mandir_qr.jpg" },
                                contentDescription = "Enlarged Temple QR Code",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp)
                            )
                        }
                    }

                    Text(
                        text = "UPI ID: ${bankSettings.upiId}",
                        color = WarmGold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Seva Cause Detail & Donor Form Dialog
    if (showDonateDialog && selectedCause != null) {
        val cause = selectedCause!!
        var donorName by remember { mutableStateOf("") }
        var mobileNumber by remember { mutableStateOf("") }
        var gotraCity by remember { mutableStateOf("") }
        var amountStr by remember { mutableStateOf(cause.recommendedAmount.toInt().toString()) }
        var selectedMethod by remember { mutableStateOf(PaymentMethod.UPI_INTENT) }
        var utrNumberInput by remember { mutableStateOf("") }
        var formError by remember { mutableStateOf("") }

        val quickAmounts = listOf("101", "251", "501", "1100", "2100", "5100")

        AlertDialog(
            onDismissRequest = { showDonateDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.playPremiumHaptic()
                        if (donorName.isBlank()) {
                            formError = if (isHindi) "कृपया अपना नाम दर्ज करें।" else "Please enter your name."
                            return@Button
                        }
                        if (mobileNumber.length < 10) {
                            formError = if (isHindi) "कृपया सही 10-अंकों का मोबाइल नंबर दर्ज करें।" else "Please enter valid 10-digit mobile number."
                            return@Button
                        }
                        val amt = amountStr.toDoubleOrNull() ?: 0.0
                        if (amt < bankSettings.minDonationAmount) {
                            formError = if (isHindi) "कृपया ₹${bankSettings.minDonationAmount.toInt()} या उससे अधिक दान राशि दर्ज करें।" else "Min donation amount is ₹${bankSettings.minDonationAmount.toInt()}."
                            return@Button
                        }

                        // Create transaction via DonationCauseManager
                        val txn = DonationCauseManager.createDonationTransaction(
                            donorName = donorName.trim(),
                            mobileNumber = mobileNumber.trim(),
                            gotraCity = gotraCity.trim(),
                            cause = cause,
                            amount = amt,
                            paymentMethod = selectedMethod,
                            utrNumber = utrNumberInput.trim()
                        )

                        // Trigger PaymentManager UPI flow if UPI Intent chosen
                        if (selectedMethod == PaymentMethod.UPI_INTENT) {
                            paymentManager.initiateUpiPayment(
                                upiId = bankSettings.upiId,
                                name = bankSettings.trustName,
                                amount = amt.toInt().toString(),
                                note = "Donation for ${cause.titleEnglish} (${txn.receiptNumber})"
                            )
                        }

                        showDonateDialog = false
                        onReceiptGeneratedClick(txn.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarmGold),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isHindi) "₹${amountStr.ifBlank { "0" }} दान करें (Pay)" else "Donate ₹${amountStr.ifBlank { "0" }}",
                        color = Color(0xFF160C05),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDonateDialog = false }) {
                    Text(if (isHindi) "रद्द करें" else "Cancel", color = TextMuted)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(cause.icon, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHindi) cause.titleHindi else cause.titleEnglish,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (formError.isNotEmpty()) {
                        Text(formError, color = Color(0xFFFF8A80), fontSize = 12.sp)
                    }

                    // Donor Name
                    OutlinedTextField(
                        value = donorName,
                        onValueChange = { donorName = it },
                        label = { Text(if (isHindi) "दाता का पूरा नाम *" else "Donor Full Name *") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )

                    // Donor Mobile
                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = { mobileNumber = it },
                        label = { Text(if (isHindi) "मोबाइल नंबर *" else "Mobile Number *") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )

                    // Gotra / City
                    OutlinedTextField(
                        value = gotraCity,
                        onValueChange = { gotraCity = it },
                        label = { Text(if (isHindi) "गोत्र / शहर (Optional)" else "Gotra / City (Optional)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )

                    Text(
                        text = if (isHindi) "दान राशि चुनें (Select Amount):" else "Select Amount:",
                        fontSize = 12.sp,
                        color = AccentGolden,
                        fontWeight = FontWeight.Bold
                    )

                    // Quick Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(quickAmounts) { qAmt ->
                            FilterChip(
                                selected = amountStr == qAmt,
                                onClick = {
                                    haptic.playPremiumHaptic()
                                    amountStr = qAmt
                                },
                                label = { Text("₹$qAmt", fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = WarmGold,
                                    selectedLabelColor = Color(0xFF160C05),
                                    containerColor = SurfaceDark,
                                    labelColor = TextWhite
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text(if (isHindi) "दान राशि (Amount ₹) *" else "Donation Amount (₹) *") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )

                    // Payment Method selector
                    Text(
                        text = if (isHindi) "भुगतान माध्यम खरीदें/चुनें:" else "Payment Method:",
                        fontSize = 12.sp,
                        color = AccentGolden,
                        fontWeight = FontWeight.Bold
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(PaymentMethod.UPI_INTENT, PaymentMethod.UPI_QR, PaymentMethod.BANK_TRANSFER).forEach { m ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { selectedMethod = m }
                            ) {
                                RadioButton(selected = selectedMethod == m, onClick = { selectedMethod = m }, colors = RadioButtonDefaults.colors(selectedColor = WarmGold))
                                Text(if (isHindi) m.hindiTitle else m.englishTitle, color = TextWhite, fontSize = 13.sp)
                            }
                        }
                    }

                    if (selectedMethod == PaymentMethod.UPI_QR || selectedMethod == PaymentMethod.BANK_TRANSFER) {
                        OutlinedTextField(
                            value = utrNumberInput,
                            onValueChange = { utrNumberInput = it },
                            label = { Text(if (isHindi) "संदर्भ/UTR नंबर (यदि हो)" else "UTR / Reference No (Optional)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )
                    }
                }
            },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
