package com.example.miyank.screens.store

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyank.Language
import com.example.miyank.LanguageManager
import com.example.miyank.ShareManager
import com.example.miyank.rememberHapticManager
import com.example.miyank.rememberSuvicharMediaHandler
import com.example.miyank.store.*
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val mediaHandler = rememberSuvicharMediaHandler()
    val isHindi = LanguageManager.currentLanguage == Language.HINDI

    val orders by StoreManager.orders.collectAsState()
    val order = orders.firstOrNull { it.id == orderId } ?: orders.firstOrNull()

    if (order == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Order not found", color = TextWhite)
        }
        return
    }

    val steps = listOf(
        OrderStatus.PLACED,
        OrderStatus.PAYMENT_CONFIRMED,
        OrderStatus.PROCESSING,
        OrderStatus.PACKED,
        OrderStatus.SHIPPED,
        OrderStatus.OUT_FOR_DELIVERY,
        OrderStatus.DELIVERED
    )

    val currentStepOrder = order.orderStatus.stepOrder

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${if (isHindi) "ऑर्डर" else "Order"} #${order.id}",
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
                    IconButton(onClick = {
                        haptic.playPremiumHaptic()
                        val msg = ShareManager.formatGeneralShareMessage(
                            title = "ऑर्डर #${order.id}",
                            description = "ट्रैकिंग नंबर: ${order.trackingNumber.ifBlank { "प्रक्रिया जारी" }}\nस्थिति: ${if (isHindi) order.orderStatus.hindiTitle else order.orderStatus.englishTitle}"
                        )
                        mediaHandler.shareSuvichar("", "", "ऑर्डर #${order.id}", "श्री जामनीवाला दरबार", msg)
                    }) {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tracking Number Card if shipped
            if (order.trackingNumber.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.5.dp, WarmGold),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = WarmGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isHindi) "पोस्ट ऑफिस ट्रैकिंग नंबर" else "India Post Tracking Number",
                                fontWeight = FontWeight.Bold,
                                color = WarmGold,
                                fontSize = 14.sp
                            )
                        }

                        Text(
                            text = order.trackingNumber,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite
                        )

                        Text(
                            text = "${if (isHindi) "सेवा" else "Provider"}: ${order.shippingProvider}",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            // Order Status Timeline Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(0.5.dp, WarmGold.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (isHindi) "ऑर्डर प्रगति (Order Timeline)" else "Order Progress",
                        fontWeight = FontWeight.Bold,
                        color = AccentGolden,
                        fontSize = 15.sp
                    )

                    steps.forEach { step ->
                        val isDone = currentStepOrder >= step.stepOrder && order.orderStatus != OrderStatus.CANCELLED
                        val isCurrent = order.orderStatus == step

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isDone) WarmGold else TextMuted.copy(alpha = 0.3f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isDone) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF160C05), modifier = Modifier.size(14.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = if (isHindi) step.hindiTitle else step.englishTitle,
                                fontSize = 13.sp,
                                fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isDone) TextWhite else TextMuted
                            )
                        }
                    }
                }
            }

            // Order Items Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(0.5.dp, WarmGold.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(if (isHindi) "ऑर्डर सामग्री" else "Order Items", fontWeight = FontWeight.Bold, color = AccentGolden, fontSize = 14.sp)

                    order.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${item.productName} × ${item.quantity}", color = TextWhite, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text("₹${item.subtotal.toInt()}", color = WarmGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = TextMuted.copy(alpha = 0.2f))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isHindi) "सब-टोटल" else "Subtotal", color = TextMuted, fontSize = 12.sp)
                        Text("₹${order.subtotal.toInt()}", color = TextWhite, fontSize = 12.sp)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isHindi) "डिलीवरी शुल्क" else "Shipping Charge", color = TextMuted, fontSize = 12.sp)
                        Text("₹${order.shippingCharge.toInt()}", color = WarmGold, fontSize = 12.sp)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isHindi) "कुल भुगतान" else "Total Amount", color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("₹${order.totalAmount.toInt()}", color = WarmGold, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            // Delivery Address Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(0.5.dp, WarmGold.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(if (isHindi) "डिलीवरी पता" else "Shipping Address", fontWeight = FontWeight.Bold, color = AccentGolden, fontSize = 14.sp)
                    Text(order.customerName, fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 13.sp)
                    Text(order.address.getFormattedAddress(), color = TextMuted, fontSize = 12.sp)
                    Text("📱 ${order.mobile}", color = WarmGold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
