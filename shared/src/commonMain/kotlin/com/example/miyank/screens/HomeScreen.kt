package com.example.miyank.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.miyank.AppStrings
import com.example.miyank.HapticManager
import com.example.miyank.LiveDarshanManager
import com.example.miyank.LocalStrings
import com.example.miyank.MusicManager
import com.example.miyank.SuvicharManager
import com.example.miyank.formatDisplayDate
import com.example.miyank.rememberAudioPlayer
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    onEventsClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onLiveDarshanClick: () -> Unit,
    onYatraClick: () -> Unit,
    onDonationClick: () -> Unit,
    onBhajansClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onDarshanTimingsClick: () -> Unit,
    onAboutTempleClick: () -> Unit,
    onPanchangClick: () -> Unit,
    onVirtualPujaClick: () -> Unit,
    onTempleLocationClick: () -> Unit,
    onMandirCalendarClick: () -> Unit,
    onBhajanLyricsClick: () -> Unit,
    onAnnouncementsClick: () -> Unit,
    onContactUsClick: () -> Unit,
    onSuvicharClick: (String) -> Unit,
    onAboutDarbarClick: () -> Unit = {},
    onDarbarisClick: () -> Unit = {},
    onPujaClick: () -> Unit = {},
    onRashiClick: () -> Unit = {},
    onStoreClick: () -> Unit = {},
    onParchaClick: () -> Unit = {},
    onDarbarisPanelClick: () -> Unit = {},
    onAdminPanelClick: () -> Unit = {},
    onSponsoredClick: () -> Unit = {},
    onSuggestionsClick: () -> Unit = {},
    onChalisaStotramClick: () -> Unit = {},
    onOpenDrawer: () -> Unit = {}
) {
    val haptic = rememberHapticManager()
    val scrollState = rememberScrollState()
    val strings = LocalStrings.current
    val isHindi = com.example.miyank.LanguageManager.currentLanguage == com.example.miyank.Language.HINDI
    val userProfile by com.example.miyank.UserProfileManager.userProfile.collectAsState()

    val liveSettings by LiveDarshanManager.settings.collectAsState()
    val isLivePlayable = LiveDarshanManager.getActualPlayableStatus()
    var showLiveNotice by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(scrollState)
    ) {
        HomeHeader(
            onNotificationClick = onNotificationClick, 
            onOpenDrawer = onOpenDrawer,
            haptic = haptic,
            strings = strings
        )

        // PROMINENT DARBARI OPERATIONAL HERO CARD
        if (userProfile.role == com.example.miyank.UserRole.DARBARIS || userProfile.role == com.example.miyank.UserRole.DEVELOPER_ADMIN) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clickable {
                        haptic.playPremiumHaptic()
                        onDarbarisPanelClick()
                    },
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, WarmGold),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(WarmGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Groups, contentDescription = null, tint = WarmGold, modifier = Modifier.size(26.dp))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isHindi) "🛕 दरबारी सेवा व मंदिर संचालन" else "🛕 Darbari Operations",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = WarmGold,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    color = Color(0xFF160C05),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }

                        Text(
                            text = if (isHindi) "पर्चा, प्रश्न-उत्तर, पूजा/यात्रा बुकिंग व मंदिर सेवाएँ" else "Parcha, queries, puja/yatra bookings & ops",
                            fontSize = 11.sp,
                            color = TextMuted,
                            maxLines = 1
                        )
                    }

                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = WarmGold, modifier = Modifier.size(20.dp))
                }
            }
        }

        // PROMINENT MASTER DEVELOPER ADMIN HERO CARD
        if (userProfile.role == com.example.miyank.UserRole.DEVELOPER_ADMIN) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clickable {
                        haptic.playPremiumHaptic()
                        onAdminPanelClick()
                    },
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, Color(0xFFFF8A80)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFF8A80).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color(0xFFFF8A80), modifier = Modifier.size(26.dp))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isHindi) "🔐 डेवलपर एडमिन मास्टर कंट्रोल" else "🔐 Master Developer Admin",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextWhite
                            )
                        }

                        Text(
                            text = if (isHindi) "यूजर व दरबारी रोल नियंत्रण, एनालिटिक्स व सिस्टम सेटिंग्स" else "User/Darbari role setup, analytics & system",
                            fontSize = 11.sp,
                            color = TextMuted,
                            maxLines = 1
                        )
                    }

                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFFF8A80), modifier = Modifier.size(20.dp))
                }
            }
        }

        if (isLivePlayable && showLiveNotice) {
            LiveDarshanAnnouncement(
                title = liveSettings.title,
                onDismiss = { showLiveNotice = false },
                onClick = onLiveDarshanClick,
                haptic = haptic,
                strings = strings
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LuxuryBannerCarousel(
            haptic = haptic,
            strings = strings,
            onEventsClick = onEventsClick,
            onLiveDarshanClick = onLiveDarshanClick,
            onYatraClick = onYatraClick,
            onGalleryClick = onGalleryClick,
            onAboutTempleClick = onAboutTempleClick,
            onDonationClick = onDonationClick,
            onAnnouncementsClick = onAnnouncementsClick
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        TodaySuvicharSection(strings, haptic, onSuvicharClick)
        
        Spacer(modifier = Modifier.height(24.dp))

        TodayAnnouncementSection(haptic, onAnnouncementsClick)

        Spacer(modifier = Modifier.height(32.dp))
        
        SectionTitle(strings.quickAccess)
        QuickAccessGrid(
            onGalleryClick = onGalleryClick,
            onLiveDarshanClick = onLiveDarshanClick,
            onPanchangClick = onPanchangClick,
            onVirtualPujaClick = onVirtualPujaClick,
            onBhajansClick = onBhajansClick,
            onYatraClick = onYatraClick,
            onDonationClick = onDonationClick,
            onDarbarisClick = onDarbarisClick,
            onPujaClick = onPujaClick,
            onRashiClick = onRashiClick,
            onStoreClick = onStoreClick,
            onParchaClick = onParchaClick,
            onSponsoredClick = onSponsoredClick,
            onSuggestionsClick = onSuggestionsClick,
            onChalisaStotramClick = onChalisaStotramClick,
            haptic = haptic,
            isLive = isLivePlayable
        )

        if (isLivePlayable) {
            MiniLivePreviewCard(
                title = liveSettings.title,
                streamUrl = liveSettings.streamUrl,
                onClick = onLiveDarshanClick,
                haptic = haptic
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        SectionTitle(strings.moreServices)
        OtherServicesLuxury(
            onDarshanTimingsClick = onDarshanTimingsClick, 
            onAboutTempleClick = onAboutTempleClick, 
            onEventsClick = onEventsClick, 
            onTempleLocationClick = onTempleLocationClick, 
            onMandirCalendarClick = onMandirCalendarClick, 
            onBhajanLyricsClick = onBhajanLyricsClick, 
            onAnnouncementsClick = onAnnouncementsClick, 
            onContactUsClick = onContactUsClick, 
            onAboutDarbarClick = onAboutDarbarClick,
            haptic = haptic,
            strings = strings
        )
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun HomeHeader(
    onNotificationClick: () -> Unit,
    onOpenDrawer: () -> Unit = {},
    haptic: HapticManager,
    strings: AppStrings
) {
    val userProfile by com.example.miyank.UserProfileManager.userProfile.collectAsState()
    val showHamburger = userProfile.role == com.example.miyank.UserRole.DARBARIS || userProfile.role == com.example.miyank.UserRole.DEVELOPER_ADMIN

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showHamburger) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SurfaceDark)
                    .clickable {
                        haptic.playPremiumHaptic()
                        onOpenDrawer()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Drawer",
                    tint = WarmGold,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = strings.greeting,
                fontSize = 16.sp,
                color = TextMuted,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = strings.guest,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite,
                letterSpacing = (-0.5).sp
            )
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(SurfaceDark)
                .clickable { 
                    haptic.playPremiumHaptic()
                    onNotificationClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = "Notifications",
                tint = AccentGolden,
                modifier = Modifier.size(28.dp)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color.Red)
            )
        }
    }
}

