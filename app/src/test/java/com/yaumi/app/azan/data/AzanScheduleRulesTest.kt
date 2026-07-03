package com.yaumi.app.azan.data

import com.yaumi.app.azan.domain.model.PrayerTiming
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AzanScheduleRulesTest {

    private val timings = listOf(
        PrayerTiming("Imsak", "04:20"),
        PrayerTiming("Fajr", "04:30"),
        PrayerTiming("Sunrise", "05:45"),
        PrayerTiming("Dhuhr", "12:00"),
        PrayerTiming("Asr", "15:20"),
        PrayerTiming("Maghrib", "18:05"),
        PrayerTiming("Isha", "19:15")
    )

    @Test
    fun selectSchedulableTimings_respectsEnabledMap() {
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

        assertEquals(setOf("Fajr", "Asr", "Maghrib"), selected.keys)
        assertEquals("04:30", selected["Fajr"])
        assertEquals("18:05", selected["Maghrib"])
    }

    @Test
    fun selectSchedulableTimings_defaultsToEnabledWhenPrayerMissingFromMap() {
        val selected = AzanScheduleRules.selectSchedulableTimings(timings, emptyMap())

        assertEquals(setOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha"), selected.keys)
    }

    @Test
    fun selectSchedulableTimings_neverIncludesUntrackedTimings() {
        val selected = AzanScheduleRules.selectSchedulableTimings(
            timings = timings,
            sholatEnabled = mapOf("Imsak" to true, "Sunrise" to true)
        )

        assertFalse("Imsak" in selected.keys)
        assertFalse("Sunrise" in selected.keys)
    }
}
