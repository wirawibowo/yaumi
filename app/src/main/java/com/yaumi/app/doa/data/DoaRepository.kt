package com.yaumi.app.doa.data

import android.content.Context
import com.yaumi.app.doa.domain.model.DoaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

class DoaRepository(private val appContext: Context) {
    suspend fun getDoaList(): List<DoaItem> = withContext(Dispatchers.IO) {
        val raw = appContext.assets.open("data/doa/doa.json").bufferedReader().use { it.readText() }
        val arr = JSONArray(raw)
        buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                add(
                    DoaItem(
                        title = obj.optString("title"),
                        arabicText = obj.optString("arab"),
                        latinText = obj.optString("latin"),
                        translation = obj.optString("translation"),
                        source = obj.optString("source")
                    )
                )
            }
        }
    }
}
