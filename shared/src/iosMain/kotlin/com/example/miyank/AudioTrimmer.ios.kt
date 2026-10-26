package com.example.miyank

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

actual class AudioTrimmer {
    actual fun getAudioMetadata(uriOrPath: String): AudioMetadata? {
        return AudioMetadata(uriOrPath, "audio_track.mp3", 60000L, 1000000L)
    }

    actual fun trimAudio(
        inputUriOrPath: String,
        outputFileName: String,
        startMs: Long,
        endMs: Long
    ): String? {
        return inputUriOrPath
    }
}

@Composable
actual fun rememberAudioTrimmer(): AudioTrimmer {
    return remember { AudioTrimmer() }
}
