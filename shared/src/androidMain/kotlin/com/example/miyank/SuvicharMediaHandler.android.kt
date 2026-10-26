package com.example.miyank

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

actual class SuvicharMediaHandler {
    private val context get() = ContextProvider.context

    actual fun saveToGallery(
        imageUrlOrPath: String,
        dateStr: String,
        quoteText: String,
        author: String,
        onResult: (Boolean, String) -> Unit
    ) {
        Thread {
            try {
                val originalBitmap = loadBitmapFromPathOrAsset(context, imageUrlOrPath)
                if (originalBitmap == null) {
                    onResult(false, "इमेज लोड करने में त्रुटि")
                    return@Thread
                }

                val posterBitmap = if (quoteText.contains("Gallery") || quoteText.isEmpty()) {
                    createGalleryPosterBitmap(originalBitmap)
                } else {
                    createSuvicharPosterBitmap(context, originalBitmap, dateStr, quoteText, author)
                }

                val filename = "Suvichar_${System.currentTimeMillis()}.jpg"
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/JamniwalaDarbar")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        posterBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        contentValues.clear()
                        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                        resolver.update(uri, contentValues, null, null)
                    }

                    onResult(true, "सुविचार आपकी Gallery में सुरक्षित है")
                } else {
                    onResult(false, "गैलरी में सेव नहीं हो सका")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false, "त्रुटि: ${e.message}")
            }
        }.start()
    }

    actual fun shareSuvichar(
        imageUrlOrPath: String,
        dateStr: String,
        quoteText: String,
        author: String,
        shareText: String
    ) {
        Thread {
            try {
                val pkgName = context.packageName
                val officialAppLink = AppShareConfig.getOfficialAppLink(pkgName)

                val formattedShareText = if (shareText.contains(officialAppLink)) {
                    shareText
                } else if (shareText.isNotEmpty()) {
                    if (shareText.contains("play.google.com")) shareText else shareText + AppShareConfig.buildShareFooter(officialAppLink)
                } else {
                    ShareManager.formatSuvicharShareMessage(quoteText, author, officialAppLink)
                }

                val originalBitmap = loadBitmapFromPathOrAsset(context, imageUrlOrPath)
                val cacheFile = File(context.cacheDir, "share_suvichar_${System.currentTimeMillis()}.jpg")

                if (originalBitmap != null) {
                    val posterBitmap = if (quoteText.contains("Gallery") || quoteText.isEmpty()) {
                        createGalleryPosterBitmap(originalBitmap)
                    } else {
                        createSuvicharPosterBitmap(context, originalBitmap, dateStr, quoteText, author)
                    }
                    val fos = FileOutputStream(cacheFile)
                    posterBitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos)
                    fos.close()
                }

                if (!cacheFile.exists() || cacheFile.length() == 0L) return@Thread

                val authority = "${context.packageName}.provider"
                val imageUri = FileProvider.getUriForFile(context, authority, cacheFile)

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/jpeg"
                    putExtra(Intent.EXTRA_STREAM, imageUri)
                    putExtra(Intent.EXTRA_TEXT, formattedShareText)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                val chooser = Intent.createChooser(shareIntent, "शेयर करें").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                context.startActivity(chooser)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    actual fun shareCleanImage(imageUrlOrPath: String) {
        Thread {
            try {
                val originalBitmap = loadBitmapFromPathOrAsset(context, imageUrlOrPath)
                if (originalBitmap == null) return@Thread

                val cacheFile = File(context.cacheDir, "parcha_photo_${System.currentTimeMillis()}.jpg")
                val fos = FileOutputStream(cacheFile)
                originalBitmap.compress(Bitmap.CompressFormat.JPEG, 98, fos)
                fos.close()

                if (!cacheFile.exists() || cacheFile.length() == 0L) return@Thread

                val authority = "${context.packageName}.provider"
                val imageUri = FileProvider.getUriForFile(context, authority, cacheFile)

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/jpeg"
                    putExtra(Intent.EXTRA_STREAM, imageUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                val chooser = Intent.createChooser(shareIntent, "Share Image").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                context.startActivity(chooser)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    actual fun cropAndCompressBitmap(
        rawPath: String,
        scale: Float,
        offsetX: Float,
        offsetY: Float,
        targetWidth: Int,
        targetHeight: Int
    ): String? {
        return try {
            val original = loadBitmapFromPathOrAsset(context, rawPath) ?: return null

            val cropped = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(cropped)

            val srcWidth = original.width.toFloat()
            val srcHeight = original.height.toFloat()

            val matrix = android.graphics.Matrix()

            val baseScale = maxOf(targetWidth / srcWidth, targetHeight / srcHeight)
            val finalScale = baseScale * scale

            val scaledW = srcWidth * finalScale
            val scaledH = srcHeight * finalScale

            val dx = (targetWidth - scaledW) / 2f + offsetX
            val dy = (targetHeight - scaledH) / 2f + offsetY

            matrix.postScale(finalScale, finalScale)
            matrix.postTranslate(dx, dy)

            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG or android.graphics.Paint.FILTER_BITMAP_FLAG)
            canvas.drawBitmap(original, matrix, paint)

            val outputFile = File(context.filesDir, "cropped_suvichar_${System.currentTimeMillis()}.jpg")
            val fos = FileOutputStream(outputFile)
            cropped.compress(Bitmap.CompressFormat.JPEG, 85, fos)
            fos.close()

            outputFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createGalleryPosterBitmap(
        originalBitmap: Bitmap
    ): Bitmap {
        val width = 1080
        val height = 1350
        val poster = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(poster)

        val targetRatio = 1080f / 1350f
        val srcRatio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()

        val srcRect = if (srcRatio > targetRatio) {
            val cropWidth = (originalBitmap.height * targetRatio).toInt()
            val left = (originalBitmap.width - cropWidth) / 2
            android.graphics.Rect(left, 0, left + cropWidth, originalBitmap.height)
        } else {
            val cropHeight = (originalBitmap.width / targetRatio).toInt()
            val top = (originalBitmap.height - cropHeight) / 2
            android.graphics.Rect(0, top, originalBitmap.width, top + cropHeight)
        }

        val dstRect = android.graphics.Rect(0, 0, width, height)
        canvas.drawBitmap(
            originalBitmap,
            srcRect,
            dstRect,
            android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG or android.graphics.Paint.FILTER_BITMAP_FLAG)
        )

        // Dark Gradient Overlay at bottom 22%
        val gradientPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            shader = android.graphics.LinearGradient(
                0f, height * 0.78f, 0f, height.toFloat(),
                intArrayOf(0x00000000, 0xD0000000.toInt(), 0xFF000000.toInt()),
                floatArrayOf(0f, 0.4f, 1f),
                android.graphics.Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, height * 0.78f, width.toFloat(), height.toFloat(), gradientPaint)

        // Golden Outer Border
        val borderPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFD700.toInt()
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 12f
        }
        canvas.drawRect(12f, 12f, width - 12f, height - 12f, borderPaint)

        // NO EMOJIS! CLEAN TEMPLE SIGNATURE BRANDING AT BOTTOM
        val titlePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFD700.toInt()
            textSize = 38f
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            setShadowLayer(8f, 2f, 2f, 0xFF000000.toInt())
        }

        val subtitlePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 28f
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            setShadowLayer(6f, 1f, 1f, 0xFF000000.toInt())
        }

        // Line 1: Devotional Heading (No Emojis)
        canvas.drawText("जय श्री गोगा जी महाराज", width / 2f, height - 90f, titlePaint)

        // Line 2: Temple Signature Watermark (No Emojis)
        canvas.drawText("श्री जामनीवाला दरबार - गोगामेड़ी मोबाइल एप", width / 2f, height - 45f, subtitlePaint)

        return poster
    }

    private fun createSuvicharPosterBitmap(
        context: Context,
        originalBitmap: Bitmap,
        dateStr: String,
        quoteText: String,
        author: String
    ): Bitmap {
        val width = 1080
        val height = 1350
        val poster = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(poster)

        val targetRatio = 1080f / 1350f
        val srcRatio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()

        val srcRect = if (srcRatio > targetRatio) {
            val cropWidth = (originalBitmap.height * targetRatio).toInt()
            val left = (originalBitmap.width - cropWidth) / 2
            android.graphics.Rect(left, 0, left + cropWidth, originalBitmap.height)
        } else {
            val cropHeight = (originalBitmap.width / targetRatio).toInt()
            val top = (originalBitmap.height - cropHeight) / 2
            android.graphics.Rect(0, top, originalBitmap.width, top + cropHeight)
        }

        val dstRect = android.graphics.Rect(0, 0, width, height)
        canvas.drawBitmap(
            originalBitmap,
            srcRect,
            dstRect,
            android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG or android.graphics.Paint.FILTER_BITMAP_FLAG)
        )

        // Smooth Dark Gradient Overlay for text contrast
        val gradientPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            shader = android.graphics.LinearGradient(
                0f, height * 0.45f, 0f, height.toFloat(),
                intArrayOf(0x00000000, 0xAA000000.toInt(), 0xEE000000.toInt()),
                floatArrayOf(0f, 0.4f, 1f),
                android.graphics.Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, height * 0.45f, width.toFloat(), height.toFloat(), gradientPaint)

        // Thin Golden Temple Border
        val borderPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFD700.toInt()
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 8f
        }
        canvas.drawRect(12f, 12f, width - 12f, height - 12f, borderPaint)

        // Date Badge at Top Left
        val formattedDate = formatDisplayDate(dateStr)
        if (formattedDate.isNotEmpty()) {
            val dateBadgePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xAA000000.toInt()
                style = android.graphics.Paint.Style.FILL
            }
            val dateTextPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFFFD700.toInt()
                textSize = 28f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            }
            val dateText = "दैनिक सुविचार  |  $formattedDate"
            val textWidth = dateTextPaint.measureText(dateText)
            canvas.drawRoundRect(40f, 40f, 80f + textWidth, 96f, 16f, 16f, dateBadgePaint)
            canvas.drawText(dateText, 60f, 78f, dateTextPaint)
        }

        // Quote Text with Devanagari Typography
        if (quoteText.isNotEmpty()) {
            val quotePaint = android.text.TextPaint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFFFFFFF.toInt()
                textSize = 38f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                setShadowLayer(8f, 2f, 2f, 0xFF000000.toInt())
            }

            val textLayout = android.text.StaticLayout.Builder.obtain(
                "“$quoteText”", 0, quoteText.length + 2, quotePaint, width - 140
            ).setAlignment(android.text.Layout.Alignment.ALIGN_CENTER).build()

            canvas.save()
            canvas.translate(70f, height - 250f - textLayout.height)
            textLayout.draw(canvas)
            canvas.restore()

            // Author Signature below Quote
            if (author.isNotEmpty()) {
                val authorPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFFFFD700.toInt()
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.CENTER
                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                    setShadowLayer(6f, 1f, 1f, 0xFF000000.toInt())
                }
                canvas.drawText("— $author", width / 2f, height - 170f, authorPaint)
            }
        }

        // NO EMOJIS! CLEAN TEMPLE SIGNATURE BRANDING AT BOTTOM
        val titlePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFD700.toInt()
            textSize = 34f
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            setShadowLayer(8f, 2f, 2f, 0xFF000000.toInt())
        }

        val subtitlePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 26f
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            setShadowLayer(6f, 1f, 1f, 0xFF000000.toInt())
        }

        canvas.drawText("जय श्री गोगा जी महाराज", width / 2f, height - 90f, titlePaint)
        canvas.drawText("श्री जामनीवाला दरबार - गोगामेड़ी मोबाइल एप", width / 2f, height - 45f, subtitlePaint)

        return poster
    }

    private fun loadBitmapFromPathOrAsset(context: Context, path: String): Bitmap? {
        return try {
            if (path.startsWith("file:///android_asset/")) {
                val assetName = path.substringAfter("file:///android_asset/")
                context.assets.open(assetName).use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            } else if (path.startsWith("content://") || path.startsWith("file://")) {
                val uri = Uri.parse(path)
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            } else if (path.startsWith("http://") || path.startsWith("https://")) {
                val url = java.net.URL(path)
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.doInput = true
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.connect()
                connection.inputStream.use { inputStream ->
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
actual fun rememberSuvicharMediaHandler(): SuvicharMediaHandler {
    return remember { SuvicharMediaHandler() }
}
