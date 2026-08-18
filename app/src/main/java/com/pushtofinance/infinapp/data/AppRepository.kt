package com.pushtofinance.infinapp.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class AppRepository private constructor(
    private val db: AppDatabase,
    private val settings: SettingsManager
) {
    val paymentMethods: Flow<List<PaymentMethodEntity>> = db.paymentMethodDao().observeAll()
    val categories: Flow<List<CategoryEntity>> = db.categoryDao().observeAll()
    val stores: Flow<List<StoreEntity>> = db.storeDao().observeAll()
    val transactions: Flow<List<TransactionEntity>> = db.transactionDao().observeAll()
    val budgets: Flow<List<BudgetEntity>> = db.budgetDao().observeAll()
    val pockets: Flow<List<TempPocketEntity>> = db.tempPocketDao().observeAll()

    fun pushLogs(status: String) = db.pushLogDao().observeByStatus(status)
    fun allPushLogs() = db.pushLogDao().observeAll()

    suspend fun paymentMethodsNow() = db.paymentMethodDao().getAll()
    suspend fun categoriesNow() = db.categoryDao().getAll()
    suspend fun storesNow() = db.storeDao().getAll()
    suspend fun defaultPaymentMethod(): PaymentMethodEntity? =
        db.paymentMethodDao().getAll().firstOrNull { it.isDefault }

    suspend fun insertPaymentMethod(item: PaymentMethodEntity): Long = db.paymentMethodDao().insert(item)
    suspend fun updatePaymentMethod(item: PaymentMethodEntity) = db.paymentMethodDao().update(item)
    suspend fun deletePaymentMethod(item: PaymentMethodEntity) = db.paymentMethodDao().delete(item)

    suspend fun insertCategory(item: CategoryEntity): Long = db.categoryDao().insert(item)
    suspend fun updateCategory(item: CategoryEntity) = db.categoryDao().update(item)
    suspend fun deleteCategory(item: CategoryEntity) = db.categoryDao().delete(item)

    suspend fun getOrCreateStore(name: String, packageName: String?): Long {
        val clean = name.trim().ifEmpty { return 0L }
        val existing = db.storeDao().findByName(clean)
        if (existing != null) {
            db.storeDao().bumpCount(existing.id)
            return existing.id
        }
        return db.storeDao().insert(StoreEntity(name = clean, packageName = packageName, count = 1))
    }

    suspend fun insertTransaction(item: TransactionEntity): Long = db.transactionDao().insert(item)
    suspend fun updateTransaction(item: TransactionEntity) = db.transactionDao().update(item)
    suspend fun deleteTransaction(item: TransactionEntity) = db.transactionDao().delete(item)

    suspend fun insertBudget(item: BudgetEntity): Long = db.budgetDao().insert(item)
    suspend fun updateBudget(item: BudgetEntity) = db.budgetDao().update(item)
    suspend fun deleteBudget(item: BudgetEntity) = db.budgetDao().delete(item)

    suspend fun insertPocket(item: TempPocketEntity): Long = db.tempPocketDao().insert(item)
    suspend fun updatePocket(item: TempPocketEntity) = db.tempPocketDao().update(item)
    suspend fun deletePocket(item: TempPocketEntity) = db.tempPocketDao().delete(item)

    suspend fun insertPushLog(item: PushLogEntity): Long = db.pushLogDao().insert(item)
    suspend fun updatePushLog(item: PushLogEntity) = db.pushLogDao().update(item)
    suspend fun deletePushLog(item: PushLogEntity) = db.pushLogDao().delete(item)
    suspend fun pendingPushLogs(): List<PushLogEntity> = db.pushLogDao().getByStatus(Types.STATUS_PENDING)
    suspend fun recentSameAmount(amount: Double, now: Long, windowMs: Long) =
        db.pushLogDao().findRecentSameAmount(amount, now, windowMs)

    companion object {
        @Volatile
        private var INSTANCE: AppRepository? = null

        fun get(context: Context): AppRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppRepository(
                    AppDatabase.get(context),
                    SettingsManager(context.applicationContext)
                ).also { INSTANCE = it }
            }
    }
}