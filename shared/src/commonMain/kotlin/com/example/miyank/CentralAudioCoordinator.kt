package com.example.miyank

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class AudioPlaybackState(
    val currentTrack: BhajanItem? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val totalDurationMs: Long = 320000L,
    val isSeeking: Boolean = false,
    val isRepeat: Boolean = false,
    val isShuffle: Boolean = false
)

object CentralAudioCoordinator {
    private val _playbackState = MutableStateFlow(AudioPlaybackState())
    val playbackState: StateFlow<AudioPlaybackState> = _playbackState

    private var defaultMusicPlayer: AudioPlayer? = null
    private var defaultMusicPath: String = ""
    private var isDefaultMusicPausedForBhajan = false

    private var bhajanAudioPlayer: AudioPlayer? = null

    fun registerDefaultMusicPlayer(player: AudioPlayer, path: String) {
        defaultMusicPlayer = player
        defaultMusicPath = path
    }

    fun setBhajanAudioPlayer(player: AudioPlayer) {
        bhajanAudioPlayer = player
    }

    fun getBhajanAudioPlayer(): AudioPlayer? = bhajanAudioPlayer

    fun pauseDefaultAppMusic() {
        try {
            if (isDefaultMusicPausedForBhajan) return // Already paused
            isDefaultMusicPausedForBhajan = true
            MusicManager.setOtherMediaPlaying(true)
            defaultMusicPlayer?.stopDefaultMusic()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playBhajanTrack(track: BhajanItem, playlist: List<BhajanItem> = emptyList()) {
        pauseDefaultAppMusic()

        val player = bhajanAudioPlayer ?: AudioPlayer().also { bhajanAudioPlayer = it }
        player.play(track.audioUrl)

        _playbackState.value = _playbackState.value.copy(
            currentTrack = track,
            isPlaying = true,
            currentPositionMs = 0L
        )

        MusicManager.playActiveBhajan(track)
        BhajanManager.addRecentPlayed(track.id)
    }

    fun togglePlayPause() {
        val current = _playbackState.value
        val track = current.currentTrack ?: return
        val player = bhajanAudioPlayer ?: AudioPlayer().also { bhajanAudioPlayer = it }

        if (current.isPlaying) {
            player.pause()
            _playbackState.value = current.copy(isPlaying = false)
            MusicManager.isBhajanActivePlaying.value = false
        } else {
            pauseDefaultAppMusic()
            player.play(track.audioUrl)
            player.seekTo(current.currentPositionMs)
            _playbackState.value = current.copy(isPlaying = true)
            MusicManager.isBhajanActivePlaying.value = true
        }
    }

    fun resume() {
        val current = _playbackState.value
        if (current.currentTrack != null && !current.isPlaying) {
            togglePlayPause()
        }
    }

    fun pause() {
        val current = _playbackState.value
        if (current.isPlaying) {
            togglePlayPause()
        }
    }

    fun seekTo(positionMs: Long) {
        val player = bhajanAudioPlayer ?: return
        player.seekTo(positionMs)
        _playbackState.value = _playbackState.value.copy(currentPositionMs = positionMs)
        MusicManager.activePosMs.value = positionMs
    }

    fun updateProgress(currentMs: Long, durationMs: Long) {
        val current = _playbackState.value
        if (!current.isSeeking) {
            val validDuration = if (durationMs > 0L) durationMs else current.totalDurationMs
            _playbackState.value = current.copy(
                currentPositionMs = currentMs,
                totalDurationMs = validDuration
            )
            MusicManager.activePosMs.value = currentMs
            MusicManager.activeDurationMs.value = validDuration
        }
    }

    fun nextTrack(playlist: List<BhajanItem> = emptyList()) {
        val activeList = if (playlist.isNotEmpty()) playlist else BhajanManager.getActiveBhajans()
        if (activeList.isEmpty()) return

        val currentTrack = _playbackState.value.currentTrack
        val currIdx = activeList.indexOfFirst { it.id == currentTrack?.id }
        val nextIdx = if (currIdx >= 0) (currIdx + 1) % activeList.size else 0
        playBhajanTrack(activeList[nextIdx], activeList)
    }

    fun previousTrack(playlist: List<BhajanItem> = emptyList()) {
        val activeList = if (playlist.isNotEmpty()) playlist else BhajanManager.getActiveBhajans()
        if (activeList.isEmpty()) return

        val currentTrack = _playbackState.value.currentTrack
        val currIdx = activeList.indexOfFirst { it.id == currentTrack?.id }
        val prevIdx = if (currIdx - 1 < 0) activeList.size - 1 else currIdx - 1
        playBhajanTrack(activeList[prevIdx], activeList)
    }

    fun stopBhajan() {
        bhajanAudioPlayer?.stop()
        _playbackState.value = AudioPlaybackState()
        MusicManager.stopActiveBhajan()

        if (isDefaultMusicPausedForBhajan) {
            isDefaultMusicPausedForBhajan = false
            MusicManager.setOtherMediaPlaying(false) // This state triggers App.kt to resume background music
            defaultMusicPlayer?.playDefaultMusic(defaultMusicPath)
        }
    }
}
