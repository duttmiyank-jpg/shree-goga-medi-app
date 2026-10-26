package com.example.miyank

import androidx.compose.runtime.Composable

expect class SuvicharMediaHandler() {
    fun saveToGallery(
        imageUrlOrPath: String,
        dateStr: String,
        quoteText: String,
        author: String,
        onResult: (Boolean, String) -> Unit
    )
    fun shareSuvichar(
        imageUrlOrPath: String,
        dateStr: String,
        quoteText: String,
        author: String,
        shareText: String
    )
    fun shareCleanImage(imageUrlOrPath: String)
    fun cropAndCompressBitmap(
        rawPath: String,
        scale: Float,
        offsetX: Float,
        offsetY: Float,
        targetWidth: Int = 1080,
        targetHeight: Int = 1350
    ): String?
}

@Composable
expect fun rememberSuvicharMediaHandler(): SuvicharMediaHandler
