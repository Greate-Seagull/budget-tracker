package com.lichsu.budget_tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.lichsu.budget_tracker.data.local.entity.budgets.BudgetEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

data class BudgetUsage(
    val categoryId: Long,
    val categoryName: String?,
    val limitAmount: Long,
    val spent: Long,
)

@Dao
interface BudgetDao {
    @Upsert
    suspend fun upsert(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE categoryId = :categoryId")
    suspend fun delete(categoryId: Long)

    @Query(
        """
        SELECT b.categoryId, c.name AS categoryName, b.limitAmount, COALESCE((
            SELECT SUM(t.amount) FROM transactions t
            WHERE t.type = 'EXPENSE' AND t.date >= :start AND t.date < :endExclusive
              AND (b.categoryId = 0 OR t.categoryId = b.categoryId)
        ), 0) AS spent
        FROM budgets b LEFT JOIN categories c ON c.id = b.categoryId
        WHERE b.categoryId = 0 OR c.type = 'EXPENSE'
        ORDER BY b.categoryId <> 0, c.name, b.categoryId
        """,
    )
    fun observeUsage(start: LocalDate, endExclusive: LocalDate): Flow<List<BudgetUsage>>

    @Query(
        """
        SELECT b.categoryId, c.name AS categoryName, b.limitAmount, COALESCE((
            SELECT SUM(t.amount) FROM transactions t
            WHERE t.type = 'EXPENSE' AND t.date >= :start AND t.date < :endExclusive
              AND (b.categoryId = 0 OR t.categoryId = b.categoryId)
        ), 0) AS spent
        FROM budgets b LEFT JOIN categories c ON c.id = b.categoryId
        WHERE b.categoryId = 0 OR (b.categoryId = :categoryId AND c.type = 'EXPENSE')
        ORDER BY b.categoryId
        """,
    )
    suspend fun getUsageForExpense(
        categoryId: Long?,
        start: LocalDate,
        endExclusive: LocalDate,
    ): List<BudgetUsage>
}
