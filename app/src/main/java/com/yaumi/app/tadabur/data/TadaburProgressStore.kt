package com.yaumi.app.tadabur.data

import android.content.Context
import java.time.LocalDate

class TadaburProgressStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun getCurrentDayIndex(totalDays: Int): Int {
        if (totalDays <= 0) return 1
        val lastDate = prefs.getString(KEY_LAST_DATE, null)
        val lastDay = prefs.getInt(KEY_LAST_DAY, 1)
        val today = LocalDate.now().toString()
        if (lastDate == null) {
            persistDay(today, lastDay)
            return clampDay(lastDay, totalDays)
        }

        if (lastDate == today) return clampDay(lastDay, totalDays)

        val skipAdvanceDate = prefs.getString(KEY_SKIP_ADVANCE_DATE, null)
        if (skipAdvanceDate == lastDate) {
            prefs.edit().putString(KEY_SKIP_ADVANCE_DATE, null).apply()
            persistDay(today, lastDay)
            return clampDay(lastDay, totalDays)
        }

        val next = if (lastDay >= totalDays) 1 else lastDay + 1
        persistDay(today, next)
        return next
    }

    fun markDayCompleted(dayIndex: Int) {
        prefs.edit()
            .putBoolean(dayKey(dayIndex), true)
            .apply()
    }

    fun isDayCompleted(dayIndex: Int): Boolean = prefs.getBoolean(dayKey(dayIndex), false)

    fun saveChecklist(dayIndex: Int, checked: List<Boolean>) {
        prefs.edit()
            .putString(checklistKey(dayIndex), checked.joinToString(",") { if (it) "1" else "0" })
            .apply()
    }

    fun loadChecklist(dayIndex: Int, size: Int): List<Boolean> {
        val raw = prefs.getString(checklistKey(dayIndex), null) ?: return List(size) { false }
        val parts = raw.split(',')
        return List(size) { index -> parts.getOrNull(index) == "1" }
    }

    fun setLastOpenedDay(dayIndex: Int) {
        persistDay(LocalDate.now().toString(), dayIndex)
    }

    fun skipToday(totalDays: Int) {
        if (totalDays <= 0) return
        val today = LocalDate.now().toString()
        val lastDay = prefs.getInt(KEY_LAST_DAY, 1)
        val next = if (lastDay >= totalDays) 1 else lastDay + 1
        prefs.edit()
            .putString(KEY_LAST_DATE, today)
            .putInt(KEY_LAST_DAY, next)
            .putString(KEY_SKIPPED_DATE, today)
            .putString(KEY_SKIP_ADVANCE_DATE, today)
            .apply()
    }

    fun isSkippedToday(): Boolean {
        val today = LocalDate.now().toString()
        return prefs.getString(KEY_SKIPPED_DATE, null) == today
    }

    private fun persistDay(date: String, dayIndex: Int) {
        prefs.edit()
            .putString(KEY_LAST_DATE, date)
            .putInt(KEY_LAST_DAY, dayIndex)
            .apply()
    }

    private fun clampDay(value: Int, totalDays: Int): Int = value.coerceIn(1, totalDays)

    private fun dayKey(dayIndex: Int) = "day_completed_$dayIndex"
    private fun checklistKey(dayIndex: Int) = "day_checklist_$dayIndex"

    companion object {
        private const val PREF_NAME = "tadabur_progress"
        private const val KEY_LAST_DATE = "last_date"
        private const val KEY_LAST_DAY = "last_day"
        private const val KEY_SKIPPED_DATE = "skipped_date"
        private const val KEY_SKIP_ADVANCE_DATE = "skip_advance_date"
    }
}
