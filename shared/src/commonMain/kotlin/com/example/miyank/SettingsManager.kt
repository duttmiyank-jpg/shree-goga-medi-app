package com.example.miyank

expect class SettingsManager() {
    fun getString(key: String, defaultValue: String): String
    fun setString(key: String, value: String)
}
