package com.lichsu.budget_tracker.data.local.entity.categories

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionType

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: TransactionType = TransactionType.EXPENSE,
    val colorHex: String = "#008B8B",
)
