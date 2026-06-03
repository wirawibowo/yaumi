package com.yaumi.app.quran.data

import android.content.Context

class QuranBookmarkStore(private val appContext: Context) {
    private val prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun getBookmarkedAyahNumbers(surahId: Int): Set<Int> {
        val raw = prefs.getStringSet(KEY_BOOKMARKS, emptySet()).orEmpty()
        val prefix = "$surahId:"
        return raw.mapNotNull { item ->
            if (!item.startsWith(prefix)) return@mapNotNull null
            item.substringAfter(':').toIntOrNull()
        }.toSet()
    }

    fun toggle(surahId: Int, ayahNumber: Int): Boolean {
        val current = prefs.getStringSet(KEY_BOOKMARKS, emptySet()).orEmpty().toMutableSet()
        val token = "$surahId:$ayahNumber"
        val nowBookmarked = if (current.contains(token)) {
            current.remove(token)
            false
        } else {
            current.add(token)
            true
        }
        prefs.edit().putStringSet(KEY_BOOKMARKS, current).apply()
        return nowBookmarked
    }

    companion object {
        private const val PREF_NAME = "quran_bookmarks"
        private const val KEY_BOOKMARKS = "items"
    }
}
