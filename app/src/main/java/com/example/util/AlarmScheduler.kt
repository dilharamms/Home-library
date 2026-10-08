package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import com.example.receiver.LibraryReminderReceiver

object AlarmScheduler {
    const val ACTION_CHECK_REMINDERS = "com.example.homelibrary.ACTION_CHECK_REMINDERS"
    private const val REQUEST_CODE = 4401

    fun schedulePeriodicCheck(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, LibraryReminderReceiver::class.java).apply {
            action = ACTION_CHECK_REMINDERS
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Repeat every 6 hours (6 * 3600 * 1000 ms)
        val intervalMs = 6L * 60 * 60 * 1000
        val triggerAtMs = SystemClock.elapsedRealtime() + intervalMs

        try {
            alarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerAtMs,
                intervalMs,
                pendingIntent
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun triggerImmediateCheck(context: Context) {
        val intent = Intent(context, LibraryReminderReceiver::class.java).apply {
            action = ACTION_CHECK_REMINDERS
        }
        context.sendBroadcast(intent)
    }
}
