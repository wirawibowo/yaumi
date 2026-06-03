package com.yaumi.app.tadabur.data

import android.content.Context
import com.yaumi.app.quran.data.QuranRepository
import com.yaumi.app.tadabur.domain.TadaburDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.Normalizer

class TadaburRepository(private val appContext: Context) {
    private var cached: List<TadaburDay>? = null
    private val quranRepository = QuranRepository(appContext)

    suspend fun getAll(): List<TadaburDay> = withContext(Dispatchers.IO) {
        val current = cached
        if (current != null) return@withContext current
        val raw = appContext.assets.open(ASSET_PATH).bufferedReader().use { it.readText() }
        val parsed = parse(JSONArray(raw))
        val enriched = enrichAyahContent(parsed)
        cached = enriched
        enriched
    }

    suspend fun getByDay(dayIndex: Int): TadaburDay? = withContext(Dispatchers.IO) {
        val list = getAll()
        list.firstOrNull { it.dayIndex == dayIndex }
    }

    private fun parse(array: JSONArray): List<TadaburDay> {
        val out = ArrayList<TadaburDay>(array.length())
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            out += TadaburDay(
                dayIndex = obj.optInt("day"),
                surahName = obj.optString("surah"),
                ayahRange = obj.optString("ayah_range"),
                arabicText = obj.optString("arab"),
                translationId = obj.optString("terjemah"),
                hikmah = obj.optString("hikmah"),
                praktik = readStringList(obj.optJSONArray("praktik")),
                amalTracker = readStringList(obj.optJSONArray("amal_tracker")),
                hadithArabic = obj.optString("hadis_ar"),
                hadithTranslation = obj.optString("hadis_id"),
                hadithReference = obj.optString("hadis_ref")
            )
        }
        return out
    }

    private suspend fun enrichAyahContent(days: List<TadaburDay>): List<TadaburDay> {
        val summaries = quranRepository.getSurahSummaries()
        val surahMap = summaries.associateBy { normalizeName(it.latinName) }

        return days.map { day ->
            val surahId = surahMap[normalizeName(day.surahName)]?.id
            if (surahId == null) {
                return@map day.copy(
                    hadithArabic = "",
                    hadithTranslation = "",
                    hadithReference = ""
                )
            }

            val ayahLines = quranRepository.getAyahLines(surahId)
            if (ayahLines.isEmpty()) {
                return@map day.copy(
                    hadithArabic = "",
                    hadithTranslation = "",
                    hadithReference = ""
                )
            }

            val (start, end) = parseAyahRange(day.ayahRange)
            val startIndex = (start - 1).coerceAtLeast(0)
            val endIndex = (end - 1).coerceAtMost(ayahLines.lastIndex)
            if (startIndex > endIndex || startIndex !in ayahLines.indices) {
                return@map day.copy(
                    hadithArabic = "",
                    hadithTranslation = "",
                    hadithReference = ""
                )
            }

            val maxCount = 2
            val limitedEnd = minOf(endIndex, startIndex + maxCount - 1)
            val selected = ayahLines.subList(startIndex, limitedEnd + 1)
            val arabic = selected.joinToString(" ") { it.arabicText }
            val translation = selected.joinToString(" ") { it.translationId }
            val resolvedRange = if (selected.size == 1) {
                selected.first().number.toString()
            } else {
                "${selected.first().number}-${selected.last().number}"
            }

            day.copy(
                ayahRange = resolvedRange,
                arabicText = arabic,
                translationId = translation,
                hadithArabic = "",
                hadithTranslation = "",
                hadithReference = ""
            )
        }
    }

    private fun parseAyahRange(raw: String): Pair<Int, Int> {
        val trimmed = raw.trim()
        val parts = trimmed.split('-')
        val start = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: 1
        val end = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: start
        return start to end
    }

    private fun normalizeName(value: String): String {
        val normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
        return normalized.lowercase()
            .replace(Regex("[^a-z0-9]+"), " ")
            .trim()
    }

    private fun readStringList(array: JSONArray?): List<String> {
        if (array == null) return emptyList()
        return (0 until array.length())
            .mapNotNull { index -> array.optString(index).takeIf { it.isNotBlank() } }
    }

    companion object {
        private const val ASSET_PATH = "data/tadabur/tadabur_99.json"
    }
}
