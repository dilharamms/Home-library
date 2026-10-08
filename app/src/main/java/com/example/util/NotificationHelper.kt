package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {
    const val CHANNEL_ID = "library_reminders_channel"
    private const val CHANNEL_NAME = "Home Library Alerts"
    private const val CHANNEL_DESCRIPTION = "Alerts for overdue books and upcoming return dates"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getLaunchPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun canSendNotifications(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showOverdueNotification(
        context: Context,
        notificationId: Int,
        bookTitle: String,
        memberName: String,
        statusDescription: String,
        rackLocation: String
    ) {
        if (!canSendNotifications(context)) return

        createNotificationChannel(context)
        val pendingIntent = getLaunchPendingIntent(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⚠️ Overdue Book: $bookTitle")
            .setContentText("Borrowed by $memberName • $statusDescription (Rack: $rackLocation)")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("The book '$bookTitle' borrowed by $memberName is $statusDescription. Please request return to $rackLocation.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Handled safely if permission is missing
        }
    }

    fun showDueSoonNotification(
        context: Context,
        notificationId: Int,
        bookTitle: String,
        memberName: String,
        statusDescription: String,
        rackLocation: String
    ) {
        if (!canSendNotifications(context)) return

        createNotificationChannel(context)
        val pendingIntent = getLaunchPendingIntent(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("⏳ Return Approaching: $bookTitle")
            .setContentText("Borrowed by $memberName • $statusDescription (Rack: $rackLocation)")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("'$bookTitle' borrowed by $memberName is $statusDescription. Remind them to return it to $rackLocation soon.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Handled safely if permission is missing
        }
    }

    fun showSummaryNotification(
        context: Context,
        overdueCount: Int,
        dueSoonCount: Int
    ) {
        if (!canSendNotifications(context)) return
        if (overdueCount == 0 && dueSoonCount == 0) return

        createNotificationChannel(context)
        val pendingIntent = getLaunchPendingIntent(context)

        val title = if (overdueCount > 0) {
            "Home Library: $overdueCount Overdue Book${if (overdueCount > 1) "s" else ""}"
        } else {
            "Home Library: $dueSoonCount Book${if (dueSoonCount > 1) "s" else ""} Due Soon"
        }

        val text = buildString {
            if (overdueCount > 0) append("$overdueCount overdue book(s) need attention. ")
            if (dueSoonCount > 0) append("$dueSoonCount book(s) due within 48 hours.")
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(9999, notification)
        } catch (_: SecurityException) {
            // Handled safely if permission is missing
        }
    }
}
