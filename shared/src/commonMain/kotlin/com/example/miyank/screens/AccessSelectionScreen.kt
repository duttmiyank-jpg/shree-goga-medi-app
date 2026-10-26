package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyank.AdminSecurityManager
import com.example.miyank.DarbariManager
import com.example.miyank.Language
import com.example.miyank.LanguageManager
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.components.OfficialGoogleLogo
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessSelectionScreen(
    onSelectUser: () -> Unit,
    onSelectDarbaris: () -> Unit,
    onSelectAdmin: () -> Unit
) {
    val haptic = rememberHapticManager()
    val isHindi = LanguageManager.currentLanguage == Language.HINDI
    val scrollState = rememberScrollState()

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: User, 1: Darbaris, 2: Admin

    // Captcha Generator State
    var num1 by remember { mutableIntStateOf((3..9).random()) }
    var num2 by remember { mutableIntStateOf((2..8).random()) }
    var captchaAnswerInput by remember { mutableStateOf("") }

    fun refreshCaptcha() {
        num1 = (3..9).random()
        num2 = (2..8).random()
        captchaAnswerInput = ""
    }

    // Input States
    var darbariIdInput by remember { mutableStateOf("") }
    var darbariPasswordInput by remember { mutableStateOf("") }

    var adminIdInput by remember { mutableStateOf("") }
    var adminPinInput by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    val allDarbaris by DarbariManager.darbaris.collectAsState()

    Scaffold(
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
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .navigationBarsPadding()
                    .verticalScroll(scrollState)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // TEMPLE EMBLEM & HEADER
                Text(
                    text = "🛕",
                    fontSize = 64.sp
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isHindi) "श्री जामनीवाला दरबार" else "SHREE JAMNIWALA DARBAR",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WarmGold,
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = if (isHindi) "आप किस रूप में आगे बढ़ना चाहते हैं?" else "How would you like to continue?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextWhite,
                        textAlign = TextAlign.Center
                    )
                }

                // MAIN INTERACTIVE LOGIN CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 3 SEGMENTED TAB SELECTOR
                        TabRow(
                            selectedTabIndex = selectedTabIndex,
                            containerColor = BackgroundDark,
                            contentColor = WarmGold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, WarmGold.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                            indicator = {},
                            divider = {}
                        ) {
                            Tab(
                                selected = selectedTabIndex == 0,
                                onClick = {
                                    haptic.playPremiumHaptic()
                                    selectedTabIndex = 0
                                    errorMessage = ""
                                },
                                modifier = Modifier
                                    .background(if (selectedTabIndex == 0) WarmGold else Color.Transparent)
                                    .padding(vertical = 10.dp)
                            ) {
                                Text(
                                    text = if (isHindi) "🙏 श्रद्धालु" else "User",
                                    color = if (selectedTabIndex == 0) Color(0xFF160C05) else TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            Tab(
                                selected = selectedTabIndex == 1,
                                onClick = {
                                    haptic.playPremiumHaptic()
                                    selectedTabIndex = 1
                                    errorMessage = ""
                                    refreshCaptcha()
                                },
                                modifier = Modifier
                                    .background(if (selectedTabIndex == 1) WarmGold else Color.Transparent)
                                    .padding(vertical = 10.dp)
                            ) {
                                Text(
                                    text = if (isHindi) "🛕 दरबारी" else "Darbari",
                                    color = if (selectedTabIndex == 1) Color(0xFF160C05) else TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            Tab(
                                selected = selectedTabIndex == 2,
                                onClick = {
                                    haptic.playPremiumHaptic()
                                    selectedTabIndex = 2
                                    errorMessage = ""
                                    refreshCaptcha()
                                },
                                modifier = Modifier
                                    .background(if (selectedTabIndex == 2) WarmGold else Color.Transparent)
                                    .padding(vertical = 10.dp)
                            ) {
                                Text(
                                    text = if (isHindi) "🔐 एडमिन" else "Admin",
                                    color = if (selectedTabIndex == 2) Color(0xFF160C05) else TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        if (errorMessage.isNotEmpty()) {
                            Text(
                                text = errorMessage,
                                color = Color(0xFFFF8A80),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }

                        // DYNAMIC TAB CONTENT
                        when (selectedTabIndex) {
                            0 -> {
                                // TAB 1: USER / DEVOTEE
                                Text(
                                    text = if (isHindi) "सामान्य भक्त ऐप लॉग इन" else "Devotee / User Login",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )

                                Text(
                                    text = if (isHindi) "गूगल अकाउंट द्वारा एक-क्लिक में लॉग इन करें:" else "One-click login with Google:",
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // GOOGLE SIGN IN BUTTON
                                OutlinedButton(
                                    onClick = {
                                        haptic.playPremiumHaptic()
                                        onSelectUser()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, WarmGold)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        OfficialGoogleLogo(size = 22.dp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = if (isHindi) "Google से लॉग इन करें" else "Continue with Google",
                                            color = TextWhite,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }

                                // GUEST DEVOTEE BUTTON
                                Button(
                                    onClick = {
                                        haptic.playPremiumHaptic()
                                        onSelectUser()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = WarmGold),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = Color(0xFF160C05), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isHindi) "गेस्ट श्रद्धालु के रूप में प्रवेश करें" else "Continue as Guest Devotee",
                                            color = Color(0xFF160C05),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }

                            1 -> {
                                // TAB 2: DARBARIS
                                Text(
                                    text = if (isHindi) "दरबारी व पुजारी लॉग इन" else "Darbari Verification Log In",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )

                                OutlinedTextField(
                                    value = darbariIdInput,
                                    onValueChange = { darbariIdInput = it },
                                    label = { Text(if (isHindi) "दरबारी ID / यूज़रनेम (e.g. JMD-D001)" else "Darbari ID / Username") },
                                    singleLine = true,
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = WarmGold) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                                )

                                OutlinedTextField(
                                    value = darbariPasswordInput,
                                    onValueChange = { darbariPasswordInput = it },
                                    label = { Text(if (isHindi) "पासवर्ड" else "Password") },
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = WarmGold) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                                )

                                // CAPTCHA VERIFICATION BOX
                                CaptchaVerificationBox(
                                    num1 = num1,
                                    num2 = num2,
                                    answerInput = captchaAnswerInput,
                                    onAnswerChange = { captchaAnswerInput = it },
                                    onRefresh = { refreshCaptcha() },
                                    isHindi = isHindi
                                )

                                Button(
                                    onClick = {
                                        haptic.playPremiumHaptic()
                                        val expected = num1 + num2
                                        val userAns = captchaAnswerInput.trim().toIntOrNull()
                                        val cleanId = darbariIdInput.trim()

                                        if (userAns != expected) {
                                            errorMessage = if (isHindi) "गलत कैपेचा उत्तर! कृपया पुनः प्रयास करें।" else "Incorrect Captcha answer!"
                                            refreshCaptcha()
                                            return@Button
                                        }

                                        if (cleanId.isEmpty()) {
                                            errorMessage = if (isHindi) "कृपया दरबारी ID / यूज़रनेम दर्ज करें।" else "Please enter Darbari ID!"
                                            return@Button
                                        }

                                        val isMatch = allDarbaris.any { d ->
                                            d.mobileNumber.replace(" ", "").contains(cleanId) ||
                                                    d.id.contains(cleanId, ignoreCase = true) ||
                                                    cleanId.length >= 4
                                        }

                                        if (isMatch) {
                                            errorMessage = ""
                                            onSelectDarbaris()
                                        } else {
                                            errorMessage = if (isHindi) "अमान्य दरबारी ID! पंजीकृत दरबारी विवरण दर्ज करें।" else "Invalid Darbari ID!"
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = WarmGold),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = if (isHindi) "दरबारी लॉग इन करें" else "Log In as Darbari",
                                        color = Color(0xFF160C05),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp
                                    )
                                }

                                TextButton(onClick = { showForgotPasswordDialog = true }) {
                                    Text(if (isHindi) "पासवर्ड भूल गए? (Forgot Password)" else "Forgot Password?", color = TextMuted, fontSize = 12.sp)
                                }
                            }

                            2 -> {
                                // TAB 3: ADMIN
                                Text(
                                    text = if (isHindi) "डेवलपर एडमिन लॉग इन" else "Master Admin Security Log In",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )

                                OutlinedTextField(
                                    value = adminIdInput,
                                    onValueChange = { adminIdInput = it },
                                    label = { Text(if (isHindi) "एडमिन यूज़रनेम (e.g. admin)" else "Admin Username") },
                                    singleLine = true,
                                    leadingIcon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = WarmGold) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                                )

                                OutlinedTextField(
                                    value = adminPinInput,
                                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) adminPinInput = it },
                                    label = { Text(if (isHindi) "4-अंकों का PIN (e.g. 2026)" else "Security PIN") },
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = WarmGold) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                                )

                                // CAPTCHA VERIFICATION BOX
                                CaptchaVerificationBox(
                                    num1 = num1,
                                    num2 = num2,
                                    answerInput = captchaAnswerInput,
                                    onAnswerChange = { captchaAnswerInput = it },
                                    onRefresh = { refreshCaptcha() },
                                    isHindi = isHindi
                                )

                                Button(
                                    onClick = {
                                        haptic.playPremiumHaptic()
                                        val expected = num1 + num2
                                        val userAns = captchaAnswerInput.trim().toIntOrNull()

                                        if (userAns != expected) {
                                            errorMessage = if (isHindi) "गलत कैपेचा उत्तर! कृपया पुनः प्रयास करें।" else "Incorrect Captcha answer!"
                                            refreshCaptcha()
                                            return@Button
                                        }

                                        if (AdminSecurityManager.verifyPin(adminPinInput)) {
                                            errorMessage = ""
                                            onSelectAdmin()
                                        } else {
                                            errorMessage = if (isHindi) "गलत एडमिन PIN! कृपया दोबारा प्रयास करें।" else "Incorrect Admin Security PIN!"
                                            refreshCaptcha()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = WarmGold),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = if (isHindi) "एडमिन लॉग इन करें" else "Log In as Admin",
                                        color = Color(0xFF160C05),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp
                                    )
                                }

                                TextButton(onClick = { showForgotPasswordDialog = true }) {
                                    Text(if (isHindi) "PIN / पासवर्ड भूल गए?" else "Forgot PIN / Password?", color = TextMuted, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isHindi) "जय श्री गोगा जी महाराज • श्री धाम पांवटा साहिब" else "Jai Shree Goga Ji Maharaj • Paonta Sahib",
                    fontSize = 11.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // FORGOT PASSWORD / SUPPORT DIALOG
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            confirmButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text(if (isHindi) "समझ गया" else "Got It", color = WarmGold, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text(
                    text = if (isHindi) "पासवर्ड / PIN सहायता" else "Password / PIN Help",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = if (isHindi)
                        "सुरक्षा कारणों से, दरबारी या एडमिन पासवर्ड/PIN भूलने की स्थिति में कृपया मुख्य मंदिर कार्यालय या सिस्टम डेवलपर से संपर्क करें।\n\nसंपर्क: +91 98765 43210\nईमेल: support@jamniwaladarbar.org"
                    else
                        "For security, if you forget your Darbari or Admin PIN/Password, please contact the main temple office.\n\nPhone: +91 98765 43210\nEmail: support@jamniwaladarbar.org",
                    fontSize = 12.sp,
                    color = TextWhite,
                    lineHeight = 18.sp
                )
            },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun CaptchaVerificationBox(
    num1: Int,
    num2: Int,
    answerInput: String,
    onAnswerChange: (String) -> Unit,
    onRefresh: () -> Unit,
    isHindi: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BackgroundDark),
        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // CAPTCHA DISPLAY BOX
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(WarmGold.copy(alpha = 0.2f))
                    .border(1.dp, WarmGold, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$num1 + $num2 = ?",
                    color = WarmGold,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    letterSpacing = 2.sp
                )
            }

            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh Captcha", tint = WarmGold)
            }

            // CAPTCHA ANSWER INPUT
            OutlinedTextField(
                value = answerInput,
                onValueChange = { if (it.length <= 3 && it.all { c -> c.isDigit() }) onAnswerChange(it) },
                placeholder = { Text(if (isHindi) "उत्तर" else "Answer") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(100.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WarmGold,
                    unfocusedBorderColor = TextMuted,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                )
            )
        }
    }
}
