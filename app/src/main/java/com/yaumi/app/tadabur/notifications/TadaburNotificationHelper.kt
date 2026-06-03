package com.yaumi.app.tadabur.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.yaumi.app.MainActivity
import com.yaumi.app.R

object TadaburNotificationHelper {
    const val CHANNEL_TADABUR = "tadabur_daily_channel"

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_TADABUR, "Tadabur Harian", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Pengingat tadabur harian setelah Subuh"
            }
        )
    }

    fun showDaily(context: Context, dayIndex: Int) {
        if (!hasNotifPermission(context)) return

        val notification = NotificationCompat.Builder(context, CHANNEL_TADABUR)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Tadabur Harian")
            .setContentText("Tadabur hari ke-$dayIndex sudah siap")
            .setContentIntent(launchAppIntent(context))
            .setAutoCancel(true)
            .setOngoing(false)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setOnlyAlertOnce(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId(dayIndex), notification)
    }

    private fun notificationId(dayIndex: Int) = 17000 + dayIndex

    fun dismissForDay(context: Context, dayIndex: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId(dayIndex))
    }

    private fun launchAppIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun hasNotifPermission(context: Context): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
