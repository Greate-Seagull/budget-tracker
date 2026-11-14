package com.lichsu.budget_tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionEntity
import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

data class CategoryTotal(
    val categoryId: Long?,
    val categoryName: String?,
    val colorHex: String?,
    val total: Long,
)

data class TransactionTotal(val type: TransactionType, val total: Long)

data class DailyTotal(val date: LocalDate, val total: Long)

data class MonthlyExpenseTotals(val current: Long, val previous: Long)

@Dao
interface StatsDao {
    @Query(
        """
        SELECT c.id AS categoryId, c.name AS categoryName, c.colorHex, SUM(t.amount) AS total
        FROM transactions t LEFT JOIN categories c ON t.categoryId = c.id
        WHERE t.type = :type AND t.date >= :start AND t.date < :endExclusive
        GROUP BY c.id, c.name, c.colorHex
        ORDER BY total DESC, categoryName, categoryId
        """,
    )
    fun observeCategoryTotals(
        type: TransactionType,
        start: LocalDate,
        endExclusive: LocalDate,
    ): Flow<List<CategoryTotal>>

    @Query(
        """
        SELECT date, SUM(amount) AS total
        FROM transactions
        WHERE type = :type AND date >= :start AND date < :endExclusive
        GROUP BY date ORDER BY date
        """,
    )
    fun observeDailyTotals(
        type: TransactionType,
        start: LocalDate,
        endExclusive: LocalDate,
    ): Flow<List<DailyTotal>>

    @Query(
        """
        SELECT COALESCE(SUM(CASE WHEN date >= :currentStart THEN amount ELSE 0 END), 0) AS `current`,
               COALESCE(SUM(CASE WHEN date < :currentStart THEN amount ELSE 0 END), 0) AS `previous`
        FROM transactions
        WHERE type = 'EXPENSE' AND date >= :previousStart AND date < :endExclusive
        """,
    )
    fun observeExpenseComparison(
        previousStart: LocalDate,
        currentStart: LocalDate,
        endExclusive: LocalDate,
    ): Flow<MonthlyExpenseTotals>

    @Query(
        """
        SELECT type, SUM(amount) AS total FROM transactions
        WHERE date >= :start AND date < :endExclusive
        GROUP BY type
        """,
    )
    fun observeMonthlyTotals(start: LocalDate, endExclusive: LocalDate): Flow<List<TransactionTotal>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE date >= :start AND date < :endExclusive
        ORDER BY date DESC, createdAt DESC, id DESC LIMIT 5
        """,
    )
    fun observeRecentInMonth(start: LocalDate, endExclusive: LocalDate): Flow<List<TransactionEntity>>
}
