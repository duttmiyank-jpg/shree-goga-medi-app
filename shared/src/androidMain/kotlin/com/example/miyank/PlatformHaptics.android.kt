package com.example.miyank

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class AndroidHapticManager(private val context: Context) : HapticManager {
    override fun playPremiumHaptic() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (vibrator == null || !vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(55L, 150))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(55L)
        }
    }
}

@Composable
actual fun rememberHapticManager(): HapticManager {
    val context = LocalContext.current
    return remember(context) { AndroidHapticManager(context) }
}

actual fun playPremiumHapticGlobal() {
    // Requires a context reference which is tricky in global scope in KMP
    // Typically initialized in Application class
}
