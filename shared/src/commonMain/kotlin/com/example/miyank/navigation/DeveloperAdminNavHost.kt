package com.example.miyank.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.miyank.screens.*

@Composable
fun DeveloperAdminNavHost(
    onExitEnvironment: () -> Unit
) {
    val navController = rememberNavController()

    Scaffold { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.AdminPanel,
            modifier = Modifier.padding(paddingValues)
        ) {
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
                    onBackClick = { onExitEnvironment() }
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
        }
    }
}
