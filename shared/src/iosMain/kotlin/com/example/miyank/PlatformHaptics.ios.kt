package com.example.miyank

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle

class IosHapticManager : HapticManager {
    override fun playPremiumHaptic() {
        val generator = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium)
        generator.prepare()
        generator.impactOccurred()
    }
}

@Composable
actual fun rememberHapticManager(): HapticManager {
    return remember { IosHapticManager() }
}

actual fun playPremiumHapticGlobal() {
    val generator = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium)
    generator.prepare()
    generator.impactOccurred()
}
