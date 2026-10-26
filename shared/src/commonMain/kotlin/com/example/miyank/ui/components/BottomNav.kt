package com.example.miyank.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.miyank.LocalStrings
import com.example.miyank.navigation.Screen
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.AccentGolden
import com.example.miyank.ui.theme.BackgroundDark
import com.example.miyank.ui.theme.TextMuted

@Composable
fun BottomNavigationBar(
    navController: NavController
) {
    val haptic = rememberHapticManager()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val strings = LocalStrings.current

    NavigationBar(
        containerColor = BackgroundDark,
        tonalElevation = 0.dp
    ) {
        val items = listOf(
            Triple(strings.navHome, Icons.Default.Home, Screen.Home as Any),
            Triple(strings.navEvents, Icons.Default.Event, Screen.Events as Any),
            Triple(strings.navGallery, Icons.Default.Image, Screen.Gallery as Any),
            Triple(strings.navDonate, Icons.Default.Favorite, Screen.Donation as Any),
            Triple(strings.navProfile, Icons.Default.Person, Screen.Profile as Any)
        )

        items.forEach { (label, icon, screen) ->
            val screenClassName = screen::class.simpleName ?: ""
            val isSelected = currentDestination?.route?.contains(screenClassName) == true
            
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    haptic.playPremiumHaptic()
                    navController.navigate(screen) {
                        popUpTo(Screen.Home) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(if (isSelected) 26.dp else 24.dp)
                    )
                },
                label = { 
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        maxLines = 1
                    ) 
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AccentGolden,
                    selectedTextColor = AccentGolden,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
