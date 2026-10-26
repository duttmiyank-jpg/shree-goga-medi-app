package com.example.miyank.screens.store

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyank.Language
import com.example.miyank.LanguageManager
import com.example.miyank.rememberHapticManager
import com.example.miyank.rememberPaymentManager
import com.example.miyank.store.*
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onOrderPlacedSuccess: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val paymentManager = rememberPaymentManager()
    val isHindi = LanguageManager.currentLanguage == Language.HINDI

    val cartItems by StoreManager.cart.collectAsState()
    val address by StoreManager.savedAddress.collectAsState()

    val subtotal = cartItems.sumOf { it.subtotal }
    val shippingCharge = StoreManager.calculateShippingCharge(cartItems)
    val totalAmount = subtotal + shippingCharge

    var isProcessing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isHindi) "ऑर्डर समरी एवं भुगतान" else "Order Summary & Payment",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Delivery Address Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(0.5.dp, WarmGold.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = AccentGolden, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isHindi) "डिलीवरी का पता" else "Delivery Address",
                            fontWeight = FontWeight.Bold,
                            color = AccentGolden,
                            fontSize = 14.sp
                        )
                    }

                    Text(
                        text = address.fullName,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        fontSize = 14.sp
                    )
                    Text(
                        text = address.getFormattedAddress(),
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Text(
                        text = "📱 ${address.mobileNumber}",
                        color = WarmGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // 2. Order Items List
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(0.5.dp, WarmGold.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isHindi) "ऑर्डर की वस्तुएँ (${cartItems.size})" else "Order Items (${cartItems.size})",
                        fontWeight = FontWeight.Bold,
                        color = AccentGolden,
                        fontSize = 14.sp
                    )

                    cartItems.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${item.product.name} × ${item.quantity}",
                                color = TextWhite,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "₹${item.subtotal.toInt()}",
                                color = WarmGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 3. Shipping & Payment Method Notice
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(0.5.dp, WarmGold.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = AccentGolden, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isHindi) "शिपिंग एवं भुगतान विधि" else "Shipping & Payment Method",
                            fontWeight = FontWeight.Bold,
                            color = AccentGolden,
                            fontSize = 14.sp
                        )
                    }

                    Text(
                        text = "• ${if (isHindi) "शिपिंग: भारतीय डाक सेवा (Post Office Delivery)" else "Shipping: India Post Delivery"}",
                        color = TextWhite,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• ${if (isHindi) "भुगतान: ऑनलाइन एडवांस पे (UPI/Online Advance Payment)" else "Payment: Online Advance Payment (UPI)"}",
                        color = WarmGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 4. Payment Price Breakdown Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (isHindi) "भुगतान विवरण" else "Payment Breakdown", fontWeight = FontWeight.Bold, color = AccentGolden, fontSize = 14.sp)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isHindi) "सामग्री उप-योग" else "Items Subtotal", color = TextMuted, fontSize = 13.sp)
                        Text("₹${subtotal.toInt()}", color = TextWhite, fontSize = 13.sp)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isHindi) "डाक/डिलीवरी शुल्क" else "Delivery Charge", color = TextMuted, fontSize = 13.sp)
                        Text("₹${shippingCharge.toInt()}", color = WarmGold, fontSize = 13.sp)
                    }

                    HorizontalDivider(color = TextMuted.copy(alpha = 0.2f))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isHindi) "कुल एडवांस भुगतान" else "Total Advance Payment", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("₹${totalAmount.toInt()}", color = WarmGold, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pay Button
            Button(
                onClick = {
                    if (isProcessing) return@Button
                    haptic.playPremiumHaptic()
                    isProcessing = true

                    // 1. Create Pending Order
                    val pendingOrder = StoreManager.createPendingOrder(address, cartItems)

                    // 2. Trigger Payment Manager UPI flow
                    paymentManager.initiateUpiPayment(
                        upiId = "shreejamniwaladarbar@upi",
                        name = "Shree Jamniwala Darbar Store",
                        amount = totalAmount.toInt().toString(),
                        note = "Darbar Store Order ${pendingOrder.id}"
                    )

                    // 3. Confirm order payment and navigate to success
                    val confirmed = StoreManager.confirmOrderPayment(
                        orderId = pendingOrder.id,
                        paymentReference = "UPI_ADV_${(100000..999999).random()}"
                    )

                    onOrderPlacedSuccess(confirmed?.id ?: pendingOrder.id)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WarmGold),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color(0xFF160C05), modifier = Modifier.size(24.dp))
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Payment, contentDescription = null, tint = Color(0xFF160C05))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isHindi) "₹${totalAmount.toInt()} एडवांस भुगतान करें" else "Pay ₹${totalAmount.toInt()} Online Advance",
                            color = Color(0xFF160C05),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
