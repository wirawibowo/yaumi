package com.eling.nativeapp.parity

import com.eling.nativeapp.azan.data.AzanScheduleRules
import com.eling.nativeapp.azan.data.AzanTimeUtils
import com.eling.nativeapp.azan.domain.model.AzanUiData
import com.eling.nativeapp.azan.domain.model.PrayerTiming
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AzanParityRulesTest {
    @Test
    fun applyOffsets_shouldShiftTargetPrayerTimes() {
        val data = AzanUiData(
            cityName = "Jakarta",
            timezone = "Asia/Jakarta",
            dateReadable = "15 Apr 2026",
            hijriReadable = "7 Shawwal 1447",
            timings = listOf(
                PrayerTiming("Fajr", "04:30"),
                PrayerTiming("Dhuhr", "12:00"),
                PrayerTiming("Asr", "15:20"),
                PrayerTiming("Maghrib", "18:05"),
                PrayerTiming("Isha", "19:15")
            )
        )

        val shifted = AzanTimeUtils.applyOffsets(
            data,
            mapOf("Fajr" to 2, "Maghrib" to -3)
        )

        val map = shifted.timings.associate { it.name to it.time }
        assertEquals("04:32", map["Fajr"])
        assertEquals("18:02", map["Maghrib"])
        assertEquals("12:00", map["Dhuhr"])
    }

    @Test
    fun selectSchedulableTimings_shouldRespectSholatEnabledMap() {
        val timings = listOf(
            PrayerTiming("Fajr", "04:30"),
            PrayerTiming("Sunrise", "05:45"),
            PrayerTiming("Dhuhr", "12:00"),
            PrayerTiming("Asr", "15:20"),
            PrayerTiming("Maghrib", "18:05"),
            PrayerTiming("Isha", "19:15")
        )

        val selected = AzanScheduleRules.selectSchedulableTimings(
            timings = timings,
            sholatEnabled = mapOf(
                "Fajr" to true,
                "Dhuhr" to false,
                "Asr" to true,
                "Maghrib" to true,
                "Isha" to false
            )
        )

        assertTrue(selected.containsKey("Fajr"))
        assertTrue(selected.containsKey("Asr"))
        assertTrue(selected.containsKey("Maghrib"))
        assertFalse(selected.containsKey("Sunrise"))
        assertFalse(selected.containsKey("Dhuhr"))
        assertFalse(selected.containsKey("Isha"))
    }
}
