package com.example.miyank

import androidx.compose.runtime.Composable

data class AudioMetadata(
    val uriOrPath: String,
    val fileName: String,
    val durationMs: Long,
    val sizeBytes: Long
)

expect class AudioTrimmer() {
    fun getAudioMetadata(uriOrPath: String): AudioMetadata?
    fun trimAudio(
        inputUriOrPath: String,
        outputFileName: String,
        startMs: Long,
        endMs: Long
    ): String?
}

@Composable
expect fun rememberAudioTrimmer(): AudioTrimmer
