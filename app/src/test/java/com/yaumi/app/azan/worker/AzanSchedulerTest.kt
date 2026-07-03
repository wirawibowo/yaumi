package com.yaumi.app.azan.worker

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class AzanSchedulerTest {

    @Test
    fun computeTodayEvents_emitsReminderTahrimAndAdhanInTimeOrder() {
        val events = AzanScheduler.computeTodayEvents(
            now = LocalDateTime.of(2026, 4, 22, 3, 0),
            timings = linkedMapOf("Fajr" to "05:00"),
            reminderEnabled = true,
            reminderMinutes = 15,
            tahrimLeadSeconds = 314
        )

        assertEquals(
            listOf(AzanWorker.EVENT_REMINDER, AzanWorker.EVENT_TAHRIM, AzanWorker.EVENT_ADHAN),
            events.map { it.eventType }
        )
        assertEquals(LocalDateTime.of(2026, 4, 22, 4, 45), events[0].triggerTime)
        assertEquals(LocalDateTime.of(2026, 4, 22, 4, 54, 46), events[1].triggerTime)
        assertEquals(LocalDateTime.of(2026, 4, 22, 5, 0), events[2].triggerTime)
    }

    @Test
    fun computeTodayEvents_skipsPastAndUnparseableTimings() {
        val events = AzanScheduler.computeTodayEvents(
            now = LocalDateTime.of(2026, 4, 22, 13, 0),
            timings = linkedMapOf(
                "Dhuhr" to "12:00",
                "Asr" to "oops",
                "Maghrib" to "18:00"
            ),
            reminderEnabled = false,
            reminderMinutes = 10
        )

        assertEquals(listOf("Maghrib"), events.map { it.prayerName }.distinct())
    }
}
