package com.pushtofinance.infinapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pushtofinance.infinapp.data.AppRepository
import com.pushtofinance.infinapp.data.BudgetEntity
import com.pushtofinance.infinapp.data.CategoryEntity
import com.pushtofinance.infinapp.data.PaymentMethodEntity
import com.pushtofinance.infinapp.data.PushLogEntity
import com.pushtofinance.infinapp.data.SettingsManager
import com.pushtofinance.infinapp.data.TempPocketEntity
import com.pushtofinance.infinapp.data.TransactionEntity
import com.pushtofinance.infinapp.data.Types
import com.pushtofinance.infinapp.util.addMonths
import com.pushtofinance.infinapp.util.monthStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardData(
    val totalBalance: Double = 0.0,
    val methods: List<PaymentMethodEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList(),
    val budgets: List<BudgetEntity> = emptyList(),
    val pockets: List<TempPocketEntity> = emptyList(),
    val thisMonthSpent: Double = 0.0,
    val thisMonthIncome: Double = 0.0,
    val prevMonthSpent: Double = 0.0,
    val byCategory: Map<Long, Double> = emptyMap(),
    val pendingPushCount: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AppRepository.get(app)
    private val settings = SettingsManager(app)
    private val rates = com.pushtofinance.infinapp.currency.ExchangeRateClient(settings)

    val theme: StateFlow<String> = settings.theme.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")
    val onboarded: StateFlow<Boolean> = settings.onboarded.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val keepAlive: StateFlow<Boolean> = settings.keepAlive.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val selectedApps: StateFlow<Set<String>> = settings.selectedApps.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())
    val aiKey: StateFlow<String> = settings.aiKey.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val aiProvider: StateFlow<String> = settings.aiProvider.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val aiModel: StateFlow<String> = settings.aiModel.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val aiBaseUrl: StateFlow<String> = settings.aiBaseUrl.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val transactions: StateFlow<List<TransactionEntity>> = repo.transactions.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val methods: StateFlow<List<PaymentMethodEntity>> = repo.paymentMethods.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val categories: StateFlow<List<CategoryEntity>> = repo.categories.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val stores: StateFlow<List<com.pushtofinance.infinapp.data.StoreEntity>> = repo.stores.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val budgets: StateFlow<List<BudgetEntity>> = repo.budgets.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val pockets: StateFlow<List<TempPocketEntity>> = repo.pockets.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val pendingPushLogs: StateFlow<List<PushLogEntity>> = repo.pushLogs(Types.STATUS_PENDING).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val allPushLogs: StateFlow<List<PushLogEntity>> = repo.allPushLogs().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val now = System.currentTimeMillis()

    val dashboard: StateFlow<DashboardData?> = combine(
        repo.transactions, repo.paymentMethods, repo.categories,
        repo.budgets, repo.pockets, repo.pushLogs(Types.STATUS_PENDING)
    ) { values ->
        val txs = values[0] as List<TransactionEntity>
        val methods = values[1] as List<PaymentMethodEntity>
        val cats = values[2] as List<CategoryEntity>
        val budgets = values[3] as List<BudgetEntity>
        val pockets = values[4] as List<TempPocketEntity>
        val pending = values[5] as List<PushLogEntity>
        val ms = monthStart(now)
        val thisMs = ms
        val prevMs = addMonths(ms, -1)
        val thisMonth = txs.filter { it.timestamp >= thisMs }
        val prevMonth = txs.filter { it.timestamp >= prevMs && it.timestamp < thisMs }
        val monthExpenses = thisMonth.filter { !it.isIncome }
        DashboardData(
            totalBalance = methods.sumOf { it.balance },
            methods = methods,
            categories = cats,
            transactions = txs,
            budgets = budgets,
            pockets = pockets,
            thisMonthSpent = monthExpenses.sumOf { it.amountPln },
            thisMonthIncome = thisMonth.filter { it.isIncome }.sumOf { it.amountPln },
            prevMonthSpent = prevMonth.filter { !it.isIncome }.sumOf { it.amountPln },
            byCategory = monthExpenses.filter { it.categoryId != null }
                .groupBy { it.categoryId!! }
                .mapValues { (_, list) -> list.sumOf { it.amountPln } },
            pendingPushCount = pending.size
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setOnboarded(v: Boolean) = viewModelScope.launch { settings.setOnboarded(v) }
    fun setKeepAlive(v: Boolean) = viewModelScope.launch { settings.setKeepAlive(v) }
    fun setTheme(v: String) = viewModelScope.launch { settings.setTheme(v) }
    fun setAiKey(v: String) = viewModelScope.launch { settings.setAiKey(v) }
    fun setAiProvider(v: String) = viewModelScope.launch { settings.setAiProvider(v) }
    fun setAiModel(v: String) = viewModelScope.launch { settings.setAiModel(v) }
    fun setAiBaseUrl(v: String) = viewModelScope.launch { settings.setAiBaseUrl(v) }
    fun setSelectedApps(v: Set<String>) = viewModelScope.launch { settings.setSelectedApps(v) }

    // ---- CRUD: metody pЕ‚atnoЕ›ci ----
    fun saveMethod(m: PaymentMethodEntity, isNew: Boolean) = viewModelScope.launch {
        if (isNew) repo.insertPaymentMethod(m) else repo.updatePaymentMethod(m)
    }
    fun deleteMethod(m: PaymentMethodEntity) = viewModelScope.launch { repo.deletePaymentMethod(m) }

    // ---- CRUD: categories ----
    fun saveCategory(c: CategoryEntity, isNew: Boolean) = viewModelScope.launch {
        if (isNew) repo.insertCategory(c) else repo.updateCategory(c)
    }
    fun deleteCategory(c: CategoryEntity) = viewModelScope.launch { repo.deleteCategory(c) }

    // ---- CRUD: transactions ----
    fun saveTransaction(t: TransactionEntity, isNew: Boolean) = viewModelScope.launch {
        if (isNew) repo.insertTransaction(t) else repo.updateTransaction(t)
    }
    fun saveTransactionWithStore(t: TransactionEntity, storeName: String?, isNew: Boolean) = viewModelScope.launch {
        val sid = if (!storeName.isNullOrBlank()) repo.getOrCreateStore(storeName, null) else null
        val pln = rates.toPln(t.amount, t.currency)
        val final = t.copy(storeId = sid, amountPln = pln)
        if (isNew) repo.insertTransaction(final) else repo.updateTransaction(final)
    }
    fun deleteTransaction(t: TransactionEntity) = viewModelScope.launch { repo.deleteTransaction(t) }

    // ---- CRUD: budЕјety ----
    fun saveBudget(b: BudgetEntity, isNew: Boolean) = viewModelScope.launch {
        if (isNew) repo.insertBudget(b) else repo.updateBudget(b)
    }
    fun deleteBudget(b: BudgetEntity) = viewModelScope.launch { repo.deleteBudget(b) }

    // ---- CRUD: pockets ----
    fun savePocket(p: TempPocketEntity, isNew: Boolean) = viewModelScope.launch {
        if (isNew) repo.insertPocket(p) else repo.updatePocket(p)
    }
    fun deletePocket(p: TempPocketEntity) = viewModelScope.launch { repo.deletePocket(p) }

    // ---- Push logs ----
    fun setPushLogStatus(log: PushLogEntity, status: String) = viewModelScope.launch {
        repo.updatePushLog(log.copy(status = status))
    }
    fun deletePushLog(log: PushLogEntity) = viewModelScope.launch { repo.deletePushLog(log) }
}