@Composable
fun LuxuryBannerCarousel(
    haptic: HapticManager,
    strings: AppStrings,
    onEventsClick: () -> Unit,
    onLiveDarshanClick: () -> Unit,
    onYatraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onAboutTempleClick: () -> Unit,
    onDonationClick: () -> Unit,
    onAnnouncementsClick: () -> Unit
) {
    val activeBanners by com.example.miyank.BannersManager.banners.collectAsState()
    val isHindi = com.example.miyank.LanguageManager.currentLanguage == com.example.miyank.Language.HINDI

    val displayBanners = remember(activeBanners) {
        com.example.miyank.BannersManager.getActiveBanners()
    }

    if (displayBanners.isEmpty()) return

    val pagerState = rememberPagerState { displayBanners.size }

    // Continuous Auto-Scroll Carousel Loop
    LaunchedEffect(displayBanners) {
        if (displayBanners.size > 1) {
            while (true) {
                delay(4000)
                if (!pagerState.isScrollInProgress) {
                    val nextPage = (pagerState.currentPage + 1) % displayBanners.size
                    pagerState.animateScrollToPage(
                        page = nextPage,
                        animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing)
                    )
                }
            }
        }
    }

    // Continuous Infinite Ken Burns Motion Specs for Slideshow
    val infiniteTransition = rememberInfiniteTransition()
    val kenBurnsZoom by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val kenBurnsPan by infiniteTransition.animateFloat(
        initialValue = -14f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentPadding = PaddingValues(horizontal = 24.dp),
            pageSpacing = 12.dp
        ) { page ->
            val banner = displayBanners[page]
            val isCurrentPage = pagerState.currentPage == page
            val displayTitle = if (isHindi || banner.titleEnglish.isEmpty()) banner.title else banner.titleEnglish
            val tagText = com.example.miyank.BannerCategories.getDisplayName(banner.categoryTag, isHindi)

            // 3D Carousel Scaling & Alpha Calculation
            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            val absOffset = kotlin.math.abs(pageOffset)

            val cardScale = (1f - (absOffset * 0.08f)).coerceIn(0.92f, 1.0f)
            val cardAlpha = (1f - (absOffset * 0.25f)).coerceIn(0.70f, 1.0f)

            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = cardScale,
                        scaleY = cardScale,
                        alpha = cardAlpha
                    )
                    .clickable {
                        haptic.playPremiumHaptic()
                        when (banner.targetRoute.uppercase()) {
                            com.example.miyank.TargetRoutes.EVENTS -> onEventsClick()
                            com.example.miyank.TargetRoutes.LIVE_DARSHAN -> onLiveDarshanClick()
                            com.example.miyank.TargetRoutes.YATRA -> onYatraClick()
                            com.example.miyank.TargetRoutes.GALLERY -> onGalleryClick()
                            com.example.miyank.TargetRoutes.ABOUT_TEMPLE -> onAboutTempleClick()
                            com.example.miyank.TargetRoutes.DONATION -> onDonationClick()
                            com.example.miyank.TargetRoutes.ANNOUNCEMENTS -> onAnnouncementsClick()
                            else -> onEventsClick()
                        }
                    },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(
                    1.5.dp,
                    if (isCurrentPage) {
                        Brush.horizontalGradient(listOf(WarmGold, AccentGolden, SaffronAccent, WarmGold))
                    } else {
                        Brush.horizontalGradient(listOf(WarmGold.copy(alpha = 0.3f), WarmGold.copy(alpha = 0.3f)))
                    }
                ),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Continuous Moving Banner Image (Ken Burns Pan + Zoom)
                    AsyncImage(
                        model = banner.imageUrl,
                        contentDescription = displayTitle,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = if (isCurrentPage) kenBurnsZoom else 1.0f,
                                scaleY = if (isCurrentPage) kenBurnsZoom else 1.0f,
                                translationX = if (isCurrentPage) kenBurnsPan else 0f
                            ),
                        contentScale = ContentScale.Crop
                    )

                    // Cinematic Dark Gradient Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.45f),
                                        Color.Black.copy(alpha = 0.90f)
                                    ),
                                    startY = 140f
                                )
                            )
                    )

                    // Banner Content Overlay
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp)
                    ) {
                        Surface(
                            color = AccentGolden,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = tagText.uppercase(),
                                color = BackgroundDark,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = displayTitle,
                            color = TextWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 26.sp,
                            maxLines = 2
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ATTRACTIVE ANIMATED EXPANDING SLIDESHOW INDICATOR DOTS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(displayBanners.size) { i ->
                val isSelected = pagerState.currentPage == i
                val width by animateDpAsState(
                    targetValue = if (isSelected) 28.dp else 8.dp,
                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(width, 7.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) {
                                Brush.horizontalGradient(listOf(WarmGold, AccentGolden))
                            } else {
                                androidx.compose.ui.graphics.SolidColor(WarmGold.copy(alpha = 0.35f))
                            }
                        )
                )
            }
        }
    }
}



