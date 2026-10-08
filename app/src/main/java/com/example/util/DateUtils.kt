package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {
    private val standardDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    private val fullDateTimeFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    fun formatDate(timestamp: Long): String {
        return standardDateFormat.format(Date(timestamp))
    }

    fun formatShortDate(timestamp: Long): String {
        return shortDateFormat.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        return fullDateTimeFormat.format(Date(timestamp))
    }

    fun isOverdue(dueDateMs: Long): Boolean {
        return dueDateMs < System.currentTimeMillis()
    }

    fun daysRemaining(dueDateMs: Long): Long {
        val diff = dueDateMs - System.currentTimeMillis()
        return TimeUnit.MILLISECONDS.toDays(diff)
    }

    fun isDueSoon(dueDateMs: Long, thresholdDays: Int = 2): Boolean {
        val now = System.currentTimeMillis()
        if (dueDateMs < now) return false // already overdue
        val diffDays = TimeUnit.MILLISECONDS.toDays(dueDateMs - now)
        return diffDays in 0..thresholdDays
    }

    fun getDueStatusDescription(dueDateMs: Long): String {
        val now = System.currentTimeMillis()
        val diff = dueDateMs - now
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            diff < 0 -> {
                val overdueDays = kotlin.math.abs(days)
                if (overdueDays == 0L) "Overdue by today" else "Overdue by $overdueDays ${if (overdueDays == 1L) "day" else "days"}"
            }
            days == 0L -> "Due today!"
            days == 1L -> "Due tomorrow"
            else -> "Due in $days days"
        }
    }
}
