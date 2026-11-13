package com.lichsu.budget_tracker.data.local.entity.transactions

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: TransactionType,
    val amount: Long,
    val categoryId: Long?,
    val date: LocalDate,
    val note: String,
    val createdAt: Long
) {
}