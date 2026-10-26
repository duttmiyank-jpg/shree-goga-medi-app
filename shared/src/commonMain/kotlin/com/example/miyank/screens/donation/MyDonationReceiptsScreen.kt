package com.example.miyank.screens.donation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyank.Language
import com.example.miyank.LanguageManager
import com.example.miyank.donation.*
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDonationReceiptsScreen(
    onReceiptClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val isHindi = LanguageManager.currentLanguage == Language.HINDI

    val transactions by DonationCauseManager.transactions.collectAsState()
    var selectedStatusFilter by remember { mutableStateOf<DonationStatus?>(null) }

    val filteredTransactions = remember(transactions, selectedStatusFilter) {
        if (selectedStatusFilter != null) {
            transactions.filter { it.paymentStatus == selectedStatusFilter }
        } else transactions
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isHindi) "मेरी दान रसीदें" else "My Donation Receipts",
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
        containerColor = BackgroundDark
    ) { paddingValues ->
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📜", fontSize = 52.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isHindi) "अभी कोई दान रसीद उपलब्ध नहीं है।" else "No donation receipts yet.",
                        color = TextMuted,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedStatusFilter == null,
                        onClick = { selectedStatusFilter = null },
                        label = { Text(if (isHindi) "सभी रसीदें" else "All Receipts") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = WarmGold, selectedLabelColor = Color(0xFF160C05), containerColor = SurfaceDark, labelColor = TextWhite)
                    )

                    FilterChip(
                        selected = selectedStatusFilter == DonationStatus.SUCCESS,
                        onClick = { selectedStatusFilter = DonationStatus.SUCCESS },
                        label = { Text(if (isHindi) "सत्यापित (Verified)" else "Verified") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = WarmGold, selectedLabelColor = Color(0xFF160C05), containerColor = SurfaceDark, labelColor = TextWhite)
                    )

                    FilterChip(
                        selected = selectedStatusFilter == DonationStatus.PENDING,
                        onClick = { selectedStatusFilter = DonationStatus.PENDING },
                        label = { Text(if (isHindi) "सत्यापन लंबित" else "Pending") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = WarmGold, selectedLabelColor = Color(0xFF160C05), containerColor = SurfaceDark, labelColor = TextWhite)
                    )
                }

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredTransactions) { txn ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptic.playPremiumHaptic()
                                    onReceiptClick(txn.id)
                                },
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            border = BorderStroke(0.5.dp, WarmGold.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("#${txn.receiptNumber}", fontWeight = FontWeight.ExtraBold, color = WarmGold, fontSize = 14.sp)

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (txn.isVerified()) Color(0xFF2E7D32) else Color(0xFFE65100))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isHindi) txn.paymentStatus.hindiTitle else txn.paymentStatus.englishTitle,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite
                                        )
                                    }
                                }

                                Text("सेवा: ${txn.sevaCauseName}", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("दाता: ${txn.donorName} (${txn.getMaskedMobile()})", color = TextMuted, fontSize = 12.sp)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("राशि: ₹${txn.amount.toInt()}", color = WarmGold, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)

                                    TextButton(onClick = {
                                        haptic.playPremiumHaptic()
                                        onReceiptClick(txn.id)
                                    }) {
                                        Text(if (isHindi) "रसीद देखें →" else "View Receipt →", color = WarmGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
