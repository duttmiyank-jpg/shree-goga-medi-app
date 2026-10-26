package com.example.miyank.navigation

import androidx.compose.runtime.Composable
import com.example.miyank.AdminSecurityManager
import com.example.miyank.UserProfileManager

@Composable
fun MandirNavHost() {
    // ALWAYS LAUNCH COMMON APPLICATION GRAPH STARTING AT SPLASH SCREEN
    UserNavHost(
        onSwitchEnvironment = {
            AdminSecurityManager.lockSession()
            UserProfileManager.clearActiveEnvironment()
        }
    )
}
