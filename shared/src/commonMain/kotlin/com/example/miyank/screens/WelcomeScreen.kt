package com.example.miyank.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyank.LocalStrings
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*

@Composable
fun WelcomeScreen(
    onContinue: () -> Unit
) {
    val haptic = rememberHapticManager()
    val scrollState = rememberScrollState()
    val strings = LocalStrings.current

    Scaffold(
        bottomBar = {
            // Static Bottom Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundDark)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        haptic.playPremiumHaptic()
                        onContinue()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGolden),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Text(
                        text = strings.continueToDarbar,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = BackgroundDark
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = strings.termsPrivacy,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    color = TextMuted
                )
            }
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DeepMidnight, BackgroundDark)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Decorative Icon
                Text(
                    text = "🛕",
                    fontSize = 70.sp,
                    modifier = Modifier.padding(top = 40.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Main Title in Hindi
                Text(
                    text = strings.welcomeTitle,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = LuxuryGold,
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // History Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = strings.history1,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextWhite.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = strings.history2,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextWhite.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = strings.history3,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextWhite.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp,
                        fontStyle = FontStyle.Italic
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Footer Devotional Text
                Column(
                    modifier = Modifier.padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = strings.footer1,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaffronAccent,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = strings.footer2,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaffronAccent,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = strings.footer3,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WarmGold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
}
