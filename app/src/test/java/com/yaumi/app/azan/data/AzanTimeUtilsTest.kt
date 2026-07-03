package com.yaumi.app.azan.data

import com.yaumi.app.azan.domain.model.AzanUiData
import com.yaumi.app.azan.domain.model.PrayerTiming
import org.junit.Assert.assertEquals
import org.junit.Test

class AzanTimeUtilsTest {

    private fun data(vararg timings: Pair<String, String>) = AzanUiData(
        cityName = "Jakarta",
        timezone = "Asia/Jakarta",
        dateReadable = "15 Apr 2026",
        hijriReadable = "27 Syawal 1447",
        timings = timings.map { PrayerTiming(it.first, it.second) }
    )

    private fun AzanUiData.time(name: String) = timings.first { it.name == name }.time

    @Test
    fun applyOffsets_shiftsOnlyTargetedPrayers() {
        val shifted = AzanTimeUtils.applyOffsets(
            data("Fajr" to "04:30", "Dhuhr" to "12:00", "Maghrib" to "18:05"),
            mapOf("Fajr" to 2, "Maghrib" to -3)
        )

        assertEquals("04:32", shifted.time("Fajr"))
        assertEquals("18:02", shifted.time("Maghrib"))
        assertEquals("12:00", shifted.time("Dhuhr"))
    }

    @Test
    fun applyOffsets_positiveOffsetWrapsPastMidnight() {
        val shifted = AzanTimeUtils.applyOffsets(data("Isha" to "23:58"), mapOf("Isha" to 5))
        assertEquals("00:03", shifted.time("Isha"))
    }

    @Test
    fun applyOffsets_negativeOffsetWrapsBeforeMidnight() {
        val shifted = AzanTimeUtils.applyOffsets(data("Fajr" to "00:02"), mapOf("Fajr" to -5))
        assertEquals("23:57", shifted.time("Fajr"))
    }

    @Test
    fun applyOffsets_keepsUnparseableTimeUnchanged() {
        val shifted = AzanTimeUtils.applyOffsets(data("Fajr" to "--:--"), mapOf("Fajr" to 10))
        assertEquals("--:--", shifted.time("Fajr"))
    }

    @Test
    fun applyOffsets_zeroAndMissingOffsetsLeaveDataIdentical() {
        val original = data("Fajr" to "04:30", "Asr" to "15:20")

        assertEquals(original, AzanTimeUtils.applyOffsets(original, mapOf("Fajr" to 0)))
        assertEquals(original, AzanTimeUtils.applyOffsets(original, emptyMap()))
    }
}
