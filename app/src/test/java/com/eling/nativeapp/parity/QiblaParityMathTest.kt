package com.eling.nativeapp.parity

import com.eling.nativeapp.qibla.data.QiblaMath
import org.junit.Assert.assertTrue
import org.junit.Test

class QiblaParityMathTest {
    @Test
    fun bearingToKaaba_shouldReturnValidCompassRange() {
        val bearing = QiblaMath.bearingToKaaba(-6.2088, 106.8456)
        assertTrue(bearing >= 0.0)
        assertTrue(bearing < 360.0)
    }

    @Test
    fun distanceToKaaba_shouldBePositive() {
        val distance = QiblaMath.distanceToKaabaKm(-6.2088, 106.8456)
        assertTrue(distance > 0.0)
    }

    @Test
    fun turnDelta_shouldNormalizeToMinus180Plus180() {
        val delta = QiblaMath.turnDelta(350.0, 10.0)
        assertTrue(delta in -180.0..180.0)
    }
}
