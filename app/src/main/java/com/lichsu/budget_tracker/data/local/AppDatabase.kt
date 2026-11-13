package com.lichsu.budget_tracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lichsu.budget_tracker.data.local.dao.BudgetDao
import com.lichsu.budget_tracker.data.local.dao.CategoryDao
import com.lichsu.budget_tracker.data.local.dao.RecurringDao
import com.lichsu.budget_tracker.data.local.dao.StatsDao
import com.lichsu.budget_tracker.data.local.dao.TransactionDao
import com.lichsu.budget_tracker.data.local.entity.budgets.BudgetEntity
import com.lichsu.budget_tracker.data.local.entity.categories.CategoryEntity
import com.lichsu.budget_tracker.data.local.entity.recurring.RecurringRuleEntity
import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
        RecurringRuleEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun statsDao(): StatsDao
    abstract fun recurringDao(): RecurringDao
}
