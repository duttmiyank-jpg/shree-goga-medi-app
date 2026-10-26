package com.example.miyank

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.io.FileOutputStream

@Composable
actual fun rememberFilePickerLauncher(
    mimeType: String,
    onFilePicked: (filePath: String?, fileName: String?, fileSize: Long) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                var fileName = "picked_file"
                var size = 0L

                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIdx != -1) fileName = cursor.getString(nameIdx) ?: fileName
                        if (sizeIdx != -1) size = cursor.getLong(sizeIdx)
                    }
                }

                val ext = fileName.substringAfterLast('.', "")
                val tempName = "file_${System.currentTimeMillis()}${if (ext.isNotEmpty()) ".$ext" else ""}"
                val customFile = File(context.filesDir, tempName)

                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val outputStream = FileOutputStream(customFile)
                    inputStream.copyTo(outputStream)
                    inputStream.close()
                    outputStream.close()

                    onFilePicked(customFile.absolutePath, fileName, if (size > 0L) size else customFile.length())
                } else {
                    onFilePicked(null, null, 0L)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onFilePicked(null, null, 0L)
            }
        }
    }

    return remember(launcher, mimeType) {
        { launcher.launch(if (mimeType.isEmpty()) "*/*" else mimeType) }
    }
}

@Composable
actual fun rememberMultipleFilePickerLauncher(
    mimeType: String,
    onFilesPicked: (files: List<Triple<String, String, Long>>) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val result = mutableListOf<Triple<String, String, Long>>()
            for ((index, uri) in uris.withIndex()) {
                try {
                    var fileName = "picked_file"
                    var size = 0L

                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (cursor.moveToFirst()) {
                            if (nameIdx != -1) fileName = cursor.getString(nameIdx) ?: fileName
                            if (sizeIdx != -1) size = cursor.getLong(sizeIdx)
                        }
                    }

                    val ext = fileName.substringAfterLast('.', "")
                    val tempName = "file_${System.currentTimeMillis()}_$index${if (ext.isNotEmpty()) ".$ext" else ""}"
                    val customFile = File(context.filesDir, tempName)

                    val inputStream = context.contentResolver.openInputStream(uri)
                    if (inputStream != null) {
                        val outputStream = FileOutputStream(customFile)
                        inputStream.copyTo(outputStream)
                        inputStream.close()
                        outputStream.close()

                        val finalSize = if (size > 0L) size else customFile.length()
                        result.add(Triple(customFile.absolutePath, fileName, finalSize))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            onFilesPicked(result)
        }
    }

    return remember(launcher, mimeType) {
        { launcher.launch(if (mimeType.isEmpty()) "*/*" else mimeType) }
    }
}
