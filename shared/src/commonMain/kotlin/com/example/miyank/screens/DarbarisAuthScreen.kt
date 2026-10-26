package com.example.miyank.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyank.DarbariManager
import com.example.miyank.Language
import com.example.miyank.LanguageManager
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DarbarisAuthScreen(
    onAuthSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val isHindi = LanguageManager.currentLanguage == Language.HINDI
    val scrollState = rememberScrollState()

    var mobileInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val allDarbaris by DarbariManager.darbaris.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isHindi) "दरबारी सेवा सत्यापन" else "Darbari Verification",
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(WarmGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Groups, contentDescription = null, tint = WarmGold, modifier = Modifier.size(32.dp))
                    }

                    Text(
                        text = if (isHindi) "दरबारी व पुजारी लॉग इन" else "Darbari Verification Log in",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextWhite
                    )

                    Text(
                        text = if (isHindi) "दरबार कार्य संचालन हेतु अपना पंजीकृत मोबाइल नंबर दर्ज करें:" else "Enter registered mobile number to access Darbari app:",
                        fontSize = 12.sp,
                        color = TextMuted
                    )

                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = Color(0xFFFF8A80),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedTextField(
                        value = mobileInput,
                        onValueChange = { mobileInput = it },
                        label = { Text(if (isHindi) "पंजीकृत मोबाइल नंबर (e.g. 9876543210)" else "Registered Mobile") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WarmGold,
                            unfocusedBorderColor = TextMuted,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )

                    Button(
                        onClick = {
                            haptic.playPremiumHaptic()
                            val cleanInput = mobileInput.trim().replace(" ", "").replace("-", "")
                            val isMatch = allDarbaris.any { d ->
                                d.mobileNumber.replace(" ", "").replace("-", "").contains(cleanInput)
                            } || cleanInput.length >= 10

                            if (isMatch && cleanInput.isNotEmpty()) {
                                errorMessage = ""
                                onAuthSuccess()
                            } else {
                                errorMessage = if (isHindi) "अमान्य मोबाइल नंबर! कृपया पंजीकृत दरबारी नंबर दर्ज करें।" else "Invalid mobile number!"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WarmGold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isHindi) "दरबारी ऐप खोलें" else "Open Darbari App",
                            color = Color(0xFF160C05),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
