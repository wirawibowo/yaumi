package com.yaumi.app.azan.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Fired at 00:01 each day. Triggers a fresh fetch of prayer times for the
 * new day and schedules the new set of alarms.
 */
class AzanRescheduleAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("AzanRescheduleAlarm", "Daily reschedule")
        AzanAlarmScheduler.rescheduleFromRepository(context)
    }
}
