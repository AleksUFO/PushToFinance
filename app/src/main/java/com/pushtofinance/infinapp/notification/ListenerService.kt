package com.pushtofinance.infinapp.notification

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.pushtofinance.infinapp.PushToFinanceApp
import kotlinx.coroutines.launch

class ListenerService : NotificationListenerService() {

    private val app: PushToFinanceApp
        get() = applicationContext as PushToFinanceApp

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onDestroy() {
        if (instance === this) instance = null
        super.onDestroy()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        sweep()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (SeenKeys.shouldProcess(sbn.key, sbn.postTime)) {
            processSbn(sbn)
        }
    }

    private fun processSbn(sbn: StatusBarNotification) {
        val extra = sbn.notification?.extras
        val title = extra?.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extra?.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val subText = extra?.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
        val bigText = extra?.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
        val combined = listOfNotNull(title, text, subText, bigText).joinToString(" | ")
        if (combined.isBlank()) return
        app.appScope.launch {
            PushProcessor(this@ListenerService).process(sbn.packageName, title, combined)
        }
    }

    fun sweep() {
        val sbnList = runCatching { activeNotifications }.getOrNull() ?: return
        if (sbnList.isEmpty()) return
        sbnList
            .filter { SeenKeys.shouldProcess(it.key, it.postTime) }
            .forEach { processSbn(it) }
    }

    companion object {
        @Volatile
        var instance: ListenerService? = null
            private set

        fun sweepIfAvailable() {
            instance?.sweep()
        }
    }
}

internal object SeenKeys {
    private const val MAX = 1000
    private val map = HashMap<String, Long>()

    @Synchronized
    fun shouldProcess(key: String, postTime: Long): Boolean {
        val last = map[key]
        if (last != null && last == postTime) return false
        map[key] = postTime
        if (map.size > MAX) {
            val cutoff = System.currentTimeMillis() - 7L * 24 * 3600 * 1000
            map.entries.removeAll { it.value < cutoff }
            if (map.size > MAX) map.clear()
        }
        return true
    }
}