package com.example.miyank.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyank.HindiStrings
import com.example.miyank.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {
    // Splash screen will always be in Hindi as per requirement
    val strings = HindiStrings
    // Animation timing control
    var startLogoAnim by remember { mutableStateOf(false) }
    var startMainTextAnim by remember { mutableStateOf(false) }
    var startSecondaryTextAnim by remember { mutableStateOf(false) }
    var startGlowAnim by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    // Logo Pulse Animation
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glow"
    )

    // Sequence Effect
    LaunchedEffect(Unit) {
        delay(300)
        startGlowAnim = true
        delay(200)
        startLogoAnim = true
        delay(500)
        startMainTextAnim = true
        delay(500)
        startSecondaryTextAnim = true
        delay(1300)
        onFinished()
    }

    // Logo Animations
    val logoAlpha by animateFloatAsState(
        targetValue = if (startLogoAnim) 1f else 0f,
        animationSpec = tween(800), label = "logo_alpha"
    )
    val logoScale by animateFloatAsState(
        targetValue = if (startLogoAnim) 1f else 0.9f,
        animationSpec = tween(1200, easing = EaseOutCubic), label = "logo_scale"
    )

    // Background Glow Animation
    val bgGlowAlpha by animateFloatAsState(
        targetValue = if (startGlowAnim) 1f else 0f,
        animationSpec = tween(1500), label = "bg_glow"
    )

    // Text Animations
    val mainTextAlpha by animateFloatAsState(
        targetValue = if (startMainTextAnim) 1f else 0f,
        animationSpec = tween(800), label = "main_alpha"
    )
    val secondaryTextAlpha by animateFloatAsState(
        targetValue = if (startSecondaryTextAnim) 1f else 0f,
        animationSpec = tween(700), label = "sec_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DeepMidnight, DarkBrown)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // LAYER 1: Subtle floating particles
        DivineParticles(infiniteTransition)

        // LAYER 2: Central Illumination behind logo
        Box(
            modifier = Modifier
                .size(400.dp)
                .alpha(bgGlowAlpha * pulseGlow)
                .background(
                    Brush.radialGradient(
                        colors = listOf(LuxuryGold.copy(alpha = 0.3f), Color.Transparent),
                        center = Offset.Unspecified,
                        radius = 600f
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // LAYER 3: Temple Logo (Existing Logo or high-end placeholder)
            Box(
                modifier = Modifier
                    .scale(logoScale)
                    .alpha(logoAlpha)
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                // Subtle glow behind logo
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .graphicsLayer {
                            scaleX = 1.2f
                            scaleY = 1.2f
                        }
                        .background(
                            Brush.radialGradient(
                                colors = listOf(WarmGold.copy(alpha = 0.2f), Color.Transparent)
                            )
                        )
                )
                
                // Existing logo replacement or high-end emoji if no asset found
                Text(
                    text = "🛕", // The project previously used this, representing the Temple
                    fontSize = 100.sp,
                    modifier = Modifier.graphicsLayer {
                        shadowElevation = 20f
                    }
                )
            }

            // LAYER 4: Main Title
            Text(
                text = strings.welcomeTitle,
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                color = LuxuryGold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(mainTextAlpha)
                    .padding(horizontal = 24.dp),
                letterSpacing = 2.sp,
                lineHeight = 46.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // LAYER 5: Secondary Text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(secondaryTextAlpha)
            ) {
                Text(
                    text = strings.splashSecondary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = WarmGold.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = strings.splashGuru,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = WarmGold.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // LAYER 6: Tagline
            Text(
                text = strings.tagline,
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                color = SaffronAccent.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(secondaryTextAlpha),
                letterSpacing = 4.sp
            )
        }
    }
}

@Composable
fun DivineParticles(infiniteTransition: InfiniteTransition) {
    val particleY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -100f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "particles"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val random = kotlin.random.Random(123)
        repeat(20) {
            val startX = random.nextFloat() * size.width
            val startY = random.nextFloat() * size.height
            val alpha = random.nextFloat() * 0.4f
            val radiusSize = random.nextFloat() * 3f + 1f
            
            drawCircle(
                color = WarmGold.copy(alpha = alpha),
                radius = radiusSize,
                center = Offset(startX, (startY + particleY) % size.height)
            )
        }
    }
}
