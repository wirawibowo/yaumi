package com.yaumi.app.azan.worker

import android.content.Context
import com.yaumi.app.azan.audio.AzanAudioPlayer
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import android.util.Log
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object AzanScheduler {
    private const val TAG = "AzanScheduler"
    private const val WORK_TAG_AZAN = "azan_schedule"
    private const val WORK_RESCHEDULE_NEXT_DAY = "azan_reschedule_next_day"
    private val formatter = DateTimeFormatter.ofPattern("HH:mm")
    private val tahrimPrayers = setOf("Fajr", "Maghrib")

    data class ScheduledEvent(
        val prayerName: String,
        val triggerTime: LocalDateTime,
        val eventType: String
    )

    fun scheduleForToday(
        context: Context,
        timings: Map<String, String>,
        reminderEnabled: Boolean,
        reminderMinutes: Int
    ) {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG_AZAN)
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_RESCHEDULE_NEXT_DAY)

        val tahrimLeadSeconds = AzanAudioPlayer.assetDurationSeconds(
            context = context,
            fileName = "sholawat_tahrim.mp3"
        )

        val now = LocalDateTime.now()
        val events = computeTodayEvents(
            now = now,
            timings = timings,
            reminderEnabled = reminderEnabled,
            reminderMinutes = reminderMinutes,
            tahrimLeadSeconds = tahrimLeadSeconds
        )

        events.forEach { event ->
            Log.d(TAG, "Enqueue ${event.eventType.uppercase()} ${event.prayerName} at ${event.triggerTime}")
            enqueue(
                context = context,
                prayerName = event.prayerName,
                triggerTime = event.triggerTime,
                eventType = event.eventType
            )
        }

        enqueueNextDayReschedule(context, now)
    }

    fun computeTodayEvents(
        now: LocalDateTime,
        timings: Map<String, String>,
        reminderEnabled: Boolean,
        reminderMinutes: Int,
        tahrimLeadSeconds: Int = 314
    ): List<ScheduledEvent> {
        val events = mutableListOf<ScheduledEvent>()
        timings.forEach { (name, value) ->
            val prayerTime = runCatching { LocalTime.parse(value, formatter) }.getOrNull() ?: return@forEach
            val prayerDateTime = now.toLocalDate().atTime(prayerTime)
            if (!prayerDateTime.isAfter(now)) return@forEach

            if (name in tahrimPrayers) {
                val tahrimTime = prayerDateTime.minusSeconds(tahrimLeadSeconds.toLong())
                if (tahrimTime.isAfter(now)) {
                    events += ScheduledEvent(name, tahrimTime, AzanWorker.EVENT_TAHRIM)
                }
            }

            events += ScheduledEvent(name, prayerDateTime, AzanWorker.EVENT_ADHAN)

            if (reminderEnabled) {
                val reminderTime = prayerDateTime.minusMinutes(reminderMinutes.toLong())
                if (reminderTime.isAfter(now)) {
                    events += ScheduledEvent(name, reminderTime, AzanWorker.EVENT_REMINDER)
                }
            }
        }

        return events.sortedBy { it.triggerTime }
    }

    private fun enqueue(
        context: Context,
        prayerName: String,
        triggerTime: LocalDateTime,
        eventType: String
    ) {
        val delay = Duration.between(LocalDateTime.now(), triggerTime)
        val triggerMillis = triggerTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val scheduleDate = triggerTime.toLocalDate().toString()
        val audioMarker = when (eventType) {
            AzanWorker.EVENT_TAHRIM -> "__TAHRIM__"
            AzanWorker.EVENT_ADHAN -> if (prayerName == "Fajr") "__FAJR__" else "__DEFAULT__"
            else -> "__DEFAULT__"
        }
        val request = OneTimeWorkRequestBuilder<AzanWorker>()
            .setInitialDelay(delay)
            .addTag(WORK_TAG_AZAN)
            .setInputData(
                workDataOf(
                    AzanWorker.KEY_PRAYER to prayerName,
                    AzanWorker.KEY_EVENT_TYPE to eventType,
                    AzanWorker.KEY_IS_REMINDER to (eventType == AzanWorker.EVENT_REMINDER),
                    AzanWorker.KEY_AUDIO_FILE to audioMarker,
                    AzanWorker.KEY_SCHEDULE_DATE to scheduleDate,
                    AzanWorker.KEY_TRIGGER_EPOCH_MILLIS to triggerMillis
                )
            )
            .build()

        val uniqueName = "azan_${prayerName}_$eventType"
        WorkManager.getInstance(context).enqueueUniqueWork(uniqueName, ExistingWorkPolicy.REPLACE, request)
    }

    private fun enqueueNextDayReschedule(context: Context, now: LocalDateTime) {
        val nextRun = now.toLocalDate().plusDays(1).atTime(0, 1)
        val delay = Duration.between(now, nextRun)

        val request = OneTimeWorkRequestBuilder<AzanRescheduleWorker>()
            .setInitialDelay(delay)
            .addTag(WORK_RESCHEDULE_NEXT_DAY)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_RESCHEDULE_NEXT_DAY,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
