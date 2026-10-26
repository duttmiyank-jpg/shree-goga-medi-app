package com.example.miyank

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
actual fun LiveVideoPlayer(
    videoId: String,
    isPlaying: Boolean,
    isMuted: Boolean,
    onPlayerReady: () -> Unit,
    onPlayerStateChange: (isPlaying: Boolean) -> Unit,
    onError: (errorMsg: String) -> Unit,
    modifier: Modifier
) {
    LaunchedEffect(videoId) {
        onPlayerReady()
        onPlayerStateChange(isPlaying)
    }

    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text("iOS Live Video Player ($videoId)", color = Color.White)
    }
}
