package com.example.miyank

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.miyank.navigation.MandirNavHost

@Composable
fun App() {
    val audioPlayer = rememberAudioPlayer()
    val musicSettings by MusicManager.settings.collectAsState()
    val isOtherMediaPlaying by MusicManager.isOtherMediaPlaying.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    val currentAudioPath = musicSettings.localFilePath ?: ""

    LaunchedEffect(currentAudioPath) {
        CentralAudioCoordinator.registerDefaultMusicPlayer(audioPlayer, currentAudioPath)
    }

    // Auto-start continuous background music unless other media is active
    LaunchedEffect(currentAudioPath, isOtherMediaPlaying) {
        if (isOtherMediaPlaying) {
            audioPlayer.stopDefaultMusic()
        } else {
            audioPlayer.playDefaultMusic(currentAudioPath)
        }
    }

    // Stop/Pause music ONLY when app is exited or minimized to background
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                audioPlayer.stopDefaultMusic()
            } else if (event == Lifecycle.Event.ON_RESUME) {
                if (!MusicManager.isOtherMediaPlaying.value) {
                    audioPlayer.playDefaultMusic(currentAudioPath)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            audioPlayer.stopDefaultMusic()
        }
    }

    ProvideStrings {
        MandirNavHost()
    }
}
