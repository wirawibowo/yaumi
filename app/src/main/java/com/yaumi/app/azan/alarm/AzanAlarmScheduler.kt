package com.yaumi.app.azan.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.yaumi.app.azan.audio.AzanAudioPlayer
import com.yaumi.app.azan.data.AzanRepository
import com.yaumi.app.azan.data.AzanSettingsStore
import com.yaumi.app.azan.worker.AzanWorker
import com.yaumi.app.tadabur.data.TadaburSettingsStore
import com.yaumi.app.tadabur.worker.TadaburAlarmScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Schedules every prayer event for today using AlarmManager.setExactAndAllowWhileIdle().
 *
 * Why AlarmManager instead of WorkManager?
 *  - WorkManager requires the OS to be willing to wake your app for the
 *    deferred job. On Doze and aggressive OEM battery savers, that wakeup
 *    can be skipped or delayed.
 *  - AlarmManager.setExactAndAllowWhileIdle() bypasses Doze restrictions
 *    for the broadcast that fires AzanAlarmReceiver, so the prayer triggers
 *    even when the app is fully closed.
 */
object AzanAlarmScheduler {
    private const val TAG = "AzanAlarmScheduler"
    private val formatter = DateTimeFormatter.ofPattern("HH:mm")
    private val tahrimPrayers = setOf("Fajr", "Maghrib")

    fun scheduleForToday(
        context: Context,
        timings: Map<String, String>,
        reminderEnabled: Boolean,
        reminderMinutes: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = LocalDateTime.now()
        val tahrimLeadSeconds = AzanAudioPlayer.assetDurationSeconds(
            context = context,
            fileName = "sholawat_tahrim.mp3"
        )

        // Cancel any prior alarms before re-scheduling
        cancelAll(context)

        val events = computeEvents(
            now = now,
            timings = timings,
            reminderEnabled = reminderEnabled,
            reminderMinutes = reminderMinutes,
            tahrimLeadSeconds = tahrimLeadSeconds
        )

        events.forEach { event ->
            scheduleAlarm(context, alarmManager, event)
            Log.d(TAG, "Scheduled ${event.eventType} ${event.prayerName} at ${event.triggerTime}")
        }
    }

