package com.example.miyank

import platform.Foundation.NSUserDefaults

actual class SettingsManager {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun getString(key: String, defaultValue: String): String {
        return defaults.stringForKey(key) ?: defaultValue
    }

    actual fun setString(key: String, value: String) {
        defaults.setObject(value, key)
    }
}
