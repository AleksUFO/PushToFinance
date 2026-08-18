package com.pushtofinance.infinapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentMethodDao {
    @Query("SELECT * FROM payment_methods ORDER BY isDefault DESC, id ASC")
    fun observeAll(): Flow<List<PaymentMethodEntity>>
    @Query("SELECT * FROM payment_methods ORDER BY isDefault DESC, id ASC")
    suspend fun getAll(): List<PaymentMethodEntity>
    @Query("SELECT * FROM payment_methods WHERE id = :id")
    suspend fun getById(id: Long): PaymentMethodEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PaymentMethodEntity): Long
    @Update
    suspend fun update(item: PaymentMethodEntity)
    @Delete
    suspend fun delete(item: PaymentMethodEntity)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<CategoryEntity>>
    @Query("SELECT * FROM categories ORDER BY name COLLATE NOCASE ASC")
    suspend fun getAll(): List<CategoryEntity>
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): CategoryEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CategoryEntity): Long
    @Update
    suspend fun update(item: CategoryEntity)
    @Delete
    suspend fun delete(item: CategoryEntity)
}

@Dao
interface StoreDao {
    @Query("SELECT * FROM stores ORDER BY count DESC")
    fun observeAll(): Flow<List<StoreEntity>>
    @Query("SELECT * FROM stores ORDER BY count DESC")
    suspend fun getAll(): List<StoreEntity>
    @Query("SELECT * FROM stores WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findByName(name: String): StoreEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: StoreEntity): Long
    @Update
    suspend fun update(item: StoreEntity)
    @Query("UPDATE stores SET count = count + 1 WHERE id = :id")
    suspend fun bumpCount(id: Long)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<TransactionEntity>>
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?
    @Query("SELECT * FROM transactions WHERE timestamp BETWEEN :from AND :to")
    suspend fun getBetween(from: Long, to: Long): List<TransactionEntity>
    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId AND timestamp BETWEEN :from AND :to")
    suspend fun getBetweenByCategory(categoryId: Long, from: Long, to: Long): List<TransactionEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TransactionEntity): Long
    @Update
    suspend fun update(item: TransactionEntity)
    @Delete
    suspend fun delete(item: TransactionEntity)
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets ORDER BY id ASC")
    fun observeAll(): Flow<List<BudgetEntity>>
    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getById(id: Long): BudgetEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: BudgetEntity): Long
    @Update
    suspend fun update(item: BudgetEntity)
    @Delete
    suspend fun delete(item: BudgetEntity)
}

@Dao
interface TempPocketDao {
    @Query("SELECT * FROM temp_pockets ORDER BY startDate ASC")
    fun observeAll(): Flow<List<TempPocketEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TempPocketEntity): Long
    @Update
    suspend fun update(item: TempPocketEntity)
    @Delete
    suspend fun delete(item: TempPocketEntity)
}

@Dao
interface PushLogDao {
    @Query("SELECT * FROM push_logs WHERE status = :status ORDER BY timestamp DESC")
    fun observeByStatus(status: String): Flow<List<PushLogEntity>>
    @Query("SELECT * FROM push_logs ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<PushLogEntity>>
    @Query("SELECT * FROM push_logs WHERE status = :status ORDER BY timestamp ASC")
    suspend fun getByStatus(status: String): List<PushLogEntity>
    @Query("SELECT * FROM push_logs WHERE status = 'PENDING' AND amount = :amount AND ABS(:now - timestamp) < :windowMs")
    suspend fun findRecentSameAmount(amount: Double, now: Long, windowMs: Long): List<PushLogEntity>
    @Insert
    suspend fun insert(item: PushLogEntity): Long
    @Update
    suspend fun update(item: PushLogEntity)
    @Delete
    suspend fun delete(item: PushLogEntity)
}
