package com.pushtofinance.infinapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

object Types {
    const val METHOD_CARD_PUSH = "CARD_PUSH"
    const val METHOD_CARD_MANUAL = "CARD_MANUAL"
    const val METHOD_CASH = "CASH"
    const val PROVIDER_GOOGLE_PAY = "GOOGLE_PAY"
    const val PROVIDER_PLAIN = "PLAIN"
    const val CAT_PLANNED = "PLANNED"
    const val CAT_SPONTANEOUS = "SPONTANEOUS"
    const val CAT_OTHER = "OTHER"
    const val PERIOD_TOTAL = "TOTAL"
    const val PERIOD_WEEKLY = "WEEKLY"
    const val PERIOD_MONTHLY = "MONTHLY"
    const val PERIOD_YEARLY = "YEARLY"
    const val SRC_MANUAL = "MANUAL"
    const val SRC_PUSH = "PUSH"
    const val STATUS_PENDING = "PENDING"
    const val STATUS_SAVED = "SAVED"
    const val STATUS_DISCARDED = "DISCARDED"
}

@Entity(tableName = "payment_methods")
data class PaymentMethodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String,
    val provider: String? = null,
    val brand: String? = null,
    val last4: String? = null,
    val color: Long,
    val iconLetter: String,
    val balance: Double = 0.0,
    val currency: String = "PLN",
    val isDefault: Boolean = false
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: Long,
    val iconLetter: String,
    val kind: String = Types.CAT_OTHER,
    val isSystem: Boolean = false,
    val parentId: Long? = null
)

@Entity(tableName = "stores")
data class StoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val packageName: String? = null,
    val count: Long = 1
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val currency: String,
    val amountPln: Double,
    val categoryId: Long? = null,
    val paymentMethodId: Long? = null,
    val storeId: Long? = null,
    val note: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val source: String = Types.SRC_MANUAL,
    val isIncome: Boolean = false
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: Double,
    val currency: String,
    val period: String,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val categoryId: Long? = null
)

@Entity(tableName = "temp_pockets")
data class TempPocketEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val categoryIds: String,
    val startDate: Long,
    val endDate: Long,
    val budgetAmount: Double? = null,
    val note: String? = null
)

@Entity(tableName = "push_logs")
data class PushLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val title: String? = null,
    val text: String? = null,
    val amount: Double? = null,
    val currency: String? = null,
    val amountPln: Double? = null,
    val cardName: String? = null,
    val storeName: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = Types.STATUS_PENDING,
    val isIncome: Boolean = false
)
