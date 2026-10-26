package com.example.miyank

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class MusicSettings(
    val musicName: String = "Shree Goga Ji Bhajan",
    val localFilePath: String? = null,
    val fileSizeFormatted: String = "1.2 MB",
    val hasCustomFile: Boolean = false,
    val isAutoPlay: Boolean = true
)

data class AartiSettings(
    val aartiName: String = "Shree Goga Ji Maharaj Ki Aarti",
    val localFilePath: String? = null,
    val fileSizeFormatted: String = "1.0 MB",
    val hasCustomFile: Boolean = false
)

object MusicManager {
    private val settingsStorage = SettingsManager()
    private const val KEY_NAME = "music_name"
    private const val KEY_PATH = "music_file_path"
    private const val KEY_SIZE = "music_file_size"

    private const val KEY_AARTI_NAME = "aarti_name"
    private const val KEY_AARTI_PATH = "aarti_file_path"
    private const val KEY_AARTI_SIZE = "aarti_file_size"

    private val _settings = MutableStateFlow(
        MusicSettings(
            musicName = settingsStorage.getString(KEY_NAME, "Shree Goga Ji Bhajan"),
            localFilePath = settingsStorage.getString(KEY_PATH, ""),
            fileSizeFormatted = settingsStorage.getString(KEY_SIZE, "1.2 MB"),
            hasCustomFile = settingsStorage.getString(KEY_PATH, "").isNotEmpty()
        )
    )
    val settings: StateFlow<MusicSettings> = _settings

    private val _aartiSettings = MutableStateFlow(
        AartiSettings(
            aartiName = settingsStorage.getString(KEY_AARTI_NAME, "Shree Goga Ji Maharaj Ki Aarti"),
            localFilePath = settingsStorage.getString(KEY_AARTI_PATH, ""),
            fileSizeFormatted = settingsStorage.getString(KEY_AARTI_SIZE, "1.0 MB"),
            hasCustomFile = settingsStorage.getString(KEY_AARTI_PATH, "").isNotEmpty()
        )
    )
    val aartiSettings: StateFlow<AartiSettings> = _aartiSettings

    val isOtherMediaPlaying = MutableStateFlow(false)

    // Global Active Bhajan Playback State
    val activeBhajanTrack = MutableStateFlow<BhajanItem?>(null)
    val isBhajanActivePlaying = MutableStateFlow(false)
    val activePosMs = MutableStateFlow(0L)
    val activeDurationMs = MutableStateFlow(320000L)

    fun updateSettings(newSettings: MusicSettings) {
        _settings.value = newSettings
        settingsStorage.setString(KEY_NAME, newSettings.musicName)
        settingsStorage.setString(KEY_PATH, newSettings.localFilePath ?: "")
        settingsStorage.setString(KEY_SIZE, newSettings.fileSizeFormatted)
    }

    fun updateAartiSettings(newSettings: AartiSettings) {
        _aartiSettings.value = newSettings
        settingsStorage.setString(KEY_AARTI_NAME, newSettings.aartiName)
        settingsStorage.setString(KEY_AARTI_PATH, newSettings.localFilePath ?: "")
        settingsStorage.setString(KEY_AARTI_SIZE, newSettings.fileSizeFormatted)
    }

    fun deleteCustomMusic() {
        val current = _settings.value
        updateSettings(
            current.copy(
                localFilePath = "",
                fileSizeFormatted = "0 MB",
                hasCustomFile = false
            )
        )
    }

    fun deleteCustomAarti() {
        val current = _aartiSettings.value
        updateAartiSettings(
            current.copy(
                localFilePath = "",
                fileSizeFormatted = "0 MB",
                hasCustomFile = false
            )
        )
    }

    fun setOtherMediaPlaying(playing: Boolean) {
        isOtherMediaPlaying.value = playing
    }

    fun playActiveBhajan(track: BhajanItem) {
        setOtherMediaPlaying(true)
        activeBhajanTrack.value = track
        isBhajanActivePlaying.value = true
    }

    fun toggleActiveBhajanPlay() {
        if (activeBhajanTrack.value != null) {
            val newState = !isBhajanActivePlaying.value
            isBhajanActivePlaying.value = newState
            if (newState) {
                setOtherMediaPlaying(true)
            }
        }
    }

    fun stopActiveBhajan() {
        isBhajanActivePlaying.value = false
        activeBhajanTrack.value = null
        setOtherMediaPlaying(false)
    }
}
