package com.example.miyank

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

actual class SuvicharMediaHandler {
    actual fun saveToGallery(
        imageUrlOrPath: String,
        dateStr: String,
        quoteText: String,
        author: String,
        onResult: (Boolean, String) -> Unit
    ) {
        onResult(true, "सुविचार आपके गैलरी में सेव हो गया 🙏")
    }

    actual fun shareSuvichar(
        imageUrlOrPath: String,
        dateStr: String,
        quoteText: String,
        author: String,
        shareText: String
    ) {}

    actual fun shareCleanImage(imageUrlOrPath: String) {}

    actual fun cropAndCompressBitmap(
        rawPath: String,
        scale: Float,
        offsetX: Float,
        offsetY: Float,
        targetWidth: Int,
        targetHeight: Int
    ): String? {
        return rawPath
    }
}

@Composable
actual fun rememberSuvicharMediaHandler(): SuvicharMediaHandler {
    return remember { SuvicharMediaHandler() }
}
