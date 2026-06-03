package com.yaumi.app.azan.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.yaumi.app.MainActivity
import com.yaumi.app.R
import com.yaumi.app.azan.alarm.AzanControlReceiver
import com.yaumi.app.azan.audio.AzanAudioPlayer
import com.yaumi.app.azan.data.AzanSettingsStore
import com.yaumi.app.azan.worker.AzanWorker

object AzanNotificationHelper {
    const val CHANNEL_AZAN = "azan_schedule_channel"
    const val CHANNEL_REMINDER = "azan_reminder_channel"
    const val CHANNEL_TAHRIM = "azan_tahrim_channel"
    const val CHANNEL_PLAYBACK = "azan_playback_channel"

    private const val PLAYBACK_NOTIFICATION_ID = 14201

    private fun localPrayerName(prayerName: String): String = when (prayerName) {
        "Fajr" -> "Subuh"
        "Dhuhr" -> "Dzuhur"
        "Asr" -> "Ashar"
        "Maghrib" -> "Maghrib"
        "Isha" -> "Isya"
        else -> prayerName
    }

    private fun audioFileLabel(file: String): String = when (file) {
        "azan.mp3" -> "Azan Standar"
        "azan_madinah.mp3" -> "Azan Madinah"
        "azan_mekah.mp3" -> "Azan Mekah"
        "azan_subuh.mp3" -> "Azan Subuh"
        "sholawat_tahrim.mp3" -> "Sholawat Tahrim"
        else -> file
    }

    private fun notificationIdFor(prayerName: String): Int = ("yaumi_prayer_" + prayerName).hashCode()

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_AZAN, "Azan", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notifikasi waktu azan"
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_REMINDER, "Pengingat Azan", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Pengingat sebelum waktu azan"
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_TAHRIM, "Sholawat Tahrim", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Sholawat sebelum azan Subuh dan Maghrib"
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_PLAYBACK, "Kontrol Pemutaran Azan", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Kontrol play, pause, volume saat azan berbunyi"
            }
        )
    }

    /** Static informational notification (auto-cancel on tap). */
    fun showPrayerNotification(context: Context, prayerName: String, eventType: String) {
        if (!hasNotifPermission(context)) return

        val display = localPrayerName(prayerName)
        val (title, body, channelId) = when (eventType) {
            AzanWorker.EVENT_REMINDER -> Triple("Pengingat Azan", "$display akan segera tiba", CHANNEL_REMINDER)
            AzanWorker.EVENT_TAHRIM -> Triple("Sholawat Tahrim", "Sholawat sebelum azan $display", CHANNEL_TAHRIM)
            else -> Triple("Waktu Azan", "Saatnya $display", CHANNEL_AZAN)
        }

        val priority = if (eventType == AzanWorker.EVENT_REMINDER) {
            NotificationCompat.PRIORITY_DEFAULT
        } else {
            NotificationCompat.PRIORITY_HIGH
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(launchAppIntent(context))
            .setPriority(priority)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .build()

        val manager = NotificationManagerCompat.from(context)
        val id = notificationIdFor(prayerName)

        if (eventType != AzanWorker.EVENT_REMINDER) {
            manager.cancel(id)
        }
        manager.notify(id, notification)
    }

    /**
     * Persistent playback notification with action buttons:
     * Vol−  Stop  Vol+.
     */
    fun showAzanWithControls(
        context: Context,
        prayerName: String,
        eventType: String,
        audioFile: String
    ) {
        if (!hasNotifPermission(context)) return
        val volume = AzanSettingsStore(context).load().audioVolumePercent
        val display = localPrayerName(prayerName)
        val title = if (eventType == AzanWorker.EVENT_TAHRIM) "Sholawat Tahrim" else "Azan $display"
        val body = "${audioFileLabel(audioFile)}  •  Volume $volume%"

        val notification = buildPlaybackNotification(
            context = context,
            title = title,
            body = body
        )
        NotificationManagerCompat.from(context).notify(PLAYBACK_NOTIFICATION_ID, notification)
    }

    fun refreshAzanControls(
        context: Context,
        volumePercent: Int? = null
    ) {
        if (!hasNotifPermission(context)) return
        val settings = AzanSettingsStore(context).load()
        val vol = volumePercent ?: settings.audioVolumePercent
        val title = "Azan"
        val body = "Volume $vol%"

        val notification = buildPlaybackNotification(
            context = context,
            title = title,
            body = body
        )
        NotificationManagerCompat.from(context).notify(PLAYBACK_NOTIFICATION_ID, notification)
    }

    fun cancelAzanPlaybackNotification(context: Context) {
        NotificationManagerCompat.from(context).cancel(PLAYBACK_NOTIFICATION_ID)
    }

    // ─── helpers ───────────────────────────────────────────────────────────

    private fun buildPlaybackNotification(
        context: Context,
        title: String,
        body: String
    ): android.app.Notification {
        val volDownAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_lock_silent_mode,
            "Vol −",
            broadcastIntent(context, AzanControlReceiver.ACTION_VOLUME_DOWN, 3)
        ).build()
        val volUpAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_lock_silent_mode_off,
            "Vol +",
            broadcastIntent(context, AzanControlReceiver.ACTION_VOLUME_UP, 4)
        ).build()
        val stopAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_close_clear_cancel,
            "Stop",
            broadcastIntent(context, AzanControlReceiver.ACTION_STOP, 5)
        ).build()

        return NotificationCompat.Builder(context, CHANNEL_PLAYBACK)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(launchAppIntent(context))
            .setOngoing(AzanAudioPlayer.isPlaying())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .addAction(volDownAction)
            .addAction(stopAction)
            .addAction(volUpAction)
            .build()
    }

    private fun broadcastIntent(context: Context, action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, AzanControlReceiver::class.java).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun launchAppIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun hasNotifPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
