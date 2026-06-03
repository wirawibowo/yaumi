package com.eling.nativeapp.parity

import com.eling.nativeapp.azan.worker.AzanScheduler
import com.eling.nativeapp.azan.worker.AzanWorker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class AzanTahrimScheduleTest {

    @Test
    fun `schedule includes tahrim based on audio duration before fajr and maghrib`() {
        val now = LocalDateTime.of(2026, 4, 22, 4, 0)
        val timings = linkedMapOf(
            "Fajr" to "05:00",
            "Dhuhr" to "12:00",
            "Asr" to "15:00",
            "Maghrib" to "18:00",
            "Isha" to "19:00"
        )

        val events = AzanScheduler.computeTodayEvents(
            now = now,
            timings = timings,
            reminderEnabled = false,
            reminderMinutes = 10,
            tahrimLeadSeconds = 314
        )

        val fajrTahrim = events.firstOrNull {
            it.prayerName == "Fajr" && it.eventType == AzanWorker.EVENT_TAHRIM
        }
        val maghribTahrim = events.firstOrNull {
            it.prayerName == "Maghrib" && it.eventType == AzanWorker.EVENT_TAHRIM
        }

        assertEquals("2026-04-22T04:54:46", fajrTahrim?.triggerTime.toString())
        assertEquals("2026-04-22T17:54:46", maghribTahrim?.triggerTime.toString())
    }

    @Test
    fun `fajr tahrim happens before fajr adhan`() {
        val now = LocalDateTime.of(2026, 4, 22, 4, 40)
        val timings = linkedMapOf("Fajr" to "05:00")

        val events = AzanScheduler.computeTodayEvents(
            now = now,
            timings = timings,
            reminderEnabled = false,
            reminderMinutes = 10,
            tahrimLeadSeconds = 314
        )

        assertEquals(2, events.size)
        assertEquals(AzanWorker.EVENT_TAHRIM, events[0].eventType)
        assertEquals(AzanWorker.EVENT_ADHAN, events[1].eventType)
        assertTrue(events[0].triggerTime.isBefore(events[1].triggerTime))
    }
}
