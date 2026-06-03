package com.yaumi.app.azan.data

import android.content.Context

private val AZAN_PRAYERS = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
private val AZAN_DEFAULT_OFFSETS = AZAN_PRAYERS.associateWith { 0 }
private val AZAN_DEFAULT_ENABLED = AZAN_PRAYERS.associateWith { true }

data class AzanSettings(
    val notificationEnabled: Boolean = true,
    val backgroundServiceEnabled: Boolean = true,
    val reminderEnabled: Boolean = false,
    val reminderMinutes: Int = 10,
    val audioEnabled: Boolean = true,
    val audioVolumePercent: Int = 100,
    val selectedAudio: String = "azan.mp3",
    val selectedFajrAudio: String = "azan_subuh.mp3",
    val useManualLocation: Boolean = false,
    val selectedProvince: String = "",
    val selectedKabkota: String = "",
    val manualOffsets: Map<String, Int> = AZAN_DEFAULT_OFFSETS,
    val sholatEnabled: Map<String, Boolean> = AZAN_DEFAULT_ENABLED
)

class AzanSettingsStore(private val appContext: Context) {
    private val prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun load(): AzanSettings {
        return AzanSettings(
            notificationEnabled = prefs.getBoolean(KEY_NOTIF_ENABLED, true),
            backgroundServiceEnabled = prefs.getBoolean(KEY_BACKGROUND_SERVICE_ENABLED, true),
            reminderEnabled = prefs.getBoolean(KEY_REMINDER_ENABLED, false),
            reminderMinutes = prefs.getInt(KEY_REMINDER_MINUTES, 10),
            audioEnabled = prefs.getBoolean(KEY_AUDIO_ENABLED, true),
            audioVolumePercent = prefs.getInt(KEY_AUDIO_VOLUME_PERCENT, 100).coerceIn(0, 100),
            selectedAudio = prefs.getString(KEY_SELECTED_AUDIO, "azan.mp3") ?: "azan.mp3",
            selectedFajrAudio = prefs.getString(KEY_SELECTED_FAJR_AUDIO, "azan_subuh.mp3") ?: "azan_subuh.mp3",
            useManualLocation = prefs.getBoolean(KEY_USE_MANUAL_LOCATION, false),
            selectedProvince = prefs.getString(KEY_SELECTED_PROVINCE, "") ?: "",
            selectedKabkota = prefs.getString(KEY_SELECTED_KABKOTA, "") ?: "",
            manualOffsets = AZAN_PRAYERS.associateWith { prayer ->
                prefs.getInt(KEY_OFFSET_PREFIX + prayer, AZAN_DEFAULT_OFFSETS[prayer] ?: 0)
            },
            sholatEnabled = AZAN_PRAYERS.associateWith { prayer ->
                prefs.getBoolean(KEY_ENABLED_PREFIX + prayer, AZAN_DEFAULT_ENABLED[prayer] ?: true)
            }
        )
    }

    fun save(settings: AzanSettings) {
        val editor = prefs.edit()
            .putBoolean(KEY_NOTIF_ENABLED, settings.notificationEnabled)
            .putBoolean(KEY_BACKGROUND_SERVICE_ENABLED, settings.backgroundServiceEnabled)
            .putBoolean(KEY_REMINDER_ENABLED, settings.reminderEnabled)
            .putInt(KEY_REMINDER_MINUTES, settings.reminderMinutes)
            .putBoolean(KEY_AUDIO_ENABLED, settings.audioEnabled)
            .putInt(KEY_AUDIO_VOLUME_PERCENT, settings.audioVolumePercent.coerceIn(0, 100))
            .putString(KEY_SELECTED_AUDIO, settings.selectedAudio)
            .putString(KEY_SELECTED_FAJR_AUDIO, settings.selectedFajrAudio)
            .putBoolean(KEY_USE_MANUAL_LOCATION, settings.useManualLocation)
            .putString(KEY_SELECTED_PROVINCE, settings.selectedProvince)
            .putString(KEY_SELECTED_KABKOTA, settings.selectedKabkota)

        AZAN_PRAYERS.forEach { prayer ->
            editor.putInt(KEY_OFFSET_PREFIX + prayer, settings.manualOffsets[prayer] ?: 0)
            editor.putBoolean(KEY_ENABLED_PREFIX + prayer, settings.sholatEnabled[prayer] ?: true)
        }

        editor.apply()
    }

    companion object {
        private const val PREF_NAME = "azan_settings"
        private const val KEY_NOTIF_ENABLED = "notif_enabled"
        private const val KEY_BACKGROUND_SERVICE_ENABLED = "background_service_enabled"
        private const val KEY_REMINDER_ENABLED = "reminder_enabled"
        private const val KEY_REMINDER_MINUTES = "reminder_minutes"
        private const val KEY_AUDIO_ENABLED = "audio_enabled"
        private const val KEY_AUDIO_VOLUME_PERCENT = "audio_volume_percent"
        private const val KEY_SELECTED_AUDIO = "selected_audio"
        private const val KEY_SELECTED_FAJR_AUDIO = "selected_fajr_audio"
        private const val KEY_USE_MANUAL_LOCATION = "use_manual_location"
        private const val KEY_SELECTED_PROVINCE = "selected_province"
        private const val KEY_SELECTED_KABKOTA = "selected_kabkota"
        private const val KEY_OFFSET_PREFIX = "offset_"
        private const val KEY_ENABLED_PREFIX = "enabled_"

    }
}
