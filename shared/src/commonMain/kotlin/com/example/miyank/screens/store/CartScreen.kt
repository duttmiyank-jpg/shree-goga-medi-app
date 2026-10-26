package com.example.miyank.screens.store

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.miyank.Language
import com.example.miyank.LanguageManager
import com.example.miyank.rememberHapticManager
import com.example.miyank.store.*
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onProceedToAddressClick: () -> Unit,
    onContinueShoppingClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val isHindi = LanguageManager.currentLanguage == Language.HINDI

    val cartItems by StoreManager.cart.collectAsState()
    val subtotal = cartItems.sumOf { it.subtotal }
    val totalWeightGrams = cartItems.sumOf { it.totalWeightGrams }
    val shippingCharge = StoreManager.calculateShippingCharge(cartItems)
    val grandTotal = subtotal + shippingCharge

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isHindi) "आपका कार्ट" else "Your Cart",
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
                    if (cartItems.isNotEmpty()) {
                        TextButton(onClick = {
                            haptic.playPremiumHaptic()
                            StoreManager.clearCart()
                        }) {
                            Text(if (isHindi) "खाली करें" else "Clear", color = Color(0xFFE57373), fontSize = 13.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark
                )
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    color = SurfaceDark,
                    tonalElevation = 8.dp,
                    border = BorderStroke(0.5.dp, TextMuted.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isHindi) "कुल राशि (Grand Total):" else "Grand Total:",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                                Text(
                                    text = "₹${grandTotal.toInt()}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = WarmGold
                                )
                            }

                            Button(
                                onClick = {
                                    haptic.playPremiumHaptic()
                                    onProceedToAddressClick()
                                },
                                modifier = Modifier
                                    .height(48.dp)
                                    .width(180.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = WarmGold),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (isHindi) "आगे बढ़ें (Address)" else "Proceed",
                                    color = Color(0xFF160C05),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("🛒", fontSize = 56.sp)
                    Text(
                        text = if (isHindi) "आपका कार्ट अभी खाली है" else "Your cart is empty",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Button(
                        onClick = {
                            haptic.playPremiumHaptic()
                            onContinueShoppingClick()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WarmGold)
                    ) {
                        Text(if (isHindi) "दरबार स्टोर देखें" else "Browse Store", color = Color(0xFF160C05), fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cart Items List
                cartItems.forEach { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = BorderStroke(0.5.dp, WarmGold.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1A120B))
                            ) {
                                AsyncImage(
                                    model = item.product.getMainImage(),
                                    contentDescription = item.product.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isHindi) item.product.name else item.product.nameEnglish,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    maxLines = 1
                                )
                                Text(
                                    text = "₹${item.product.price.toInt()} × ${item.quantity} = ₹${item.subtotal.toInt()}",
                                    fontSize = 13.sp,
                                    color = WarmGold,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Quantity Controls
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BackgroundDark)
                                ) {
                                    IconButton(
                                        onClick = {
                                            haptic.playPremiumHaptic()
                                            StoreManager.updateCartQuantity(item.product.id, item.quantity - 1)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Text("-", color = WarmGold, fontWeight = FontWeight.Bold)
                                    }

                                    Text(
                                        text = "${item.quantity}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )

                                    IconButton(
                                        onClick = {
                                            haptic.playPremiumHaptic()
                                            StoreManager.updateCartQuantity(item.product.id, item.quantity + 1)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Text("+", color = WarmGold, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            IconButton(onClick = {
                                haptic.playPremiumHaptic()
                                StoreManager.removeFromCart(item.product.id)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color(0xFFE57373), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                // Order Price & Delivery Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(0.5.dp, WarmGold.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (isHindi) "मूल्य एवं डिलीवरी विवरण" else "Price & Delivery Details",
                            fontWeight = FontWeight.Bold,
                            color = AccentGolden,
                            fontSize = 15.sp
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(if (isHindi) "सामग्री उप-योग (Subtotal)" else "Subtotal", color = TextMuted, fontSize = 13.sp)
                            Text("₹${subtotal.toInt()}", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = if (isHindi) "डाक/डिलीवरी शुल्क (${totalWeightGrams}g)" else "Postage/Delivery (${totalWeightGrams}g)",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                            Text("₹${shippingCharge.toInt()}", color = WarmGold, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }

                        HorizontalDivider(color = TextMuted.copy(alpha = 0.2f))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(if (isHindi) "कुल देय राशि" else "Total Payable", color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("₹${grandTotal.toInt()}", color = WarmGold, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                // Shipping Note
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF231A14))
                        .padding(12.dp)
                ) {
                    Text(
                        text = if (isHindi) "📦 आपका ऑर्डर भारतीय डाक सेवा (Post Office) द्वारा भेजा जाएगा। ट्रैक नंबर ऑर्डर प्रेषित होने पर उपलब्ध होगा।" else "📦 Orders are shipped via India Post Office delivery with tracking.",
                        fontSize = 11.sp,
                        color = TextMuted,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
