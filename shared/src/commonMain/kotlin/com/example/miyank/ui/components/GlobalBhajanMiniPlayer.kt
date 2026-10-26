package com.example.miyank.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.miyank.BhajanItem
import com.example.miyank.HapticManager
import com.example.miyank.ui.theme.*

@Composable
fun GlobalBhajanMiniPlayer(
    bhajan: BhajanItem?,
    isPlaying: Boolean,
    currentPosMs: Long,
    totalDurationMs: Long,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onOpenFullScreen: () -> Unit,
    onClose: () -> Unit,
    haptic: HapticManager,
    modifier: Modifier = Modifier
) {
    if (bhajan == null) return

    val progressRatio = if (totalDurationMs > 0L) (currentPosMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f) else 0f

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpenFullScreen() },
        color = SurfaceDark,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            LinearProgressIndicator(
                progress = { progressRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = AccentGolden,
                trackColor = WarmGold.copy(alpha = 0.2f)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    modifier = Modifier.size(42.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    AsyncImage(
                        model = bhajan.thumbnailUrl.ifEmpty { "file:///android_asset/temple_pooja.jpg" },
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = bhajan.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = bhajan.artist,
                        fontSize = 11.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(
                        onClick = {
                            haptic.playPremiumHaptic()
                            onPrevious()
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Prev", tint = AccentGolden, modifier = Modifier.size(16.dp))
                    }

                    Surface(
                        onClick = {
                            haptic.playPremiumHaptic()
                            onTogglePlay()
                        },
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = AccentGolden
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = BackgroundDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            haptic.playPremiumHaptic()
                            onNext()
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = AccentGolden, modifier = Modifier.size(16.dp))
                    }

                    IconButton(
                        onClick = {
                            haptic.playPremiumHaptic()
                            onOpenFullScreen()
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.Fullscreen, contentDescription = "Full Screen", tint = AccentGolden, modifier = Modifier.size(16.dp))
                    }

                    IconButton(
                        onClick = {
                            haptic.playPremiumHaptic()
                            onClose()
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close Player", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
