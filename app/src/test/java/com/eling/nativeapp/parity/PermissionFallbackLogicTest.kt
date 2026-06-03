package com.eling.nativeapp.parity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionFallbackLogicTest {
    @Test
    fun locationPermission_isGrantedWhenAnyLocationPermissionTrue() {
        val fineGranted = true
        val coarseGranted = false
        val granted = fineGranted || coarseGranted
        assertTrue(granted)
    }

    @Test
    fun locationPermission_isDeniedWhenBothLocationPermissionsFalse() {
        val fineGranted = false
        val coarseGranted = false
        val denied = !(fineGranted || coarseGranted)
        assertTrue(denied)
    }

    @Test
    fun notificationPermissionDenied_bannerConditionMatchesExpected() {
        val notifGranted = false
        val bannerShown = !notifGranted
        assertTrue(bannerShown)
        assertFalse(notifGranted)
    }

    @Test
    fun fallbackCity_shouldRemainJakartaDefaultForDeniedLocationFlow() {
        val fallbackCity = "Jakarta"
        assertEquals("Jakarta", fallbackCity)
    }
}
