package com.yaumi.app.azan.data

import com.yaumi.app.azan.domain.model.PrayerTiming

object AzanScheduleRules {
    private val trackedPrayers = setOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")

    fun selectSchedulableTimings(
        timings: List<PrayerTiming>,
        sholatEnabled: Map<String, Boolean>
    ): Map<String, String> {
        return timings
            .filter { it.name in trackedPrayers && (sholatEnabled[it.name] ?: true) }
            .associate { it.name to it.time }
    }
}
