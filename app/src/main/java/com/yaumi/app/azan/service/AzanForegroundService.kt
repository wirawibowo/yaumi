package com.yaumi.app.azan.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.yaumi.app.MainActivity
import com.yaumi.app.R
import com.yaumi.app.azan.alarm.AzanControlReceiver
import com.yaumi.app.azan.audio.AzanAudioPlayer
import com.yaumi.app.azan.data.AzanSettingsStore
import com.yaumi.app.azan.worker.AzanWorker

/**
 * Foreground service that handles azan audio playback and shows a persistent
 * notification with playback controls (Vol−, Pause/Resume, Vol+, Stop).
 *
 * Running audio from a foreground service (instead of directly from a
 * BroadcastReceiver) ensures it works reliably on Android 8+ when the app
 * is fully killed.
 */
class AzanForegroundService : Service() {

    private val tag = "AzanForegroundService"
    private var currentPrayer = ""
    private var currentEventType = ""

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_SERVICE -> {
                stopPlayback()
                return START_NOT_STICKY
            }
            ACTION_VOLUME_UP -> {
                val store = AzanSettingsStore(this)
                val settings = store.load()
                val next = (settings.audioVolumePercent + 10).coerceAtMost(100)
                store.save(settings.copy(audioVolumePercent = next))
                AzanAudioPlayer.setVolumePercent(next)
                updateNotification(next)
                return START_NOT_STICKY
            }
            ACTION_VOLUME_DOWN -> {
                val store = AzanSettingsStore(this)
                val settings = store.load()
                val next = (settings.audioVolumePercent - 10).coerceAtLeast(0)
                store.save(settings.copy(audioVolumePercent = next))
                AzanAudioPlayer.setVolumePercent(next)
                updateNotification(next)
                return START_NOT_STICKY
            }
        }

        // New playback request
        val prayerName = intent?.getStringExtra(EXTRA_PRAYER) ?: "Azan"
        val eventType = intent?.getStringExtra(EXTRA_EVENT_TYPE) ?: AzanWorker.EVENT_ADHAN
        val audioMarker = intent?.getStringExtra(EXTRA_AUDIO_MARKER) ?: "__DEFAULT__"
        currentPrayer = prayerName
        currentEventType = eventType

        val settings = AzanSettingsStore(this).load()
        val audioFile = when (audioMarker) {
            "__TAHRIM__" -> "sholawat_tahrim.mp3"
            "__FAJR__" -> settings.selectedFajrAudio
            else -> settings.selectedAudio
        }

        // Must call startForeground within 5s of startForegroundService
        startForeground(NOTIFICATION_ID, buildNotification(prayerName, eventType, settings.audioVolumePercent))

        if (settings.audioEnabled) {
            AzanAudioPlayer.setVolumePercent(settings.audioVolumePercent)
            AzanAudioPlayer.playAssetAudio(
                context = this,
                fileName = audioFile,
                onCompletion = {
                    Log.d(tag, "Audio completed: $prayerName $eventType")
                    stopPlayback()
                }
            )
        } else {
            stopPlayback()
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        AzanAudioPlayer.stopCurrent()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun stopPlayback() {
        AzanAudioPlayer.stopCurrent()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun updateNotification(volumePercent: Int) {
        val notification = buildNotification(currentPrayer, currentEventType, volumePercent)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(prayerName: String, eventType: String, volumePercent: Int): Notification {
        val display = when (prayerName) {
            "Fajr" -> "Subuh"; "Dhuhr" -> "Dzuhur"; "Asr" -> "Ashar"
            "Maghrib" -> "Maghrib"; "Isha" -> "Isya"; else -> prayerName
        }
        val title = if (eventType == AzanWorker.EVENT_TAHRIM) "Sholawat Tahrim" else "Azan $display"

        val launchPi = PendingIntent.getActivity(
            this, 1001,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val volDownPi = servicePi(ACTION_VOLUME_DOWN, 2)
        val stopPi = servicePi(ACTION_STOP_SERVICE, 3)
        val volUpPi = servicePi(ACTION_VOLUME_UP, 4)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText("Volume $volumePercent%  •  Yaumi")
            .setOngoing(false)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setContentIntent(launchPi)
            .addAction(android.R.drawable.ic_lock_silent_mode, "Vol −", volDownPi)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopPi)
            .addAction(android.R.drawable.ic_lock_silent_mode_off, "Vol +", volUpPi)
            .build()
    }

    private fun servicePi(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(this, AzanForegroundService::class.java).apply { this.action = action }
        return PendingIntent.getService(
            this, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun ensureChannel() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Pemutaran Azan", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notifikasi azan dengan kontrol Vol−, Stop, Vol+"
                setShowBadge(false)
            }
        )
    }

    companion object {
        private const val CHANNEL_ID = "yaumi_azan_playback_service"
        const val NOTIFICATION_ID = 14001
        const val EXTRA_PRAYER = "extra_prayer"
        const val EXTRA_EVENT_TYPE = "extra_event_type"
        const val EXTRA_AUDIO_MARKER = "extra_audio_marker"
        const val ACTION_STOP_SERVICE = "com.yaumi.app.action.STOP_AZAN_SERVICE"
        const val ACTION_VOLUME_UP = "com.yaumi.app.action.AZAN_VOLUME_UP"
        const val ACTION_VOLUME_DOWN = "com.yaumi.app.action.AZAN_VOLUME_DOWN"

        fun start(context: Context, prayerName: String, eventType: String, audioMarker: String) {
            val intent = Intent(context, AzanForegroundService::class.java).apply {
                putExtra(EXTRA_PRAYER, prayerName)
                putExtra(EXTRA_EVENT_TYPE, eventType)
                putExtra(EXTRA_AUDIO_MARKER, audioMarker)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, AzanForegroundService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
