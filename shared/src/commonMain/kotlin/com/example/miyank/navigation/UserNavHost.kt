package com.example.miyank.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.miyank.AppEnvironment
import com.example.miyank.LanguageManager
import com.example.miyank.UserProfileManager
import com.example.miyank.UserRole
import com.example.miyank.screens.*
import com.example.miyank.ui.components.BottomNavigationBar
import com.example.miyank.ui.components.RoleNavigationDrawerContent
import kotlinx.coroutines.launch

@Composable
fun UserNavHost(
    onSwitchEnvironment: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val bottomBarScreens = listOf(
        Screen.Home::class.simpleName,
        Screen.Events::class.simpleName,
        Screen.Gallery::class.simpleName,
        Screen.Yatra::class.simpleName,
        Screen.Profile::class.simpleName,
    )
    val showBottomBar = currentDestination?.route?.split(".")?.last() in bottomBarScreens

    val activeBhajanTrack by com.example.miyank.MusicManager.activeBhajanTrack.collectAsState()
    val isBhajanPlaying by com.example.miyank.MusicManager.isBhajanActivePlaying.collectAsState()
    val currentPosMs by com.example.miyank.MusicManager.activePosMs.collectAsState()
    val totalDurationMs by com.example.miyank.MusicManager.activeDurationMs.collectAsState()
    val haptic = com.example.miyank.rememberHapticManager()
    val audioPlayer = com.example.miyank.rememberAudioPlayer()
    val activeBhajansList by com.example.miyank.BhajanManager.bhajans.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentDestination?.route?.contains("Home") == true,
        drawerContent = {
            RoleNavigationDrawerContent(
                onNavigate = { screen ->
                    scope.launch { drawerState.close() }
                    navController.navigate(screen)
                }
            )
        }
    ) {
        Scaffold(
            bottomBar = {
                Column {
                if (activeBhajanTrack != null && currentDestination?.route?.contains("Bhajans") == false) {
                    com.example.miyank.ui.components.GlobalBhajanMiniPlayer(
                        bhajan = activeBhajanTrack,
                        isPlaying = isBhajanPlaying,
                        currentPosMs = currentPosMs,
                        totalDurationMs = totalDurationMs,
                        onTogglePlay = {
                            if (isBhajanPlaying) {
                                audioPlayer.pause()
                                com.example.miyank.MusicManager.isBhajanActivePlaying.value = false
                            } else {
                                audioPlayer.resume()
                                com.example.miyank.MusicManager.isBhajanActivePlaying.value = true
                            }
                        },
                        onNext = {
                            val activeList = activeBhajansList.filter { it.isActive }
                            if (activeList.isNotEmpty() && activeBhajanTrack != null) {
                                val currIdx = activeList.indexOfFirst { it.id == activeBhajanTrack!!.id }
                                val nextIdx = (currIdx + 1) % activeList.size
                                val nextTrack = activeList[nextIdx]
                                com.example.miyank.CentralAudioCoordinator.playBhajanTrack(nextTrack, activeList)
                            }
                        },
                        onPrevious = {
                            val activeList = activeBhajansList.filter { it.isActive }
                            if (activeList.isNotEmpty() && activeBhajanTrack != null) {
                                val currIdx = activeList.indexOfFirst { it.id == activeBhajanTrack!!.id }
                                val prevIdx = if (currIdx - 1 < 0) activeList.size - 1 else currIdx - 1
                                val prevTrack = activeList[prevIdx]
                                com.example.miyank.CentralAudioCoordinator.playBhajanTrack(prevTrack, activeList)
                            }
                        },
                        onOpenFullScreen = {
                            navController.navigate(Screen.Bhajans)
                        },
                        onClose = {
                            com.example.miyank.CentralAudioCoordinator.stopBhajan()
                        },
                        haptic = haptic
                    )
                }

                if (showBottomBar) {
                    BottomNavigationBar(navController = navController)
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash,
            modifier = Modifier.padding(paddingValues)
        ) {
            // 1. SPLASH SCREEN ALWAYS EXECUTES FIRST ON EVERY APP LAUNCH
            composable<Screen.Splash> {
                SplashScreen {
                    val currentEnv = UserProfileManager.activeEnvironment.value
                    if (currentEnv == AppEnvironment.USER) {
                        navController.navigate(Screen.Home) {
                            popUpTo(Screen.Splash) { inclusive = true }
                        }
                    } else {
                        // Transient Darbari/Admin session or new launch
                        UserProfileManager.clearActiveEnvironment()
                        val targetScreen = if (LanguageManager.isLanguageSelected) {
                            Screen.Welcome
                        } else {
                            Screen.LanguageSelection
                        }
                        navController.navigate(targetScreen) {
                            popUpTo(Screen.Splash) { inclusive = true }
                        }
                    }
                }
            }

            composable<Screen.LanguageSelection> {
                LanguageSelectionScreen(
                    onContinue = {
                        navController.navigate(Screen.Welcome) {
                            popUpTo(Screen.LanguageSelection) { inclusive = true }
                        }
                    }
                )
            }

            composable<Screen.Welcome> {
                WelcomeScreen {
                    navController.navigate(Screen.AccessSelection) {
                        popUpTo(Screen.Welcome) { inclusive = true }
                    }
                }
            }

            composable<Screen.AccessSelection> {
                AccessSelectionScreen(
                    onSelectUser = {
                        UserProfileManager.updateUserRole(UserRole.USER)
                        UserProfileManager.setActiveEnvironment(AppEnvironment.USER)
                        navController.navigate(Screen.Home) {
                            popUpTo(Screen.AccessSelection) { inclusive = true }
                        }
                    },
                    onSelectDarbaris = {
                        navController.navigate(Screen.DarbarisAuth)
                    },
                    onSelectAdmin = {
                        navController.navigate(Screen.AdminAuth)
                    }
                )
            }

            composable<Screen.DarbarisAuth> {
                DarbarisAuthScreen(
                    onAuthSuccess = {
                        UserProfileManager.updateUserRole(UserRole.DARBARIS)
                        UserProfileManager.setActiveEnvironment(AppEnvironment.DARBARIS)
                        navController.navigate(Screen.Home) {
                            popUpTo(Screen.DarbarisAuth) { inclusive = true }
                        }
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable<Screen.AdminAuth> {
                AdminAuthScreen(
                    onAuthSuccess = {
                        UserProfileManager.updateUserRole(UserRole.DEVELOPER_ADMIN)
                        UserProfileManager.setActiveEnvironment(AppEnvironment.DEVELOPER_ADMIN)
                        navController.navigate(Screen.Home) {
                            popUpTo(Screen.AdminAuth) { inclusive = true }
                        }
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable<Screen.Home> {
                HomeScreen(
                    onEventsClick = { navController.navigate(Screen.Events) },
                    onGalleryClick = { navController.navigate(Screen.Gallery) },
                    onLiveDarshanClick = {
                        audioPlayer.stopDefaultMusic()
                        com.example.miyank.MusicManager.setOtherMediaPlaying(true)
                        navController.navigate(Screen.LiveDarshan)
                    },
                    onYatraClick = { navController.navigate(Screen.Yatra) },
                    onDonationClick = { navController.navigate(Screen.Donation) },
                    onBhajansClick = {
                        audioPlayer.stopDefaultMusic()
                        com.example.miyank.MusicManager.setOtherMediaPlaying(true)
                        navController.navigate(Screen.Bhajans)
                    },
                    onNotificationClick = { navController.navigate(Screen.NotificationHistory) },
                    onDarshanTimingsClick = { navController.navigate(Screen.DarshanTimings) },
                    onAboutTempleClick = { navController.navigate(Screen.AboutTemple) },
                    onPanchangClick = { navController.navigate(Screen.Panchang) },
                    onVirtualPujaClick = { navController.navigate(Screen.VirtualPuja) },
                    onTempleLocationClick = { navController.navigate(Screen.TempleLocation) },
                    onMandirCalendarClick = { navController.navigate(Screen.MandirCalendar) },
                    onBhajanLyricsClick = {
                        audioPlayer.stopDefaultMusic()
                        com.example.miyank.MusicManager.setOtherMediaPlaying(true)
                        navController.navigate(Screen.Bhajans)
                    },
                    onAnnouncementsClick = { navController.navigate(Screen.Announcements) },
                    onContactUsClick = { navController.navigate(Screen.ContactUs) },
                    onSuvicharClick = { id -> navController.navigate(Screen.SuvicharPreview(id)) },
                    onAboutDarbarClick = { navController.navigate(Screen.BlogListing) },
                    onDarbarisClick = { navController.navigate(Screen.Darbaris) },
                    onPujaClick = { navController.navigate(Screen.Puja) },
                    onRashiClick = {
                        val hasResult = com.example.miyank.astrology.RashiManager.calculationResult.value != null
                        if (hasResult) {
                            navController.navigate(Screen.RashiResult)
                        } else {
                            navController.navigate(Screen.RashiInput)
                        }
                    },
                    onStoreClick = { navController.navigate(Screen.DarbarStore) },
                    onParchaClick = {
                        if (com.example.miyank.ParchaManager.isVerifiedSession.value) {
                            navController.navigate(Screen.Parcha)
                        } else {
                            navController.navigate(Screen.ParchaOtpVerification())
                        }
                    },
                    onDarbarisPanelClick = { navController.navigate(Screen.DarbarisPanel) },
                    onAdminPanelClick = { navController.navigate(Screen.AdminPanel) },
                    onSponsoredClick = { navController.navigate(Screen.Sponsored) },
                    onSuggestionsClick = { navController.navigate(Screen.Suggestions) },
                    onChalisaStotramClick = { navController.navigate(Screen.ChalisaStotram) },
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            }

            composable<Screen.RashiInput> {
                RashiInputScreen(
                    onCalculateSuccess = {
                        navController.navigate(Screen.RashiResult) {
                            popUpTo(Screen.RashiInput) { inclusive = true }
                        }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.RashiResult> {
                RashiResultScreen(
                    onEditDetailsClick = {
                        navController.navigate(Screen.RashiInput)
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.Events> {
                EventsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.Gallery> {
                GalleryScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.LiveDarshan> {
                LiveDarshanScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.Yatra> {
                YatraScreen(
                    onBookSeatsClick = { yatraId -> navController.navigate(Screen.YatraBooking(yatraId)) },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.YatraBooking> { backStackEntry ->
                val route = backStackEntry.toRoute<Screen.YatraBooking>()
                YatraBookingScreen(
                    yatraId = route.yatraId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.Donation> {
                DonationScreen(
                    onMyReceiptsClick = { navController.navigate(Screen.MyDonationReceipts) },
                    onReceiptGeneratedClick = { txnId ->
                        navController.navigate(Screen.DonationPaymentStatus(txnId))
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.MyDonationReceipts> {
                com.example.miyank.screens.donation.MyDonationReceiptsScreen(
                    onReceiptClick = { txnId ->
                        navController.navigate(Screen.DonationReceipt(txnId))
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.DonationPaymentStatus> { backStackEntry ->
                val route = backStackEntry.toRoute<Screen.DonationPaymentStatus>()
                com.example.miyank.screens.donation.DonationPaymentStatusScreen(
                    transactionId = route.transactionId,
                    onViewReceiptClick = { txnId -> navController.navigate(Screen.DonationReceipt(txnId)) },
                    onMyReceiptsClick = { navController.navigate(Screen.MyDonationReceipts) },
                    onBackToDonationClick = { navController.navigate(Screen.Donation) }
                )
            }

            composable<Screen.DonationReceipt> { backStackEntry ->
                val route = backStackEntry.toRoute<Screen.DonationReceipt>()
                com.example.miyank.screens.donation.DonationReceiptScreen(
                    transactionId = route.transactionId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.Profile> {
                ProfileScreen(
                    onNotificationClick = { navController.navigate(Screen.NotificationHistory) },
                    onSettingsClick = { navController.navigate(Screen.Settings) },
                    onAdminClick = { navController.navigate(Screen.AdminPanel) },
                    onDarbarisPanelClick = { navController.navigate(Screen.DarbarisPanel) },
                    onRashiClick = {
                        val hasResult = com.example.miyank.astrology.RashiManager.calculationResult.value != null
                        if (hasResult) {
                            navController.navigate(Screen.RashiResult)
                        } else {
                            navController.navigate(Screen.RashiInput)
                        }
                    },
                    onOrdersClick = { navController.navigate(Screen.MyOrders) },
                    onDonationsClick = { navController.navigate(Screen.MyDonationReceipts) },
                    onYatraClick = { navController.navigate(Screen.Yatra) },
                    onLanguageClick = { navController.navigate(Screen.LanguageSelection) },
                    onContactUsClick = { navController.navigate(Screen.ContactUs) },
                    onParchaClick = {
                        if (com.example.miyank.ParchaManager.isVerifiedSession.value) {
                            navController.navigate(Screen.Parcha)
                        } else {
                            navController.navigate(Screen.ParchaOtpVerification())
                        }
                    },
                    onSwitchRoleClick = {
                        UserProfileManager.clearActiveEnvironment()
                        navController.navigate(Screen.AccessSelection) {
                            popUpTo(Screen.Home) { inclusive = true }
                        }
                    }
                )
            }

            composable<Screen.DarbarisPanel> {
                DarbarisPanelScreen(
                    onManageParcha = { navController.navigate(Screen.AdminParcha()) },
                    onManageUserQueries = { navController.navigate(Screen.AdminUserQueries) },
                    onManagePuja = { navController.navigate(Screen.AdminPuja) },
                    onManageYatra = { navController.navigate(Screen.AdminYatra) },
                    onManageCalendar = { navController.navigate(Screen.AdminMandirCalendar) },
                    onManageAnnouncements = { navController.navigate(Screen.AdminAnnouncements) },
                    onManageEvents = { navController.navigate(Screen.AdminEvents) },
                    onManageGallery = { navController.navigate(Screen.AdminGallery) },
                    onManageSuvichar = { navController.navigate(Screen.AdminSuvichar) },
                    onManageLiveStream = { navController.navigate(Screen.AdminLiveStream) },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.AdminPanel> {
                AdminPanelScreen(
                    onManageAnnouncements = { navController.navigate(Screen.AdminAnnouncements) },
                    onManageEvents = { navController.navigate(Screen.AdminEvents) },
                    onSendNotification = { navController.navigate(Screen.AdminSendNotification) },
                    onManageGallery = { navController.navigate(Screen.AdminGallery) },
                    onManageUserQueries = { navController.navigate(Screen.AdminUserQueries) },
                    onViewAnalytics = { navController.navigate(Screen.AdminAnalytics) },
                    onManageBanners = { navController.navigate(Screen.AdminBanners) },
                    onManageAppSettings = { navController.navigate(Screen.AdminAppSettings) },
                    onManageLiveStream = { navController.navigate(Screen.AdminLiveStream) },
                    onManageBhajans = { navController.navigate(Screen.AdminBhajans) },
                    onManageSuvichar = { navController.navigate(Screen.AdminSuvichar) },
                    onManageAboutDarbar = { navController.navigate(Screen.AdminBlog) },
                    onManageMandirCalendar = { navController.navigate(Screen.AdminMandirCalendar) },
                    onManageDarbaris = { navController.navigate(Screen.AdminDarbaris) },
                    onManagePuja = { navController.navigate(Screen.AdminPuja) },
                    onManageYatra = { navController.navigate(Screen.AdminYatra) },
                    onManageStore = { navController.navigate(Screen.AdminStore) },
                    onManageDonation = { navController.navigate(Screen.AdminDonation) },
                    onManageParcha = { navController.navigate(Screen.AdminParcha()) },
                    onManageSponsored = { navController.navigate(Screen.AdminSponsored) },
                    onManageSuggestions = { navController.navigate(Screen.AdminSuggestions) },
                    onManageDevotionalContent = { navController.navigate(Screen.AdminDevotionalContent) },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.AdminStore> {
                com.example.miyank.screens.store.AdminStoreScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.AdminDonation> {
                com.example.miyank.screens.donation.AdminDonationScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.AdminPuja> {
                AdminPujaScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.AdminYatra> {
                AdminYatraScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.AdminMandirCalendar> {
                AdminMandirCalendarScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.AdminAnnouncements> {
                AdminAnnouncementsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.AdminEvents> {
                AdminEventsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.AdminSendNotification> {
                AdminSendNotificationScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.AdminGallery> {
                AdminGalleryScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.AdminUserQueries> {
                AdminUserQueriesScreen(
                    onSendAsParcha = { queryId -> navController.navigate(Screen.AdminParcha(prefillQueryId = queryId)) },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.AdminParcha> { backStackEntry ->
                val route = backStackEntry.toRoute<Screen.AdminParcha>()
                com.example.miyank.screens.AdminParchaScreen(
                    prefillQueryId = route.prefillQueryId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.AdminAnalytics> {
                AdminAnalyticsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.AdminBanners> {
                AdminBannersScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.AdminAppSettings> {
                AdminAppSettingsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.AdminLiveStream> {
                AdminLiveStreamScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.AdminMusicSettings> {
                AdminMusicSettingsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.AdminSuvichar> {
                AdminSuvicharScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.AdminBlog> {
                AdminBlogScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.AdminBhajans> {
                AdminBhajansScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.AdminDarbaris> {
                AdminDarbarisScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.Sponsored> {
                SponsoredScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.Suggestions> {
                SuggestionsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.EngineersMessage> {
                EngineersMessageScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.ChalisaStotram> {
                ChalisaStotramScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.AdminSponsored> {
                AdminSponsoredScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.AdminSuggestions> {
                AdminSuggestionsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.AdminDevotionalContent> {
                AdminDevotionalContentScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.Panchang> {
                PanchangScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.VirtualPuja> {
                VirtualPujaScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.TempleLocation> {
                TempleLocationScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.MandirCalendar> {
                MandirCalendarScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.Settings> {
                SettingsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.Bhajans> {
                BhajansScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.BhajanLyrics> {
                BhajanLyricsScreen(
                    onBhajanClick = { item ->
                        navController.navigate(Screen.BhajanLyricsDetail(item.title, item.lyricsText))
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.BhajanLyricsDetail> { backStackEntry ->
                val route = backStackEntry.toRoute<Screen.BhajanLyricsDetail>()
                BhajanLyricsDetailScreen(
                    title = route.title,
                    lyrics = route.lyrics,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.AboutTemple> {
                AboutTempleScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.DarbarStore> {
                com.example.miyank.screens.store.DarbarStoreScreen(
                    onProductClick = { id -> navController.navigate(Screen.ProductDetail(id)) },
                    onCartClick = { navController.navigate(Screen.Cart) },
                    onMyOrdersClick = { navController.navigate(Screen.MyOrders) },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.ProductDetail> { backStackEntry ->
                val route = backStackEntry.toRoute<Screen.ProductDetail>()
                com.example.miyank.screens.store.ProductDetailScreen(
                    productId = route.productId,
                    onCartClick = { navController.navigate(Screen.Cart) },
                    onBuyNowClick = { navController.navigate(Screen.Cart) },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.Cart> {
                com.example.miyank.screens.store.CartScreen(
                    onProceedToAddressClick = { navController.navigate(Screen.CheckoutAddress) },
                    onContinueShoppingClick = { navController.navigate(Screen.DarbarStore) },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.CheckoutAddress> {
                com.example.miyank.screens.store.CheckoutAddressScreen(
                    onAddressSavedClick = { navController.navigate(Screen.Checkout) },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.Checkout> {
                com.example.miyank.screens.store.CheckoutScreen(
                    onOrderPlacedSuccess = { orderId ->
                        navController.navigate(Screen.OrderSuccess(orderId)) {
                            popUpTo(Screen.DarbarStore) { inclusive = false }
                        }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.OrderSuccess> { backStackEntry ->
                val route = backStackEntry.toRoute<Screen.OrderSuccess>()
                com.example.miyank.screens.store.OrderSuccessScreen(
                    orderId = route.orderId,
                    onViewMyOrdersClick = { navController.navigate(Screen.MyOrders) },
                    onBackToStoreClick = { navController.navigate(Screen.DarbarStore) }
                )
            }

            composable<Screen.MyOrders> {
                com.example.miyank.screens.store.MyOrdersScreen(
                    onOrderClick = { orderId -> navController.navigate(Screen.OrderDetail(orderId)) },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.OrderDetail> { backStackEntry ->
                val route = backStackEntry.toRoute<Screen.OrderDetail>()
                com.example.miyank.screens.store.OrderDetailScreen(
                    orderId = route.orderId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.Puja> {
                PujaScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.Darbaris> {
                DarbarisScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.Parcha> {
                com.example.miyank.screens.ParchaScreen(
                    onParchaClick = { parchaId -> navController.navigate(Screen.ParchaDetail(parchaId)) },
                    onRequestOtpVerification = {
                        navController.navigate(Screen.ParchaOtpVerification()) {
                            popUpTo(Screen.Parcha) { inclusive = true }
                        }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.ParchaOtpVerification> { backStackEntry ->
                val route = backStackEntry.toRoute<Screen.ParchaOtpVerification>()
                com.example.miyank.screens.ParchaOtpVerificationScreen(
                    initialMobileNumber = route.registeredMobile,
                    onVerificationSuccess = {
                        navController.navigate(Screen.Parcha) {
                            popUpTo(Screen.ParchaOtpVerification()) { inclusive = true }
                        }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.ParchaDetail> { backStackEntry ->
                val route = backStackEntry.toRoute<Screen.ParchaDetail>()
                com.example.miyank.screens.ParchaDetailScreen(
                    parchaId = route.parchaId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.SuvicharPreview> { backStackEntry ->
                val route = backStackEntry.toRoute<Screen.SuvicharPreview>()
                SuvicharPreviewScreen(
                    suvicharId = route.suvicharId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.Announcements> {
                AnnouncementsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.ContactUs> {
                ContactUsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.DarshanTimings> {
                DarshanTimingsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.NotificationHistory> {
                NotificationHistoryScreen(
                    onBackClick = { navController.popBackStack() },
                    onNavigateTarget = { route, id ->
                        when (route.uppercase()) {
                            com.example.miyank.TargetRoutes.EVENTS -> navController.navigate(Screen.Events)
                            com.example.miyank.TargetRoutes.LIVE_DARSHAN -> navController.navigate(Screen.LiveDarshan)
                            com.example.miyank.TargetRoutes.YATRA -> navController.navigate(Screen.Yatra)
                            com.example.miyank.TargetRoutes.PUJA -> navController.navigate(Screen.Puja)
                            com.example.miyank.TargetRoutes.GALLERY -> navController.navigate(Screen.Gallery)
                            com.example.miyank.TargetRoutes.ABOUT_TEMPLE -> navController.navigate(Screen.AboutTemple)
                            com.example.miyank.TargetRoutes.DONATION -> navController.navigate(Screen.Donation)
                            com.example.miyank.TargetRoutes.ANNOUNCEMENTS -> navController.navigate(Screen.Announcements)
                            com.example.miyank.TargetRoutes.SUVICHAR -> {
                                if (id.isNotEmpty()) navController.navigate(Screen.SuvicharPreview(id))
                                else navController.navigate(Screen.Home)
                            }
                            else -> navController.navigate(Screen.Home)
                        }
                    }
                )
            }

            composable<Screen.BlogListing> {
                BlogListingScreen(
                    onBlogClick = { blogId ->
                        navController.navigate(Screen.BlogDetail(blogId))
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.BlogDetail> { backStackEntry ->
                val route = backStackEntry.toRoute<Screen.BlogDetail>()
                BlogDetailScreen(
                    blogId = route.blogId,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
}
