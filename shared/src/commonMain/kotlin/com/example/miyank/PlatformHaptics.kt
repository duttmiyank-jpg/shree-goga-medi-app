package com.example.miyank

import androidx.compose.runtime.Composable

@Composable
expect fun rememberHapticManager(): HapticManager

interface HapticManager {
    fun playPremiumHaptic()
}

// Global helper for simple non-composable access if needed
expect fun playPremiumHapticGlobal()

fun premiumHaptic(context: Any? = null) {
    playPremiumHapticGlobal()
}
