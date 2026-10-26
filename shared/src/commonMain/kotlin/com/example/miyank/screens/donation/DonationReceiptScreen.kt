package com.example.miyank.screens.donation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyank.Language
import com.example.miyank.LanguageManager
import com.example.miyank.ShareManager
import com.example.miyank.donation.*
import com.example.miyank.rememberHapticManager
import com.example.miyank.rememberSuvicharMediaHandler
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationReceiptScreen(
    transactionId: String,
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val mediaHandler = rememberSuvicharMediaHandler()
    val isHindi = LanguageManager.currentLanguage == Language.HINDI

    val transactions by DonationCauseManager.transactions.collectAsState()
    val txn = transactions.firstOrNull { it.id == transactionId } ?: transactions.firstOrNull()

    if (txn == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Receipt not found", color = TextWhite)
        }
        return
    }

    val isVerified = txn.isVerified()

    val shareMsg = ShareManager.formatGeneralShareMessage(
        title = "दान एवं सेवा रसीद #${txn.receiptNumber}",
        description = "दाता: ${txn.donorName}\nसेवा: ${txn.sevaCauseName}\nराशि: ₹${txn.amount.toInt()}\nस्थिति: ${if (isVerified) "सत्यापित एवं स्वीकृत ✓" else "सत्यापन लंबित"}"
    )

    fun handleShareReceipt() {
        haptic.playPremiumHaptic()
        mediaHandler.shareSuvichar(
            imageUrlOrPath = "",
            dateStr = "",
            quoteText = "रसीद #${txn.receiptNumber}",
            author = "श्री जामनीवाला दरबार",
            shareText = shareMsg
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isHindi) "दान रसीद" else "Donation Receipt",
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
                    IconButton(onClick = { handleShareReceipt() }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = WarmGold)
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Digital Receipt Document Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(2.dp, Brush.linearGradient(listOf(GoldenGradientStart, WarmGold))),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header Emblem
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(GoldenGradientStart, GoldenGradientEnd))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🛕", fontSize = 32.sp)
                    }

                    Text(
                        text = "श्री जामनीवाला दरबार मंदिर ट्रस्ट",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextWhite,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "दान एवं सेवा रसीद • DONATION RECEIPT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarmGold,
                        textAlign = TextAlign.Center
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(WarmGold.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "रसीद सं: #${txn.receiptNumber}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WarmGold
                        )
                    }

                    HorizontalDivider(color = TextMuted.copy(alpha = 0.25f))

                    // Receipt Fields Grid
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReceiptRow(if (isHindi) "दाता का नाम:" else "Donor Name:", txn.donorName, isBold = true)
                        ReceiptRow(if (isHindi) "मोबाइल नंबर:" else "Mobile:", txn.getMaskedMobile())
                        if (txn.gotraCity.isNotBlank()) {
                            ReceiptRow(if (isHindi) "गोत्र / शहर:" else "Gotra / City:", txn.gotraCity)
                        }
                        ReceiptRow(if (isHindi) "सेवा का नाम:" else "Seva Cause:", txn.sevaCauseName, isHighlight = true)
                        ReceiptRow(if (isHindi) "दान राशि:" else "Amount:", "₹${txn.amount.toInt()}", isGold = true)
                        ReceiptRow(if (isHindi) "भुगतान माध्यम:" else "Payment Method:", if (isHindi) txn.paymentMethod.hindiTitle else txn.paymentMethod.englishTitle)

                        if (txn.utrNumber.isNotBlank()) {
                            ReceiptRow(if (isHindi) "UTR / संदर्भ संख्या:" else "UTR Reference:", txn.utrNumber)
                        }

                        ReceiptRow(
                            label = if (isHindi) "भुगतान स्थिति:" else "Status:",
                            value = if (isVerified) (if (isHindi) "सत्यापित एवं स्वीकृत ✓" else "Verified & Approved")
                            else (if (isHindi) "सत्यापन लंबित (Pending)" else "Pending Verification"),
                            isBadge = true,
                            isVerified = isVerified
                        )
                    }

                    HorizontalDivider(color = TextMuted.copy(alpha = 0.25f))

                    Text(
                        text = "🙏 \"सेवा ही सबसे बड़ी साधना है\"\nश्री जामनीवाला दरबार परिवार आपका धन्यवाद व्यक्त करता है।",
                        fontSize = 12.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }

            // Share Receipt Button
            Button(
                onClick = { handleShareReceipt() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WarmGold),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF160C05))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isHindi) "दान रसीद शेयर करें" else "Share Receipt",
                    color = Color(0xFF160C05),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    isHighlight: Boolean = false,
    isGold: Boolean = false,
    isBadge: Boolean = false,
    isVerified: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextMuted, fontSize = 12.sp)

        if (isBadge) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isVerified) Color(0xFF2E7D32) else Color(0xFFE65100))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            }
        } else {
            Text(
                text = value,
                fontSize = if (isGold) 16.sp else 13.sp,
                fontWeight = if (isBold || isGold) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isGold -> WarmGold
                    isHighlight -> AccentGolden
                    else -> TextWhite
                }
            )
        }
    }
}
