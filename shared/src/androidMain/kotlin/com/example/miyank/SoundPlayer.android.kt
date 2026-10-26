package com.example.miyank

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

actual class BellSoundPlayer {
    private var audioTrack: AudioTrack? = null

    actual fun playBellSound() {
        stop()

        try {
            val sampleRate = 44100
            val durationSeconds = 2.5f
            val totalSamples = (sampleRate * durationSeconds).toInt()
            val pcmData = ShortArray(totalSamples)

            val strikes = floatArrayOf(0.0f, 0.55f, 1.10f, 1.65f)

            for (i in 0 until totalSamples) {
                val t = i.toFloat() / sampleRate
                var sampleValue = 0f

                for (strikeTime in strikes) {
                    if (t >= strikeTime) {
                        val dt = t - strikeTime
                        val decay = kotlin.math.exp(-dt / 0.40f)
                        if (decay > 0.001f) {
                            val f1 = 880f
                            val f2 = 1320f
                            val f3 = 2200f
                            val f4 = 3520f

                            val wave = 0.45f * kotlin.math.sin(2.0 * kotlin.math.PI * f1 * t) +
                                       0.30f * kotlin.math.sin(2.0 * kotlin.math.PI * f2 * t) +
                                       0.15f * kotlin.math.sin(2.0 * kotlin.math.PI * f3 * t) +
                                       0.10f * kotlin.math.sin(2.0 * kotlin.math.PI * f4 * t)

                            sampleValue += (decay * wave).toFloat()
                        }
                    }
                }

                val clamped = sampleValue.coerceIn(-1.0f, 1.0f)
                pcmData[i] = (clamped * 32767).toInt().toShort()
            }

            val bufferSize = totalSamples * 2
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(pcmData, 0, totalSamples)
            track.play()
            audioTrack = track
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun stop() {
        try {
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        } catch (e: Exception) {
            // Ignore
        }
    }
}

@Composable
actual fun rememberBellSoundPlayer(): BellSoundPlayer {
    return remember { BellSoundPlayer() }
}
