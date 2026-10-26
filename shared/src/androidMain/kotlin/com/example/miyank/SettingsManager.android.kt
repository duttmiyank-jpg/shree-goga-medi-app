package com.example.miyank

import android.content.Context
import android.content.SharedPreferences

actual class SettingsManager {
    private val prefs: SharedPreferences = ContextProvider.context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    actual fun getString(key: String, defaultValue: String): String {
        return prefs.getString(key, defaultValue) ?: defaultValue
    }

    actual fun setString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }
}
