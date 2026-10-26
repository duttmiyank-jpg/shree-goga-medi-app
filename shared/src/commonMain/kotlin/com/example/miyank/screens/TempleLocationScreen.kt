package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyank.LanguageManager
import com.example.miyank.PanchangProvider
import com.example.miyank.UserQueryManager
import com.example.miyank.openExternalUrl
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TempleLocationScreen(
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val scrollState = rememberScrollState()
    val isHindi = LanguageManager.currentLanguage == com.example.miyank.Language.HINDI

    val locationConfig by PanchangProvider.locationConfig.collectAsState()
    val contactSettings by UserQueryManager.contactSettings.collectAsState()

    val templeAddress = contactSettings.address.ifEmpty { "पांवटा साहिब, जिला सिरमौर, हिमाचल प्रदेश - 173025" }
    val helplinePhone = contactSettings.helplinePhone.ifEmpty { "+91 98765 43210" }
    val cleanPhone = helplinePhone.replace(" ", "").replace("-", "")

    val directionsUrl = "https://www.google.com/maps/dir/?api=1&destination=${locationConfig.latitude},${locationConfig.longitude}"
    val mapsSearchUrl = "https://www.google.com/maps/search/?api=1&query=${locationConfig.latitude},${locationConfig.longitude}"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isHindi) "मंदिर का स्थान" else "Temple Location", fontWeight = FontWeight.ExtraBold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // 1. MAP PREVIEW CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        haptic.playPremiumHaptic()
                        openExternalUrl(mapsSearchUrl)
                    },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .background(BackgroundDark)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(AccentGolden.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = AccentGolden
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = locationConfig.templeName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "${locationConfig.city}, ${locationConfig.state}",
                            fontSize = 13.sp,
                            color = AccentGolden,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            color = AccentGolden,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Map, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isHindi) "गूगल मैप्स पर देखें" else "View on Google Maps",
                                    color = BackgroundDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. GET DIRECTIONS PRIMARY BUTTON
            Button(
                onClick = {
                    haptic.playPremiumHaptic()
                    openExternalUrl(directionsUrl)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Navigation, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHindi) "🗺️ रास्ता देखें (Get Directions)" else "🗺️ Get Directions",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BackgroundDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. TEMPLE ADDRESS CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = AccentGolden, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isHindi) "📍 मुख्य धाम का पता" else "📍 Temple Address",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = AccentGolden
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = templeAddress,
                        fontSize = 14.sp,
                        color = TextWhite,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. HOW TO REACH SECTION (कैसे पहुँचें)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (isHindi) "🚩 मंदिर कैसे पहुँचें? (How to Reach)" else "🚩 How to Reach",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentGolden
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    ReachRow(
                        icon = Icons.Default.Flight,
                        title = if (isHindi) "✈️ हवाई मार्ग (By Air)" else "✈️ By Air",
                        description = if (isHindi) "निकटतम हवाई अड्डा: जौली ग्रांट देहरादून (approx. 65 km) एवं चंडीगढ़ एयरपोर्ट (approx. 100 km)"
                                      else "Nearest Airports: Jolly Grant Dehradun (65 km) & Chandigarh Airport (100 km)"
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = WarmGold.copy(alpha = 0.2f))

                    ReachRow(
                        icon = Icons.Default.Train,
                        title = if (isHindi) "🚆 रेल मार्ग (By Train)" else "🚆 By Train",
                        description = if (isHindi) "निकटतम प्रमुख रेलवे स्टेशन: देहरादून, यमुनानगर-जगाधरी, एवं अंबाला कैंट"
                                      else "Nearest Railway Stations: Dehradun, Yamunanagar-Jagadhri & Ambala Cantt"
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = WarmGold.copy(alpha = 0.2f))

                    ReachRow(
                        icon = Icons.Default.TwoWheeler,
                        title = if (isHindi) "🚌 सड़क मार्ग (By Road)" else "🚌 By Road",
                        description = if (isHindi) "पांवटा साहिब बस स्टैंड से सीधा स्थानीय वाहन / ऑटो / टैक्सी सेवा उपलब्ध है।"
                                      else "Direct local autos and taxis available from Paonta Sahib Bus Stand."
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. HELPLINE CALL CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        haptic.playPremiumHaptic()
                        openExternalUrl("tel:$cleanPhone")
                    },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isHindi) "📞 मंदिर हेल्पलाइन" else "Temple Helpline",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        Text(
                            text = helplinePhone,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = TextWhite
                        )
                    }

                    Surface(
                        color = Color(0xFF4CAF50),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isHindi) "कॉल करें" else "Call",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ReachRow(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(AccentGolden.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = AccentGolden, modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = TextMuted,
                lineHeight = 16.sp
            )
        }
    }
}
