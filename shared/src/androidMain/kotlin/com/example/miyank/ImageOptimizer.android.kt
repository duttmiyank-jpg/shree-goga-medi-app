package com.example.miyank

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.io.File
import java.io.FileOutputStream

actual class ImageOptimizer {
    private val context get() = ContextProvider.context

    actual fun processAndOptimizeImage(
        originalPath: String,
        maxDimensionFull: Int,
        maxDimensionThumb: Int,
        qualityFull: Int,
        qualityThumb: Int
    ): OptimizedImageResult? {
        return try {
            val originalBitmap = loadBitmap(context, originalPath) ?: return null

            // 1. Generate Optimized Full Image (Preserves aspect ratio)
            val fullBitmap = scaleBitmapToMaxDimension(originalBitmap, maxDimensionFull)
            val fullFile = File(context.filesDir, "gallery_full_${System.currentTimeMillis()}.webp")
            val fosFull = FileOutputStream(fullFile)
            val compressFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Bitmap.CompressFormat.WEBP_LOSSY
            } else {
                Bitmap.CompressFormat.JPEG
            }
            fullBitmap.compress(compressFormat, qualityFull, fosFull)
            fosFull.close()

            // 2. Generate Lightweight Thumbnail (~30-100 KB)
            val thumbBitmap = scaleBitmapToMaxDimension(originalBitmap, maxDimensionThumb)
            val thumbFile = File(context.filesDir, "gallery_thumb_${System.currentTimeMillis()}.webp")
            val fosThumb = FileOutputStream(thumbFile)
            thumbBitmap.compress(compressFormat, qualityThumb, fosThumb)
            fosThumb.close()

            OptimizedImageResult(
                optimizedFullImagePath = fullFile.absolutePath,
                thumbnailImagePath = thumbFile.absolutePath,
                fullImageSizeBytes = fullFile.length(),
                thumbnailSizeBytes = thumbFile.length()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun scaleBitmapToMaxDimension(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) {
            return bitmap
        }

        val ratio = width.toFloat() / height.toFloat()
        val targetWidth: Int
        val targetHeight: Int

        if (width > height) {
            targetWidth = maxDimension
            targetHeight = (maxDimension / ratio).toInt()
        } else {
            targetHeight = maxDimension
            targetWidth = (maxDimension * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    private fun loadBitmap(context: Context, path: String): Bitmap? {
        return try {
            if (path.startsWith("content://") || path.startsWith("file://")) {
                val uri = Uri.parse(path)
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            } else {
                val file = File(path)
                if (file.exists()) {
                    BitmapFactory.decodeFile(file.absolutePath)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

@Composable
actual fun rememberImageOptimizer(): ImageOptimizer {
    return remember { ImageOptimizer() }
}
