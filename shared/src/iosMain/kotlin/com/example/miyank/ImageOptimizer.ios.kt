package com.example.miyank

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

actual class ImageOptimizer {
    actual fun processAndOptimizeImage(
        originalPath: String,
        maxDimensionFull: Int,
        maxDimensionThumb: Int,
        qualityFull: Int,
        qualityThumb: Int
    ): OptimizedImageResult? {
        return OptimizedImageResult(
            optimizedFullImagePath = originalPath,
            thumbnailImagePath = originalPath,
            fullImageSizeBytes = 0L,
            thumbnailSizeBytes = 0L
        )
    }
}

@Composable
actual fun rememberImageOptimizer(): ImageOptimizer {
    return remember { ImageOptimizer() }
}
