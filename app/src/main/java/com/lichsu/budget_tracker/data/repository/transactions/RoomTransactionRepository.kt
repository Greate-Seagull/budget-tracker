package com.lichsu.budget_tracker.data.repository.transactions

import com.lichsu.budget_tracker.data.local.dao.TransactionDao
import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionEntity
import jakarta.inject.Inject

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
}