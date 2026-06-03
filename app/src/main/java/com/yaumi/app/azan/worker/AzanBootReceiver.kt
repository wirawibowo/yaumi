package com.yaumi.app.azan.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.yaumi.app.azan.alarm.AzanAlarmScheduler

class AzanBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (
            action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_LOCKED_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }
        // Re-schedule today's alarms via AlarmManager
        AzanAlarmScheduler.rescheduleFromRepository(context)
    }
}
