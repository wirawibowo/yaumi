package com.yaumi.app.ui.data

import android.content.Context

class PickerLayoutStore(appContext: Context) {
    private val prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun getQuranSurahLayout(defaultValue: String): String {
        return prefs.getString(KEY_QURAN_SURAH_LAYOUT, defaultValue) ?: defaultValue
    }

    fun setQuranSurahLayout(value: String) {
        prefs.edit().putString(KEY_QURAN_SURAH_LAYOUT, value).apply()
    }

    fun getHadisKitabLayout(defaultValue: String): String {
        return prefs.getString(KEY_HADIS_KITAB_LAYOUT, defaultValue) ?: defaultValue
    }

    fun setHadisKitabLayout(value: String) {
        prefs.edit().putString(KEY_HADIS_KITAB_LAYOUT, value).apply()
    }

    companion object {
        private const val PREF_NAME = "picker_layout_settings"
        private const val KEY_QURAN_SURAH_LAYOUT = "quran_surah_layout"
        private const val KEY_HADIS_KITAB_LAYOUT = "hadis_kitab_layout"
    }
}
