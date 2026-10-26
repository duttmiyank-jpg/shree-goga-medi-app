package com.example.miyank

import android.content.Intent
import android.net.Uri

actual fun openExternalUrl(url: String): Boolean {
    val context = ContextProvider.context
    val trimmedUrl = url.trim()
    if (trimmedUrl.isEmpty()) return false

    return try {
        val parsedUri = Uri.parse(trimmedUrl)

        // 1. Prefer opening directly in the YouTube app if YouTube link
        if (trimmedUrl.contains("youtube.com") || trimmedUrl.contains("youtu.be")) {
            val youtubeIntent = Intent(Intent.ACTION_VIEW, parsedUri).apply {
                setPackage("com.google.android.youtube")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (youtubeIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(youtubeIntent)
                return true
            }
        }

        // 2. Prefer opening directly in Instagram app if Instagram link
        if (trimmedUrl.contains("instagram.com")) {
            val instaIntent = Intent(Intent.ACTION_VIEW, parsedUri).apply {
                setPackage("com.instagram.android")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (instaIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(instaIntent)
                return true
            }
        }

        // 3. Prefer opening directly in Facebook app if Facebook link
        if (trimmedUrl.contains("facebook.com") || trimmedUrl.contains("fb.me") || trimmedUrl.contains("fb.com")) {
            val fbIntent = Intent(Intent.ACTION_VIEW, parsedUri).apply {
                setPackage("com.facebook.katana")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (fbIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(fbIntent)
                return true
            }
        }

        // 4. Generic browser/app intent fallback
        val genericIntent = Intent(Intent.ACTION_VIEW, parsedUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(genericIntent)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        try {
            val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(trimmedUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallbackIntent)
            true
        } catch (ex: Exception) {
            ex.printStackTrace()
            false
        }
    }
}
