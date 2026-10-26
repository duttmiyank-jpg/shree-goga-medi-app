package com.example.miyank.screens.donation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.miyank.Language
import com.example.miyank.LanguageManager
import com.example.miyank.donation.*
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationPaymentStatusScreen(
    transactionId: String,
    onViewReceiptClick: (String) -> Unit,
    onMyReceiptsClick: () -> Unit,
    onBackToDonationClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val isHindi = LanguageManager.currentLanguage == Language.HINDI

    val transactions by DonationCauseManager.transactions.collectAsState()
    val txn = transactions.firstOrNull { it.id == transactionId } ?: transactions.firstOrNull()

    var utrInput by remember { mutableStateOf(txn?.utrNumber ?: "") }
    var utrSavedNotice by remember { mutableStateOf(false) }

    if (txn == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Transaction not found", color = TextWhite)
        }
        return
    }

    val isVerified = txn.isVerified()

    Scaffold(
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Status Badge Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(if (isVerified) Color(0xFF2E7D32) else Color(0xFFE65100)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Default.HourglassTop,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isVerified) (if (isHindi) "दान रसीद स्वीकृत" else "Donation Verified")
                else (if (isHindi) "भुगतान सत्यापन लंबित" else "Payment Verification Pending"),
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isVerified) (if (isHindi) "आपका दान मंदिर सेवा हेतु सफलतापूर्वक प्राप्त हो गया है।" else "Your donation has been verified and received with gratitude.")
                else (if (isHindi) "आपका भुगतान अनुरोध दर्ज किया गया है। बैंक/UPI से सत्यापन के बाद रसीद 'स्वीकृत' अपडेट होगी।" else "Your payment request is recorded. Receipt status will update upon bank/UPI verification."),
                fontSize = 13.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Transaction Overview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isHindi) "रसीद नंबर" else "Receipt No", color = TextMuted, fontSize = 12.sp)
                        Text("#${txn.receiptNumber}", color = WarmGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isHindi) "दाता का नाम" else "Donor Name", color = TextMuted, fontSize = 12.sp)
                        Text(txn.donorName, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isHindi) "सेवा क्षेत्र" else "Seva Cause", color = TextMuted, fontSize = 12.sp)
                        Text(txn.sevaCauseName, color = TextWhite, fontSize = 12.sp)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isHindi) "दान राशि" else "Donation Amount", color = TextMuted, fontSize = 12.sp)
                        Text("₹${txn.amount.toInt()}", color = WarmGold, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isHindi) "भुगतान माध्यम" else "Method", color = TextMuted, fontSize = 12.sp)
                        Text(if (isHindi) txn.paymentMethod.hindiTitle else txn.paymentMethod.englishTitle, color = TextWhite, fontSize = 12.sp)
                    }
                }
            }

            // UTR Reference Entry Section if Pending
            if (!isVerified) {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(0.5.dp, WarmGold.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (isHindi) "📌 UTR / बैंक संदर्भ संख्या जोड़ें" else "Enter UTR / Bank Reference",
                            fontWeight = FontWeight.Bold,
                            color = AccentGolden,
                            fontSize = 13.sp
                        )

                        OutlinedTextField(
                            value = utrInput,
                            onValueChange = { utrInput = it },
                            placeholder = { Text("e.g. 12-digit UTR Number") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )

                        Button(
                            onClick = {
                                if (utrInput.isNotBlank()) {
                                    haptic.playPremiumHaptic()
                                    DonationCauseManager.updateUtrNumber(txn.id, utrInput)
                                    utrSavedNotice = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WarmGold),
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (isHindi) "UTR सहेजें" else "Submit UTR Reference", color = Color(0xFF160C05), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        if (utrSavedNotice) {
                            Text(
                                text = if (isHindi) "✓ UTR संदर्भ नंबर सहेजा गया! मंदिर एडमिन शीघ्र सत्यापित करेगा।" else "✓ UTR Reference saved! Admin will verify soon.",
                                color = Color(0xFF81C784),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        haptic.playPremiumHaptic()
                        onViewReceiptClick(txn.id)
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WarmGold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, tint = Color(0xFF160C05))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isHindi) "डिजिटल रसीद देखें" else "View Digital Receipt", color = Color(0xFF160C05), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                }

                OutlinedButton(
                    onClick = {
                        haptic.playPremiumHaptic()
                        onMyReceiptsClick()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    border = BorderStroke(1.dp, WarmGold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isHindi) "मेरी सभी रसीदें" else "My Receipts History", color = WarmGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                TextButton(onClick = {
                    haptic.playPremiumHaptic()
                    onBackToDonationClick()
                }) {
                    Text(if (isHindi) "मुख्य दान पेज पर जाएँ" else "Back to Donation Page", color = TextMuted, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
