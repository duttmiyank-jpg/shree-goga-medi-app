package com.example.miyank

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberFilePickerLauncher(
    mimeType: String,
    onFilePicked: (filePath: String?, fileName: String?, fileSize: Long) -> Unit
): () -> Unit {
    return remember {
        { onFilePicked(null, null, 0L) }
    }
}

@Composable
actual fun rememberMultipleFilePickerLauncher(
    mimeType: String,
    onFilesPicked: (files: List<Triple<String, String, Long>>) -> Unit
): () -> Unit {
    return remember {
        { onFilesPicked(emptyList()) }
    }
}
