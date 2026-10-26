package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyank.AdminSecurityManager
import com.example.miyank.AuditLogManager
import com.example.miyank.Language
import com.example.miyank.LanguageManager
import com.example.miyank.UserQueryManager
import com.example.miyank.UserProfileManager
import com.example.miyank.UserRole
import com.example.miyank.donation.DonationCauseManager
import com.example.miyank.donation.DonationStatus
import com.example.miyank.rememberHapticManager
import com.example.miyank.store.OrderStatus
import com.example.miyank.store.StoreManager
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onManageAnnouncements: () -> Unit,
    onManageEvents: () -> Unit,
    onSendNotification: () -> Unit,
    onManageGallery: () -> Unit,
    onManageUserQueries: () -> Unit,
    onViewAnalytics: () -> Unit,
    onManageBanners: () -> Unit,
    onManageAppSettings: () -> Unit,
    onManageLiveStream: () -> Unit,
    onManageBhajans: () -> Unit,
    onManageSuvichar: () -> Unit,
    onManageAboutDarbar: () -> Unit = {},
    onManageMandirCalendar: () -> Unit = {},
    onManageDarbaris: () -> Unit = {},
    onManagePuja: () -> Unit = {},
    onManageYatra: () -> Unit = {},
    onManageStore: () -> Unit = {},
    onManageDonation: () -> Unit = {},
    onManageParcha: () -> Unit = {},
    onManageSponsored: () -> Unit = {},
    onManageSuggestions: () -> Unit = {},
    onManageDevotionalContent: () -> Unit = {},
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val isHindi = LanguageManager.currentLanguage == Language.HINDI
    val userProfile by UserProfileManager.userProfile.collectAsState()

    val isSessionUnlocked by AdminSecurityManager.isSessionUnlocked.collectAsState()
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf("") }

    var showChangePinDialog by remember { mutableStateOf(false) }
    var showRoleManagementDialog by remember { mutableStateOf(false) }
    var showAuditLogsDialog by remember { mutableStateOf(false) }
    var moduleSearchQuery by remember { mutableStateOf("") }

    // Live Metrics
    val storeOrders by StoreManager.orders.collectAsState()
    val pendingOrdersCount = storeOrders.count {
        it.orderStatus == OrderStatus.PLACED ||
                it.orderStatus == OrderStatus.PAYMENT_CONFIRMED ||
                it.orderStatus == OrderStatus.PROCESSING
    }

    val donationTxns by DonationCauseManager.transactions.collectAsState()
    val pendingDonationsCount = donationTxns.count { it.paymentStatus == DonationStatus.PENDING }

    val userQueries by UserQueryManager.queries.collectAsState()
    val unreadQueriesCount = userQueries.count { !it.isReadByAdmin || it.status == com.example.miyank.QueryStatus.NEW }

    val auditLogs by AuditLogManager.auditLogs.collectAsState()

    val scrollState = rememberScrollState()

    // 1. PIN ACCESS GUARD DIALOG IF SESSION IS LOCKED
    if (!isSessionUnlocked) {
        AlertDialog(
            onDismissRequest = { onBackClick() },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.playPremiumHaptic()
                        if (AdminSecurityManager.verifyPin(pinInput)) {
                            pinError = ""
                            pinInput = ""
                        } else {
                            pinError = if (isHindi) "गलत PIN! कृपया दोबारा प्रयास करें।" else "Incorrect PIN! Please try again."
                            pinInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarmGold),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isHindi) "एडमिन डैशबोर्ड खोलें" else "Unlock Dashboard",
                        color = Color(0xFF160C05),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { onBackClick() }) {
                    Text(if (isHindi) "वापस जाएँ" else "Back", color = TextMuted)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = WarmGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHindi) "डेवलपर एडमिन सुरक्षा सत्यापन" else "Developer Admin Security",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (isHindi) "डेवलपर एडमिन मास्टर पैनल हेतु 4-अंकों का PIN दर्ज करें:" else "Enter 4-digit PIN to unlock Master Admin Panel:",
                        color = TextMuted,
                        fontSize = 13.sp
                    )

                    if (pinError.isNotEmpty()) {
                        Text(pinError, color = Color(0xFFFF8A80), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinInput = it },
                        label = { Text("PIN (e.g. 2026)") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WarmGold,
                            unfocusedBorderColor = TextMuted,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )
                }
            },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(16.dp)
        )
        return
    }

    // 2. UNLOCKED MAIN DASHBOARD
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isHindi) "डेवलपर एडमिन पैनल" else "Developer Admin Panel",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = if (isHindi) "श्री जामनीवाला दरबार — मास्टर सिस्टम नियंत्रण" else "Shree Jamniwala Darbar Master Control",
                            color = AccentGolden,
                            fontSize = 11.sp
                        )
                    }
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
                    IconButton(onClick = {
                        haptic.playPremiumHaptic()
                        showAuditLogsDialog = true
                    }) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = "Audit Logs", tint = WarmGold)
                    }

                    IconButton(onClick = {
                        haptic.playPremiumHaptic()
                        showChangePinDialog = true
                    }) {
                        Icon(Icons.Default.Key, contentDescription = "Change PIN", tint = WarmGold)
                    }

                    IconButton(onClick = {
                        haptic.playPremiumHaptic()
                        AdminSecurityManager.lockSession()
                    }) {
                        Icon(Icons.Default.Lock, contentDescription = "Lock", tint = Color(0xFFFF8A80))
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // DEVELOPER ADMIN ROLE BADGE CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(WarmGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = WarmGold, modifier = Modifier.size(28.dp))
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = userProfile.getDisplayName(isHindi),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = WarmGold,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "MASTER ADMIN",
                                    color = Color(0xFF160C05),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = if (isHindi) "रोल एवं अधिकार नियंत्रण हेतु नीचे क्लिक करें:" else "Manage roles & permissions:",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    TextButton(
                        onClick = {
                            haptic.playPremiumHaptic()
                            showRoleManagementDialog = true
                        }
                    ) {
                        Text(if (isHindi) "रोल बदलें" else "Role Setup", color = WarmGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // HERO LIVE METRICS OVERVIEW SECTION
            Text(
                text = if (isHindi) "आज का लाइव अवलोकन (Live Overview)" else "Dashboard Overview",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = AccentGolden
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Metric 1: Pending Orders
                AdminMetricCard(
                    icon = "🛍️",
                    title = if (isHindi) "नए ऑर्डर्स" else "Pending Orders",
                    count = "$pendingOrdersCount",
                    note = if (pendingOrdersCount > 0) (if (isHindi) "प्रक्रिया हेतु लंबित" else "Action required") else (if (isHindi) "कोई लंबित नहीं" else "All clear"),
                    badgeColor = if (pendingOrdersCount > 0) WarmGold else Color(0xFF81C784),
                    onClick = { haptic.playPremiumHaptic(); onManageStore() },
                    modifier = Modifier.weight(1f)
                )

                // Metric 2: Pending Donation Verification
                AdminMetricCard(
                    icon = "📜",
                    title = if (isHindi) "दान सत्यापन" else "Pending Verification",
                    count = "$pendingDonationsCount",
                    note = if (pendingDonationsCount > 0) (if (isHindi) "UTR जाँचें" else "Verify UTR") else (if (isHindi) "सभी सत्यापित" else "All verified"),
                    badgeColor = if (pendingDonationsCount > 0) Color(0xFFFFB74D) else Color(0xFF81C784),
                    onClick = { haptic.playPremiumHaptic(); onManageDonation() },
                    modifier = Modifier.weight(1f)
                )

                // Metric 3: Unread Queries
                AdminMetricCard(
                    icon = "💬",
                    title = if (isHindi) "नए संदेश" else "Unread Queries",
                    count = "$unreadQueriesCount",
                    note = if (unreadQueriesCount > 0) (if (isHindi) "उत्तर दें" else "Needs reply") else (if (isHindi) "कोई नया नहीं" else "No unread"),
                    badgeColor = if (unreadQueriesCount > 0) Color(0xFF64B5F6) else Color(0xFF81C784),
                    onClick = { haptic.playPremiumHaptic(); onManageUserQueries() },
                    modifier = Modifier.weight(1f)
                )
            }

            // SEARCH FILTER BAR
            OutlinedTextField(
                value = moduleSearchQuery,
                onValueChange = { moduleSearchQuery = it },
                placeholder = { Text(if (isHindi) "मॉड्यूल खोजें (e.g. रोल, स्टोर, सुविचार, लाइव...)" else "Search admin module...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentGolden) },
                trailingIcon = {
                    if (moduleSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { moduleSearchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = TextMuted)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WarmGold,
                    unfocusedBorderColor = TextMuted.copy(alpha = 0.5f),
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // CATEGORIZED MODULES
            val q = moduleSearchQuery.trim().lowercase()

            val allModules = listOf(
                // SYSTEM & ROLE MANAGEMENT
                AdminModuleItem("0", if (isHindi) "रोल एवं यूजर अधिकार (RBAC)" else "Role & Permission Control", if (isHindi) "User, Darbaris & Master Admin अधिकार सेट करें" else "Manage 3-panel roles & permissions", Icons.Default.ManageAccounts, { showRoleManagementDialog = true }),
                AdminModuleItem("0", if (isHindi) "सिस्टम ऑडिट लॉग्ज (Audit Logs)" else "System Audit Logs", if (isHindi) "प्रशासकीय गतिविधियों का इतिहास देखें" else "View admin action history log", Icons.Default.ReceiptLong, { showAuditLogsDialog = true }),

                // SECTION 1: STORE & SEVA
                AdminModuleItem("1", if (isHindi) "दरबार स्टोर ऑनलाइन" else "Darbar Store Online", if (isHindi) "उत्पाद, स्टॉक, मूल्य एवं ऑर्डर्स प्रबंधन" else "Manage products, stock & orders", Icons.Default.Storefront, onManageStore),
                AdminModuleItem("1", if (isHindi) "दान एवं सेवा प्रबंधन" else "Donations & Seva Management", if (isHindi) "सेवा क्षेत्र, बैंक/UPI एवं UTR सत्यापन" else "Manage causes, bank/UPI & verification", Icons.Default.VolunteerActivism, onManageDonation),
                AdminModuleItem("1", if (isHindi) "पूजा प्रबंधन" else "Puja Management", if (isHindi) "वर्चुअल एवं विशेष पूजा बुकिंग" else "Manage puja bookings", Icons.Default.SelfImprovement, onManagePuja),
                AdminModuleItem("1", if (isHindi) "यात्रा व बस प्रबंधन" else "Yatras & Buses", if (isHindi) "यात्रा शेड्यूल एवं बस बुकिंग" else "Manage yatra schedules & seats", Icons.Default.DirectionsBus, onManageYatra),
                AdminModuleItem("1", if (isHindi) "मंदिर कैलेंडर" else "Mandir Calendar", if (isHindi) "त्यौहार, व्रत एवं प्रमुख तिथियाँ" else "Manage fasts, festivals & calendar", Icons.Default.CalendarMonth, onManageMandirCalendar),

                AdminModuleItem("1", if (isHindi) "स्पॉन्सर प्रमोशन" else "Sponsored Business", if (isHindi) "व्यापार प्रचार एवं विज्ञापन प्रबंधन" else "Manage local business promos", Icons.Default.Campaign, onManageSponsored),

                // SECTION 2: CONTENT & MEDIA
                AdminModuleItem("2", if (isHindi) "लाइव स्ट्रीम सेटिंग्स" else "Live Stream Settings", if (isHindi) "YouTube लाइव URL व शेड्यूल सेटिंग्स" else "Manage YouTube Live URL & status", Icons.Default.LiveTv, onManageLiveStream),
                AdminModuleItem("2", if (isHindi) "दैनिक सुविचार" else "Daily Suvichar", if (isHindi) "दैनिक सुविचार पोस्ट व इमेज जनरेशन" else "Manage quotes & posters", Icons.Default.FormatQuote, onManageSuvichar),
                AdminModuleItem("2", if (isHindi) "मंदिर घोषणाएं" else "Announcements", if (isHindi) "महत्वपूर्ण मंदिर सूचनाएं व अपडेट" else "Manage temple announcements", Icons.Default.Campaign, onManageAnnouncements),
                AdminModuleItem("2", if (isHindi) "मंदिर आयोजन" else "Temple Events", if (isHindi) "आगामी धार्मिक कार्यक्रम एवं उत्सव" else "Manage upcoming events", Icons.Default.Event, onManageEvents),
                AdminModuleItem("2", if (isHindi) "गैलरी व फोटो" else "Gallery Photos", if (isHindi) "मंदिर की दिव्य झलकियाँ व फोटो" else "Manage gallery photos with optimizer", Icons.Default.Image, onManageGallery),
                AdminModuleItem("2", if (isHindi) "होम बैनर" else "Home Banners", if (isHindi) "होम स्क्रीन कैरोसेल बैनर्स" else "Manage home carousel banners", Icons.Default.ViewCarousel, onManageBanners),
                AdminModuleItem("2", if (isHindi) "दरबार जानकारी" else "About Darbar / Blog", if (isHindi) "इतिहास, महिमा एवं ब्लॉग आर्टिकल्स" else "Manage blog & temple history", Icons.Default.MenuBook, onManageAboutDarbar),
                AdminModuleItem("2", if (isHindi) "चालीसा व कथा साहित्य" else "Chalisa & Devotional Library", if (isHindi) "चालीसा, स्तोत्रम एवं कथा साहित्य प्रबंधन" else "Manage chalisa, stotram & katha library", Icons.Default.MenuBook, onManageDevotionalContent),

                // SECTION 3: DEVOTEE INTERACTION
                AdminModuleItem("3", if (isHindi) "पर्चा प्रबंधन" else "Parcha Management", if (isHindi) "श्रद्धालुओं को निजी पर्चा व मार्गदर्शन भेजें" else "Manage & send personal parchas", Icons.Default.Description, onManageParcha),
                AdminModuleItem("3", if (isHindi) "सुझाव व शिकायतें" else "Suggestions & Complaints", if (isHindi) "श्रद्धालुओं के सुझाव व शिकायत प्रबंधन" else "Manage user suggestions & complaints", Icons.Default.RateReview, onManageSuggestions),
                AdminModuleItem("3", if (isHindi) "सूचनाएं भेजें" else "Send Notifications", if (isHindi) "सभी श्रद्धालुओं को पुश सूचना भेजें" else "Broadcast push notifications", Icons.Default.NotificationsActive, onSendNotification),
                AdminModuleItem("3", if (isHindi) "संपर्क व प्रश्न" else "Devotee Queries", if (isHindi) "श्रद्धालुओं के प्रश्न देखें व उत्तर दें" else "View & reply to user queries", Icons.AutoMirrored.Filled.Chat, onManageUserQueries),
                AdminModuleItem("3", if (isHindi) "दरबारी व पुजारी" else "Darbaris Directory", if (isHindi) "दरबार सेवादार व पुजारी सूची" else "Manage darbaris directory", Icons.Default.Groups, onManageDarbaris),
                AdminModuleItem("3", if (isHindi) "मंदिर स्थान व संपर्क" else "Location & Contact", if (isHindi) "कोऑर्डिनेट्स एवं संपर्क सेटिंग्स" else "Manage location & contact settings", Icons.Default.LocationOn, onManageAppSettings),
                AdminModuleItem("3", if (isHindi) "ऐप एनालिटिक्स" else "App Analytics", if (isHindi) "उपयोग एवं प्रशासनिक रिपोर्ट" else "View overall app usage analytics", Icons.Default.BarChart, onViewAnalytics)
            )

            val filteredModules = allModules.filter { m ->
                q.isBlank() || m.title.lowercase().contains(q) || m.description.lowercase().contains(q)
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filteredModules.forEach { mod ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                haptic.playPremiumHaptic()
                                mod.onClick()
                            },
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = BorderStroke(0.5.dp, WarmGold.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(WarmGold.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = mod.icon, contentDescription = null, tint = WarmGold, modifier = Modifier.size(22.dp))
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mod.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Text(
                                    text = mod.description,
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                    maxLines = 1
                                )
                            }

                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = WarmGold, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Role Management Dialog
    if (showRoleManagementDialog) {
        var selectedRole by remember { mutableStateOf(userProfile.role) }

        AlertDialog(
            onDismissRequest = { showRoleManagementDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.playPremiumHaptic()
                        UserProfileManager.updateUserRole(selectedRole, actorName = userProfile.getDisplayName(isHindi))
                        showRoleManagementDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarmGold)
                ) {
                    Text(if (isHindi) "रोल अपडेट करें" else "Update Role", color = Color(0xFF160C05), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRoleManagementDialog = false }) {
                    Text(if (isHindi) "बंद करें" else "Cancel", color = TextMuted)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ManageAccounts, contentDescription = null, tint = WarmGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isHindi) "3-Panel रोल प्रबंधन" else "Role Management", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isHindi) "वर्तमान खाता रोल चुनें:" else "Select user role:",
                        fontSize = 12.sp,
                        color = TextMuted
                    )

                    UserRole.entries.forEach { roleEnum ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptic.playPremiumHaptic()
                                    selectedRole = roleEnum
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedRole == roleEnum) WarmGold.copy(alpha = 0.2f) else BackgroundDark
                            ),
                            border = BorderStroke(1.dp, if (selectedRole == roleEnum) WarmGold else TextMuted.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedRole == roleEnum,
                                    onClick = { selectedRole = roleEnum },
                                    colors = RadioButtonDefaults.colors(selectedColor = WarmGold)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = roleEnum.getDisplayName(isHindi),
                                        color = TextWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = when (roleEnum) {
                                            UserRole.USER -> if (isHindi) "सामान्य सार्वजनिक भक्त ऐप अनुभव" else "Default public devotee app"
                                            UserRole.DARBARIS -> if (isHindi) "दरबारी एवं सेवादार कार्य पैनल" else "Darbari operational panel"
                                            UserRole.DEVELOPER_ADMIN -> if (isHindi) "मास्टर डेवलपर एडमिन कंट्रोल" else "Master developer admin dashboard"
                                        },
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Audit Logs Dialog
    if (showAuditLogsDialog) {
        AlertDialog(
            onDismissRequest = { showAuditLogsDialog = false },
            confirmButton = {
                TextButton(onClick = { showAuditLogsDialog = false }) {
                    Text(if (isHindi) "बंद करें" else "Close", color = WarmGold, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        haptic.playPremiumHaptic()
                        AuditLogManager.clearLogs()
                    }
                ) {
                    Text(if (isHindi) "लॉग्स साफ़ करें" else "Clear Logs", color = Color.Red)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = WarmGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isHindi) "सिस्टम ऑडिट लॉग्ज" else "System Audit Logs", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                    if (auditLogs.isEmpty()) {
                        Text(if (isHindi) "कोई नया ऑडिट लॉग उपलब्ध नहीं है।" else "No audit logs found.", color = TextMuted, fontSize = 12.sp)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(auditLogs) { log ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = BackgroundDark),
                                    border = BorderStroke(0.5.dp, WarmGold.copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(log.action, color = WarmGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text(log.actorRole.name, color = TextMuted, fontSize = 10.sp)
                                        }
                                        if (log.details.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(log.details, color = TextWhite, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Change PIN Dialog
    if (showChangePinDialog) {
        var oldPinInput by remember { mutableStateOf("") }
        var newPinInput by remember { mutableStateOf("") }
        var confirmPinInput by remember { mutableStateOf("") }
        var changePinError by remember { mutableStateOf("") }
        var changePinSuccess by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.playPremiumHaptic()
                        if (newPinInput != confirmPinInput) {
                            changePinError = if (isHindi) "नया PIN मेल नहीं खाता!" else "New PIN confirmation does not match!"
                            return@Button
                        }
                        val success = AdminSecurityManager.changePin(oldPinInput, newPinInput)
                        if (success) {
                            changePinSuccess = true
                            changePinError = ""
                            AuditLogManager.logAction(
                                actorUserId = userProfile.mobileNumber,
                                actorName = userProfile.getDisplayName(isHindi),
                                actorRole = userProfile.role,
                                action = "ADMIN_PIN_CHANGED",
                                details = "Admin security PIN updated successfully"
                            )
                        } else {
                            changePinError = if (isHindi) "पुराना PIN अमान्य है या नया PIN 4-अंकों का नहीं है!" else "Invalid current PIN or new PIN is not 4 digits!"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarmGold)
                ) {
                    Text(if (isHindi) "PIN बदलें" else "Save New PIN", color = Color(0xFF160C05), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePinDialog = false }) {
                    Text(if (isHindi) "बंद करें" else "Close", color = TextMuted)
                }
            },
            title = {
                Text(if (isHindi) "एडमिन सुरक्षा PIN बदलें" else "Change Admin PIN", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (changePinError.isNotEmpty()) {
                        Text(changePinError, color = Color(0xFFFF8A80), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    if (changePinSuccess) {
                        Text(if (isHindi) "✓ PIN सफलतापूर्वक बदल दिया गया!" else "✓ PIN changed successfully!", color = Color(0xFF81C784), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedTextField(
                        value = oldPinInput,
                        onValueChange = { if (it.length <= 4) oldPinInput = it },
                        label = { Text(if (isHindi) "वर्तमान PIN" else "Current PIN") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )

                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = { if (it.length <= 4) newPinInput = it },
                        label = { Text(if (isHindi) "नया 4-अंकों का PIN" else "New 4-digit PIN") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )

                    OutlinedTextField(
                        value = confirmPinInput,
                        onValueChange = { if (it.length <= 4) confirmPinInput = it },
                        label = { Text(if (isHindi) "नया PIN दोबारा लिखें" else "Confirm New PIN") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )
                }
            },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

private data class AdminModuleItem(
    val section: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
private fun AdminMetricCard(
    icon: String,
    title: String,
    count: String,
    note: String,
    badgeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(0.5.dp, WarmGold.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(icon, fontSize = 18.sp)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(count, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = badgeColor)
                }
            }

            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                maxLines = 1
            )

            Text(
                text = note,
                fontSize = 10.sp,
                color = TextMuted,
                maxLines = 1
            )
        }
    }
}
