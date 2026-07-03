package com.yaumi.app.home.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DailyAyatProviderTest {

    @Test
    fun forDate_isDeterministicForSameDate() {
        val date = LocalDate.of(2026, 7, 3)
        assertEquals(DailyAyatProvider.forDate(date), DailyAyatProvider.forDate(date))
    }

    @Test
    fun forDate_changesFromOneDayToTheNext() {
        val date = LocalDate.of(2026, 7, 3)
        assertNotEquals(DailyAyatProvider.forDate(date), DailyAyatProvider.forDate(date.plusDays(1)))
    }

    @Test
    fun forDate_handlesDatesBeforeEpochWithoutCrashing() {
        // Negative epoch day exercises the double-modulo normalization.
        val verse = DailyAyatProvider.forDate(LocalDate.of(1969, 12, 30))
        assertTrue(verse.surahLabel.startsWith("Q.S."))
    }

    @Test
    fun forDate_alwaysReturnsFullyPopulatedVerse() {
        var date = LocalDate.of(2026, 1, 1)
        repeat(60) {
            val verse = DailyAyatProvider.forDate(date)
            assertTrue(verse.surahLabel.isNotBlank())
            assertTrue(verse.arabic.isNotBlank())
            assertTrue(verse.translation.isNotBlank())
            date = date.plusDays(1)
        }
    }
}
