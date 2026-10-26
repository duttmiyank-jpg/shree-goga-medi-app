package com.example.miyank.screens

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
import com.example.miyank.LanguageManager
import com.example.miyank.Language
import com.example.miyank.ShareManager
import com.example.miyank.astrology.*
import com.example.miyank.rememberHapticManager
import com.example.miyank.rememberSuvicharMediaHandler
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RashiResultScreen(
    onEditDetailsClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val mediaHandler = rememberSuvicharMediaHandler()
    val isHindi = LanguageManager.currentLanguage == Language.HINDI

    val resultState by RashiManager.calculationResult.collectAsState()
    val result = resultState ?: RashiCalculationResult()
    val rashi = result.rashi
    val guidance = RashiGuidanceRepository.getGuidanceForRashi(rashi)
    val details = result.birthDetails

    val scrollState = rememberScrollState()

    val rawShareMsg = if (isHindi) {
        "🔱 मेरी चंद्र राशि: ${rashi.symbol} ${rashi.hindiName} (${rashi.englishName})\n" +
                "स्वामी ग्रह: ${guidance.rulingPlanet}\n" +
                "शुभ रंग: ${guidance.preferredColorsHindi.joinToString(", ")}\n" +
                "मंत्र: ${guidance.mantraDevanagari}"
    } else {
        "🔱 My Chandra Rashi: ${rashi.symbol} ${rashi.englishName}\n" +
                "Ruling Planet: ${rashi.rulingPlanetEnglish}\n" +
                "Preferred Colors: ${guidance.preferredColorsEnglish.joinToString(", ")}\n" +
                "Mantra: ${guidance.mantraTransliteration}"
    }

    val shareText = ShareManager.formatGeneralShareMessage(
        title = if (isHindi) "मेरी जन्म चंद्र राशि" else "My Birth Chandra Rashi",
        description = rawShareMsg
    )

    fun handleShare() {
        haptic.playPremiumHaptic()
        mediaHandler.shareSuvichar(
            imageUrlOrPath = "",
            dateStr = "",
            quoteText = if (isHindi) "मेरी राशि: ${rashi.hindiName}" else "My Rashi: ${rashi.englishName}",
            author = "श्री जामनीवाला दरबार",
            shareText = shareText
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isHindi) "आपकी चंद्र राशि" else "Your Chandra Rashi",
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
                    IconButton(onClick = { handleShare() }) {
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
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Main Hero Rashi Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(GoldenGradientStart, WarmGold))),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Symbol Badge
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(GoldenGradientStart, GoldenGradientEnd))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = rashi.symbol,
                            fontSize = 38.sp,
                            color = Color(0xFF160C05)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isHindi) "आपकी जन्म चंद्र राशि" else "Your Birth Chandra Rashi",
                        fontSize = 13.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = if (isHindi) "${rashi.hindiName} (${rashi.englishName})" else "${rashi.englishName} (${rashi.hindiName})",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextWhite,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isHindi) "स्वामी ग्रह: ${guidance.rulingPlanet} • तत्व: ${rashi.elementHindi}"
                        else "Ruling Planet: ${rashi.rulingPlanetEnglish} • Element: ${rashi.elementHindi}",
                        fontSize = 13.sp,
                        color = WarmGold,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = TextMuted.copy(alpha = 0.2f))

                    Spacer(modifier = Modifier.height(12.dp))

                    // Calculation system tag
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isHindi) "गणना पद्धति: ${result.calculationSystem}" else "System: Sidereal / Nirayana (Lahiri Ayanamsha)",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    // Confidence Notice if Birth Time was unknown
                    if (result.status == CalculationConfidenceStatus.TIME_UNKNOWN_SAME_RASHI) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF2E7D32).copy(alpha = 0.2f))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = if (isHindi) "✅ जन्म समय उपलब्ध नहीं था, परन्तु उस पूरे दिन चंद्र राशि समान है।" else "✅ Birth time was unknown, but Moon remained in this Rashi throughout the day.",
                                fontSize = 11.sp,
                                color = Color(0xFFA5D6A7),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else if (result.status == CalculationConfidenceStatus.TIME_UNKNOWN_BOUNDARY_CROSSED) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE65100).copy(alpha = 0.2f))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = if (isHindi) "⚠️ उस तिथि को चंद्र राशि बदलती है। सटीक चंद्र राशि हेतु जन्म समय दर्ज करें।" else "⚠️ Moon crosses Rashi boundary on that date. Exact birth time is required for 100% certainty.",
                                fontSize = 11.sp,
                                color = Color(0xFFFFCC80),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Birth Profile Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                            text = if (isHindi) "जन्म विवरण" else "Birth Profile",
                            fontWeight = FontWeight.Bold,
                            color = AccentGolden,
                            fontSize = 14.sp
                        )
                        TextButton(
                            onClick = {
                                haptic.playPremiumHaptic()
                                onEditDetailsClick()
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = WarmGold, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isHindi) "विवरण बदलें" else "Edit Details", color = WarmGold, fontSize = 12.sp)
                        }
                    }

                    Text(
                        text = "${if (isHindi) "नाम" else "Name"}: ${details.userName}",
                        color = TextWhite,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "${if (isHindi) "जन्म तिथि" else "Birth Date"}: ${details.getFormattedBirthDate()} ${if (details.isTimeUnknown) "(समय ज्ञात नहीं)" else "• ${details.birthTimeStr}"}",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${if (isHindi) "जन्म स्थान" else "Birth Place"}: ${details.getFormattedLocation()}",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            // Section Header: Traditional Guidance
            Text(
                text = if (isHindi) "धार्मिक एवं परंपरागत मार्गदर्शन" else "Traditional Devotional Guidance",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AccentGolden
            )

            // 1. Preferred Colors Card
            GuidanceCard(
                icon = "🎨",
                title = if (isHindi) "आज का शुभ/अनुकूल रंग" else "Preferred Colors",
                items = if (isHindi) guidance.preferredColorsHindi else guidance.preferredColorsEnglish,
                note = if (isHindi) "परंपरागत मान्यता के अनुसार इन रंगों को प्राथमिकता दी जा सकती है।" else "Traditional preference."
            )

            // 2. Donation Suggestions Card
            GuidanceCard(
                icon = "🌾",
                title = if (isHindi) "क्या दान कर सकते हैं?" else "Donation Suggestions",
                items = if (isHindi) guidance.donationSuggestionsHindi else guidance.donationSuggestionsEnglish,
                note = if (isHindi) "अपनी श्रद्धा और सामर्थ्य अनुसार व्यावहारिक सात्विक दान करें।" else "Practical voluntary donation according to your faith."
            )

            // 3. Auspicious Actions Card
            GuidanceCard(
                icon = "✨",
                title = if (isHindi) "शुभ एवं हितकारी कार्य" else "Auspicious Actions",
                items = if (isHindi) guidance.goodActionsHindi else guidance.goodActionsEnglish,
                note = if (isHindi) "सद्भाव, सेवा व धार्मिक स्मरण हेतु सुझाव।" else "Devotional and righteous suggestions."
            )

            // 4. Cautionary Advice Card
            GuidanceCard(
                icon = "🛡️",
                title = if (isHindi) "किन बातों से बचें" else "Things to Avoid",
                items = if (isHindi) guidance.avoidActionsHindi else guidance.avoidActionsEnglish,
                note = if (isHindi) "शांत, संयमित एवं धर्मनिष्ठ आचरण रखें।" else "Maintain peaceful and balanced conduct."
            )

            // 5. Mantra & Devotional Practice Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🕉️ ", fontSize = 20.sp)
                        Text(
                            text = if (isHindi) "मंत्र एवं धार्मिक स्मरण" else "Mantra & Practice",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarmGold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2A1C12))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = guidance.mantraDevanagari,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "(${guidance.mantraTransliteration})",
                                fontSize = 12.sp,
                                color = WarmGold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Text(
                        text = if (isHindi) guidance.devotionalPracticeHindi else guidance.devotionalPracticeEnglish,
                        fontSize = 13.sp,
                        color = TextWhite,
                        lineHeight = 18.sp
                    )
                }
            }

            // 6. Seva Suggestions Card
            GuidanceCard(
                icon = "🛕",
                title = if (isHindi) "धार्मिक सेवा गतिविधि" else "Seva Suggestions",
                items = if (isHindi) guidance.sevaSuggestionsHindi else guidance.sevaSuggestionsEnglish,
                note = if (isHindi) "मंदिर एवं समाज कल्याण सेवा में योगदान।" else "Temple and community service."
            )

            // Disclaimer Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF231A14))
                    .border(BorderStroke(0.5.dp, WarmGold.copy(alpha = 0.3f)), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = if (isHindi) "📌 महत्वपूर्ण धार्मिक सूचना" else "📌 Important Devotional Note",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarmGold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isHindi) guidance.disclaimerHindi else guidance.disclaimerEnglish,
                        fontSize = 11.sp,
                        color = TextMuted,
                        lineHeight = 16.sp
                    )
                }
            }

            // Bottom Buttons Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        haptic.playPremiumHaptic()
                        onEditDetailsClick()
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    border = BorderStroke(1.dp, WarmGold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isHindi) "विवरण बदलें" else "Edit Details",
                        color = WarmGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Button(
                    onClick = { handleShare() },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WarmGold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF160C05), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isHindi) "शेयर करें" else "Share",
                        color = Color(0xFF160C05),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun GuidanceCard(
    icon: String,
    title: String,
    items: List<String>,
    note: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(0.5.dp, WarmGold.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$icon ", fontSize = 20.sp)
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmGold
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items.forEach { item ->
                    Row(verticalAlignment = Alignment.Top) {
                        Text("• ", color = AccentGolden, fontWeight = FontWeight.Bold)
                        Text(
                            text = item,
                            fontSize = 13.sp,
                            color = TextWhite,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            if (note.isNotEmpty()) {
                Text(
                    text = note,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}
