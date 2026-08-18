package com.pushtofinance.infinapp.notification

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.pushtofinance.infinapp.data.AppRepository
import java.util.concurrent.TimeUnit

class PushSurfaceWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return runCatching {
            val repo = AppRepository.get(applicationContext)
            val pending = repo.pendingPushLogs()
            if (pending.isEmpty()) return Result.success()
            val captures = pending.map { d ->
                CapturedPush(
                    id = d.id, packageName = d.packageName, appName = d.appName,
                    title = d.title, text = d.text, amount = d.amount ?: 0.0,
                    currency = d.currency ?: "PLN", amountPln = d.amountPln ?: 0.0,
                    cardName = d.cardName, storeName = d.storeName, timestamp = d.timestamp,
                    isIncome = d.isIncome
                )
            }
            NotificationHelper(applicationContext).updateCapturesGroup(captures)
            Result.success()
        }.getOrElse { Result.retry() }
    }

    companion object {
        const val WORK_NAME = "ptf_push_surface"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<PushSurfaceWorker>(15, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}