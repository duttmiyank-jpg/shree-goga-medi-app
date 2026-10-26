package com.example.miyank.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun OfficialGoogleLogo(size: Dp = 24.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.2f))
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize(0.75f)) {
            val w = this.size.width
            val h = this.size.height

            // Red Arc
            drawPath(
                path = Path().apply {
                    moveTo(w * 0.5f, h * 0.5f)
                    lineTo(w * 0.9f, h * 0.25f)
                    quadraticBezierTo(w * 0.7f, 0f, w * 0.5f, 0f)
                    quadraticBezierTo(w * 0.2f, 0f, 0f, h * 0.35f)
                    close()
                },
                color = Color(0xEA4335)
            )

            // Yellow Arc
            drawPath(
                path = Path().apply {
                    moveTo(0f, h * 0.35f)
                    quadraticBezierTo(0f, h * 0.65f, w * 0.2f, h)
                    lineTo(w * 0.5f, h * 0.5f)
                    close()
                },
                color = Color(0xFBBC05)
            )

            // Green Arc
            drawPath(
                path = Path().apply {
                    moveTo(w * 0.2f, h)
                    quadraticBezierTo(w * 0.5f, h, w * 0.85f, h * 0.85f)
                    lineTo(w * 0.5f, h * 0.5f)
                    close()
                },
                color = Color(0x34A853)
            )

            // Blue Bar & Arc
            drawPath(
                path = Path().apply {
                    moveTo(w * 0.5f, h * 0.5f)
                    lineTo(w, h * 0.5f)
                    lineTo(w, h * 0.3f)
                    lineTo(w * 0.5f, h * 0.3f)
                    close()
                },
                color = Color(0x4285F4)
            )
        }
    }
}

@Composable
fun OfficialYouTubeLogo(size: Dp = 30.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.2f))
            .background(Color(0xFFFF0000)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize(0.5f)) {
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(0f, this@Canvas.size.height)
                lineTo(this@Canvas.size.width, this@Canvas.size.height / 2f)
                close()
            }
            drawPath(path, Color.White)
        }
    }
}

@Composable
fun OfficialInstagramLogo(size: Dp = 30.dp) {
    val instaGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF405DE6), // Blue
            Color(0xFF833AB4), // Purple
            Color(0xFFC13584), // Pink
            Color(0xFFE1306C), // Red
            Color(0xFFFD1D1D), // Orange
            Color(0xFFF56040), // Dark Orange
            Color(0xFFF77737), // Light Orange
            Color(0xFFFCAF45), // Yellow
            Color(0xFFFFDC80)  // Light Yellow
        ),
        start = Offset(0f, 100f),
        end = Offset(100f, 0f)
    )

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.25f))
            .background(instaGradient),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize(0.65f)) {
            val strokeWidth = this.size.width * 0.1f
            
            // Outer Square
            drawPath(
                path = Path().apply {
                    addRoundRect(
                        androidx.compose.ui.geometry.RoundRect(
                            0f, 0f, this@Canvas.size.width, this@Canvas.size.height,
                            this@Canvas.size.width * 0.3f, this@Canvas.size.width * 0.3f
                        )
                    )
                },
                color = Color.White,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
            )
            
            // Center Circle
            drawCircle(
                color = Color.White,
                radius = this.size.width * 0.22f,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
            )
            
            // Top Right Dot
            drawCircle(
                color = Color.White,
                radius = this.size.width * 0.06f,
                center = Offset(this.size.width * 0.8f, this.size.height * 0.2f)
            )
        }
    }
}

@Composable
fun OfficialFacebookLogo(size: Dp = 30.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.2f))
            .background(Color(0xFF1877F2)),
        contentAlignment = Alignment.Center
    ) {
        // Simple 'f' representation for KMP without external font dependency
        Canvas(modifier = Modifier.fillMaxSize(0.7f)) {
            val w = this.size.width
            val h = this.size.height
            val path = Path().apply {
                moveTo(w * 0.65f, h)
                lineTo(w * 0.65f, h * 0.55f)
                lineTo(w * 0.85f, h * 0.55f)
                lineTo(w * 0.85f, h * 0.35f)
                lineTo(w * 0.65f, h * 0.35f)
                lineTo(w * 0.65f, h * 0.25f)
                quadraticBezierTo(w * 0.65f, h * 0.1f, w * 0.8f, h * 0.1f)
                lineTo(w * 0.9f, h * 0.1f)
                lineTo(w * 0.9f, 0f)
                lineTo(w * 0.7f, 0f)
                quadraticBezierTo(w * 0.45f, 0f, w * 0.45f, h * 0.25f)
                lineTo(w * 0.45f, h * 0.35f)
                lineTo(w * 0.3f, h * 0.35f)
                lineTo(w * 0.3f, h * 0.55f)
                lineTo(w * 0.45f, h * 0.55f)
                lineTo(w * 0.45f, h)
            }
            drawPath(path, Color.White)
        }
    }
}
