package com.pushtofinance.infinapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.pushtofinance.infinapp.data.AppRepository
import com.pushtofinance.infinapp.data.SettingsManager
import com.pushtofinance.infinapp.notification.PushSurfaceWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class PushToFinanceApp : Application() {

    lateinit var repository: AppRepository
        private set
    lateinit var settings: SettingsManager
        private set
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        repository = AppRepository.get(this)
        settings = SettingsManager(this)
        createChannels()
        PushSurfaceWorker.schedule(this)
    }

    private fun createChannels() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_CAPTURES,
                getString(R.string.channel_captures),
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = getString(R.string.channel_captures) }
        )
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ERRORS,
                getString(R.string.channel_errors),
                NotificationManager.IMPORTANCE_LOW
            )
        )
    }

    companion object {
        const val CHANNEL_CAPTURES = "ptf_captures"
        const val CHANNEL_ERRORS = "ptf_errors"
    }
}