package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.example.miyank.LanguageManager
import com.example.miyank.Language
import com.example.miyank.astrology.*
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RashiInputScreen(
    onCalculateSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val isHindi = LanguageManager.currentLanguage == Language.HINDI
    val savedDetails by RashiManager.birthDetails.collectAsState()

    var userName by remember { mutableStateOf(savedDetails.userName) }
    var birthDateStr by remember { mutableStateOf(if (savedDetails.birthDateStr.isNotEmpty()) savedDetails.birthDateStr else "2000-01-01") }
    var birthTimeStr by remember { mutableStateOf(if (savedDetails.birthTimeStr.isNotEmpty()) savedDetails.birthTimeStr else "12:00 PM") }
    var isTimeUnknown by remember { mutableStateOf(savedDetails.isTimeUnknown) }

    var selectedCity by remember {
        mutableStateOf(
            RashiManager.presetCities.firstOrNull { it.cityName == savedDetails.cityName }
                ?: RashiManager.presetCities.first()
        )
    }

    var showCityPicker by remember { mutableStateOf(false) }
    var showNaamRashiDialog by remember { mutableStateOf(false) }
    var citySearchQuery by remember { mutableStateOf("") }

    var locationModeTab by remember { mutableStateOf(0) }
    var customCityName by remember { mutableStateOf(selectedCity.cityName) }
    var customStateName by remember { mutableStateOf(selectedCity.stateName) }
    var customCountryName by remember { mutableStateOf(selectedCity.countryName) }
    var customLatStr by remember { mutableStateOf(selectedCity.latitude.toString()) }
    var customLonStr by remember { mutableStateOf(selectedCity.longitude.toString()) }
    var customTzOffsetStr by remember { mutableStateOf(selectedCity.tzOffsetHours.toString()) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isHindi) "जन्म विवरण एवं राशि गणना" else "Birth Details & Rashi Calculation",
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                actions = {
                    TextButton(onClick = {
                        haptic.playPremiumHaptic()
                        showNaamRashiDialog = true
                    }) {
                        Text(
                            text = if (isHindi) "नाम राशि?" else "Name Rashi?",
                            color = WarmGold,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(listOf(GoldenGradientStart, GoldenGradientEnd))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔱", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = if (isHindi) "सटीक चंद्र राशि" else "Accurate Chandra Rashi",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = if (isHindi) "वैदिक निरयन पद्धति एवं लाहिरी अयनांश पर आधारित astronomical गणना" else "Astronomical calculation based on Vedic Nirayana Lahiri Ayanamsha",
                            fontSize = 12.sp,
                            color = TextMuted,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Input Form Section
            Text(
                text = if (isHindi) "अपना जन्म विवरण दर्ज करें" else "Enter Birth Details",
                color = AccentGolden,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            // 1. Name Input
            OutlinedTextField(
                value = userName,
                onValueChange = { userName = it },
                label = { Text(if (isHindi) "नाम (Name)" else "Name") },
                placeholder = { Text(if (isHindi) "अपना पूरा नाम लिखें" else "Enter your full name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AccentGolden) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WarmGold,
                    unfocusedBorderColor = TextMuted.copy(alpha = 0.5f),
                    focusedLabelColor = WarmGold,
                    unfocusedLabelColor = TextMuted,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // 2. Birth Date Input (YYYY-MM-DD)
            OutlinedTextField(
                value = birthDateStr,
                onValueChange = { birthDateStr = it },
                label = { Text(if (isHindi) "जन्म तिथि (DD-MM-YYYY या YYYY-MM-DD)" else "Birth Date (YYYY-MM-DD)") },
                placeholder = { Text("YYYY-MM-DD") },
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = AccentGolden) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WarmGold,
                    unfocusedBorderColor = TextMuted.copy(alpha = 0.5f),
                    focusedLabelColor = WarmGold,
                    unfocusedLabelColor = TextMuted,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // 3. Birth Time Input & Checkbox
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = birthTimeStr,
                    onValueChange = { birthTimeStr = it },
                    enabled = !isTimeUnknown,
                    label = { Text(if (isHindi) "जन्म समय (Birth Time e.g. 06:30 AM)" else "Birth Time (e.g. 06:30 AM)") },
                    placeholder = { Text("HH:MM AM/PM") },
                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = AccentGolden) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WarmGold,
                        unfocusedBorderColor = TextMuted.copy(alpha = 0.5f),
                        focusedLabelColor = WarmGold,
                        unfocusedLabelColor = TextMuted,
                        disabledBorderColor = TextMuted.copy(alpha = 0.2f),
                        disabledLabelColor = TextMuted.copy(alpha = 0.4f),
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        disabledTextColor = TextMuted,
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark,
                        disabledContainerColor = SurfaceDark.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            haptic.playPremiumHaptic()
                            isTimeUnknown = !isTimeUnknown
                        }
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                ) {
                    Checkbox(
                        checked = isTimeUnknown,
                        onCheckedChange = { isTimeUnknown = it },
                        colors = CheckboxDefaults.colors(checkedColor = WarmGold)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHindi) "जन्म समय ज्ञात नहीं है (Exact birth time unknown)" else "Birth time unknown",
                        fontSize = 13.sp,
                        color = if (isTimeUnknown) WarmGold else TextMuted
                    )
                }

                if (isTimeUnknown) {
                    Text(
                        text = if (isHindi) "⚠️ जन्म समय उपलब्ध न होने पर पूरे दिन का चंद्र परिभ्रमण जांचा जाएगा। यदि उस दिन राशि बदलती है, तो आपको स्पष्ट सूचित किया जाएगा।" else "⚠️ Full day Moon transit will be checked. If Moon crosses a Rashi boundary on that day, exact time notice will be shown.",
                        fontSize = 11.sp,
                        color = Color(0xFFFFB74D),
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            // 4. Birth Place Selection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isHindi) "जन्म स्थान (Birth Place)" else "Birth Place",
                    fontSize = 14.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            haptic.playPremiumHaptic()
                            showCityPicker = true
                        },
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = AccentGolden)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${selectedCity.cityName}, ${selectedCity.stateName}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                text = "${selectedCity.countryName} (${selectedCity.latitude}° N, ${selectedCity.longitude}° E)",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = WarmGold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Button: Calculate Rashi
            Button(
                onClick = {
                    haptic.playPremiumHaptic()
                    // Format inputs
                    var formattedDate = birthDateStr.trim()
                    if (formattedDate.contains("/")) {
                        val parts = formattedDate.split("/")
                        if (parts.size == 3 && parts[2].length == 4) {
                            formattedDate = "${parts[2]}-${parts[1].padStart(2, '0')}-${parts[0].padStart(2, '0')}"
                        }
                    }

                    val details = BirthDetails(
                        userName = userName.ifBlank { if (isHindi) "श्रद्धालु" else "Devotee" },
                        birthDateStr = formattedDate,
                        birthTimeStr = birthTimeStr,
                        isTimeUnknown = isTimeUnknown,
                        cityName = selectedCity.cityName,
                        stateName = selectedCity.stateName,
                        countryName = selectedCity.countryName,
                        latitude = selectedCity.latitude,
                        longitude = selectedCity.longitude,
                        timezone = selectedCity.timezone,
                        tzOffsetHours = selectedCity.tzOffsetHours
                    )

                    RashiManager.saveBirthDetailsAndCalculate(details)
                    onCalculateSuccess()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Unspecified),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(listOf(GoldenGradientStart, GoldenGradientEnd)),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔱", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isHindi) "राशि देखें एवं धार्मिक मार्गदर्शन पाएं" else "Calculate Rashi & Get Guidance",
                            color = Color(0xFF160C05),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // City Selector BottomSheet/Dialog
    if (showCityPicker) {
        AlertDialog(
            onDismissRequest = { showCityPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    haptic.playPremiumHaptic()
                    if (locationModeTab == 1) {
                        val lat = customLatStr.toDoubleOrNull() ?: selectedCity.latitude
                        val lon = customLonStr.toDoubleOrNull() ?: selectedCity.longitude
                        val tzOffset = customTzOffsetStr.toDoubleOrNull() ?: selectedCity.tzOffsetHours
                        selectedCity = CityLocation(
                            cityName = customCityName.ifBlank { if (isHindi) "स्थान" else "Location" },
                            stateName = customStateName.ifBlank { if (isHindi) "राज्य" else "State" },
                            countryName = customCountryName.ifBlank { if (isHindi) "भारत" else "India" },
                            latitude = lat,
                            longitude = lon,
                            timezone = "Asia/Kolkata",
                            tzOffsetHours = tzOffset
                        )
                    }
                    showCityPicker = false
                }) {
                    Text(if (isHindi) "हो गया (DONE)" else "DONE", color = WarmGold, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    haptic.playPremiumHaptic()
                    showCityPicker = false
                }) {
                    Text(if (isHindi) "बंद करें" else "Close", color = TextMuted)
                }
            },
            title = {
                Text(
                    text = if (isHindi) "जन्म स्थान चुनें या दर्ज करें" else "Select / Enter Birth Place",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                ) {
                    // Tab Row: Preset Cities vs Manual Custom Address Entry
                    TabRow(
                        selectedTabIndex = locationModeTab,
                        containerColor = SurfaceDark,
                        contentColor = WarmGold,
                        divider = { HorizontalDivider(color = TextMuted.copy(alpha = 0.2f)) }
                    ) {
                        Tab(
                            selected = locationModeTab == 0,
                            onClick = {
                                haptic.playPremiumHaptic()
                                locationModeTab = 0
                            },
                            text = {
                                Text(
                                    text = if (isHindi) "शहर सूची" else "Preset Cities",
                                    fontSize = 13.sp,
                                    fontWeight = if (locationModeTab == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                        Tab(
                            selected = locationModeTab == 1,
                            onClick = {
                                haptic.playPremiumHaptic()
                                locationModeTab = 1
                            },
                            text = {
                                Text(
                                    text = if (isHindi) "मैन्युअल स्थान" else "Custom Entry",
                                    fontSize = 13.sp,
                                    fontWeight = if (locationModeTab == 1) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (locationModeTab == 0) {
                        // Presets List with Search
                        OutlinedTextField(
                            value = citySearchQuery,
                            onValueChange = { citySearchQuery = it },
                            placeholder = { Text(if (isHindi) "शहर खोजें..." else "Search city...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = WarmGold,
                                unfocusedBorderColor = TextMuted,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            )
                        )

                        val filtered = RashiManager.presetCities.filter {
                            it.cityName.contains(citySearchQuery, ignoreCase = true) ||
                                    it.stateName.contains(citySearchQuery, ignoreCase = true)
                        }

                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            filtered.forEach { city ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            haptic.playPremiumHaptic()
                                            selectedCity = city
                                            customCityName = city.cityName
                                            customStateName = city.stateName
                                            customCountryName = city.countryName
                                            customLatStr = city.latitude.toString()
                                            customLonStr = city.longitude.toString()
                                            customTzOffsetStr = city.tzOffsetHours.toString()
                                            showCityPicker = false
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (city.cityName == selectedCity.cityName) WarmGold.copy(alpha = 0.2f) else SurfaceDark
                                    ),
                                    border = BorderStroke(0.5.dp, TextMuted.copy(alpha = 0.3f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "${city.cityName}, ${city.stateName}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite
                                        )
                                        Text(
                                            text = "${city.countryName} (Lat: ${city.latitude}, Lon: ${city.longitude})",
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Manual Custom Entry Form
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = customCityName,
                                onValueChange = { customCityName = it },
                                label = { Text(if (isHindi) "शहर / स्थान का नाम" else "City / Location Name") },
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
                                value = customStateName,
                                onValueChange = { customStateName = it },
                                label = { Text(if (isHindi) "राज्य (State)" else "State") },
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
                                value = customCountryName,
                                onValueChange = { customCountryName = it },
                                label = { Text(if (isHindi) "देश (Country)" else "Country") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = WarmGold,
                                    unfocusedBorderColor = TextMuted,
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = customLatStr,
                                    onValueChange = { customLatStr = it },
                                    label = { Text("Latitude") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = WarmGold,
                                        unfocusedBorderColor = TextMuted,
                                        focusedTextColor = TextWhite,
                                        unfocusedTextColor = TextWhite
                                    )
                                )

                                OutlinedTextField(
                                    value = customLonStr,
                                    onValueChange = { customLonStr = it },
                                    label = { Text("Longitude") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
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
                }
            },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Name-Based Rashi (Naam Rashi) Dialog Notice
    if (showNaamRashiDialog) {
        AlertDialog(
            onDismissRequest = { showNaamRashiDialog = false },
            confirmButton = {
                TextButton(onClick = { showNaamRashiDialog = false }) {
                    Text(if (isHindi) "समझ गया" else "Got It", color = WarmGold, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("ℹ️ ", fontSize = 20.sp)
                    Text(
                        text = if (isHindi) "नाम राशि — पारंपरिक अनुमान" else "Name Rashi — Traditional Estimate",
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isHindi)
                            "यह नाम के प्रथम अक्षर/ध्वनि पर आधारित पारंपरिक अनुमान है। सटीक जन्म राशि के लिए जन्म तिथि, समय और स्थान आवश्यक हैं।"
                        else
                            "This is a traditional estimate based on the first letter/sound of your name. For accurate Birth Moon Sign (Chandra Rashi), exact birth date, time, and location are required.",
                        fontSize = 13.sp,
                        color = TextWhite,
                        lineHeight = 18.sp
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(WarmGold.copy(alpha = 0.15f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = if (isHindi)
                                "मुख्य परिणाम हमेशा खगोलीय (ephemeris) गणना पर आधारित चंद्र राशि ही प्रदर्शित करता है।"
                            else
                                "The main feature always displays astronomical ephemeris calculated Chandra Rashi.",
                            fontSize = 12.sp,
                            color = WarmGold
                        )
                    }
                }
            },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
