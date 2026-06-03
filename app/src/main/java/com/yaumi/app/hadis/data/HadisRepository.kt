package com.yaumi.app.hadis.data

import android.content.Context
import com.yaumi.app.hadis.domain.model.HadisCollection
import com.yaumi.app.hadis.domain.model.HadisItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

class HadisRepository(private val appContext: Context) {
    suspend fun getCollections(): List<HadisCollection> = withContext(Dispatchers.IO) {
        val raw = appContext.assets.open("data/hadis/list.json").bufferedReader().use { it.readText() }
        val arr = JSONArray(raw)
        buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                add(
                    HadisCollection(
                        name = obj.optString("name"),
                        slug = obj.optString("slug"),
                        total = obj.optInt("total")
                    )
                )
            }
        }
    }

    suspend fun getHadisBySlug(slug: String, limit: Int = 100): List<HadisItem> = withContext(Dispatchers.IO) {
        val raw = appContext.assets.open("data/hadis/$slug.json").bufferedReader().use { it.readText() }
        val arr = JSONArray(raw)
        val size = minOf(arr.length(), limit)
        buildList {
            for (i in 0 until size) {
                val obj = arr.getJSONObject(i)
                add(
                    HadisItem(
                        number = obj.optInt("number"),
                        arabicText = obj.optString("arab"),
                        translationId = obj.optString("id")
                    )
                )
            }
        }
    }
}
