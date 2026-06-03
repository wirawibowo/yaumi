package com.yaumi.app.azan.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yaumi.app.azan.data.AzanRepository
import com.yaumi.app.azan.data.AzanScheduleRules
import com.yaumi.app.azan.data.AzanSettingsStore
import com.yaumi.app.azan.data.AzanTimeUtils

class AzanRescheduleWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        return runCatching {
            val repository = AzanRepository(applicationContext)
            val settings = AzanSettingsStore(applicationContext).load()
            if (!settings.notificationEnabled) return@runCatching Result.success()

            val data = repository.loadAzanData()
            val withOffsets = AzanTimeUtils.applyOffsets(data, settings.manualOffsets)
            val target = AzanScheduleRules.selectSchedulableTimings(
                timings = withOffsets.timings,
                sholatEnabled = settings.sholatEnabled
            )

            AzanScheduler.scheduleForToday(
                context = applicationContext,
                timings = target,
                reminderEnabled = settings.reminderEnabled,
                reminderMinutes = settings.reminderMinutes
            )
            Result.success()
        }.getOrElse { Result.retry() }
    }
}
