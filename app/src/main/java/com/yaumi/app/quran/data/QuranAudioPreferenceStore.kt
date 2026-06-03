package com.yaumi.app.quran.data

import android.content.Context

data class QuranReciter(
    val id: String,
    val displayName: String,
    val pathSegment: String
)

object QuranReciters {
    val ALL = listOf(
        QuranReciter("alafasy", "Mishary Rashid Alafasy", "Alafasy_128kbps"),
        QuranReciter("husary", "Mahmoud Khalil Al-Husary", "Husary_128kbps"),
        QuranReciter("abdul_basit", "Abdul Basit Abdus-Samad", "Abdul_Basit_Murattal_192kbps"),
        QuranReciter("sudais", "Abdurrahman As-Sudais", "Abdurrahmaan_As-Sudais_192kbps"),
        QuranReciter("shuraim", "Saud Ash-Shuraym", "Saood_ash-Shuraym_128kbps"),
        QuranReciter("muaiqly", "Maher Al-Muaiqly", "Maher_AlMuaiqly_64kbps"),
        QuranReciter("ghamadi", "Saad Al-Ghamadi", "Ghamadi_40kbps"),
        QuranReciter("ajamy", "Ahmed Al-Ajmy", "ahmed_ibn_ali_al_ajamy_128kbps")
    )

    fun byId(id: String): QuranReciter = ALL.firstOrNull { it.id == id } ?: ALL.first()
    val DEFAULT_ID: String = ALL.first().id
}

class QuranAudioPreferenceStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun getReciterId(): String = prefs.getString(KEY_RECITER, QuranReciters.DEFAULT_ID) ?: QuranReciters.DEFAULT_ID
    fun setReciterId(id: String) {
        prefs.edit().putString(KEY_RECITER, id).apply()
    }

    fun getReciter(): QuranReciter = QuranReciters.byId(getReciterId())

    companion object {
        private const val PREF_NAME = "quran_audio_pref"
        private const val KEY_RECITER = "reciter_id"
    }
}
