package com.yaumi.app.qibla.data

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object QiblaMath {
    private const val KAABA_LAT = 21.4225
    private const val KAABA_LON = 39.8262
    private const val EARTH_RADIUS_KM = 6371.0

    fun bearingToKaaba(lat: Double, lon: Double): Double {
        val lat1 = Math.toRadians(lat)
        val lon1 = Math.toRadians(lon)
        val lat2 = Math.toRadians(KAABA_LAT)
        val lon2 = Math.toRadians(KAABA_LON)

        val dLon = lon2 - lon1
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        val bearing = Math.toDegrees(atan2(y, x))
        return (bearing + 360.0) % 360.0
    }

    fun distanceToKaabaKm(lat: Double, lon: Double): Double {
        val dLat = Math.toRadians(KAABA_LAT - lat)
        val dLon = Math.toRadians(KAABA_LON - lon)
        val a = sin(dLat / 2).pow(2.0) +
            cos(Math.toRadians(lat)) * cos(Math.toRadians(KAABA_LAT)) *
            sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }

    fun turnDelta(currentHeading: Double, targetBearing: Double): Double {
        val raw = ((targetBearing - currentHeading + 540.0) % 360.0) - 180.0
        return raw
    }
}
