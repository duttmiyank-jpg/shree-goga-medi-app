package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyank.Language
import com.example.miyank.LanguageManager
import com.example.miyank.ParchaManager
import com.example.miyank.UserProfileManager
import com.example.miyank.UserQueryManager
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DarbarisPanelScreen(
    onManageParcha: () -> Unit,
    onManageUserQueries: () -> Unit,
    onManagePuja: () -> Unit,
    onManageYatra: () -> Unit,
    onManageCalendar: () -> Unit,
    onManageAnnouncements: () -> Unit,
    onManageEvents: () -> Unit,
    onManageGallery: () -> Unit,
    onManageSuvichar: () -> Unit,
    onManageLiveStream: () -> Unit,
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val isHindi = LanguageManager.currentLanguage == Language.HINDI
    val userProfile by UserProfileManager.userProfile.collectAsState()

    val parchas by ParchaManager.parchas.collectAsState()
    val activeParchasCount = remember(parchas) { parchas.count { it.isActive } }

    val userQueries by UserQueryManager.queries.collectAsState()
    val unreadQueriesCount = remember(userQueries) { userQueries.count { !it.isReadByAdmin } }

    var searchQuery by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isHindi) "दरबारी सेवा डैशबोर्ड" else "Darbaris Operational Panel",
                            color = TextWhite,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = if (isHindi) "श्री जामनीवाला दरबार — मंदिर कार्य एवं सेवा" else "Shree Jamniwala Darbar — Temple Work",
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
            // DARBARI BADGE & WELCOME CARD
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
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(WarmGold.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Groups, contentDescription = null, tint = WarmGold, modifier = Modifier.size(28.dp))
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = userProfile.getDisplayName(isHindi),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = WarmGold.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (isHindi) "दरबारी" else "Darbari",
                                    color = WarmGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = if (isHindi) "जय श्री गोगा जी महाराज! आज की मंदिर सेवाएँ नीचे देखें:" else "Jai Shree Goga Ji Maharaj! Operational tasks below:",
                            fontSize = 11.sp,
                            color = TextMuted,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // OPERATIONAL METRICS SUMMARY
            Text(
                text = if (isHindi) "आज का सेवा अवलोकन (Operations)" else "Today's Operations",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = AccentGolden
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Metric 1: Active Parchas
                DarbariMetricCard(
                    icon = "📜",
                    title = if (isHindi) "कुल पर्चा रिकॉर्ड" else "Parcha Records",
                    count = "$activeParchasCount",
                    note = if (activeParchasCount > 0) (if (isHindi) "सक्रिय पर्चे" else "Active parchas") else (if (isHindi) "कोई पर्चा नहीं" else "No parchas"),
                    badgeColor = WarmGold,
                    onClick = { haptic.playPremiumHaptic(); onManageParcha() },
                    modifier = Modifier.weight(1f)
                )

                // Metric 2: Unread Queries
                DarbariMetricCard(
                    icon = "💬",
                    title = if (isHindi) "श्रद्धालु संदेश" else "Unread Queries",
                    count = "$unreadQueriesCount",
                    note = if (unreadQueriesCount > 0) (if (isHindi) "उत्तर दें" else "Reply needed") else (if (isHindi) "कोई नया नहीं" else "No unread"),
                    badgeColor = if (unreadQueriesCount > 0) Color(0xFF64B5F6) else Color(0xFF81C784),
                    onClick = { haptic.playPremiumHaptic(); onManageUserQueries() },
                    modifier = Modifier.weight(1f)
                )
            }

            // SEARCH FILTER BAR
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (isHindi) "सेवा कार्य खोजें (e.g. पर्चा, पूजा, यात्रा, सुविचार...)" else "Search darbari module...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentGolden) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
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

            // MODULES LIST
            val q = searchQuery.trim().lowercase()

            val darbariModules = listOf(
                DarbariModuleItem(if (isHindi) "पर्चा एवं टोकन प्रबंधन" else "Parcha & Token Management", if (isHindi) "श्रद्धालुओं के पर्चा देखें, टोकन व तिथि जारी करें" else "Review & issue token dates for parcha", Icons.Default.Description, onManageParcha),
                DarbariModuleItem(if (isHindi) "श्रद्धालु प्रश्न एवं उत्तर" else "Devotee Queries & Replies", if (isHindi) "श्रद्धालुओं द्वारा पूछे गए प्रश्नों के उत्तर दें" else "Answer devotee queries & spiritual doubts", Icons.AutoMirrored.Filled.Chat, onManageUserQueries),
                DarbariModuleItem(if (isHindi) "पूजा सेवा एवं बुकिंग" else "Puja Bookings & Rituals", if (isHindi) "पूजा बुकिंग, संकल्प एवं प्रसाद सेवा" else "Manage puja bookings & sankalp details", Icons.Default.SelfImprovement, onManagePuja),
                DarbariModuleItem(if (isHindi) "यात्रा व बस शेड्यूल" else "Yatra & Bus Management", if (isHindi) "धाम यात्राएं, बस बुकिंग व सीट प्रबंधन" else "Manage yatra schedules & seat bookings", Icons.Default.DirectionsBus, onManageYatra),
                DarbariModuleItem(if (isHindi) "मंदिर कैलेंडर एवं व्रत तिथियाँ" else "Mandir Calendar & Fasts", if (isHindi) "आगामी त्यौहार, व्रत एवं प्रमुख तिथियाँ" else "Manage calendar fasts & tithi dates", Icons.Default.CalendarMonth, onManageCalendar),
                DarbariModuleItem(if (isHindi) "मंदिर सूचनाएं व घोषणाएं" else "Announcements & Notices", if (isHindi) "दरबार की महत्वपूर्ण घोषणाएं प्रकाशित करें" else "Publish temple notices & announcements", Icons.Default.Campaign, onManageAnnouncements),
                DarbariModuleItem(if (isHindi) "धार्मिक कार्यक्रम व आयोजन" else "Events & Celebrations", if (isHindi) "आगामी कार्यक्रम, भंडारा व उत्सव" else "Manage temple events & celebrations", Icons.Default.Event, onManageEvents),
                DarbariModuleItem(if (isHindi) "गैलरी व दिव्य फोटो" else "Gallery & Photos", if (isHindi) "मंदिर के दिव्य दर्शन व तस्वीरें" else "Manage gallery photos", Icons.Default.Image, onManageGallery),
                DarbariModuleItem(if (isHindi) "दैनिक सुविचार शेयरिंग" else "Daily Suvichar", if (isHindi) "दैनिक सुविचार कार्ड तैयार व शेयर करें" else "Manage daily quotes & wallpapers", Icons.Default.FormatQuote, onManageSuvichar),
                DarbariModuleItem(if (isHindi) "लाइव दर्शन स्थिति" else "Live Darshan Status", if (isHindi) "लाइव दर्शन लिंक व समय अपडेट करें" else "Check YouTube Live status", Icons.Default.LiveTv, onManageLiveStream)
            )

            val filteredModules = darbariModules.filter { m ->
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
}

private data class DarbariModuleItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
private fun DarbariMetricCard(
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
