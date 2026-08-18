package com.pushtofinance.infinapp.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pushtofinance.infinapp.data.AppRepository
import com.pushtofinance.infinapp.data.Types
import kotlinx.coroutines.runBlocking

class CaptureActionsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra("id", 0L)
        if (id <= 0L) return
        when (intent.action) {
            "DISCARD" -> runBlocking {
                val repo = AppRepository.get(context)
                val log = repo.pendingPushLogs().firstOrNull { it.id == id }
                if (log != null) {
                    repo.updatePushLog(log.copy(status = Types.STATUS_DISCARDED))
                }
                NotificationHelper(context).dismiss(id)
            }
        }
    }
}