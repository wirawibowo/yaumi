package com.yaumi.app.tadabur.data

import android.content.Context

data class TadaburSettings(
    val notificationEnabled: Boolean = true,
    val offsetMinutesAfterFajr: Int = 30
)

class TadaburSettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun load(): TadaburSettings {
        return TadaburSettings(
            notificationEnabled = prefs.getBoolean(KEY_NOTIF_ENABLED, true),
            offsetMinutesAfterFajr = prefs.getInt(KEY_OFFSET_MINUTES, DEFAULT_MINUTES)
        )
    }

    fun save(settings: TadaburSettings) {
        prefs.edit()
            .putBoolean(KEY_NOTIF_ENABLED, settings.notificationEnabled)
            .putInt(KEY_OFFSET_MINUTES, settings.offsetMinutesAfterFajr)
            .apply()
    }

    companion object {
        private const val PREF_NAME = "tadabur_settings"
        private const val KEY_NOTIF_ENABLED = "notif_enabled"
        private const val KEY_OFFSET_MINUTES = "notif_offset_minutes"
        private const val DEFAULT_MINUTES = 30
    }
}
