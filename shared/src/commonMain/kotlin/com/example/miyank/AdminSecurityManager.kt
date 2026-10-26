package com.example.miyank

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Admin Security & Session Manager
 */
object AdminSecurityManager {

    private val settingsStorage = SettingsManager()
    private const val KEY_ADMIN_PIN = "admin_security_pin_v1"
    private const val DEFAULT_PIN = "2026"

    private val _isSessionUnlocked = MutableStateFlow(false)
    val isSessionUnlocked: StateFlow<Boolean> = _isSessionUnlocked

    fun getStoredPin(): String {
        val savedPin = settingsStorage.getString(KEY_ADMIN_PIN, "")
        return if (savedPin.isNotBlank()) savedPin else DEFAULT_PIN
    }

    fun verifyPin(enteredPin: String): Boolean {
        val correctPin = getStoredPin()
        val isValid = enteredPin.trim() == correctPin.trim()
        if (isValid) {
            _isSessionUnlocked.value = true
        }
        return isValid
    }

    fun changePin(oldPin: String, newPin: String): Boolean {
        val currentPin = getStoredPin()
        if (oldPin.trim() != currentPin.trim()) return false
        if (newPin.trim().length != 4 || !newPin.all { it.isDigit() }) return false

        settingsStorage.setString(KEY_ADMIN_PIN, newPin.trim())
        _isSessionUnlocked.value = true
        return true
    }

    fun lockSession() {
        _isSessionUnlocked.value = false
    }
}
