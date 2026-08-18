package com.pushtofinance.infinapp.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.pushtofinance.infinapp.CaptureActivity
import com.pushtofinance.infinapp.PushToFinanceApp
import com.pushtofinance.infinapp.R

class NotificationHelper(private val context: Context) {

    fun canPost(): Boolean = NotificationManagerCompat.from(context).areNotificationsEnabled()

    fun postCapture(capture: CapturedPush) {
        if (!canPost()) return
        val intent = Intent(context, CaptureActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val savePi = PendingIntent.getActivity(
            context,
            capture.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val discardPi = PendingIntent.getBroadcast(
            context,
            capture.id.toInt() * -1,
            Intent(context, CaptureActionsReceiver::class.java).apply {
                action = "DISCARD"
                putExtra("id", capture.id)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val text = buildString {
            append("${com.pushtofinance.infinapp.util.Format.moneyPlain(capture.amount, capture.currency)}")
            capture.cardName?.let { append(" • $it") }
            capture.storeName?.let { append(" • $it") }
        }
        val notification = NotificationCompat.Builder(context, PushToFinanceApp.CHANNEL_CAPTURES)
            .setSmallIcon(R.drawable.ic_stat_push)
            .setContentTitle(if (capture.isIncome) "Save this income?" else "Save this transaction?")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setFullScreenIntent(savePi, true)
            .setAutoCancel(true)
            .setContentIntent(savePi)
            .addAction(0, "Save", savePi)
            .addAction(0, "Discard", discardPi)
            .build()
        notify(capture.id.toInt(), notification)
    }

    fun updateCapturesGroup(captures: List<CapturedPush>) {
        if (!canPost()) return
        val savePi = PendingIntent.getActivity(
            context,
            captures.first().id.toInt(),
            Intent(context, CaptureActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val text = captures.joinToString("\n") {
            "${com.pushtofinance.infinapp.util.Format.moneyPlain(it.amount, it.currency)} • ${it.storeName ?: it.appName}"
        }
        val notification = NotificationCompat.Builder(context, PushToFinanceApp.CHANNEL_CAPTURES)
            .setSmallIcon(R.drawable.ic_stat_push)
            .setContentTitle("Captured ${captures.size} payments")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .setContentIntent(savePi)
            .addAction(0, "Save all", savePi)
            .build()
        notify(1001, notification)
    }

    fun dismiss(id: Long) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(id.toInt())
        nm.cancel(1001)
    }

    private fun notify(id: Int, n: Notification) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(id, n)
    }
}