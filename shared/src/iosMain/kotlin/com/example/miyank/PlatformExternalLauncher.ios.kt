package com.example.miyank

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual fun openExternalUrl(url: String): Boolean {
    val trimmed = url.trim()
    if (trimmed.isEmpty()) return false
    return try {
        val nsUrl = NSURL.URLWithString(trimmed) ?: return false
        if (UIApplication.sharedApplication.canOpenURL(nsUrl)) {
            UIApplication.sharedApplication.openURL(nsUrl)
            true
        } else {
            false
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
