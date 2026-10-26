package com.example.miyank.screens.store

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyank.Language
import com.example.miyank.LanguageManager
import com.example.miyank.rememberHapticManager
import com.example.miyank.store.*
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutAddressScreen(
    onAddressSavedClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val isHindi = LanguageManager.currentLanguage == Language.HINDI

    val savedAddress by StoreManager.savedAddress.collectAsState()

    var fullName by remember { mutableStateOf(savedAddress.fullName) }
    var mobileNumber by remember { mutableStateOf(savedAddress.mobileNumber) }
    var houseFlat by remember { mutableStateOf(savedAddress.houseFlat) }
    var streetLocality by remember { mutableStateOf(savedAddress.streetLocality) }
    var villageArea by remember { mutableStateOf(savedAddress.villageArea) }
    var city by remember { mutableStateOf(savedAddress.city.ifBlank { "पांवटा साहिब" }) }
    var district by remember { mutableStateOf(savedAddress.district.ifBlank { "सिरमौर" }) }
    var state by remember { mutableStateOf(savedAddress.state.ifBlank { "हिमाचल प्रदेश" }) }
    var pincode by remember { mutableStateOf(savedAddress.pincode.ifBlank { "173025" }) }
    var landmark by remember { mutableStateOf(savedAddress.landmark) }

    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isHindi) "डिलीवरी पतादर्ज करें" else "Delivery Address",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (isHindi) "पार्सल भेजने हेतु सटीक डिलीवरी पता लिखें" else "Enter Delivery Address for Post Office Shipping",
                color = AccentGolden,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            if (errorMessage.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Red.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(errorMessage, color = Color(0xFFFF8A80), fontSize = 12.sp)
                }
            }

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text(if (isHindi) "पूरा नाम (Full Name) *" else "Full Name *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WarmGold,
                    unfocusedBorderColor = TextMuted,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                )
            )

            OutlinedTextField(
                value = mobileNumber,
                onValueChange = { mobileNumber = it },
                label = { Text(if (isHindi) "मोबाइल नंबर (Mobile Number) *" else "Mobile Number *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WarmGold,
                    unfocusedBorderColor = TextMuted,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                )
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = houseFlat,
                    onValueChange = { houseFlat = it },
                    label = { Text(if (isHindi) "मकान नं / फ्लैट" else "House / Flat No.") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite
                    )
                )

                OutlinedTextField(
                    value = streetLocality,
                    onValueChange = { streetLocality = it },
                    label = { Text(if (isHindi) "गली / मोहल्ला" else "Street / Locality") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite
                    )
                )
            }

            OutlinedTextField(
                value = villageArea,
                onValueChange = { villageArea = it },
                label = { Text(if (isHindi) "गांव / क्षेत्र / लैंडमार्क" else "Village / Area / Landmark") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite
                )
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text(if (isHindi) "शहर (City) *" else "City *") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite
                    )
                )

                OutlinedTextField(
                    value = district,
                    onValueChange = { district = it },
                    label = { Text(if (isHindi) "जिला (District)" else "District") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite
                    )
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state,
                    onValueChange = { state = it },
                    label = { Text(if (isHindi) "राज्य (State) *" else "State *") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite
                    )
                )

                OutlinedTextField(
                    value = pincode,
                    onValueChange = { pincode = it },
                    label = { Text(if (isHindi) "पिनकोड (6 डिजिट) *" else "Pincode (6 digits) *") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite
                    )
                )
            }

            OutlinedTextField(
                value = landmark,
                onValueChange = { landmark = it },
                label = { Text(if (isHindi) "निकटतम लैंडमार्क (Optional)" else "Landmark (Optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    haptic.playPremiumHaptic()
                    if (fullName.isBlank()) {
                        errorMessage = if (isHindi) "कृपया अपना पूरा नाम लिखें।" else "Please enter full name."
                        return@Button
                    }
                    if (mobileNumber.length < 10) {
                        errorMessage = if (isHindi) "कृपया सही 10-अंकों का मोबाइल नंबर लिखें।" else "Please enter valid 10-digit mobile number."
                        return@Button
                    }
                    if (city.isBlank() || pincode.length != 6) {
                        errorMessage = if (isHindi) "कृपया शहर एवं सही 6-अंकों का पिनकोड दर्ज करें।" else "Please enter valid city and 6-digit pincode."
                        return@Button
                    }

                    val addr = DeliveryAddress(
                        fullName = fullName.trim(),
                        mobileNumber = mobileNumber.trim(),
                        houseFlat = houseFlat.trim(),
                        streetLocality = streetLocality.trim(),
                        villageArea = villageArea.trim(),
                        city = city.trim(),
                        district = district.trim(),
                        state = state.trim(),
                        pincode = pincode.trim(),
                        landmark = landmark.trim()
                    )

                    StoreManager.saveAddress(addr)
                    onAddressSavedClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WarmGold),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isHindi) "चेकआउट पर आगे बढ़ें" else "Continue to Checkout",
                    color = Color(0xFF160C05),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
