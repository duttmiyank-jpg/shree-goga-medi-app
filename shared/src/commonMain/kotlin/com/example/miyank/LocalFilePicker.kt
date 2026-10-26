package com.example.miyank

import androidx.compose.runtime.Composable

@Composable
expect fun rememberFilePickerLauncher(
    mimeType: String = "*/*",
    onFilePicked: (filePath: String?, fileName: String?, fileSize: Long) -> Unit
): () -> Unit

@Composable
expect fun rememberMultipleFilePickerLauncher(
    mimeType: String = "*/*",
    onFilesPicked: (files: List<Triple<String, String, Long>>) -> Unit
): () -> Unit
