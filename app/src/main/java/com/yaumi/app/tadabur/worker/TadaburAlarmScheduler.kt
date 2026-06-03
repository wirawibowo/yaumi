package com.yaumi.app.tadabur.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.yaumi.app.azan.domain.model.AzanUiData
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object TadaburAlarmScheduler {
    private const val TAG = "TadaburAlarm"
    private const val REQUEST_CODE = 21201
    private val formatter = DateTimeFormatter.ofPattern("HH:mm")

    fun scheduleFromAzan(
        context: Context,
        data: AzanUiData?,
        offsetMinutes: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        cancel(context, alarmManager)

        val fajrTime = data?.timings?.firstOrNull { it.name == "Fajr" }?.time
        val parsed = runCatching { fajrTime?.let { LocalTime.parse(it, formatter) } }.getOrNull()
        val baseTime = parsed ?: LocalTime.of(5, 30)
        val trigger = LocalDate.now().atTime(baseTime).plusMinutes(offsetMinutes.toLong())
        val triggerMillis = trigger.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val intent = Intent(context, TadaburAlarmReceiver::class.java).apply {
            putExtra(TadaburAlarmReceiver.EXTRA_SCHEDULE_DATE, trigger.toLocalDate().toString())
        }
        val pi = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
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
            Log.d(TAG, "Scheduled tadabur at $trigger")
        } catch (e: SecurityException) {
            Log.w(TAG, "No exact alarm permission for tadabur", e)
        }
    }

    fun scheduleFallback(context: Context, offsetMinutes: Int) {
        val fallback = AzanUiData(
            cityName = "",
            timezone = "Asia/Jakarta",
            dateReadable = "",
            hijriReadable = "",
            timings = listOf(
                com.yaumi.app.azan.domain.model.PrayerTiming("Fajr", "05:00")
            )
        )
        scheduleFromAzan(context, fallback, offsetMinutes)
    }

    fun cancel(context: Context, alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager) {
        val intent = Intent(context, TadaburAlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pi != null) {
            alarmManager.cancel(pi)
            pi.cancel()
        }
    }
}
