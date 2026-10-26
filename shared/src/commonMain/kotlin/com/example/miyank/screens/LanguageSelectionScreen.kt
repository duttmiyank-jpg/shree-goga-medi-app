package com.example.miyank.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyank.Language
import com.example.miyank.LanguageManager
import com.example.miyank.LocalStrings
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*

@Composable
fun LanguageSelectionScreen(
    onContinue: () -> Unit
) {
    val haptic = rememberHapticManager()
    val strings = LocalStrings.current

    // Default language is English
    var selectedLang by remember { mutableStateOf(Language.ENGLISH) }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DeepMidnight, DarkBrown)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Temple Icon Header
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(WarmGold.copy(alpha = 0.25f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🛕",
                    fontSize = 54.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Screen Titles
            Text(
                text = strings.chooseLanguageTitle,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = LuxuryGold,
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = strings.chooseLanguageSubtitle,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = TextMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Language Option 1: English
            LanguageOptionCard(
                title = strings.langEnglishName,
                flag = "🇬🇧",
                subTitle = "English",
                isSelected = selectedLang == Language.ENGLISH,
                onClick = {
                    haptic.playPremiumHaptic()
                    selectedLang = Language.ENGLISH
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Language Option 2: Hindi
            LanguageOptionCard(
                title = strings.langHindiName,
                flag = "🇮🇳",
                subTitle = "हिन्दी",
                isSelected = selectedLang == Language.HINDI,
                onClick = {
                    haptic.playPremiumHaptic()
                    selectedLang = Language.HINDI
                }
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(40.dp))

            // Continue Button
            Button(
                onClick = {
                    haptic.playPremiumHaptic()
                    LanguageManager.selectLanguage(selectedLang)
                    onContinue()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentGolden,
                    contentColor = BackgroundDark
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Text(
                    text = strings.continueText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BackgroundDark,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LanguageOptionCard(
    title: String,
    flag: String,
    subTitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) AccentGolden else SurfaceDark,
        animationSpec = tween(300), label = "border"
    )

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) SurfaceDark else SurfaceDark.copy(alpha = 0.6f),
        animationSpec = tween(300), label = "container"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = tween(300), label = "scale"
    )

    val checkScale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(250), label = "checkScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        elevation = CardDefaults.cardElevation(if (isSelected) 6.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flag Icon Container
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) AccentGolden.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = flag,
                    fontSize = 28.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Titles
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) TextWhite else TextMuted
                )
                if (subTitle != title) {
                    Text(
                        text = subTitle,
                        fontSize = 13.sp,
                        color = TextMuted.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Selection Indicator
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) AccentGolden else Color.Transparent
                    )
                    .scale(checkScale),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = BackgroundDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
