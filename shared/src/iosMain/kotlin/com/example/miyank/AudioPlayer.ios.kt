package com.example.miyank

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.AVFoundation.*
import platform.Foundation.NSURL
import platform.Foundation.NSBundle

actual class AudioPlayer {
    private var player: AVPlayer? = null
    private var currentSource: String? = null

    actual fun playDefaultMusic(url: String) {
        play(url)
    }

    actual fun stopDefaultMusic() {
        stop()
    }

    actual fun play(url: String) {
        val isDefault = url.isEmpty()
        val sourceKey = if (isDefault) "DEFAULT" else url
        if (player != null && currentSource == sourceKey && isPlaying()) return

        player?.pause()
        currentSource = sourceKey

        val nsUrl = if (!isDefault && (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("/"))) {
            NSURL.fileURLWithPath(url)
        } else {
            NSBundle.mainBundle.URLForResource("background_bhajan", "mp3")
        } ?: NSBundle.mainBundle.URLForResource("background_bhajan", "mp3")

        if (nsUrl != null) {
            player = AVPlayer(nsUrl)
            player?.play()
        }
    }

    actual fun playSegment(url: String, startMs: Long, endMs: Long) {
        play(url)
    }

    actual fun resume() {
        player?.play()
    }

    actual fun seekTo(positionMs: Long) {
    }

    actual fun getCurrentPosition(): Long {
        return 0L
    }

    actual fun getDuration(): Long {
        return 320000L
    }

    actual fun isPrepared(): Boolean {
        return player != null
    }

    actual fun pause() {
        player?.pause()
    }

    actual fun stop() {
        player?.pause()
        player = null
        currentSource = null
    }

    actual fun isPlaying(): Boolean {
        return player?.rate != 0f
    }
}

@Composable
actual fun rememberAudioPlayer(): AudioPlayer {
    return remember { AudioPlayer() }
}
