package com.example.miyank.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyank.Language
import com.example.miyank.LanguageManager
import com.example.miyank.UserProfileManager
import com.example.miyank.UserRole
import com.example.miyank.navigation.Screen
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*

@Composable
fun RoleNavigationDrawerContent(
    onNavigate: (Any) -> Unit
) {
    val haptic = rememberHapticManager()
    val isHindi = LanguageManager.currentLanguage == Language.HINDI
    val userProfile by UserProfileManager.userProfile.collectAsState()
    val scrollState = rememberScrollState()

    ModalDrawerSheet(
        drawerContainerColor = SurfaceDark,
        drawerContentColor = TextWhite,
        modifier = Modifier.width(300.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DeepMidnight, SurfaceDark)
                    )
                )
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // DRAWER HEADER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(BackgroundDark)
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🛕", fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isHindi) "श्री जामनीवाला दरबार" else "SHREE JAMNIWALA DARBAR",
                                color = WarmGold,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = if (isHindi) "विशेष अधिकार पैनल" else "Privileged Operations",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = when (userProfile.role) {
                            UserRole.DEVELOPER_ADMIN -> Color(0xFFFF8A80).copy(alpha = 0.2f)
                            else -> WarmGold.copy(alpha = 0.2f)
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = when (userProfile.role) {
                                UserRole.DEVELOPER_ADMIN -> if (isHindi) "👑 मास्टर डेवलपर एडमिन" else "👑 Master Developer Admin"
                                UserRole.DARBARIS -> if (isHindi) "🚩 दरबारी सेवादार" else "🚩 Darbari Member"
                                else -> if (isHindi) "श्रद्धालु" else "Devotee"
                            },
                            color = when (userProfile.role) {
                                UserRole.DEVELOPER_ADMIN -> Color(0xFFFF8A80)
                                else -> WarmGold
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = WarmGold.copy(alpha = 0.25f))

            // SECTION 1: MASTER ADMIN PANEL (For DEVELOPER_ADMIN ONLY)
            if (userProfile.role == UserRole.DEVELOPER_ADMIN) {
                Text(
                    text = if (isHindi) "👑 मास्टर एडमिन पैनल" else "👑 MASTER ADMIN PANEL",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFF8A80)
                )

                DrawerItemRow(if (isHindi) "मास्टर कंट्रोल डैशबोर्ड" else "Master Dashboard", Icons.Default.AdminPanelSettings, Screen.AdminPanel, onNavigate, haptic)
                DrawerItemRow(if (isHindi) "दरबार स्टोर ऑनलाइन" else "Darbar Store Admin", Icons.Default.Storefront, Screen.AdminStore, onNavigate, haptic)
                DrawerItemRow(if (isHindi) "दान व सेवा प्रबंधन" else "Donation Admin", Icons.Default.VolunteerActivism, Screen.AdminDonation, onNavigate, haptic)
                DrawerItemRow(if (isHindi) "पुश सूचनाएं भेजें" else "Send Notifications", Icons.Default.NotificationsActive, Screen.AdminSendNotification, onNavigate, haptic)
                DrawerItemRow(if (isHindi) "ऐप एनालिटिक्स" else "App Analytics", Icons.Default.BarChart, Screen.AdminAnalytics, onNavigate, haptic)
                DrawerItemRow(if (isHindi) "होम बैनर" else "Home Banners", Icons.Default.ViewCarousel, Screen.AdminBanners, onNavigate, haptic)
                DrawerItemRow(if (isHindi) "स्थान व ऐप सेटिंग्स" else "App Settings", Icons.Default.LocationOn, Screen.AdminAppSettings, onNavigate, haptic)
                DrawerItemRow(if (isHindi) "स्पॉन्सर प्रमोशन" else "Sponsored Business Admin", Icons.Default.Campaign, Screen.AdminSponsored, onNavigate, haptic)
                DrawerItemRow(if (isHindi) "सुझाव व शिकायतें" else "Suggestions Admin", Icons.Default.RateReview, Screen.AdminSuggestions, onNavigate, haptic)
                DrawerItemRow(if (isHindi) "चालीसा व कथा साहित्य" else "Devotional Library Admin", Icons.AutoMirrored.Filled.MenuBook, Screen.AdminDevotionalContent, onNavigate, haptic)

                HorizontalDivider(color = WarmGold.copy(alpha = 0.25f))
            }

            // SECTION 2: DARBARI OPERATIONS PANEL (For DARBARIS & DEVELOPER_ADMIN)
            if (userProfile.role == UserRole.DARBARIS || userProfile.role == UserRole.DEVELOPER_ADMIN) {
                Text(
                    text = if (isHindi) "🚩 दरबारी कार्य संचालन" else "🚩 DARBARI OPERATIONS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmGold
                )

                DrawerItemRow(if (isHindi) "दरबारी ऑपरेशन्स डैशबोर्ड" else "Darbari Dashboard", Icons.Default.Dashboard, Screen.DarbarisPanel, onNavigate, haptic)
                DrawerItemRow(if (isHindi) "पर्चा प्रबंधन" else "Parcha Management", Icons.Default.Description, Screen.AdminParcha(), onNavigate, haptic)
                DrawerItemRow(if (isHindi) "श्रद्धालुओं के प्रश्न" else "Devotee Queries", Icons.AutoMirrored.Filled.Chat, Screen.AdminUserQueries, onNavigate, haptic)
                DrawerItemRow(if (isHindi) "पूजा बुकिंग प्रबंधन" else "Puja Management", Icons.Default.SelfImprovement, Screen.AdminPuja, onNavigate, haptic)
                DrawerItemRow(if (isHindi) "यात्रा व बस प्रबंधन" else "Yatra Management", Icons.Default.DirectionsBus, Screen.AdminYatra, onNavigate, haptic)
                DrawerItemRow(if (isHindi) "मंदिर कैलेंडर" else "Mandir Calendar", Icons.Default.CalendarMonth, Screen.AdminMandirCalendar, onNavigate, haptic)
                DrawerItemRow(if (isHindi) "मंदिर घोषणाएं" else "Announcements", Icons.Default.Campaign, Screen.AdminAnnouncements, onNavigate, haptic)
                DrawerItemRow(if (isHindi) "गैलरी व फोटो" else "Gallery Photos", Icons.Default.Image, Screen.AdminGallery, onNavigate, haptic)
                DrawerItemRow(if (isHindi) "दैनिक सुविचार" else "Daily Suvichar", Icons.Default.FormatQuote, Screen.AdminSuvichar, onNavigate, haptic)
                DrawerItemRow(if (isHindi) "लाइव स्ट्रीम सेटिंग्स" else "Live Stream Settings", Icons.Default.LiveTv, Screen.AdminLiveStream, onNavigate, haptic)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun DrawerItemRow(
    title: String,
    icon: ImageVector,
    screen: Any,
    onNavigate: (Any) -> Unit,
    haptic: com.example.miyank.HapticManager
) {
    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                haptic.playPremiumHaptic()
                onNavigate(screen)
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = WarmGold, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = title, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}
