package com.lichsu.budget_tracker.domain

import androidx.room.withTransaction
import com.lichsu.budget_tracker.data.local.AppDatabase
import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionEntity
import jakarta.inject.Inject
import java.time.LocalDate
import java.time.YearMonth

class RecurringProcessor @Inject constructor(private val db: AppDatabase) {
    suspend fun process(through: YearMonth = YearMonth.now()) {
        db.withTransaction {
            val rules = db.recurringDao()
            val transactions = db.transactionDao()
            val categories = db.categoryDao()
            for (rule in rules.getPendingRules(through.toString())) {
                if (categories.findById(rule.categoryId)?.type != rule.type) continue
                val lastGenerated = rule.lastGeneratedMonth?.let(YearMonth::parse)
                for (date in datesToGenerate(lastGenerated, through, rule.dayOfMonth)) {
                    transactions.insert(
                        TransactionEntity(
                            type = rule.type,
                            amount = rule.amount,
                            categoryId = rule.categoryId,
                            date = date,
                            note = rule.note,
                            createdAt = System.currentTimeMillis(),
                        ),
                    )
                }
                rules.markGenerated(rule.id, through.toString())
            }
        }
    }

    companion object {
        internal fun datesToGenerate(
            lastGenerated: YearMonth?,
            through: YearMonth,
            dayOfMonth: Int,
        ): Sequence<LocalDate> {
            require(dayOfMonth in 1..31)
            if (lastGenerated != null && lastGenerated >= through) return emptySequence()
            val start = lastGenerated?.plusMonths(1) ?: through
            return generateSequence(start) { month ->
                if (month < through) month.plusMonths(1) else null
            }.map { month -> month.atDay(minOf(dayOfMonth, month.lengthOfMonth())) }
        }
    }
}
