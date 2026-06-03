package com.yaumi.app.quran.data

import android.content.Context

class QuranReadingProgressStore(appContext: Context) {
    private val prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun getLastReadAyahNumber(surahId: Int): Int? {
        val value = prefs.getInt(keyForSurah(surahId), -1)
        return if (value > 0) value else null
    }

    fun setLastReadAyahNumber(surahId: Int, ayahNumber: Int) {
        if (ayahNumber <= 0) return
        prefs.edit().putInt(keyForSurah(surahId), ayahNumber).apply()
    }

    private fun keyForSurah(surahId: Int): String = "surah_${surahId}_last_read_ayah"

    companion object {
        private const val PREF_NAME = "quran_reading_progress"
    }
}
