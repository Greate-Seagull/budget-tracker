package com.lichsu.budget_tracker.data.local.entity.budgets

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val categoryId: Long,
    val limitAmount: Long,
) {
    companion object {
        const val TOTAL_CATEGORY_ID = 0L
    }
}
