package com.lichsu.budget_tracker

import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lichsu.budget_tracker.data.local.AppDatabase
import com.lichsu.budget_tracker.data.local.DEFAULT_CATEGORIES
import com.lichsu.budget_tracker.data.local.MIGRATION_1_2
import com.lichsu.budget_tracker.data.local.entity.budgets.BudgetEntity
import com.lichsu.budget_tracker.data.local.entity.recurring.RecurringRuleEntity
import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionEntity
import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionType
import com.lichsu.budget_tracker.data.repository.BudgetRepository
import com.lichsu.budget_tracker.domain.RecurringProcessor
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Tv3DatabaseTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun expenseWarningsOnlyFireOnRisingThresholdsAndTotalsUseTheSelectedMonth() = runBlocking {
        inMemoryDatabase().withOpenDatabase { db ->
            val budget = BudgetRepository(db)
            val category = budget.observeExpenseCategories().first().first()
            val start = LocalDate.of(2026, 11, 1)
            val end = start.plusMonths(1)
            val totalId = BudgetEntity.TOTAL_CATEGORY_ID
            budget.setLimit(totalId, 100)
            budget.setLimit(category.id, 100)
            db.transactionDao().insert(expense(60, category.id, start.minusDays(1)))

            for ((amount, expected) in listOf(79L to 0, 1L to 2, 19L to 0, 1L to 2, 1L to 0)) {
                val expense = expense(amount, category.id, start)
                assertEquals(expected, budget.saveExpense(expense) { db.transactionDao().insert(expense) }.size)
            }
            val stats = db.statsDao()
            assertEquals(101L, stats.observeCategoryTotals(TransactionType.EXPENSE, start, end).first().single().total)
            assertEquals(101L, stats.observeDailyTotals(TransactionType.EXPENSE, start, end).first().single().total)
            val comparison = stats.observeExpenseComparison(start.minusMonths(1), start, end).first()
            assertEquals(101L, comparison.current)
            assertEquals(60L, comparison.previous)
            db.categoryDao().delete(category)
            assertEquals(listOf(totalId), budget.observeUsage(YearMonth.from(start)).first().map { it.categoryId })
            assertEquals(101L, stats.observeCategoryTotals(TransactionType.EXPENSE, start, end).first().single().total)
        }
    }

    @Test fun repeatedConcurrentRunsGenerateOneEntryPerMonth() = runBlocking {
        inMemoryDatabase().withOpenDatabase { db ->
            val category = BudgetRepository(db).observeExpenseCategories().first().first()
            db.recurringDao().insert(
                RecurringRuleEntity(
                    type = TransactionType.EXPENSE,
                    amount = 450_000,
                    categoryId = category.id,
                    note = "Tiền nhà",
                    dayOfMonth = 31,
                    lastGeneratedMonth = "2026-01",
                ),
            )
            val processor = RecurringProcessor(db)
            val through = YearMonth.of(2026, 4)
            List(3) { async { processor.process(through) } }.awaitAll()

            val stats = db.statsDao()
            val days = stats.observeDailyTotals(
                TransactionType.EXPENSE, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 5, 1),
            ).first()
            assertEquals(
                listOf(LocalDate.of(2026, 2, 28), LocalDate.of(2026, 3, 31), LocalDate.of(2026, 4, 30)),
                days.map { it.date },
            )
            assertTrue(days.all { it.total == 450_000L })
            assertEquals("2026-04", db.recurringDao().observeRules().first().single().lastGeneratedMonth)
        }
    }

    @Test fun migrationRetainsV1TransactionsAndSeedsCategories() = runBlocking {
        val file = "tv3-migration-test.db"
        context.deleteDatabase(file)
        val date = LocalDate.of(2026, 11, 13)
        try {
            val v1 = SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(file)
                .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        db.execSQL(
                            """
                            CREATE TABLE transactions (
                                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                type TEXT NOT NULL, amount INTEGER NOT NULL, categoryId INTEGER,
                                date INTEGER NOT NULL, note TEXT NOT NULL, createdAt INTEGER NOT NULL
                            )
                            """.trimIndent(),
                        )
                        db.execSQL(
                            "INSERT INTO transactions VALUES (1, 'EXPENSE', 123000, 10, ?, 'Legacy', 0)",
                            arrayOf(date.toEpochDay()),
                        )
                    }

                    override fun onUpgrade(db: androidx.sqlite.db.SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
                }).build()
            FrameworkSQLiteOpenHelperFactory().create(v1).use { it.writableDatabase }
            Room.databaseBuilder(context, AppDatabase::class.java, file)
                .addMigrations(MIGRATION_1_2)
                .build().withOpenDatabase { db ->
                    val transaction = db.transactionDao().findById(1)
                    assertEquals(123_000L, transaction?.amount)
                    assertEquals(date, transaction?.date)
                    assertEquals(10L, transaction?.categoryId)
                    val categories = db.categoryDao().observeAll().first()
                    assertEquals(12, categories.size)
                    assertTrue(categories.all { it.id > 10 })
                }
        } finally {
            context.deleteDatabase(file)
        }
    }

    private inline fun AppDatabase.withOpenDatabase(block: (AppDatabase) -> Unit) {
        try {
            block(this)
        } finally {
            close()
        }
    }

    private fun inMemoryDatabase() =
        Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).addCallback(DEFAULT_CATEGORIES).build()

    private fun expense(amount: Long, categoryId: Long, date: LocalDate) =
        TransactionEntity(
            type = TransactionType.EXPENSE, amount = amount, categoryId = categoryId,
            date = date, note = "", createdAt = 0,
        )
}