@Composable
fun QuickAccessGrid(
    onGalleryClick: () -> Unit,
    onLiveDarshanClick: () -> Unit,
    onPanchangClick: () -> Unit,
    onVirtualPujaClick: () -> Unit,
    onBhajansClick: () -> Unit,
    onYatraClick: () -> Unit,
    onDonationClick: () -> Unit,
    onDarbarisClick: () -> Unit = {},
    onPujaClick: () -> Unit = {},
    onRashiClick: () -> Unit = {},
    onStoreClick: () -> Unit = {},
    onParchaClick: () -> Unit = {},
    onSponsoredClick: () -> Unit = {},
    onSuggestionsClick: () -> Unit = {},
    onChalisaStotramClick: () -> Unit = {},
    haptic: HapticManager,
    isLive: Boolean
) {
    val strings = LocalStrings.current
    val isHindi = com.example.miyank.LanguageManager.currentLanguage == com.example.miyank.Language.HINDI

    val userProfile by com.example.miyank.UserProfileManager.userProfile.collectAsState()
    val userId = userProfile.mobileNumber.ifEmpty { "guest_user" }
    val parchaUnreadCount = com.example.miyank.ParchaManager.getUnreadCountForUser(userId)

    val items = listOf(
        QuickItem(if (isHindi) "पर्चा" else "Parcha", Icons.Default.Description, onParchaClick, false, parchaUnreadCount),
        QuickItem(if (isHindi) "दरबार स्टोर" else "Store", Icons.Default.ShoppingBag, onStoreClick, false),
        QuickItem(if (isHindi) "मेरी राशि" else "My Rashi", Icons.Default.AutoAwesome, onRashiClick, false),
        QuickItem(strings.liveDarshan, Icons.Default.Videocam, onLiveDarshanClick, isLive),
        QuickItem(if (isHindi) "पूजा बुक" else "Puja", Icons.Default.SelfImprovement, onPujaClick, false),
        QuickItem(if (isHindi) "दरबारी" else "Darbaris", Icons.Default.Groups, onDarbarisClick, false),
        QuickItem(strings.panchang, Icons.Default.CalendarMonth, onPanchangClick, false),
        QuickItem(strings.virtualAarti, Icons.Default.WbSunny, onVirtualPujaClick, false),
        QuickItem(strings.yatra, Icons.Default.DirectionsBus, onYatraClick, false),
        QuickItem(if (isHindi) "स्पॉन्सर" else "Sponsored", Icons.Default.Campaign, onSponsoredClick, false),
        QuickItem(if (isHindi) "सुझाव व शिकायत" else "Suggestions", Icons.Default.RateReview, onSuggestionsClick, false),
        QuickItem(if (isHindi) "चालीसा व कथाएं" else "Chalisa & Katha", Icons.AutoMirrored.Filled.MenuBook, onChalisaStotramClick, false)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
            .padding(horizontal = 8.dp),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = false
    ) {
        items(items) { item ->
            LuxuryQuickCard(item, haptic)
        }
    }
}

