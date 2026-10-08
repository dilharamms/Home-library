package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.LibraryDatabase
import com.example.util.AlarmScheduler
import com.example.util.DateUtils
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LibraryReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            try {
                val database = LibraryDatabase.getDatabase(context, scope)
                val activeLoans = database.loanDao().getActiveLoansWithDetailsSync()

                val now = System.currentTimeMillis()
                var overdueCount = 0
                var dueSoonCount = 0

                activeLoans.forEachIndexed { index, loan ->
                    val rackLocation = "${loan.rackId} • ${loan.shelfNumber}"
                    when {
                        loan.dueDate < now -> {
                            overdueCount++
                            val status = DateUtils.getDueStatusDescription(loan.dueDate)
                            NotificationHelper.showOverdueNotification(
                                context = context,
                                notificationId = 1000 + index,
                                bookTitle = loan.bookTitle,
                                memberName = loan.memberName,
                                statusDescription = status,
                                rackLocation = rackLocation
                            )
                        }
                        DateUtils.isDueSoon(loan.dueDate, thresholdDays = 2) -> {
                            dueSoonCount++
                            val status = DateUtils.getDueStatusDescription(loan.dueDate)
                            NotificationHelper.showDueSoonNotification(
                                context = context,
                                notificationId = 2000 + index,
                                bookTitle = loan.bookTitle,
                                memberName = loan.memberName,
                                statusDescription = status,
                                rackLocation = rackLocation
                            )
                        }
                    }
                }

                // If more than 2 items, also ensure a clear summary alert
                if (overdueCount + dueSoonCount > 2) {
                    NotificationHelper.showSummaryNotification(context, overdueCount, dueSoonCount)
                }

                // Ensure next schedule is active
                AlarmScheduler.schedulePeriodicCheck(context)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
