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
fun DarbarisNavHost(
    onExitEnvironment: () -> Unit
) {
    val navController = rememberNavController()

    Scaffold { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.DarbarisPanel,
            modifier = Modifier.padding(paddingValues)
        ) {
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
                    onBackClick = { onExitEnvironment() }
                )
            }

            composable<Screen.AdminParcha> { backStackEntry ->
                val route = backStackEntry.toRoute<Screen.AdminParcha>()
                com.example.miyank.screens.AdminParchaScreen(
                    prefillQueryId = route.prefillQueryId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.AdminUserQueries> {
                AdminUserQueriesScreen(
                    onSendAsParcha = { queryId -> navController.navigate(Screen.AdminParcha(prefillQueryId = queryId)) },
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

            composable<Screen.AdminGallery> {
                AdminGalleryScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.AdminSuvichar> {
                AdminSuvicharScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.AdminLiveStream> {
                AdminLiveStreamScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
