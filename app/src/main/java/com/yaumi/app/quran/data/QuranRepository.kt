package com.yaumi.app.quran.data

import android.content.Context
import com.yaumi.app.quran.domain.model.AyahLine
import com.yaumi.app.quran.domain.model.SurahSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuranRepository(private val appContext: Context) {
    private var cachedRaw: List<com.yaumi.app.quran.data.model.SurahAsset>? = null
    private val tafsirApi = QuranTafsirApi()
    private val tafsirCache = LinkedHashMap<Int, Map<Int, String>>()

    suspend fun getSurahSummaries(): List<SurahSummary> = withContext(Dispatchers.IO) {
        ensureLoaded().map { surah ->
            SurahSummary(
                id = surah.id,
                latinName = surah.name,
                arabicName = surah.nameAr,
                translation = surah.translation,
                location = surah.location,
                ayahCount = surah.numAyah
            )
        }
    }

    suspend fun getAyahLines(surahId: Int): List<AyahLine> = withContext(Dispatchers.IO) {
        val surah = ensureLoaded().firstOrNull { it.id == surahId }
        surah?.ayat?.map { ayah ->
            AyahLine(
                number = ayah.ayah,
                arabicText = ayah.ar,
                translationId = ayah.translationId
            )
        } ?: emptyList()
    }

    suspend fun getTafsirMap(surahId: Int): Map<Int, String> = withContext(Dispatchers.IO) {
        tafsirCache[surahId]?.let { return@withContext it }
        val fetched = runCatching { tafsirApi.fetchTafsirBySurah(surahId) }.getOrDefault(emptyMap())
        tafsirCache[surahId] = fetched
        fetched
    }

    private fun ensureLoaded(): List<com.yaumi.app.quran.data.model.SurahAsset> {
        val current = cachedRaw
        if (current != null) return current

        val rawJson = appContext.assets.open("quran_index.json").bufferedReader().use { it.readText() }
        val parsed = QuranJsonParser.parseSurahList(rawJson)
        cachedRaw = parsed
        return parsed
    }
}
