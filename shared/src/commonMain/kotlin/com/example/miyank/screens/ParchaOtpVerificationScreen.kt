package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyank.HapticManager
import com.example.miyank.Language
import com.example.miyank.LanguageManager
import com.example.miyank.LocalStrings
import com.example.miyank.ParchaManager
import com.example.miyank.UserProfileManager
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParchaOtpVerificationScreen(
    initialMobileNumber: String = "",
    onVerificationSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val strings = LocalStrings.current
    val isHindi = LanguageManager.currentLanguage == Language.HINDI

    val userProfile by UserProfileManager.userProfile.collectAsState()

    var mobileInput by remember {
        mutableStateOf(
            initialMobileNumber.ifEmpty { userProfile.mobileNumber.ifEmpty { "9876543210" } }
        )
    }
    var otpInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var successNotice by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.parchaVerificationTitle,
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
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhite
                        )
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
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Lock Icon
                    Surface(
                        color = WarmGold.copy(alpha = 0.15f),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f)),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = WarmGold,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Text(
                        text = strings.parchaVerificationTitle,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextWhite,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = strings.parchaVerificationSubtitle,
                        fontSize = 13.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    if (errorMessage.isNotEmpty()) {
                        Surface(
                            color = Color.Red.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage,
                                color = Color(0xFFFF8A80),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(10.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    if (successNotice.isNotEmpty()) {
                        Surface(
                            color = Color(0xFF1B5E20).copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF81C784)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = successNotice,
                                color = Color(0xFF81C784),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(10.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Registered Mobile Number Input
                    OutlinedTextField(
                        value = mobileInput,
                        onValueChange = {
                            if (it.length <= 13) {
                                mobileInput = it
                                errorMessage = ""
                            }
                        },
                        label = { Text(strings.enterMobileNumberLabel) },
                        leadingIcon = {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = AccentGolden)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WarmGold,
                            unfocusedBorderColor = TextMuted.copy(alpha = 0.5f),
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // OTP Input
                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = {
                            if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                otpInput = it
                                errorMessage = ""
                            }
                        },
                        label = { Text(strings.enterOtpLabel) },
                        placeholder = { Text("e.g. 123456") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WarmGold,
                            unfocusedBorderColor = TextMuted.copy(alpha = 0.5f),
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Verify Button
                    Button(
                        onClick = {
                            haptic.playPremiumHaptic()
                            val success = ParchaManager.verifyOtp(mobileInput, otpInput)
                            if (success) {
                                errorMessage = ""
                                onVerificationSuccess()
                            } else {
                                errorMessage = strings.invalidOtpError
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WarmGold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = strings.verifyOtpButton,
                            color = BackgroundDark,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }

                    // Resend OTP Button
                    TextButton(onClick = {
                        haptic.playPremiumHaptic()
                        otpInput = ""
                        errorMessage = ""
                        successNotice = if (isHindi) "✓ नया OTP आपके नंबर पर भेजा गया।" else "✓ New OTP sent to your registered number."
                    }) {
                        Text(
                            text = strings.resendOtpButton,
                            color = AccentGolden,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
