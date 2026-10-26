package com.example.miyank

import androidx.compose.runtime.Composable

expect class BellSoundPlayer() {
    fun playBellSound()
    fun stop()
}

@Composable
expect fun rememberBellSoundPlayer(): BellSoundPlayer
