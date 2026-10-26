package com.example.miyank.screens.store

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
import com.example.miyank.rememberHapticManager
import com.example.miyank.store.*
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOrdersScreen(
    onOrderClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val isHindi = LanguageManager.currentLanguage == Language.HINDI

    val orders by StoreManager.orders.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isHindi) "मेरे ऑर्डर इतिहास" else "My Orders",
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
        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📦", fontSize = 52.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isHindi) "अभी तक कोई ऑर्डर नहीं है" else "No orders placed yet",
                        color = TextMuted,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders) { order ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                haptic.playPremiumHaptic()
                                onOrderClick(order.id)
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
                                Text(
                                    text = "#${order.id}",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = WarmGold,
                                    fontSize = 15.sp
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when (order.orderStatus) {
                                                OrderStatus.DELIVERED -> Color(0xFF2E7D32)
                                                OrderStatus.SHIPPED -> Color(0xFF1565C0)
                                                OrderStatus.CANCELLED -> Color(0xFFC62828)
                                                else -> AccentGolden.copy(alpha = 0.3f)
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (isHindi) order.orderStatus.hindiTitle else order.orderStatus.englishTitle,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                }
                            }

                            Text(
                                text = "${if (isHindi) "वस्तुएँ" else "Items"}: ${order.items.joinToString(", ") { it.productName }}",
                                fontSize = 13.sp,
                                color = TextWhite,
                                maxLines = 2
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "कुल: ₹${order.totalAmount.toInt()}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarmGold
                                )

                                if (order.trackingNumber.isNotBlank()) {
                                    Text(
                                        text = "📍 ${order.trackingNumber}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF81C784),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
