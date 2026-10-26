package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
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
import com.example.miyank.SponsoredItem
import com.example.miyank.SponsoredManager
import com.example.miyank.openExternalUrl
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SponsoredScreen(
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val isHindi = LanguageManager.currentLanguage == Language.HINDI

    val allPromotions by SponsoredManager.sponsoredItems.collectAsState()
    val activePromotions = remember(allPromotions) {
        allPromotions.filter { it.isActive }.sortedBy { it.priority }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isHindi) "स्पॉन्सर प्रमोशन" else "Sponsored Businesses",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite
                        )
                        Text(
                            text = if (isHindi) "स्थानीय व्यापार एवं सेवाएँ" else "Local Business & Services",
                            fontSize = 11.sp,
                            color = WarmGold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark,
                    titleContentColor = TextWhite,
                    navigationIconContentColor = TextWhite
                )
            )
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (activePromotions.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(WarmGold.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = WarmGold, modifier = Modifier.size(36.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isHindi) "फिलहाल कोई स्पॉन्सर प्रमोशन उपलब्ध नहीं है।" else "No sponsored promotions available right now.",
                        color = TextWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isHindi) "व्यापार प्रचार हेतु मुख्य मंदिर कार्यालय से संपर्क करें।" else "Contact temple office for business advertising.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(activePromotions) { item ->
                        SponsoredCardUser(
                            item = item,
                            isHindi = isHindi,
                            haptic = haptic
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SponsoredCardUser(
    item: SponsoredItem,
    isHindi: Boolean,
    haptic: com.example.miyank.HapticManager
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.35f))
    ) {
        Column {
            if (item.imageUrl.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Surface(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(bottomStart = 8.dp),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = if (isHindi) "स्पॉन्सर" else "SPONSORED",
                            color = WarmGold,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextWhite,
                        modifier = Modifier.weight(1f)
                    )

                    if (item.offerText.isNotEmpty()) {
                        Surface(
                            color = WarmGold,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = item.offerText,
                                color = Color(0xFF160C05),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                if (item.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.description,
                        fontSize = 12.sp,
                        color = TextMuted,
                        lineHeight = 16.sp
                    )
                }

                if (item.address.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = WarmGold, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = item.address, fontSize = 11.sp, color = TextWhite)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons: Call, WhatsApp, Website
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (item.phone.isNotEmpty()) {
                        OutlinedButton(
                            onClick = {
                                haptic.playPremiumHaptic()
                                val cleanPhone = item.phone.replace(" ", "").replace("-", "")
                                openExternalUrl("tel:$cleanPhone")
                            },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, tint = WarmGold, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isHindi) "कॉल करें" else "Call", color = WarmGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (item.whatsapp.isNotEmpty()) {
                        Button(
                            onClick = {
                                haptic.playPremiumHaptic()
                                val cleanWa = item.whatsapp.replace(" ", "").replace("-", "").replace("+", "")
                                openExternalUrl("https://wa.me/$cleanWa")
                            },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WhatsApp", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (item.websiteUrl.isNotEmpty()) {
                        OutlinedButton(
                            onClick = {
                                haptic.playPremiumHaptic()
                                openExternalUrl(item.websiteUrl)
                            },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, WarmGold)
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = WarmGold, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isHindi) "वेबसाइट" else "Website", color = WarmGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
