package com.example.miyank

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.session.MediaSession
import android.os.Build
import android.os.IBinder

class BhajanPlaybackService : Service() {

    companion object {
        const val CHANNEL_ID = "bhajan_playback_channel"
        const val NOTIFICATION_ID = 888

        const val ACTION_PLAY = "com.example.miyank.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.miyank.ACTION_PAUSE"
        const val ACTION_STOP = "com.example.miyank.ACTION_STOP"
        const val ACTION_NEXT = "com.example.miyank.ACTION_NEXT"
        const val ACTION_PREVIOUS = "com.example.miyank.ACTION_PREVIOUS"

        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_ARTIST = "extra_artist"
        const val EXTRA_IS_PLAYING = "extra_is_playing"

        fun startOrUpdateService(context: Context, title: String, artist: String, isPlaying: Boolean) {
            try {
                val intent = Intent(context, BhajanPlaybackService::class.java).apply {
                    action = if (isPlaying) ACTION_PLAY else ACTION_PAUSE
                    putExtra(EXTRA_TITLE, title)
                    putExtra(EXTRA_ARTIST, artist)
                    putExtra(EXTRA_IS_PLAYING, isPlaying)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun stopService(context: Context) {
            try {
                val intent = Intent(context, BhajanPlaybackService::class.java).apply {
                    action = ACTION_STOP
                }
                context.startService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private var mediaSession: MediaSession? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initMediaSession()
    }

    private fun initMediaSession() {
        try {
            mediaSession = MediaSession(this, "BhajanMediaSession").apply {
                isActive = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_PLAY
        val title = intent?.getStringExtra(EXTRA_TITLE) ?: "श्री गोगा जी महाराज भजन"
        val artist = intent?.getStringExtra(EXTRA_ARTIST) ?: "श्री जामनीवाला दरबार"
        val isPlaying = intent?.getBooleanExtra(EXTRA_IS_PLAYING, true) ?: true

        if (action == ACTION_STOP) {
            try {
                AudioPlayer().stop()
                CentralAudioCoordinator.stopBhajan()
            } catch (e: Exception) {
                // Ignore
            }
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        } else if (action == ACTION_PAUSE) {
            try {
                AudioPlayer().pause()
                MusicManager.isBhajanActivePlaying.value = false
            } catch (e: Exception) {
                // Ignore
            }
        } else if (action == ACTION_PLAY) {
            try {
                AudioPlayer().resume()
                MusicManager.isBhajanActivePlaying.value = true
            } catch (e: Exception) {
                // Ignore
            }
        } else if (action == ACTION_NEXT) {
            try {
                CentralAudioCoordinator.nextTrack()
            } catch (e: Exception) {
                // Ignore
            }
        } else if (action == ACTION_PREVIOUS) {
            try {
                CentralAudioCoordinator.previousTrack()
            } catch (e: Exception) {
                // Ignore
            }
        }

        updateNotification(isPlaying, title, artist)
        return START_STICKY
    }

    private fun updateNotification(isPlaying: Boolean, title: String, artist: String) {
        try {
            val playPauseActionIntent = Intent(this, BhajanPlaybackService::class.java).apply {
                action = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_ARTIST, artist)
                putExtra(EXTRA_IS_PLAYING, !isPlaying)
            }
            val playPausePendingIntent = PendingIntent.getService(
                this, 0, playPauseActionIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val stopActionIntent = Intent(this, BhajanPlaybackService::class.java).apply {
                action = ACTION_STOP
            }
            val stopPendingIntent = PendingIntent.getService(
                this, 1, stopActionIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val prevIntent = Intent(this, BhajanPlaybackService::class.java).apply { action = ACTION_PREVIOUS }
            val prevPendingIntent = PendingIntent.getService(
                this, 2, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val nextIntent = Intent(this, BhajanPlaybackService::class.java).apply { action = ACTION_NEXT }
            val nextPendingIntent = PendingIntent.getService(
                this, 3, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Notification.Builder(this, CHANNEL_ID)
            } else {
                @Suppress("DEPRECATION")
                Notification.Builder(this)
            }

            builder.setContentTitle(title)
                .setContentText(artist)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setOngoing(isPlaying)
                .addAction(
                    Notification.Action.Builder(
                        android.R.drawable.ic_media_previous,
                        "Previous",
                        prevPendingIntent
                    ).build()
                )
                .addAction(
                    Notification.Action.Builder(
                        if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                        if (isPlaying) "Pause" else "Play",
                        playPausePendingIntent
                    ).build()
                )
                .addAction(
                    Notification.Action.Builder(
                        android.R.drawable.ic_media_next,
                        "Next",
                        nextPendingIntent
                    ).build()
                )
                .addAction(
                    Notification.Action.Builder(
                        android.R.drawable.ic_menu_close_clear_cancel,
                        "Stop",
                        stopPendingIntent
                    ).build()
                )

            if (mediaSession != null) {
                builder.setStyle(
                    Notification.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                        .setMediaSession(mediaSession!!.sessionToken)
                )
            }

            startForeground(NOTIFICATION_ID, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Shree Jamniwala Darbar Bhajan Playback"
            val descriptionText = "Media notification for Bhajan audio playback"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession?.release()
    }
}
