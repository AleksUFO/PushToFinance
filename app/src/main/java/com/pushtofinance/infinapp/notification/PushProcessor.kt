package com.pushtofinance.infinapp.notification

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.os.Build
import com.pushtofinance.infinapp.CaptureActivity
import com.pushtofinance.infinapp.PushToFinanceApp
import com.pushtofinance.infinapp.data.PushLogEntity
import com.pushtofinance.infinapp.data.Types
import com.pushtofinance.infinapp.currency.ExchangeRateClient

class PushProcessor(private val context: Context) {

    suspend fun process(pkg: String, title: String?, text: String, force: Boolean = false) {
        val app = context.applicationContext as PushToFinanceApp
        if (!force) {
            if (pkg == context.packageName) return
            val selected = app.settings.currentSelectedApps()
            if (selected.isEmpty() || pkg !in selected) return
        }
        val parsed = PushParser.parse(title, text)
        val amount = parsed.amount ?: return

        val appName = runCatching {
            val info = context.packageManager.getApplicationInfo(pkg, 0)
            context.packageManager.getApplicationLabel(info).toString()
        }.getOrElse {
            if (force) "PushToFinance (test)" else pkg
        }

        val amountPln = ExchangeRateClient(app.settings).toPln(amount, parsed.currency)

        val logId = app.repository.insertPushLog(
            PushLogEntity(
                packageName = pkg,
                appName = appName,
                title = title,
                text = text,
                amount = amount,
                currency = parsed.currency,
                amountPln = amountPln,
                cardName = parsed.cardName,
                storeName = parsed.storeName,
                timestamp = System.currentTimeMillis(),
                status = Types.STATUS_PENDING,
                isIncome = parsed.isIncome
            )
        )

        val capture = CapturedPush(
            id = logId,
            packageName = pkg,
            appName = appName,
            title = title,
            text = text,
            amount = amount,
            currency = parsed.currency,
            amountPln = amountPln,
            cardName = parsed.cardName,
            storeName = parsed.storeName,
            isIncome = parsed.isIncome
        )
        PendingCaptures.add(capture)

        val now = System.currentTimeMillis()
        val duplicates = app.repository.recentSameAmount(amount, now, 60_000L)
        val helper = NotificationHelper(context)
        if (duplicates.size > 1) {
            helper.updateCapturesGroup(duplicates.map { d ->
                CapturedPush(
                    id = d.id, packageName = d.packageName, appName = d.appName,
                    title = d.title, text = d.text, amount = d.amount ?: 0.0,
                    currency = d.currency ?: "PLN", amountPln = d.amountPln ?: 0.0,
                    cardName = d.cardName, storeName = d.storeName, timestamp = d.timestamp,
                    isIncome = d.isIncome
                )
            })
        } else {
            helper.postCapture(capture)
        }

        tryLaunchCapture(capture)
    }

    private fun tryLaunchCapture(capture: CapturedPush) {
        if (Build.VERSION.SDK_INT >= 29) return
        runCatching {
            val intent = Intent(context, CaptureActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(intent)
        }
    }
}