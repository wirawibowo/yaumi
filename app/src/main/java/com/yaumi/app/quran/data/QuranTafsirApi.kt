package com.yaumi.app.quran.data

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class QuranTafsirApi {
    fun fetchTafsirBySurah(surahId: Int): Map<Int, String> {
        val url = URL("https://equran.id/api/v2/tafsir/$surahId")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8000
            readTimeout = 8000
        }

        val raw = connection.inputStream.bufferedReader().use { it.readText() }
        val root = JSONObject(raw)
        val data = root.optJSONObject("data") ?: JSONObject()
        val arr = data.optJSONArray("tafsir") ?: return emptyMap()

        val result = LinkedHashMap<Int, String>()
        for (i in 0 until arr.length()) {
            val item = arr.optJSONObject(i) ?: continue
            val ayah = item.optInt("ayat", -1)
            val text = item.optString("teks").trim()
            if (ayah > 0 && text.isNotEmpty()) {
                result[ayah] = text
            }
        }
        return result
    }
}
