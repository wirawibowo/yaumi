package com.yaumi.app.quran.data

import org.json.JSONArray
import org.json.JSONObject
import com.yaumi.app.quran.data.model.AyahAsset
import com.yaumi.app.quran.data.model.SurahAsset

object QuranJsonParser {
    fun parseSurahList(raw: String): List<SurahAsset> {
        val root = JSONArray(raw)
        val surahList = ArrayList<SurahAsset>(root.length())
        for (i in 0 until root.length()) {
            val surahObject = root.getJSONObject(i)
            surahList += surahFromJson(surahObject)
        }
        return surahList
    }

    private fun surahFromJson(obj: JSONObject): SurahAsset {
        val ayatArray = obj.getJSONArray("ayat")
        val ayat = ArrayList<AyahAsset>(ayatArray.length())
        for (j in 0 until ayatArray.length()) {
            val item = ayatArray.getJSONObject(j)
            ayat += AyahAsset(
                ayah = item.getInt("ayah"),
                ar = item.getString("ar"),
                translationId = item.getString("id")
            )
        }

        return SurahAsset(
            id = obj.getInt("id"),
            name = obj.getString("name"),
            nameAr = obj.getString("name_ar"),
            translation = obj.getString("translation"),
            location = obj.getString("location"),
            numAyah = obj.getInt("num_ayah"),
            ayat = ayat
        )
    }
}
