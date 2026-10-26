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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyank.AppShareConfig
import com.example.miyank.LanguageManager
import com.example.miyank.LiveDarshanManager
import com.example.miyank.LivePlatformType
import com.example.miyank.QueryCategories
import com.example.miyank.UserQuery
import com.example.miyank.UserQueryManager
import com.example.miyank.getEpochMillis
import com.example.miyank.getTodayDateString
import com.example.miyank.openExternalUrl
import com.example.miyank.rememberHapticManager
import com.example.miyank.rememberSuvicharMediaHandler
import com.example.miyank.ui.components.OfficialYouTubeLogo
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactUsScreen(
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val mediaHandler = rememberSuvicharMediaHandler()
    val scrollState = rememberScrollState()
    val isHindi = LanguageManager.currentLanguage == com.example.miyank.Language.HINDI

    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf("सामान्य प्रश्न") }
    var messageInput by remember { mutableStateOf("") }

    var formErrorText by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val liveSettings by LiveDarshanManager.settings.collectAsState()
    val ytPlatform = liveSettings.platforms.find { it.platformType == LivePlatformType.YOUTUBE }
    val instaPlatform = liveSettings.platforms.find { it.platformType == LivePlatformType.INSTAGRAM }
    val fbPlatform = liveSettings.platforms.find { it.platformType == LivePlatformType.FACEBOOK }

    val contactSettings by UserQueryManager.contactSettings.collectAsState()
    val helplinePhone = contactSettings.helplinePhone.replace(" ", "").replace("-", "")
    val formattedHelplinePhone = contactSettings.helplinePhone.ifEmpty { "+91 98765 43210" }
    val whatsappNumber = contactSettings.whatsappPhone.replace(" ", "").replace("-", "").replace("+", "").ifEmpty { "919876543210" }

    val categories = listOf("सामान्य प्रश्न", "सेवा एवं दान", "भंडारा", "पूजा")

    fun handleSubmitQuery() {
        haptic.playPremiumHaptic()
        formErrorText = null
        successMessage = null

        val cleanName = nameInput.trim()
        val cleanPhone = phoneInput.trim().replace(" ", "").replace("-", "")
        val cleanMsg = messageInput.trim()

        if (cleanName.isEmpty()) {
            formErrorText = if (isHindi) "कृपया अपना नाम लिखें।" else "Please enter your full name."
            return
        }

        if (cleanPhone.length != 10 || !cleanPhone.all { it.isDigit() }) {
            formErrorText = if (isHindi) "कृपया 10 अंकों का सही मोबाइल नंबर दर्ज करें।" else "Please enter a valid 10-digit mobile number."
            return
        }

        if (cleanMsg.isEmpty()) {
            formErrorText = if (isHindi) "कृपया अपनी जिज्ञासा या संदेश लिखें।" else "Please enter your message or prayer."
            return
        }

        isSubmitting = true

        val newQuery = UserQuery(
            id = "query_${getEpochMillis()}",
            name = cleanName,
            phone = "+91 $cleanPhone",
            category = categoryInput,
            message = cleanMsg,
            date = getTodayDateString(),
            createdAt = getEpochMillis(),
            isResolved = false
        )

        UserQueryManager.addQuery(newQuery)

        isSubmitting = false
        successMessage = if (isHindi) "आपकी प्रार्थना/जिज्ञासा सफलतापूर्वक दर्ज कर ली गई है! 🙏" else "Your prayer/query has been recorded successfully! 🙏"

        // Reset Form
        nameInput = ""
        phoneInput = ""
        categoryInput = "सामान्य प्रश्न"
        messageInput = ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isHindi) "संपर्क करें" else "Contact Us", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark,
                    titleContentColor = TextWhite,
                    navigationIconContentColor = TextWhite
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
                .padding(16.dp)
        ) {
            // 1. DEVOTIONAL HEADER CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🛕 श्री जामनीवाला दरबार",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentGolden,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isHindi) "सेवा, प्रार्थना या किसी भी जानकारी के लिए हमसे संपर्क करें। 🙏" else "Contact us for Sewa, Prayers, or information.",
                        fontSize = 13.sp,
                        color = TextWhite,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. DIRECT CONTACT QUICK ACTION CARDS
            Text(
                text = if (isHindi) "📞 सीधा संपर्क" else "Direct Helpline",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AccentGolden
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickContactCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Call,
                    title = if (isHindi) "फ़ोन पर बात करें" else "Call Helpline",
                    subtitle = formattedHelplinePhone,
                    badgeColor = Color(0xFF4CAF50),
                    onClick = {
                        haptic.playPremiumHaptic()
                        openExternalUrl("tel:$helplinePhone")
                    }
                )

                QuickContactCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Message,
                    title = if (isHindi) "WhatsApp मैसेज" else "WhatsApp Chat",
                    subtitle = "जय श्री गोगा जी 🙏",
                    badgeColor = Color(0xFF25D366),
                    onClick = {
                        haptic.playPremiumHaptic()
                        openExternalUrl("https://wa.me/$whatsappNumber?text=जय%20श्री%20गोगा%20जी%20महाराज%20🙏")
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // TEMPLE LOCATION CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        haptic.playPremiumHaptic()
                        openExternalUrl("https://www.google.com/maps/search/?api=1&query=30.45,77.62")
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(AccentGolden.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = AccentGolden, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isHindi) "📍 मंदिर स्थान देखें" else "Temple Location",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = if (isHindi) "पांवटा साहिब, जिला सिरमौर, हिमाचल प्रदेश" else "Paonta Sahib, Himachal Pradesh",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 3. DEVOTEES QUERY / PRAYER FORM
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (isHindi) "अपनी जिज्ञासा / प्रार्थना भेजें" else "Send Query or Prayer",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentGolden
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isHindi) "अपना संदेश भेजें, हम यथासंभव आपसे संपर्क करने का प्रयास करेंगे। 🙏" else "Send your message, we will reach out as soon as possible. 🙏",
                        fontSize = 12.sp,
                        color = TextMuted,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Field 1: Name
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = {
                            nameInput = it
                            formErrorText = null
                        },
                        label = { Text(if (isHindi) "आपका पूरा नाम *" else "Full Name *") },
                        placeholder = { Text(if (isHindi) "अपना पूरा नाम लिखें" else "Enter full name") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AccentGolden) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentGolden,
                            unfocusedBorderColor = BackgroundDark,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Field 2: Mobile Number
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { input ->
                            val digitsOnly = input.filter { it.isDigit() }
                            if (digitsOnly.length <= 10) {
                                phoneInput = digitsOnly
                                formErrorText = null
                            }
                        },
                        label = { Text(if (isHindi) "मोबाइल नंबर *" else "Mobile Number *") },
                        placeholder = { Text("10 अंकों का मोबाइल नंबर") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = AccentGolden) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentGolden,
                            unfocusedBorderColor = BackgroundDark,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Field 3: Category Choice Chips in 2-Column Responsive Layout
                    Text(if (isHindi) "विषय / श्रेणी *" else "Category *", fontSize = 12.sp, color = AccentGolden, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    val categoryRows = listOf(
                        listOf(QueryCategories.GENERAL_QUERY to "सामान्य प्रश्न", QueryCategories.SEVA_DAAN to "सेवा एवं दान"),
                        listOf(QueryCategories.BHANDARA to "भंडारा", QueryCategories.POOJA_ANUSHTHAN to "पूजा एवं अनुष्ठान")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        categoryRows.forEach { rowCategories ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowCategories.forEach { (catKey, catLabel) ->
                                    val isSelected = categoryInput == catLabel
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            haptic.playPremiumHaptic()
                                            categoryInput = catLabel
                                        },
                                        modifier = Modifier.weight(1f),
                                        label = {
                                            Text(
                                                text = catLabel,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                                color = if (isSelected) BackgroundDark else TextWhite,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = AccentGolden,
                                            containerColor = BackgroundDark
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = isSelected,
                                            borderColor = WarmGold.copy(alpha = 0.4f),
                                            selectedBorderColor = AccentGolden
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Field 4: Message
                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = {
                            messageInput = it
                            formErrorText = null
                        },
                        label = { Text(if (isHindi) "संदेश / प्रार्थना *" else "Message / Prayer *") },
                        placeholder = { Text(if (isHindi) "अपनी जिज्ञासा, सुझाव या प्रार्थना लिखें..." else "Type message or prayer...") },
                        modifier = Modifier.fillMaxWidth().height(110.dp),
                        leadingIcon = { Icon(Icons.Default.Message, contentDescription = null, tint = AccentGolden) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentGolden,
                            unfocusedBorderColor = BackgroundDark,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 5
                    )

                    if (formErrorText != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = formErrorText!!,
                            color = Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (successMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = Color(0xFF1B5E20).copy(alpha = 0.3f),
                            border = BorderStroke(1.dp, Color(0xFF4CAF50)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = successMessage!!,
                                    color = Color(0xFF81C784),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Submit Button
                    Button(
                        onClick = { handleSubmitQuery() },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGolden),
                        enabled = !isSubmitting
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isHindi) "संदेश भेजें" else "Send Message",
                                color = BackgroundDark,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            // DEVOTEE MY QUERIES & RESPONSES SECTION
            val allUserQueries by UserQueryManager.queries.collectAsState()
            if (allUserQueries.isNotEmpty()) {
                Spacer(modifier = Modifier.height(22.dp))
                Text(
                    text = if (isHindi) "📩 मेरी प्रार्थनाएं व उत्तर" else "My Queries & Responses",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentGolden
                )
                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        allUserQueries.take(3).forEach { query ->
                            Surface(
                                color = BackgroundDark,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = AccentGolden.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = query.category,
                                                color = AccentGolden,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(text = query.date, fontSize = 11.sp, color = TextMuted)
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = query.message, fontSize = 13.sp, color = TextWhite)

                                    if (query.replies.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        query.replies.forEach { reply ->
                                            Surface(
                                                color = AccentGolden.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(10.dp),
                                                border = BorderStroke(1.dp, AccentGolden.copy(alpha = 0.4f)),
                                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Text("🛕 ${reply.senderName}:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentGolden)
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(reply.message, fontSize = 12.sp, color = TextWhite)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 4. TEMPLE ADDRESS & CONTACT DETAILS (NO WEBSITE BUTTON AS PER REQ 7 & 18)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "🛕 श्री जामनीवाला दरबार",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentGolden
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(if (isHindi) "मुख्य धाम" else "Main Temple", fontSize = 11.sp, color = TextMuted)
                    Text("श्री जामनीवाला दरबार", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = WarmGold.copy(alpha = 0.2f))

                    Text(if (isHindi) "पता (Address)" else "Address", fontSize = 11.sp, color = TextMuted)
                    Text(contactSettings.address.ifEmpty { "पांवटा साहिब, जिला सिरमौर, हिमाचल प्रदेश - 173025" }, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextWhite)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = WarmGold.copy(alpha = 0.2f))

                    Text(if (isHindi) "ईमेल" else "Email", fontSize = 11.sp, color = TextMuted)
                    Text(contactSettings.email.ifEmpty { "info@jamniwaladarbar.org" }, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccentGolden)
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 5. OFFICIAL SOCIAL MEDIA (YouTube, Instagram, Facebook)
            Text(
                text = if (isHindi) "🌐 हमसे जुड़े रहें" else "Connect With Us",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AccentGolden
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SocialMediaButton(
                    modifier = Modifier.weight(1f),
                    title = "YouTube",
                    color = Color.Red,
                    onClick = {
                        haptic.playPremiumHaptic()
                        val url = ytPlatform?.url?.ifEmpty { "https://www.youtube.com" } ?: "https://www.youtube.com"
                        openExternalUrl(url)
                    }
                )

                SocialMediaButton(
                    modifier = Modifier.weight(1f),
                    title = "Instagram",
                    color = Color(0xFFE1306C),
                    onClick = {
                        haptic.playPremiumHaptic()
                        val url = instaPlatform?.url?.ifEmpty { "https://www.instagram.com" } ?: "https://www.instagram.com"
                        openExternalUrl(url)
                    }
                )

                SocialMediaButton(
                    modifier = Modifier.weight(1f),
                    title = "Facebook",
                    color = Color(0xFF1877F2),
                    onClick = {
                        haptic.playPremiumHaptic()
                        val url = fbPlatform?.url?.ifEmpty { "https://www.facebook.com" } ?: "https://www.facebook.com"
                        openExternalUrl(url)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 6. SHARE APP CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isHindi) "ऐप को अपने प्रियजनों तक पहुँचाएँ ❤️" else "Share App With Loved Ones ❤️",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextWhite,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isHindi) "श्री जामनीवाला दरबार ऐप को अपने परिवार और मित्रों के साथ साझा करें।" else "Share Shree Jamniwala Darbar App with family and friends.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            haptic.playPremiumHaptic()
                            val shareMsg = com.example.miyank.ShareManager.formatGeneralShareMessage(
                                title = "श्री जामनीवाला दरबार मोबाइल ऐप",
                                description = "बाबा गोगा जी महाराज और सत् गुरु बाबा सोम दत्त जी महाराज के पावन ऐप से जुड़े।"
                            )
                            mediaHandler.shareSuvichar(
                                imageUrlOrPath = "file:///android_asset/temple_pooja.jpg",
                                dateStr = "",
                                quoteText = "श्री जामनीवाला दरबार ऐप",
                                author = "श्री जामनीवाला दरबार",
                                shareText = shareMsg
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isHindi) "📤 ऐप शेयर करें" else "📤 Share App",
                                color = BackgroundDark,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun QuickContactCard(
    modifier: Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 11.sp, color = TextMuted)
        }
    }
}

@Composable
private fun SocialMediaButton(
    modifier: Modifier,
    title: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            maxLines = 1
        )
    }
}
