package com.yaumi.app.azan.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yaumi.app.azan.data.AzanEventGuardStore
import com.yaumi.app.azan.audio.AzanAudioPlayer
import com.yaumi.app.azan.data.AzanSettingsStore
import com.yaumi.app.azan.notifications.AzanNotificationHelper
import java.time.LocalDate

class AzanWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    private val tag = "AzanWorker"

    override suspend fun doWork(): Result {
        AzanNotificationHelper.ensureChannel(applicationContext)
        val prayer = inputData.getString(KEY_PRAYER) ?: return Result.success()
        val eventType = inputData.getString(KEY_EVENT_TYPE) ?: if (
            inputData.getBoolean(KEY_IS_REMINDER, false)
        ) {
            EVENT_REMINDER
        } else {
            EVENT_ADHAN
        }

        val scheduledDate = inputData.getString(KEY_SCHEDULE_DATE)
        if (scheduledDate != null && scheduledDate != LocalDate.now().toString()) {
            Log.d(tag, "Skip stale date event=$eventType prayer=$prayer scheduledDate=$scheduledDate")
            return Result.success()
        }

        val triggerMillis = inputData.getLong(KEY_TRIGGER_EPOCH_MILLIS, -1L)
        if (triggerMillis > 0L) {
            val lateMillis = System.currentTimeMillis() - triggerMillis
            if (lateMillis > MAX_LATE_EVENT_MILLIS) {
                Log.d(tag, "Skip stale late event=$eventType prayer=$prayer lateMillis=$lateMillis")
                return Result.success()
            }
        }

        val guardStore = AzanEventGuardStore(applicationContext)
        if (!guardStore.shouldRunAndMark(prayerName = prayer, eventType = eventType)) {
            Log.d(tag, "Skip duplicate event=$eventType prayer=$prayer")
            return Result.success()
        }

        Log.d(tag, "Run worker event=$eventType prayer=$prayer")
        AzanNotificationHelper.showPrayerNotification(applicationContext, prayer, eventType)

        val settings = AzanSettingsStore(applicationContext).load()
        if (settings.audioEnabled && eventType != EVENT_REMINDER) {
            AzanAudioPlayer.setVolumePercent(settings.audioVolumePercent)
            val marker = inputData.getString(KEY_AUDIO_FILE) ?: "__DEFAULT__"
            val selected = when (marker) {
                "__FAJR__" -> settings.selectedFajrAudio
                "__TAHRIM__" -> "sholawat_tahrim.mp3"
                else -> settings.selectedAudio
            }
            AzanAudioPlayer.playAssetAudio(applicationContext, selected)
        }

        return Result.success()
    }

    companion object {
        const val KEY_PRAYER = "prayer_name"
        const val KEY_IS_REMINDER = "is_reminder"
        const val KEY_EVENT_TYPE = "event_type"
        const val KEY_AUDIO_FILE = "audio_file"
        const val KEY_SCHEDULE_DATE = "schedule_date"
        const val KEY_TRIGGER_EPOCH_MILLIS = "trigger_epoch_millis"

        const val EVENT_ADHAN = "adhan"
        const val EVENT_REMINDER = "reminder"
        const val EVENT_TAHRIM = "tahrim"

        private const val MAX_LATE_EVENT_MILLIS = 20 * 60 * 1000L
    }
}
