package com.example.miyank

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.AudioToolbox.AudioServicesPlaySystemSound

actual class BellSoundPlayer {
    actual fun playBellSound() {
        try {
            AudioServicesPlaySystemSound(1005u)
        } catch (e: Exception) {
            // Ignore
        }
    }

    actual fun stop() {}
}

@Composable
actual fun rememberBellSoundPlayer(): BellSoundPlayer {
    return remember { BellSoundPlayer() }
}
