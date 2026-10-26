package com.example.miyank

import androidx.compose.runtime.Composable

expect class AudioPlayer() {
    fun play(url: String)
    fun playDefaultMusic(url: String)
    fun stopDefaultMusic()
    fun playSegment(url: String, startMs: Long, endMs: Long)
    fun pause()
    fun resume()
    fun stop()
    fun seekTo(positionMs: Long)
    fun getCurrentPosition(): Long
    fun getDuration(): Long
    fun isPlaying(): Boolean
    fun isPrepared(): Boolean
}

@Composable
expect fun rememberAudioPlayer(): AudioPlayer
