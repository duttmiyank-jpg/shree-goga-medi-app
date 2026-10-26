package com.example.miyank.screens.store

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
fun OrderSuccessScreen(
    orderId: String,
    onViewMyOrdersClick: () -> Unit,
    onBackToStoreClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val mediaHandler = rememberSuvicharMediaHandler()
    val isHindi = LanguageManager.currentLanguage == Language.HINDI

    val orders by StoreManager.orders.collectAsState()
    val order = orders.firstOrNull { it.id == orderId } ?: orders.firstOrNull()

    val shareMsg = ShareManager.formatGeneralShareMessage(
        title = "ऑर्डर पुष्टि: ${order?.id ?: orderId}",
        description = "श्री जामनीवाला दरबार स्टोर से आपका ऑर्डर सफलतापूर्वक दर्ज कर लिया गया है।\nकुल राशि: ₹${order?.totalAmount?.toInt() ?: 0}"
    )

    fun handleShareOrder() {
        haptic.playPremiumHaptic()
        mediaHandler.shareSuvichar(
            imageUrlOrPath = "",
            dateStr = "",
            quoteText = "ऑर्डर #$orderId - श्री जामनीवाला दरबार",
            author = "श्री जामनीवाला दरबार स्टोर",
            shareText = shareMsg
        )
    }

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

            // Celebration Badge
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(GoldenGradientStart, GoldenGradientEnd))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF160C05),
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (isHindi) "🙏 जय श्री गोगा जी महाराज" else "🙏 Jai Shree Goga Ji Maharaj",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = AccentGolden
            )

            Text(
                text = if (isHindi) "ऑर्डर सफलतापूर्वक दर्ज हुआ!" else "Order Placed Successfully!",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(WarmGold.copy(alpha = 0.2f))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${if (isHindi) "ऑर्डर आईडी" else "Order ID"}: #${order?.id ?: orderId}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmGold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Order Confirmation Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isHindi) "भुगतान स्थिति" else "Payment Status", color = TextMuted, fontSize = 13.sp)
                        Text(if (isHindi) "✅ एडवांस भुगतान पूर्ण" else "✅ Paid Online", color = Color(0xFF81C784), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isHindi) "कुल भुगतान राशि" else "Total Amount Paid", color = TextMuted, fontSize = 13.sp)
                        Text("₹${order?.totalAmount?.toInt() ?: 0}", color = WarmGold, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isHindi) "डिलिवरी विधि" else "Shipping Method", color = TextMuted, fontSize = 13.sp)
                        Text("भारतीय डाक सेवा (India Post)", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }

                    HorizontalDivider(color = TextMuted.copy(alpha = 0.2f))

                    Text(
                        text = if (isHindi) "📦 आपका पार्सल तैयार करके पोस्ट ऑफिस के माध्यम से भेजा जाएगा। ट्रैकिंग नंबर शीघ्र उपलब्ध होगा।" else "📦 Your package will be packed and shipped via India Post.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        haptic.playPremiumHaptic()
                        onViewMyOrdersClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WarmGold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color(0xFF160C05))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHindi) "मेरे ऑर्डर देखें (Track Order)" else "View My Orders",
                        color = Color(0xFF160C05),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }

                OutlinedButton(
                    onClick = {
                        haptic.playPremiumHaptic()
                        onBackToStoreClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    border = BorderStroke(1.dp, WarmGold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isHindi) "दरबार स्टोर पर वापस जाएँ" else "Back to Store",
                        color = WarmGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                TextButton(onClick = { handleShareOrder() }) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = WarmGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isHindi) "ऑर्डर रसीद शेयर करें" else "Share Receipt", color = WarmGold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