    /**
     * Called from receiver after a prayer fires — if there are no remaining
     * events today, schedule the next-day reschedule worker.
     */
    fun rescheduleIfNeeded(context: Context) {
        // Schedule tomorrow's reschedule at 00:01
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val nextRun = LocalDate.now().plusDays(1).atTime(0, 1)
        val triggerMillis = nextRun.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val intent = Intent(context, AzanRescheduleAlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_RESCHEDULE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pi)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pi)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pi)
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "No exact alarm permission for reschedule", e)
        }
    }

    /**
     * Called by the daily reschedule receiver: refetch prayer times via
     * repository then schedule today's alarms.
     */
    fun rescheduleFromRepository(context: Context) {
        GlobalScope.launch(Dispatchers.IO) {
            runCatching {
                val settings = AzanSettingsStore(context).load()
                val repo = AzanRepository(context)
                val data = repo.loadAzanData()
                val timings = data.timings.associate { it.name to it.time }
                scheduleForToday(
                    context = context,
                    timings = timings,
                    reminderEnabled = settings.reminderEnabled,
                    reminderMinutes = settings.reminderMinutes
                )
                val tadaburSettings = TadaburSettingsStore(context).load()
                if (tadaburSettings.notificationEnabled) {
                    TadaburAlarmScheduler.scheduleFromAzan(
                        context = context,
                        data = data,
                        offsetMinutes = tadaburSettings.offsetMinutesAfterFajr
                    )
                }
            }.onFailure {
                Log.w(TAG, "Failed to reschedule from repository", it)
                runCatching {
                    val settings = AzanSettingsStore(context).load()
                    val fallbackTimings = linkedMapOf(
                        "Fajr" to "04:35",
                        "Dhuhr" to "12:03",
                        "Asr" to "15:24",
                        "Maghrib" to "18:07",
                        "Isha" to "19:18"
                    )
                    scheduleForToday(
                        context = context,
                        timings = fallbackTimings,
                        reminderEnabled = settings.reminderEnabled,
                        reminderMinutes = settings.reminderMinutes
                    )
                    val tadaburSettings = TadaburSettingsStore(context).load()
                    if (tadaburSettings.notificationEnabled) {
                        TadaburAlarmScheduler.scheduleFallback(
                            context = context,
                            offsetMinutes = tadaburSettings.offsetMinutesAfterFajr
                        )
                    }
                    rescheduleIfNeeded(context)
                }.onFailure { fallbackErr ->
                    Log.w(TAG, "Fallback schedule failed", fallbackErr)
                }
            }
        }
    }

    fun cancelAll(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // Cancel by iterating known prayers + event types
        val prayers = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
        val eventTypes = listOf(AzanWorker.EVENT_ADHAN, AzanWorker.EVENT_REMINDER, AzanWorker.EVENT_TAHRIM)
        prayers.forEach { p ->
            eventTypes.forEach { e ->
                val intent = Intent(context, AzanAlarmReceiver::class.java)
                val requestCode = requestCodeFor(p, e)
                val pi = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
                if (pi != null) {
                    alarmManager.cancel(pi)
                    pi.cancel()
                }
            }
        }
    }

    private fun computeEvents(
        now: LocalDateTime,
        timings: Map<String, String>,
        reminderEnabled: Boolean,
        reminderMinutes: Int,
        tahrimLeadSeconds: Int
    ): List<ScheduledEvent> {
        val orderedNames = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
        val events = mutableListOf<ScheduledEvent>()

        timings.forEach { (name, value) ->
            if (name !in orderedNames) return@forEach
            val prayerTime = runCatching { LocalTime.parse(value, formatter) }.getOrNull()
                ?: return@forEach
            val prayerDateTime = now.toLocalDate().atTime(prayerTime)
            if (!prayerDateTime.isAfter(now)) return@forEach

            // Tahrim before Fajr/Maghrib
            if (name in tahrimPrayers) {
                val tahrimTime = prayerDateTime.minusSeconds(tahrimLeadSeconds.toLong())
                if (tahrimTime.isAfter(now)) {
                    events += ScheduledEvent(name, tahrimTime, AzanWorker.EVENT_TAHRIM)
                }
            }

            // Adhan
            events += ScheduledEvent(name, prayerDateTime, AzanWorker.EVENT_ADHAN)

            // Reminder
            if (reminderEnabled) {
                val reminderTime = prayerDateTime.minusMinutes(reminderMinutes.toLong())
                if (reminderTime.isAfter(now)) {
                    events += ScheduledEvent(name, reminderTime, AzanWorker.EVENT_REMINDER)
                }
            }
        }

        return events.sortedBy { it.triggerTime }
    }

    private fun scheduleAlarm(
        context: Context,
        alarmManager: AlarmManager,
        event: ScheduledEvent
    ) {
        val triggerMillis = event.triggerTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val audioMarker = when (event.eventType) {
            AzanWorker.EVENT_TAHRIM -> "__TAHRIM__"
            AzanWorker.EVENT_ADHAN -> if (event.prayerName == "Fajr") "__FAJR__" else "__DEFAULT__"
            else -> "__DEFAULT__"
        }

        val intent = Intent(context, AzanAlarmReceiver::class.java).apply {
            putExtra(AzanAlarmReceiver.EXTRA_PRAYER, event.prayerName)
            putExtra(AzanAlarmReceiver.EXTRA_EVENT_TYPE, event.eventType)
            putExtra(AzanAlarmReceiver.EXTRA_AUDIO_MARKER, audioMarker)
            putExtra(AzanAlarmReceiver.EXTRA_SCHEDULE_DATE, event.triggerTime.toLocalDate().toString())
        }

        val pi = PendingIntent.getBroadcast(
            context,
            requestCodeFor(event.prayerName, event.eventType),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pi)
                } else {
                    // Fall back to inexact while-idle to still fire even without permission
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pi)
                    Log.w(TAG, "Exact alarm permission not granted; using setAndAllowWhileIdle for ${event.prayerName}")
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pi)
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Failed to schedule exact alarm for ${event.prayerName}", e)
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pi)
        }
    }

    private fun requestCodeFor(prayerName: String, eventType: String): Int {
        // Stable hash so PendingIntent is reproducible
        return ("$prayerName|$eventType").hashCode() and 0x7FFFFFFF
    }

    private const val REQUEST_CODE_RESCHEDULE = 9001

    data class ScheduledEvent(
        val prayerName: String,
        val triggerTime: LocalDateTime,
        val eventType: String
    )
}
