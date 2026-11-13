package com.lichsu.budget_tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionEntity

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun findById(id: Long): TransactionEntity?
    @Insert
    suspend fun insert(transaction: TransactionEntity)
    @Update
    suspend fun update(transaction: TransactionEntity)
    @Delete
    suspend fun delete(transaction: TransactionEntity)

}
