package com.yaumi.app.tadabur.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.yaumi.app.tadabur.data.TadaburProgressStore
import com.yaumi.app.tadabur.data.TadaburRepository
import com.yaumi.app.tadabur.data.TadaburSettingsStore
import com.yaumi.app.tadabur.notifications.TadaburNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class TadaburAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val scheduledDate = intent?.getStringExtra(EXTRA_SCHEDULE_DATE)
        if (scheduledDate != null && scheduledDate != LocalDate.now().toString()) {
            Log.d(TAG, "Skip stale tadabur event for $scheduledDate")
            return
        }

        val settings = TadaburSettingsStore(context).load()
        if (!settings.notificationEnabled) return

        TadaburNotificationHelper.ensureChannel(context)

        CoroutineScope(Dispatchers.IO).launch {
            val totalDays = TadaburRepository(context).getAll().size
            val progress = TadaburProgressStore(context)
            if (progress.isSkippedToday()) return@launch
            val dayIndex = progress.getCurrentDayIndex(totalDays)
            if (!progress.isDayCompleted(dayIndex)) {
                TadaburNotificationHelper.showDaily(context, dayIndex)
            }
        }
    }

    companion object {
        const val EXTRA_SCHEDULE_DATE = "schedule_date"
        private const val TAG = "TadaburAlarm"
    }
}
