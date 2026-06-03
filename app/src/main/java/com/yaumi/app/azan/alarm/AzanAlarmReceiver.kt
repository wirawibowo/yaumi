package com.yaumi.app.azan.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import com.yaumi.app.azan.data.AzanEventGuardStore
import com.yaumi.app.azan.data.AzanSettingsStore
import com.yaumi.app.azan.notifications.AzanNotificationHelper
import com.yaumi.app.azan.service.AzanForegroundService
import com.yaumi.app.azan.worker.AzanWorker
import java.time.LocalDate

/**
 * BroadcastReceiver that AlarmManager fires at the exact prayer time.
 *
 * For adhan/tahrim: delegates audio playback and notification to
 * AzanForegroundService, which runs as a proper foreground service
 * so audio works reliably even when the app is killed (Android 8+).
 *
 * For reminders: shows a simple notification directly (no audio needed).
 */
class AzanAlarmReceiver : BroadcastReceiver() {
    private val tag = "AzanAlarmReceiver"

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val prayer = intent.getStringExtra(EXTRA_PRAYER) ?: return
        val eventType = intent.getStringExtra(EXTRA_EVENT_TYPE) ?: AzanWorker.EVENT_ADHAN
        val scheduledDate = intent.getStringExtra(EXTRA_SCHEDULE_DATE)

        // Skip stale events (e.g. boot triggered an alarm meant for previous day)
        if (scheduledDate != null && scheduledDate != LocalDate.now().toString()) {
            Log.d(tag, "Skip stale event=$eventType prayer=$prayer scheduledDate=$scheduledDate")
            return
        }

        // Dedupe guard prevents the same alarm firing twice (e.g., rescheduled at boot)
        val guardStore = AzanEventGuardStore(context)
        if (!guardStore.shouldRunAndMark(prayerName = prayer, eventType = eventType)) {
            Log.d(tag, "Skip duplicate event=$eventType prayer=$prayer")
            return
        }

        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Yaumi::AzanAlarm")
        wakeLock.acquire(10_000L)

        try {
            AzanNotificationHelper.ensureChannel(context)
            val settings = AzanSettingsStore(context).load()

            if (!settings.notificationEnabled) {
                AzanAlarmScheduler.rescheduleIfNeeded(context)
                return
            }

            if (eventType == AzanWorker.EVENT_REMINDER) {
                // Reminders: simple notification, no audio
                AzanNotificationHelper.showPrayerNotification(context, prayer, eventType)
            } else {
                // Adhan / Tahrim: start foreground service to handle audio + notification
                val audioMarker = intent.getStringExtra(EXTRA_AUDIO_MARKER) ?: "__DEFAULT__"
                AzanForegroundService.start(context, prayer, eventType, audioMarker)
            }

            AzanAlarmScheduler.rescheduleIfNeeded(context)
        } finally {
            if (wakeLock.isHeld) wakeLock.release()
        }
    }

    companion object {
        const val EXTRA_PRAYER = "prayer"
        const val EXTRA_EVENT_TYPE = "event_type"
        const val EXTRA_AUDIO_MARKER = "audio_marker"
        const val EXTRA_SCHEDULE_DATE = "schedule_date"
    }
}
