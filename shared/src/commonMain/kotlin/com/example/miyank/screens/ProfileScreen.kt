package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.miyank.Language
import com.example.miyank.LanguageManager
import com.example.miyank.UserProfile
import com.example.miyank.UserProfileManager
import com.example.miyank.UserRole
import com.example.miyank.astrology.RashiManager
import com.example.miyank.donation.DonationCauseManager
import com.example.miyank.rememberFilePickerLauncher
import com.example.miyank.rememberHapticManager
import com.example.miyank.rememberImageOptimizer
import com.example.miyank.store.StoreManager
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNotificationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAdminClick: () -> Unit,
    onDarbarisPanelClick: () -> Unit = {},
    onSwitchRoleClick: () -> Unit = {},
    onRashiClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onDonationsClick: () -> Unit = {},
    onYatraClick: () -> Unit = {},
    onLanguageClick: () -> Unit = {},
    onContactUsClick: () -> Unit = {},
    onParchaClick: () -> Unit = {}
) {
    val haptic = rememberHapticManager()
    val imageOptimizer = rememberImageOptimizer()
    val scrollState = rememberScrollState()
    val isHindi = LanguageManager.currentLanguage == Language.HINDI

    val userProfile by UserProfileManager.userProfile.collectAsState()
    val rashiResult by RashiManager.calculationResult.collectAsState()
    val storeOrders by StoreManager.orders.collectAsState()
    val donationTransactions by DonationCauseManager.transactions.collectAsState()
    val verifiedDonationCount = donationTransactions.count { it.isVerified() }

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }

    // File Picker Launcher for Avatar Photo Upload
    val avatarPickerLauncher = rememberFilePickerLauncher("image/*") { filePath, _, _ ->
        if (!filePath.isNullOrBlank()) {
            val optResult = imageOptimizer.processAndOptimizeImage(filePath)
            val finalPath = optResult?.optimizedFullImagePath ?: filePath
            UserProfileManager.updateAvatar(finalPath)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isHindi) "मेरी प्रोफ़ाइल" else "My Profile",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                actions = {
                    IconButton(onClick = {
                        haptic.playPremiumHaptic()
                        onSettingsClick()
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = WarmGold)
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. HERO DEVOTEE PROFILE CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(GoldenGradientStart, WarmGold))),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar Image with Golden Ring & Camera Badge
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .border(2.5.dp, WarmGold, CircleShape)
                                .background(Color(0xFF1A120B)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (userProfile.avatarImageUrl.isNotBlank()) {
                                AsyncImage(
                                    model = userProfile.avatarImageUrl,
                                    contentDescription = "Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = WarmGold,
                                    modifier = Modifier.size(54.dp)
                                )
                            }
                        }

                        // Camera overlay button
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(WarmGold)
                                .clickable {
                                    haptic.playPremiumHaptic()
                                    avatarPickerLauncher()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Change Photo",
                                tint = Color(0xFF160C05),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = userProfile.getDisplayName(isHindi),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextWhite,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "📱 ${userProfile.mobileNumber.ifBlank { if (isHindi) "मोबाइल नंबर दर्ज करें" else "Mobile not set" }} • 📍 ${userProfile.getFormattedLocation()}",
                        fontSize = 12.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )

                    if (userProfile.gotra.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(WarmGold.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${if (isHindi) "गोत्र" else "Gotra"}: ${userProfile.gotra}",
                                fontSize = 11.sp,
                                color = WarmGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            haptic.playPremiumHaptic()
                            showEditProfileDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WarmGold),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF160C05), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isHindi) "प्रोफ़ाइल बदलें" else "Edit Profile",
                            color = Color(0xFF160C05),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // 2. DEVOTEE ACTIVITY SUMMARY (2x2 GRID)
            Text(
                text = if (isHindi) "मेरी गतिविधियाँ" else "My Activities",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = AccentGolden
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Rashi
                ActivitySummaryCard(
                    icon = "🔱",
                    title = if (isHindi) "मेरी राशि" else "My Rashi",
                    value = if (rashiResult != null) "${rashiResult!!.rashi.symbol} ${if (isHindi) rashiResult!!.rashi.hindiName else rashiResult!!.rashi.englishName}" else (if (isHindi) "अपनी राशि जानें" else "Calculate Rashi"),
                    onClick = {
                        haptic.playPremiumHaptic()
                        onRashiClick()
                    },
                    modifier = Modifier.weight(1f)
                )

                // Card 2: Orders
                ActivitySummaryCard(
                    icon = "🛍️",
                    title = if (isHindi) "मेरे ऑर्डर्स" else "Store Orders",
                    value = if (storeOrders.isNotEmpty()) "${storeOrders.size} ${if (isHindi) "ऑर्डर्स" else "Orders"}" else (if (isHindi) "कोई ऑर्डर नहीं" else "No Orders"),
                    onClick = {
                        haptic.playPremiumHaptic()
                        onOrdersClick()
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 3: Donations
                ActivitySummaryCard(
                    icon = "📜",
                    title = if (isHindi) "दान रसीदें" else "Donations",
                    value = if (verifiedDonationCount > 0) "$verifiedDonationCount ${if (isHindi) "रसीदें" else "Receipts"}" else (if (isHindi) "कोई रसीद नहीं" else "No Receipts"),
                    onClick = {
                        haptic.playPremiumHaptic()
                        onDonationsClick()
                    },
                    modifier = Modifier.weight(1f)
                )

                // Card 4: Yatras
                ActivitySummaryCard(
                    icon = "🚩",
                    title = if (isHindi) "मेरी यात्राएँ" else "My Yatras",
                    value = if (isHindi) "यात्रा बुक करें" else "Book Yatra",
                    onClick = {
                        haptic.playPremiumHaptic()
                        onYatraClick()
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // 3. STRUCTURED MENU SECTION - DEVOTIONAL ACTIVITIES
            ProfileSectionTitle(if (isHindi) "धार्मिक गतिविधियाँ" else "Devotional Activities")

            val userId = userProfile.mobileNumber.ifEmpty { "guest_user" }
            val parchaUnreadCount = com.example.miyank.ParchaManager.getUnreadCountForUser(userId)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(0.5.dp, WarmGold.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    ProfileMenuRow(
                        icon = Icons.Default.Description,
                        title = if (isHindi) "पर्चा" else "Parcha",
                        badgeText = if (parchaUnreadCount > 0) "$parchaUnreadCount" else null,
                        onClick = { haptic.playPremiumHaptic(); onParchaClick() }
                    )
                    HorizontalDivider(color = TextMuted.copy(alpha = 0.15f), modifier = Modifier.padding(horizontal = 16.dp))

                    ProfileMenuRow(
                        icon = Icons.Default.AutoAwesome,
                        title = if (isHindi) "मेरी चंद्र राशि एवं मार्गदर्शन" else "My Rashi & Guidance",
                        onClick = { haptic.playPremiumHaptic(); onRashiClick() }
                    )
                    HorizontalDivider(color = TextMuted.copy(alpha = 0.15f), modifier = Modifier.padding(horizontal = 16.dp))

                    ProfileMenuRow(
                        icon = Icons.Default.ShoppingBag,
                        title = if (isHindi) "दरबार स्टोर ऑर्डर्स" else "My Store Orders",
                        onClick = { haptic.playPremiumHaptic(); onOrdersClick() }
                    )
                    HorizontalDivider(color = TextMuted.copy(alpha = 0.15f), modifier = Modifier.padding(horizontal = 16.dp))

                    ProfileMenuRow(
                        icon = Icons.Default.Receipt,
                        title = if (isHindi) "दान एवं सेवा रसीदें" else "My Donation Receipts",
                        onClick = { haptic.playPremiumHaptic(); onDonationsClick() }
                    )
                    HorizontalDivider(color = TextMuted.copy(alpha = 0.15f), modifier = Modifier.padding(horizontal = 16.dp))

                    ProfileMenuRow(
                        icon = Icons.Default.DirectionsBus,
                        title = if (isHindi) "मेरी यात्रा बुकिंग" else "My Yatra Bookings",
                        onClick = { haptic.playPremiumHaptic(); onYatraClick() }
                    )
                    HorizontalDivider(color = TextMuted.copy(alpha = 0.15f), modifier = Modifier.padding(horizontal = 16.dp))

                    ProfileMenuRow(
                        icon = Icons.Default.Notifications,
                        title = if (isHindi) "सूचना इतिहास" else "Notification History",
                        onClick = { haptic.playPremiumHaptic(); onNotificationClick() }
                    )
                }
            }

            // 4. APP & SETTINGS SECTION
            ProfileSectionTitle(if (isHindi) "ऐप एवं सेटिंग्स" else "App & Settings")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(0.5.dp, WarmGold.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    ProfileMenuRow(
                        icon = Icons.Default.Language,
                        title = if (isHindi) "भाषा बदलें (Language)" else "Change Language",
                        onClick = { haptic.playPremiumHaptic(); onLanguageClick() }
                    )
                    HorizontalDivider(color = TextMuted.copy(alpha = 0.15f), modifier = Modifier.padding(horizontal = 16.dp))

                    ProfileMenuRow(
                        icon = Icons.Default.Settings,
                        title = if (isHindi) "ऐप सेटिंग्स" else "App Settings",
                        onClick = { haptic.playPremiumHaptic(); onSettingsClick() }
                    )
                    HorizontalDivider(color = TextMuted.copy(alpha = 0.15f), modifier = Modifier.padding(horizontal = 16.dp))

                    ProfileMenuRow(
                        icon = Icons.Default.PrivacyTip,
                        title = if (isHindi) "गोपनीयता नीति (Privacy Policy)" else "Privacy Policy",
                        onClick = { haptic.playPremiumHaptic(); showPrivacyPolicyDialog = true }
                    )
                    HorizontalDivider(color = TextMuted.copy(alpha = 0.15f), modifier = Modifier.padding(horizontal = 16.dp))

                    ProfileMenuRow(
                        icon = Icons.Default.HelpOutline,
                        title = if (isHindi) "संपर्क एवं सहायता" else "Contact & Help",
                        onClick = { haptic.playPremiumHaptic(); onContactUsClick() }
                    )
                }
            }

            // 5. ADMINISTRATIVE & ROLE CAPABILITIES SECTION
            ProfileSectionTitle(if (isHindi) "मंदिर प्रशासन एवं सेवा" else "Administration & Seva")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(0.5.dp, WarmGold.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    if (userProfile.role == UserRole.DARBARIS || userProfile.role == UserRole.DEVELOPER_ADMIN) {
                        ProfileMenuRow(
                            icon = Icons.Default.Groups,
                            title = if (isHindi) "दरबारी सेवा डैशबोर्ड" else "Darbaris Operational Panel",
                            onClick = { haptic.playPremiumHaptic(); onDarbarisPanelClick() }
                        )
                        HorizontalDivider(color = TextMuted.copy(alpha = 0.15f), modifier = Modifier.padding(horizontal = 16.dp))
                    }

                    if (userProfile.role == UserRole.DEVELOPER_ADMIN) {
                        ProfileMenuRow(
                            icon = Icons.Default.Security,
                            title = if (isHindi) "डेवलपर एडमिन पैनल" else "Developer Admin Dashboard",
                            onClick = { haptic.playPremiumHaptic(); onAdminClick() }
                        )
                        HorizontalDivider(color = TextMuted.copy(alpha = 0.15f), modifier = Modifier.padding(horizontal = 16.dp))
                    }

                    ProfileMenuRow(
                        icon = Icons.Default.Logout,
                        title = if (isHindi) "रोल बदलें / लॉग आउट" else "Switch Role / Exit Mode",
                        onClick = { haptic.playPremiumHaptic(); onSwitchRoleClick() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var fullNameInput by remember { mutableStateOf(userProfile.fullName) }
        var mobileInput by remember { mutableStateOf(userProfile.mobileNumber) }
        var emailInput by remember { mutableStateOf(userProfile.email) }
        var cityInput by remember { mutableStateOf(userProfile.cityName) }
        var stateInput by remember { mutableStateOf(userProfile.stateName) }
        var gotraInput by remember { mutableStateOf(userProfile.gotra) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.playPremiumHaptic()
                        val updated = userProfile.copy(
                            fullName = fullNameInput.trim(),
                            mobileNumber = mobileInput.trim(),
                            email = emailInput.trim(),
                            cityName = cityInput.trim(),
                            stateName = stateInput.trim(),
                            gotra = gotraInput.trim()
                        )
                        UserProfileManager.saveProfile(updated)
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarmGold),
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (isHindi) "प्रोफ़ाइल सेव करें" else "Save Profile",
                        color = Color(0xFF160C05),
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text(if (isHindi) "रद्द करें" else "Cancel", color = TextMuted)
                }
            },
            title = {
                Text(
                    text = if (isHindi) "अपनी प्रोफ़ाइल बदलें" else "Edit Profile Details",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Button to pick photo from gallery inside edit dialog
                    OutlinedButton(
                        onClick = {
                            haptic.playPremiumHaptic()
                            avatarPickerLauncher()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, WarmGold)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = WarmGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isHindi) "गैलरी से प्रोफ़ाइल फोटो बदलें" else "Change Profile Photo", color = WarmGold, fontSize = 12.sp)
                    }

                    OutlinedTextField(
                        value = fullNameInput,
                        onValueChange = { fullNameInput = it },
                        label = { Text(if (isHindi) "श्रद्धालु का नाम" else "Full Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )

                    OutlinedTextField(
                        value = mobileInput,
                        onValueChange = { mobileInput = it },
                        label = { Text(if (isHindi) "मोबाइल नंबर" else "Mobile Number") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text(if (isHindi) "ईमेल आईडी (Optional)" else "Email Address") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = cityInput,
                            onValueChange = { cityInput = it },
                            label = { Text(if (isHindi) "शहर" else "City") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )

                        OutlinedTextField(
                            value = stateInput,
                            onValueChange = { stateInput = it },
                            label = { Text(if (isHindi) "राज्य" else "State") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )
                    }

                    OutlinedTextField(
                        value = gotraInput,
                        onValueChange = { gotraInput = it },
                        label = { Text(if (isHindi) "गोत्र (Optional)" else "Gotra (Optional)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )
                }
            },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Privacy Policy Dialog
    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            confirmButton = {
                TextButton(onClick = { showPrivacyPolicyDialog = false }) {
                    Text(if (isHindi) "समझ गया" else "Got It", color = WarmGold, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text(
                    text = if (isHindi) "गोपनीयता नीति (Privacy Policy)" else "Privacy Policy",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = if (isHindi)
                            "श्री जामनीवाला दरबार मोबाइल एप्प श्रद्धालुओं की निजता का पूर्ण सम्मान करता है।\n\n1. आपकी व्यक्तिगत जानकारी (नाम, मोबाइल नंबर, जन्म विवरण) आपके डिवाइस पर सुरक्षित रूप से सहेजी जाती है।\n2. ऑनलाइन ऑर्डर तथा दान हेतु दर्ज की गई जानकारी केवल सेवा पूर्ति एवं सत्यापन हेतु उपयोग की जाती है।\n3. कोई भी संवेदनशील जानकारी बिना आपकी अनुमति के किसी तीसरे पक्ष के साथ साझा नहीं की जाती।"
                        else
                            "Shree Jamniwala Darbar Mobile App values devotee privacy.\n\n1. Personal details are stored securely on device.\n2. Information provided for orders and donations is used solely for service fulfillment.\n3. Your data is never shared with third parties.",
                        fontSize = 12.sp,
                        color = TextWhite,
                        lineHeight = 18.sp
                    )
                }
            },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun ActivitySummaryCard(
    icon: String,
    title: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(0.5.dp, WarmGold.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ProfileSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = AccentGolden,
        letterSpacing = 0.5.sp
    )
}

@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    badgeText: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(WarmGold.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = WarmGold, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            fontSize = 14.sp
        )

        if (badgeText != null) {
            Surface(
                color = Color.Red,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = badgeText,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(14.dp)
        )
    }
}
