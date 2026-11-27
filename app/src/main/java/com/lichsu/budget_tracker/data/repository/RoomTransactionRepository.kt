package com.lichsu.budget_tracker.data.repository

import com.lichsu.budget_tracker.data.local.dao.TransactionDao
import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionEntity
import com.lichsu.budget_tracker.data.local.relation.TransactionWithCategory
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class RoomTransactionRepository @Inject constructor(private val dao: TransactionDao){
    suspend fun findById(id: Long): TransactionEntity? {
        return dao.findById(id)
    }

    suspend fun insert(transaction: TransactionEntity) {
        return dao.insert(transaction)
    }

    suspend fun update(transaction: TransactionEntity) {
        return dao.update(transaction)
    }

    suspend fun delete(transaction: TransactionEntity) {
        return dao.delete(transaction)
    }

    fun observeAll(): Flow<List<TransactionWithCategory>> {
        return dao.observeAll()
    }
}