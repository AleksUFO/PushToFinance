package com.pushtofinance.infinapp.notification

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.pushtofinance.infinapp.PushToFinanceApp
import kotlinx.coroutines.launch

class ListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val app = applicationContext as PushToFinanceApp
        app.appScope.launch {
            val extra = sbn.notification?.extras
            val title = extra?.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            val text = extra?.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            val subText = extra?.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
            val bigText = extra?.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            val combined = listOfNotNull(title, text, subText, bigText).joinToString(" | ")
            if (combined.isBlank()) return@launch
            PushProcessor(this@ListenerService).process(sbn.packageName, title, combined)
        }
    }
}