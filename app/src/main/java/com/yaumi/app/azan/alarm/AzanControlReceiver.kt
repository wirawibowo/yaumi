package com.yaumi.app.azan.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.yaumi.app.azan.service.AzanForegroundService

/**
 * Receives playback control actions from the notification buttons and
 * forwards them to AzanForegroundService which owns the audio state.
 */
class AzanControlReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return

        val serviceIntent = Intent(context, AzanForegroundService::class.java).apply {
            this.action = when (action) {
                ACTION_VOLUME_UP -> AzanForegroundService.ACTION_VOLUME_UP
                ACTION_VOLUME_DOWN -> AzanForegroundService.ACTION_VOLUME_DOWN
                ACTION_STOP -> AzanForegroundService.ACTION_STOP_SERVICE
                else -> return
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    companion object {
        const val ACTION_VOLUME_UP = "com.yaumi.app.AZAN_VOLUME_UP"
        const val ACTION_VOLUME_DOWN = "com.yaumi.app.AZAN_VOLUME_DOWN"
        const val ACTION_STOP = "com.yaumi.app.AZAN_STOP"
    }
}
