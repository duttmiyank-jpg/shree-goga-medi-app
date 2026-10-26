package com.example.miyank

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.io.File

actual class AudioPlayer {

    companion object {
        private var defaultMediaPlayer: MediaPlayer? = null
        private var bhajanMediaPlayer: MediaPlayer? = null
        private var currentBhajanSource: String? = null
        private var currentDefaultSource: String? = null
        private var shouldPlayDefaultOnPrepare = false
        private var shouldPlayBhajanOnPrepare = false
        private val handler = Handler(Looper.getMainLooper())

        private fun releasePlayerAsync(player: MediaPlayer?) {
            if (player == null) return
            Thread {
                try { player.stop() } catch (e: Exception) {}
                try { player.release() } catch (e: Exception) {}
            }.start()
        }
    }

    actual fun playDefaultMusic(url: String) {
        val context = ContextProvider.context
        val targetSource = url.trim()

        try {
            // CRITICAL GUARD: DO NOT PLAY DEFAULT MUSIC IF OTHER MEDIA / BHAJAN IS ACTIVE!
            if (MusicManager.isOtherMediaPlaying.value || bhajanMediaPlayer?.isPlaying == true) {
                println("AudioPlayer: Other media or Bhajan is active. Skipping default music.")
                return
            }

            if (defaultMediaPlayer != null && currentDefaultSource == targetSource) {
                shouldPlayDefaultOnPrepare = true
                try {
                    if (defaultMediaPlayer?.isPlaying == false) {
                        defaultMediaPlayer?.start()
                    }
                } catch (e: Exception) {}
                return
            }

            val oldPlayer = defaultMediaPlayer
            defaultMediaPlayer = null
            currentDefaultSource = null
            releasePlayerAsync(oldPlayer)

            shouldPlayDefaultOnPrepare = true

            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                if (targetSource.isNotEmpty() && targetSource != "DEFAULT") {
                    val file = File(targetSource)
                    if (file.exists() && file.length() > 0) {
                        setDataSource(file.absolutePath)
                    } else {
                        val afd = context.assets.openFd("background_bhajan.mp3")
                        setDataSource(afd)
                        afd.close()
                    }
                } else {
                    val afd = context.assets.openFd("background_bhajan.mp3")
                    setDataSource(afd)
                    afd.close()
                }

                setOnPreparedListener {
                    if (this == defaultMediaPlayer) {
                        try {
                            it.setVolume(0.8f, 0.8f)
                            it.isLooping = true
                            if (shouldPlayDefaultOnPrepare) {
                                it.start()
                            }
                        } catch (e: Exception) {}
                    } else {
                        releasePlayerAsync(it)
                    }
                }
                
                setOnErrorListener { _, _, _ ->
                    releasePlayerAsync(this)
                    if (this == defaultMediaPlayer) {
                        defaultMediaPlayer = null
                        currentDefaultSource = null
                    }
                    true
                }
                
                prepareAsync()
            }

            currentDefaultSource = targetSource
            defaultMediaPlayer = mp
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun stopDefaultMusic() {
        shouldPlayDefaultOnPrepare = false
        releasePlayerAsync(defaultMediaPlayer)
        defaultMediaPlayer = null
        currentDefaultSource = null
    }

    actual fun play(url: String) {
        val targetSource = url.trim()
        val context = ContextProvider.context

        try {
            // 1. IMMEDIATELY STOP AND RELEASE DEFAULT BACKGROUND MUSIC
            stopDefaultMusic()
            MusicManager.setOtherMediaPlaying(true)

            if (bhajanMediaPlayer != null && currentBhajanSource == targetSource) {
                shouldPlayBhajanOnPrepare = true
                try {
                    if (bhajanMediaPlayer?.isPlaying == false) {
                        bhajanMediaPlayer?.start()
                    }
                } catch (e: Exception) {}
                BhajanPlaybackService.startOrUpdateService(context, "श्री गोगा जी महाराज भजन", "श्री जामनीवाला दरबार", true)
                return
            }

            val oldPlayer = bhajanMediaPlayer
            bhajanMediaPlayer = null
            currentBhajanSource = null
            releasePlayerAsync(oldPlayer)

            shouldPlayBhajanOnPrepare = true
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                if (targetSource.startsWith("file:///android_asset/")) {
                    val assetName = targetSource.substringAfter("file:///android_asset/")
                    val afd = context.assets.openFd(assetName)
                    setDataSource(afd)
                    afd.close()
                } else if (targetSource.startsWith("content://") || targetSource.startsWith("file://")) {
                    setDataSource(context, android.net.Uri.parse(targetSource))
                } else if (targetSource.startsWith("http://") || targetSource.startsWith("https://")) {
                    setDataSource(targetSource)
                } else {
                    val file = File(targetSource)
                    if (file.exists() && file.length() > 0) {
                        setDataSource(file.absolutePath)
                    } else {
                        val afd = context.assets.openFd("background_bhajan.mp3")
                        setDataSource(afd)
                        afd.close()
                    }
                }

                setOnPreparedListener {
                    if (this == bhajanMediaPlayer) {
                        try {
                            it.setVolume(1f, 1f)
                            it.isLooping = true
                            if (shouldPlayBhajanOnPrepare) {
                                it.start()
                            }
                        } catch (e: Exception) {}
                    } else {
                        releasePlayerAsync(it)
                    }
                }
                
                setOnErrorListener { _, _, _ ->
                    releasePlayerAsync(this)
                    if (this == bhajanMediaPlayer) {
                        bhajanMediaPlayer = null
                        currentBhajanSource = null
                    }
                    true
                }
                
                prepareAsync()
            }

            currentBhajanSource = targetSource
            bhajanMediaPlayer = mp

            BhajanPlaybackService.startOrUpdateService(context, "श्री गोगा जी महाराज भजन", "श्री जामनीवाला दरबार", true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun playSegment(url: String, startMs: Long, endMs: Long) {
        stop()
        if (url.trim().isEmpty()) return
        play(url)
    }

    actual fun resume() {
        try {
            stopDefaultMusic()
            MusicManager.setOtherMediaPlaying(true)
            shouldPlayBhajanOnPrepare = true
            bhajanMediaPlayer?.start()
            val context = ContextProvider.context
            BhajanPlaybackService.startOrUpdateService(context, "श्री गोगा जी महाराज भजन", "श्री जामनीवाला दरबार", true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun seekTo(positionMs: Long) {
        try {
            bhajanMediaPlayer?.seekTo(positionMs.toInt())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun getCurrentPosition(): Long {
        return try {
            bhajanMediaPlayer?.currentPosition?.toLong() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    actual fun getDuration(): Long {
        return try {
            val dur = bhajanMediaPlayer?.duration?.toLong() ?: 0L
            if (dur > 0L) dur else 320000L
        } catch (e: Exception) {
            320000L
        }
    }

    actual fun isPrepared(): Boolean {
        return bhajanMediaPlayer != null
    }

    actual fun pause() {
        shouldPlayBhajanOnPrepare = false
        try {
            bhajanMediaPlayer?.pause()
        } catch (e: Exception) {}
        val context = ContextProvider.context
        BhajanPlaybackService.startOrUpdateService(context, "श्री गोगा जी महाराज भजन", "श्री जामनीवाला दरबार", false)
    }

    actual fun stop() {
        shouldPlayBhajanOnPrepare = false
        releasePlayerAsync(bhajanMediaPlayer)
        bhajanMediaPlayer = null
        currentBhajanSource = null
        val context = ContextProvider.context
        BhajanPlaybackService.stopService(context)
        MusicManager.setOtherMediaPlaying(false)
    }

    actual fun isPlaying(): Boolean = try { bhajanMediaPlayer?.isPlaying ?: false } catch (e: Exception) { false }
}

@Composable
actual fun rememberAudioPlayer(): AudioPlayer {
    return remember { AudioPlayer() }
}
