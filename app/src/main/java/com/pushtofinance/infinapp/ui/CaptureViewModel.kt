package com.pushtofinance.infinapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pushtofinance.infinapp.ai.AiClient
import com.pushtofinance.infinapp.ai.AiGuess
import com.pushtofinance.infinapp.currency.ExchangeRateClient
import com.pushtofinance.infinapp.data.AppRepository
import com.pushtofinance.infinapp.data.CategoryEntity
import com.pushtofinance.infinapp.data.PaymentMethodEntity
import com.pushtofinance.infinapp.data.PushLogEntity
import com.pushtofinance.infinapp.data.SettingsManager
import com.pushtofinance.infinapp.data.StoreEntity
import com.pushtofinance.infinapp.data.TransactionEntity
import com.pushtofinance.infinapp.data.Types
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CaptureRowInput(
    val logId: Long,
    val amount: Double,
    val currency: String,
    val storeName: String?,
    val categoryId: Long?,
    val methodId: Long?,
    val isIncome: Boolean = false
)

class CaptureViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AppRepository.get(app)
    private val settings = SettingsManager(app)
    private val aiClient = AiClient(settings)
    private val rates = ExchangeRateClient(settings)

    val theme: StateFlow<String> = settings.theme.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")

    val methods: StateFlow<List<PaymentMethodEntity>> = repo.paymentMethods
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val categories: StateFlow<List<CategoryEntity>> = repo.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val stores: StateFlow<List<StoreEntity>> = repo.stores
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val pendingLogs: StateFlow<List<PushLogEntity>> = repo.pushLogs(Types.STATUS_PENDING)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _ai = MutableStateFlow<Map<Long, AiGuess>>(emptyMap())
    val ai: StateFlow<Map<Long, AiGuess>> = _ai

    fun defaultMethodId(): Long? {
        val m = methods.value
        return m.firstOrNull { it.isDefault }?.id ?: m.firstOrNull()?.id
    }

    fun refreshAi() {
        viewModelScope.launch {
            val logs = repo.pendingPushLogs()
            if (logs.isEmpty()) return@launch
            val key = settings.currentAiKey()
            if (key.isBlank()) return@launch
            val catNames = repo.categoriesNow().map { it.name }
            logs.forEach { log ->
                if (!_ai.value.containsKey(log.id)) {
                    val guess = aiClient.detectCategoryAndStore(
                        listOfNotNull(log.title, log.text).joinToString(" | "),
                        log.appName,
                        catNames
                    )
                    _ai.value = _ai.value + (log.id to guess)
                }
            }
        }
    }

    suspend fun toPln(amount: Double, currency: String): Double = rates.toPln(amount, currency)

    fun save(rows: List<CaptureRowInput>) = viewModelScope.launch {
        rows.forEach { row ->
            val amountPln = rates.toPln(row.amount, row.currency)
            val storeId = if (!row.storeName.isNullOrBlank()) {
                repo.getOrCreateStore(row.storeName!!, null)
            } else null
            val log = repo.pendingPushLogs().firstOrNull { it.id == row.logId }
            repo.insertTransaction(
                TransactionEntity(
                    amount = row.amount,
                    currency = row.currency,
                    amountPln = amountPln,
                    categoryId = row.categoryId,
                    paymentMethodId = row.methodId,
                    storeId = storeId,
                    note = log?.text,
                    timestamp = log?.timestamp ?: System.currentTimeMillis(),
                    source = Types.SRC_PUSH,
                    isIncome = row.isIncome
                )
            )
            if (log != null) {
                repo.updatePushLog(log.copy(status = Types.STATUS_SAVED))
            }
        }
        _ai.value = emptyMap()
    }

    fun discard(logId: Long) = viewModelScope.launch {
        val log = repo.pendingPushLogs().firstOrNull { it.id == logId }
        if (log != null) repo.updatePushLog(log.copy(status = Types.STATUS_DISCARDED))
        _ai.value = _ai.value - logId
    }
}