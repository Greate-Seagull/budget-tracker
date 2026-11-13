package com.lichsu.budget_tracker.data.repository

import androidx.room.withTransaction
import com.lichsu.budget_tracker.data.local.AppDatabase
import com.lichsu.budget_tracker.data.local.dao.BudgetUsage
import com.lichsu.budget_tracker.data.local.entity.budgets.BudgetEntity
import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionEntity
import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionType
import com.lichsu.budget_tracker.domain.BudgetChecker
import jakarta.inject.Inject
import kotlinx.coroutines.flow.map
import java.time.YearMonth

class BudgetRepository @Inject constructor(private val db: AppDatabase) {
    private val budgets = db.budgetDao()

    fun observeUsage(month: YearMonth) =
        budgets.observeUsage(month.atDay(1), month.plusMonths(1).atDay(1))

    fun observeExpenseCategories() = db.categoryDao().observeAll().map { categories ->
        categories.filter { it.type == TransactionType.EXPENSE }
    }

    suspend fun setLimit(categoryId: Long, limit: Long?) {
        db.withTransaction {
            if (limit == null) {
                budgets.delete(categoryId)
            } else {
                require(limit > 0)
                require(
                    categoryId == BudgetEntity.TOTAL_CATEGORY_ID ||
                        db.categoryDao().findById(categoryId)?.type == TransactionType.EXPENSE,
                )
                budgets.upsert(BudgetEntity(categoryId, limit))
            }
        }
    }

    suspend fun saveExpense(
        transaction: TransactionEntity,
        save: suspend () -> Unit,
    ): List<BudgetUsage> = db.withTransaction {
        require(transaction.type == TransactionType.EXPENSE)
        val month = YearMonth.from(transaction.date)
        val start = month.atDay(1)
        val end = month.plusMonths(1).atDay(1)
        val before = budgets.getUsageForExpense(transaction.categoryId, start, end)
            .associateBy { it.categoryId }
        save()
        budgets.getUsageForExpense(transaction.categoryId, start, end).filter { after ->
            val previous = before.getValue(after.categoryId)
            BudgetChecker.check(after.spent, after.limitAmount) >
                BudgetChecker.check(previous.spent, previous.limitAmount)
        }
    }
}
