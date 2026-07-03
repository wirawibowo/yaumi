package com.yaumi.app.azan.alarm

import com.yaumi.app.azan.worker.AzanWorker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

/**
 * Tests for the event computation of the AlarmManager-based scheduler,
 * which is the path production actually uses to fire adhan/tahrim/reminder.
 */
class AzanAlarmSchedulerTest {

    private val allTimings = linkedMapOf(
        "Fajr" to "05:00",
        "Dhuhr" to "12:00",
        "Asr" to "15:00",
        "Maghrib" to "18:00",
        "Isha" to "19:00"
    )

    @Test
    fun computeEvents_schedulesTahrimByAudioDurationBeforeFajrAndMaghrib() {
        val events = AzanAlarmScheduler.computeEvents(
            now = LocalDateTime.of(2026, 4, 22, 4, 0),
            timings = allTimings,
            reminderEnabled = false,
            reminderMinutes = 10,
            tahrimLeadSeconds = 314
        )

        val fajrTahrim = events.first {
            it.prayerName == "Fajr" && it.eventType == AzanWorker.EVENT_TAHRIM
        }
        val maghribTahrim = events.first {
            it.prayerName == "Maghrib" && it.eventType == AzanWorker.EVENT_TAHRIM
        }

        assertEquals(LocalDateTime.of(2026, 4, 22, 4, 54, 46), fajrTahrim.triggerTime)
        assertEquals(LocalDateTime.of(2026, 4, 22, 17, 54, 46), maghribTahrim.triggerTime)
        assertEquals(2, events.count { it.eventType == AzanWorker.EVENT_TAHRIM })
    }

    @Test
    fun computeEvents_tahrimFiresBeforeItsAdhan() {
        val events = AzanAlarmScheduler.computeEvents(
            now = LocalDateTime.of(2026, 4, 22, 4, 40),
            timings = linkedMapOf("Fajr" to "05:00"),
            reminderEnabled = false,
            reminderMinutes = 10,
            tahrimLeadSeconds = 314
        )

        assertEquals(
            listOf(AzanWorker.EVENT_TAHRIM, AzanWorker.EVENT_ADHAN),
            events.map { it.eventType }
        )
        assertTrue(events[0].triggerTime.isBefore(events[1].triggerTime))
    }

    @Test
    fun computeEvents_skipsPrayersAlreadyPassed() {
        val events = AzanAlarmScheduler.computeEvents(
            now = LocalDateTime.of(2026, 4, 22, 15, 30),
            timings = allTimings,
            reminderEnabled = false,
            reminderMinutes = 10,
            tahrimLeadSeconds = 314
        )

        assertEquals(setOf("Maghrib", "Isha"), events.map { it.prayerName }.toSet())
    }

    @Test
    fun computeEvents_skipsTahrimWhenLeadTimeAlreadyPassedButAdhanRemains() {
        // At 04:58 the 314s tahrim lead (04:54:46) is already past; only adhan remains.
        val events = AzanAlarmScheduler.computeEvents(
            now = LocalDateTime.of(2026, 4, 22, 4, 58),
            timings = linkedMapOf("Fajr" to "05:00"),
            reminderEnabled = false,
            reminderMinutes = 10,
            tahrimLeadSeconds = 314
        )

        assertEquals(listOf(AzanWorker.EVENT_ADHAN), events.map { it.eventType })
    }

    @Test
    fun computeEvents_addsRemindersOnlyWhenEnabledAndStillUpcoming() {
        // Dhuhr's reminder slot (11:50) is already past; Asr's (14:50) is not.
        val events = AzanAlarmScheduler.computeEvents(
            now = LocalDateTime.of(2026, 4, 22, 11, 55),
            timings = linkedMapOf("Dhuhr" to "12:00", "Asr" to "15:00"),
            reminderEnabled = true,
            reminderMinutes = 10,
            tahrimLeadSeconds = 314
        )

        val reminders = events.filter { it.eventType == AzanWorker.EVENT_REMINDER }
        assertEquals(listOf("Asr"), reminders.map { it.prayerName })
        assertEquals(LocalDateTime.of(2026, 4, 22, 14, 50), reminders[0].triggerTime)
    }

    @Test
    fun computeEvents_ignoresUntrackedAndUnparseableTimings() {
        val events = AzanAlarmScheduler.computeEvents(
            now = LocalDateTime.of(2026, 4, 22, 3, 0),
            timings = linkedMapOf(
                "Imsak" to "04:20",
                "Sunrise" to "05:45",
                "Fajr" to "bad-time",
                "Dhuhr" to "12:00"
            ),
            reminderEnabled = false,
            reminderMinutes = 10,
            tahrimLeadSeconds = 314
        )

        assertEquals(listOf("Dhuhr"), events.map { it.prayerName })
    }

    @Test
    fun computeEvents_returnsEventsSortedByTriggerTime() {
        val events = AzanAlarmScheduler.computeEvents(
            now = LocalDateTime.of(2026, 4, 22, 0, 0),
            timings = allTimings,
            reminderEnabled = true,
            reminderMinutes = 10,
            tahrimLeadSeconds = 314
        )

        assertEquals(events.sortedBy { it.triggerTime }, events)
        assertEquals(12, events.size) // 5 adhan + 5 reminders + 2 tahrim
    }
}