data class QuickItem(
    val name: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val isLive: Boolean = false,
    val badgeCount: Int = 0
)

@Composable
fun LuxuryQuickCard(item: QuickItem, haptic: HapticManager) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable {
                haptic.playPremiumHaptic()
                item.onClick()
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (item.isLive) {
                Surface(
                    color = Color.Red.copy(alpha = pulseAlpha),
                    shape = RoundedCornerShape(bottomStart = 8.dp, topEnd = 24.dp),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = "LIVE",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            } else if (item.badgeCount > 0) {
                Surface(
                    color = Color.Red,
                    shape = RoundedCornerShape(bottomStart = 8.dp, topEnd = 24.dp),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = "${item.badgeCount}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.name,
                    tint = if (item.isLive) Color.Red else AccentGolden,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
fun MiniLivePreviewCard(
    title: String,
    streamUrl: String,
    onClick: () -> Unit,
    haptic: HapticManager
) {
    val videoId = LiveDarshanManager.extractYouTubeVideoId(streamUrl) ?: return
    val thumbUrl = LiveDarshanManager.getYoutubeThumbnailUrl(videoId)

    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable {
                haptic.playPremiumHaptic()
                onClick()
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color.Red.copy(alpha = pulseAlpha),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.White))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "🔴 LIVE DARSHAN",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Text(
                    text = "श्री जामनीवाला दरबार",
                    color = AccentGolden,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 16:9 THUMBNAIL PREVIEW
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = thumbUrl,
                    contentDescription = "Live Darshan Preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Dark overlay gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                            )
                        )
                )

                // Play Icon Overlay
                Surface(
                    color = Color.Red.copy(alpha = 0.9f),
                    shape = CircleShape,
                    modifier = Modifier.align(Alignment.Center).size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Watch on YouTube",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title.ifEmpty { "लाइव दर्शन एवं आरती" },
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "अभी लाइव दर्शन चल रहा है",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted
                )

                Button(
                    onClick = {
                        haptic.playPremiumHaptic()
                        val targetUrl = LiveDarshanManager.getValidLaunchUrl(streamUrl)
                        com.example.miyank.openExternalUrl(targetUrl)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "YouTube पर देखें",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun OtherServicesLuxury(
    onDarshanTimingsClick: () -> Unit, 
    onAboutTempleClick: () -> Unit, 
    onEventsClick: () -> Unit, 
    onTempleLocationClick: () -> Unit, 
    onMandirCalendarClick: () -> Unit, 
    onBhajanLyricsClick: () -> Unit, 
    onAnnouncementsClick: () -> Unit, 
    onContactUsClick: () -> Unit, 
    onAboutDarbarClick: () -> Unit = {},
    haptic: HapticManager,
    strings: AppStrings
) {
    val services = listOf(
        strings.aboutDarbarTitle to onAboutDarbarClick,
        strings.darshanTimings to onDarshanTimingsClick,
        strings.templeLocation to onTempleLocationClick,
        strings.contactUs to onContactUsClick
    )

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        services.forEach { (title, onClick) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { 
                        haptic.playPremiumHaptic()
                        onClick() 
                    },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        modifier = Modifier.weight(1f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = AccentGolden,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LiveDarshanAnnouncement(
    title: String,
    onDismiss: () -> Unit,
    onClick: () -> Unit,
    haptic: HapticManager,
    strings: AppStrings
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { 
                haptic.playPremiumHaptic()
                onClick() 
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Red.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color.Red, CircleShape)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🔴 LIVE NOW",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Red
                )
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        fontSize = 22.sp,
        fontWeight = FontWeight.ExtraBold,
        color = TextWhite
    )
}

@Composable
fun TodaySuvicharSection(
    strings: AppStrings,
    haptic: HapticManager,
    onSuvicharClick: (String) -> Unit
) {
    val todaySuvichar = SuvicharManager.getTodaySuvichar()
    val isHindi = com.example.miyank.LanguageManager.currentLanguage == com.example.miyank.Language.HINDI

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        // Section Header with Title & Subtitle
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (isHindi) "दैनिक सुविचार" else "Daily Suvichar",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isHindi) "आज का दिव्य संदेश" else "Today's Divine Message",
                fontSize = 12.sp,
                color = AccentGolden,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (todaySuvichar != null) {
            // COMPACT PREMIUM SUVICHAR CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        haptic.playPremiumHaptic()
                        onSuvicharClick(todaySuvichar.id)
                    },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Side: Compact 4:5 Thumbnail Poster Preview
                    Card(
                        modifier = Modifier
                            .width(110.dp)
                            .height(138.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.5f))
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = todaySuvichar.imageUrl,
                                contentDescription = strings.todaySuvicharTitle,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Subtle Dark Overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                        )
                                    )
                            )

                            // Glass Date Tag on Thumbnail
                            Surface(
                                color = Color.Black.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(topStart = 16.dp, bottomEnd = 10.dp),
                                modifier = Modifier.align(Alignment.TopStart)
                            ) {
                                Text(
                                    text = formatDisplayDate(todaySuvichar.date),
                                    color = AccentGolden,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // Right Side: Content, Quote, Author & Action Pill
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            // Section Badge inside card
                            Surface(
                                color = AccentGolden.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (isHindi) "आज का संदेश" else "Today's Message",
                                    color = AccentGolden,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Quote Text Preview
                            if (todaySuvichar.quoteText.isNotEmpty()) {
                                Text(
                                    text = "“${todaySuvichar.quoteText}”",
                                    color = TextWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 3,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }

                            // Author
                            Text(
                                text = "— ${todaySuvichar.author}",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Action Pill
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = AccentGolden,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = if (isHindi) "पढ़ें ➔" else "Read ➔",
                                    color = BackgroundDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TodayAnnouncementSection(
    haptic: HapticManager,
    onAnnouncementsClick: () -> Unit
) {
    val announcements by com.example.miyank.AnnouncementManager.announcements.collectAsState()
    val isHindi = com.example.miyank.LanguageManager.currentLanguage == com.example.miyank.Language.HINDI

    val latestAnnouncements = remember(announcements) {
        com.example.miyank.AnnouncementManager.getLatestImportantAnnouncements(2)
    }

    if (latestAnnouncements.isEmpty()) return

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isHindi) "महत्वपूर्ण घोषणाएं" else "Announcements",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextWhite
                )
                Text(
                    text = if (isHindi) "दरबार की महत्वपूर्ण सूचनाएं" else "Important Temple Notices",
                    fontSize = 12.sp,
                    color = AccentGolden,
                    fontWeight = FontWeight.Medium
                )
            }

            TextButton(onClick = {
                haptic.playPremiumHaptic()
                onAnnouncementsClick()
            }) {
                Text(if (isHindi) "सभी देखें ➔" else "View All ➔", color = AccentGolden, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            latestAnnouncements.forEach { ann ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            haptic.playPremiumHaptic()
                            onAnnouncementsClick()
                        },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(
                        1.dp,
                        if (ann.isUrgent) Color.Red.copy(alpha = 0.5f) else WarmGold.copy(alpha = 0.35f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (ann.imageUrl.isNotEmpty()) {
                            Card(
                                modifier = Modifier
                                    .size(60.dp)
                                    .padding(end = 12.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                AsyncImage(
                                    model = ann.imageUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (ann.isUrgent) {
                                        Surface(
                                            color = Color.Red.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.padding(end = 6.dp)
                                        ) {
                                            Text("🔴 अति आवश्यक", color = Color.Red, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                    Surface(
                                        color = AccentGolden.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = com.example.miyank.AnnouncementCategories.getDisplayName(ann.categoryTag, isHindi),
                                            color = AccentGolden,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = formatDisplayDate(ann.dateStr),
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (isHindi || ann.titleEnglish.isEmpty()) ann.title else ann.titleEnglish,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

