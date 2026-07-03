package com.yaumi.app.qibla.data

import org.junit.Assert.assertEquals
import org.junit.Test

class QiblaMathTest {

    @Test
    fun bearingToKaaba_fromJakarta_matchesKnownQiblaDirection() {
        assertEquals(295.15, QiblaMath.bearingToKaaba(-6.2088, 106.8456), 0.05)
    }

    @Test
    fun bearingToKaaba_fromNewYork_matchesKnownGreatCircleDirection() {
        assertEquals(58.48, QiblaMath.bearingToKaaba(40.7128, -74.0060), 0.05)
    }

    @Test
    fun bearingToKaaba_fromPointDueSouthOfKaaba_pointsNorth() {
        assertEquals(0.0, QiblaMath.bearingToKaaba(0.0, 39.8262), 0.0001)
    }

    @Test
    fun distanceToKaaba_fromJakarta_matchesGreatCircleDistance() {
        assertEquals(7920.1, QiblaMath.distanceToKaabaKm(-6.2088, 106.8456), 1.0)
    }

    @Test
    fun distanceToKaaba_atKaabaItself_isZero() {
        assertEquals(0.0, QiblaMath.distanceToKaabaKm(21.4225, 39.8262), 0.0001)
    }

    @Test
    fun turnDelta_wrapsClockwiseAcrossNorth() {
        assertEquals(20.0, QiblaMath.turnDelta(350.0, 10.0), 0.0001)
    }

    @Test
    fun turnDelta_wrapsCounterClockwiseAcrossNorth() {
        assertEquals(-20.0, QiblaMath.turnDelta(10.0, 350.0), 0.0001)
    }

    @Test
    fun turnDelta_oppositeDirection_isMinus180() {
        assertEquals(-180.0, QiblaMath.turnDelta(0.0, 180.0), 0.0001)
    }

    @Test
    fun turnDelta_alignedHeading_isZero() {
        assertEquals(0.0, QiblaMath.turnDelta(90.0, 90.0), 0.0001)
    }
}
