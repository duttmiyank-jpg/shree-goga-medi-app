package com.example.miyank

import androidx.compose.runtime.Composable

data class OptimizedImageResult(
    val optimizedFullImagePath: String,
    val thumbnailImagePath: String,
    val fullImageSizeBytes: Long,
    val thumbnailSizeBytes: Long
)

expect class ImageOptimizer {
    fun processAndOptimizeImage(
        originalPath: String,
        maxDimensionFull: Int = 2048,
        maxDimensionThumb: Int = 400,
        qualityFull: Int = 82,
        qualityThumb: Int = 80
    ): OptimizedImageResult?
}

@Composable
expect fun rememberImageOptimizer(): ImageOptimizer
