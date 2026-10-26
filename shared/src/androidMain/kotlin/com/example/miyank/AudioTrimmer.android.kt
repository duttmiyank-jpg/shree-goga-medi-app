package com.example.miyank

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

actual class AudioTrimmer {
    private val context get() = ContextProvider.context

    actual fun getAudioMetadata(uriOrPath: String): AudioMetadata? {
        return try {
            val retriever = MediaMetadataRetriever()
            val uri = Uri.parse(uriOrPath)
            if (uriOrPath.startsWith("content://") || uriOrPath.startsWith("file://")) {
                retriever.setDataSource(context, uri)
            } else {
                retriever.setDataSource(uriOrPath)
            }

            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMs = durationStr?.toLongOrNull() ?: 60000L

            var size = 0L
            var name = "audio_track.mp3"

            if (uriOrPath.startsWith("content://")) {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst()) {
                        if (sizeIndex != -1) size = cursor.getLong(sizeIndex)
                        if (nameIndex != -1) name = cursor.getString(nameIndex) ?: name
                    }
                }
            } else {
                val f = File(uriOrPath)
                size = f.length()
                name = f.name
            }

            retriever.release()
            AudioMetadata(uriOrPath, name, maxOf(1000L, durationMs), size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    actual fun trimAudio(
        inputUriOrPath: String,
        outputFileName: String,
        startMs: Long,
        endMs: Long
    ): String? {
        val outputFile = File(context.filesDir, outputFileName)
        if (outputFile.exists()) outputFile.delete()

        // 1. Try MP3 Frame Trimming (100% precise, losslessly cuts MP3 audio frames)
        try {
            val uri = Uri.parse(inputUriOrPath)
            val inputStream: InputStream? = if (inputUriOrPath.startsWith("content://") || inputUriOrPath.startsWith("file://")) {
                context.contentResolver.openInputStream(uri)
            } else {
                File(inputUriOrPath).inputStream()
            }

            if (inputStream != null) {
                val fos = FileOutputStream(outputFile)
                val success = trimMp3Frames(inputStream, fos, startMs, endMs)
                fos.close()
                inputStream.close()

                if (success && outputFile.exists() && outputFile.length() > 0) {
                    println("AudioTrimmer: MP3 Frame Trimming SUCCESS! Size: ${outputFile.length()} bytes")
                    return outputFile.absolutePath
                }
            }
        } catch (e: Exception) {
            println("AudioTrimmer: MP3 frame trimming exception: ${e.message}")
        }

        // 2. Try MediaExtractor + MediaMuxer fallback for AAC / M4A / MP4
        var extractor: MediaExtractor? = null
        var muxer: MediaMuxer? = null

        try {
            extractor = MediaExtractor()
            val uri = Uri.parse(inputUriOrPath)
            if (inputUriOrPath.startsWith("content://") || inputUriOrPath.startsWith("file://")) {
                extractor.setDataSource(context, uri, null)
            } else {
                extractor.setDataSource(inputUriOrPath)
            }

            var audioTrackIndex = -1
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) {
                    audioTrackIndex = i
                    break
                }
            }

            if (audioTrackIndex >= 0) {
                extractor.selectTrack(audioTrackIndex)
                val format = extractor.getTrackFormat(audioTrackIndex)

                val startUs = startMs * 1000L
                val endUs = endMs * 1000L

                extractor.seekTo(startUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

                muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
                val muxerTrackIndex = muxer.addTrack(format)
                muxer.start()

                val bufferSize = 256 * 1024
                val buffer = java.nio.ByteBuffer.allocate(bufferSize)
                val bufferInfo = MediaCodec.BufferInfo()

                while (true) {
                    bufferInfo.offset = 0
                    bufferInfo.size = extractor.readSampleData(buffer, 0)
                    if (bufferInfo.size < 0) break

                    val pts = extractor.sampleTime
                    if (pts > endUs) break

                    bufferInfo.presentationTimeUs = Math.max(0L, pts - startUs)
                    val isSync = (extractor.sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC) != 0
                    bufferInfo.flags = if (isSync) MediaCodec.BUFFER_FLAG_KEY_FRAME else 0

                    muxer.writeSampleData(muxerTrackIndex, buffer, bufferInfo)
                    extractor.advance()
                }

                muxer.stop()
                muxer.release()
                extractor.release()

                if (outputFile.exists() && outputFile.length() > 0) {
                    return outputFile.absolutePath
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            try { muxer?.release() } catch (_: Exception) {}
            try { extractor?.release() } catch (_: Exception) {}
        }

        return null
    }

    private fun trimMp3Frames(
        inputStream: InputStream,
        outputStream: FileOutputStream,
        startMs: Long,
        endMs: Long
    ): Boolean {
        val bitratesMpeg1Layer3 = intArrayOf(0, 32, 40, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320)
        val bitratesMpeg2Layer3 = intArrayOf(0, 8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160)
        val sampleRatesMpeg1 = intArrayOf(44100, 48000, 32000)
        val sampleRatesMpeg2 = intArrayOf(22050, 24000, 16000)

        val buffer = inputStream.readBytes()
        if (buffer.size < 4) return false

        var pos = 0
        var currentTimeMs = 0.0
        var writtenBytes = 0

        // Skip ID3v2 tag if present
        if (buffer.size >= 10 && buffer[0] == 'I'.code.toByte() && buffer[1] == 'D'.code.toByte() && buffer[2] == '3'.code.toByte()) {
            val tagSize = ((buffer[6].toInt() and 0x7F) shl 21) or
                          ((buffer[7].toInt() and 0x7F) shl 14) or
                          ((buffer[8].toInt() and 0x7F) shl 7) or
                          (buffer[9].toInt() and 0x7F)
            pos = 10 + tagSize
        }

        val startMsD = startMs.toDouble()
        val endMsD = endMs.toDouble()

        while (pos < buffer.size - 4) {
            val b0 = buffer[pos].toInt() and 0xFF
            val b1 = buffer[pos + 1].toInt() and 0xFF

            if (b0 == 0xFF && (b1 and 0xE0) == 0xE0) {
                val mpegVer = (b1 shr 3) and 0x03
                val layer = (b1 shr 1) and 0x03
                val b2 = buffer[pos + 2].toInt() and 0xFF
                val bitrateIdx = (b2 shr 4) and 0x0F
                val sampleRateIdx = (b2 shr 2) and 0x03
                val padding = (b2 shr 1) and 0x01

                val isMpeg1 = mpegVer == 3
                val bitrates = if (isMpeg1) bitratesMpeg1Layer3 else bitratesMpeg2Layer3
                val sampleRates = if (isMpeg1) sampleRatesMpeg1 else sampleRatesMpeg2

                if (layer == 1 && bitrateIdx in 1..14 && sampleRateIdx in 0..2) {
                    val bitrateKbps = bitrates[bitrateIdx]
                    val sampleRate = sampleRates[sampleRateIdx]
                    val samplesPerFrame = if (isMpeg1) 1152 else 576
                    val frameLen = (samplesPerFrame * bitrateKbps * 125) / sampleRate + padding
                    val frameDurationMs = (samplesPerFrame * 1000.0) / sampleRate

                    if (pos + frameLen <= buffer.size) {
                        val frameEndMs = currentTimeMs + frameDurationMs
                        if (frameEndMs >= startMsD && currentTimeMs <= endMsD) {
                            outputStream.write(buffer, pos, frameLen)
                            writtenBytes += frameLen
                        }
                        currentTimeMs = frameEndMs
                        pos += frameLen
                        if (currentTimeMs > endMsD) break
                        continue
                    }
                }
            }
            pos++
        }

        return writtenBytes > 0
    }
}

@Composable
actual fun rememberAudioTrimmer(): AudioTrimmer {
    return remember { AudioTrimmer() }
}
