package com.yaumi.app.azan.data

import android.content.Context
import java.time.LocalDate

class AzanEventGuardStore(appContext: Context) {
    private val prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun shouldRunAndMark(prayerName: String, eventType: String): Boolean {
        val today = LocalDate.now().toString()
        val key = "$today|$prayerName|$eventType"
        val current = prefs.getStringSet(KEY_HANDLED_EVENTS, emptySet()).orEmpty().toMutableSet()

        val cleaned = current.filterTo(mutableSetOf()) { entry ->
            entry.substringBefore('|', missingDelimiterValue = "") == today
        }

        if (cleaned.contains(key)) {
            return false
        }

        cleaned.add(key)
        prefs.edit().putStringSet(KEY_HANDLED_EVENTS, cleaned).apply()
        return true
    }

    companion object {
        private const val PREF_NAME = "azan_event_guard"
        private const val KEY_HANDLED_EVENTS = "handled_events"
    }
}
