package com.example.miyank

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun LiveVideoPlayer(
    videoId: String,
    isPlaying: Boolean,
    isMuted: Boolean,
    onPlayerReady: () -> Unit,
    onPlayerStateChange: (isPlaying: Boolean) -> Unit,
    onError: (errorMsg: String) -> Unit,
    modifier: Modifier = Modifier
)
