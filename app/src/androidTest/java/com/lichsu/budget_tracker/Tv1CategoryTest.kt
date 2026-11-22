package com.lichsu.budget_tracker

import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lichsu.budget_tracker.data.local.AppDatabase
import com.lichsu.budget_tracker.data.local.DEFAULT_CATEGORIES
import com.lichsu.budget_tracker.data.local.MIGRATION_1_2
import com.lichsu.budget_tracker.data.local.MIGRATION_2_3
import com.lichsu.budget_tracker.data.local.entity.categories.CategoryEntity
import com.lichsu.budget_tracker.data.local.entity.recurring.RecurringRuleEntity
import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionEntity
import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionType.EXPENSE
import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionType.INCOME
import com.lichsu.budget_tracker.data.repository.BudgetRepository
import com.lichsu.budget_tracker.data.repository.CategoryRepository
import com.lichsu.budget_tracker.data.repository.CategoryResult
import com.lichsu.budget_tracker.domain.CategoryNameError
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Tv1CategoryTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun freshInstallSeedsIconsAndExactlyOneDefaultPerType() = runBlocking {
        inMemoryDatabase().useDb { db ->
            val all = db.categoryDao().observeAll().first()
            assertEquals(12, all.size)
            assertDefaults(all)
            assertEquals("food", all.single { it.name == "Ăn uống" }.iconKey)
            assertEquals("salary", all.single { it.name == "Lương" }.iconKey)
        }
    }

    @Test fun deleteMovesTransactionsAndRulesToOtherAndDropsBudget() = runBlocking {
        inMemoryDatabase().useDb { db ->
            val repo = CategoryRepository(db)
            val food = db.byName("Ăn uống")
            val other = checkNotNull(db.categoryDao().findDefault(EXPENSE))
            db.transactionDao().insert(expense(50_000, food.id))
            db.transactionDao().insert(expense(70_000, food.id))
            BudgetRepository(db).setLimit(food.id, 1_000_000)
            db.recurringDao().insert(
                RecurringRuleEntity(type = EXPENSE, amount = 30_000, categoryId = food.id, note = "Cơm tháng", dayOfMonth = 5),
            )
            assertEquals(2, repo.countTransactions(food.id))

            assertEquals(CategoryResult.Success, repo.delete(food.id))

            assertNull(db.categoryDao().findById(food.id))
            assertEquals(0, repo.countTransactions(food.id))
            assertEquals(2, repo.countTransactions(other.id))
            assertEquals(0, db.budgetCount(food.id))
            assertEquals(listOf(other.id), db.recurringDao().observeRules().first().map { it.categoryId })
        }
    }

    @Test fun defaultCategoriesCannotBeDeleted() = runBlocking {
        inMemoryDatabase().useDb { db ->
            val repo = CategoryRepository(db)
            for (type in listOf(EXPENSE, INCOME)) {
                val other = checkNotNull(db.categoryDao().findDefault(type))
                assertEquals(CategoryResult.DefaultCannotBeDeleted, repo.delete(other.id))
                assertNotNull(db.categoryDao().findById(other.id))
            }
            assertEquals(CategoryResult.NotFound, repo.delete(999_999))
        }
    }

    @Test fun addRejectsBlankAndDuplicateOnlyWithinSameType() = runBlocking {
        inMemoryDatabase().useDb { db ->
            val repo = CategoryRepository(db)
            assertEquals(CategoryResult.InvalidName(CategoryNameError.EMPTY), repo.add("   ", EXPENSE, "other", "#123456"))
            assertEquals(
                CategoryResult.InvalidName(CategoryNameError.DUPLICATE),
                repo.add("ăn UỐNG", EXPENSE, "food", "#123456"),
            )
            // "Lương" đã có bên Thu, thêm bên Chi vẫn được.
            assertEquals(CategoryResult.Success, repo.add("Lương", EXPENSE, "salary", "#123456"))
            assertEquals(CategoryResult.Success, repo.add("  Cà   phê ", EXPENSE, "food", "#123456"))

            val coffee = db.byName("Cà phê")
            assertEquals(EXPENSE, coffee.type)
            assertEquals(false, coffee.isDefault)
            assertEquals(10, db.categoryDao().getByType(EXPENSE).size)
        }
    }

    @Test fun updateChecksDuplicatesExceptItselfAndKeepsTypeAndDefaultFlag() = runBlocking {
        inMemoryDatabase().useDb { db ->
            val repo = CategoryRepository(db)
            val food = db.byName("Ăn uống")
            assertEquals(
                CategoryResult.InvalidName(CategoryNameError.DUPLICATE),
                repo.update(food.id, "di chuyển", "food", food.colorHex),
            )
            // Đổi hoa thường của chính nó không tính là trùng.
            assertEquals(CategoryResult.Success, repo.update(food.id, "ĂN UỐNG", "shopping", "#000000"))
            val updated = checkNotNull(db.categoryDao().findById(food.id))
            assertEquals(CategoryEntity(food.id, "ĂN UỐNG", EXPENSE, "#000000", "shopping", false), updated)

            val other = checkNotNull(db.categoryDao().findDefault(INCOME))
            assertEquals(CategoryResult.Success, repo.update(other.id, "Thu khác", "other", other.colorHex))
            assertEquals(true, db.categoryDao().findById(other.id)?.isDefault)
            assertEquals(CategoryResult.NotFound, repo.update(999_999, "X", "other", "#000000"))
        }
    }

    @Test fun migrationFromV2KeepsDataAndMarksBothDefaults() = runBlocking {
        val file = "tv1-migration-test.db"
        context.deleteDatabase(file)
        try {
            createVersion2Database(file)
            Room.databaseBuilder(context, AppDatabase::class.java, file)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build().useDb { db ->
                    // Mở được tức là Room đã so schema sau MIGRATION_2_3 và khớp với entity.
                    val all = db.categoryDao().observeAll().first()
                    assertEquals(12, all.size)
                    assertDefaults(all)
                    val food = all.single { it.name == "Ăn uống" }
                    assertEquals("food", food.iconKey)
                    assertEquals(food.id, db.transactionDao().findById(1)?.categoryId)
                    assertEquals(1, db.budgetCount(food.id))

                    assertEquals(CategoryResult.Success, CategoryRepository(db).delete(food.id))
                    assertEquals(db.categoryDao().findDefault(EXPENSE)?.id, db.transactionDao().findById(1)?.categoryId)
                }
        } finally {
            context.deleteDatabase(file)
        }
    }

    /** Dựng DB đúng như bản app version 2: bảng transactions của v1 rồi chạy MIGRATION_1_2 của TV3. */
    private fun createVersion2Database(file: String) {
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(file)
            .callback(object : SupportSQLiteOpenHelper.Callback(2) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE transactions (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            type TEXT NOT NULL, amount INTEGER NOT NULL, categoryId INTEGER,
                            date INTEGER NOT NULL, note TEXT NOT NULL, createdAt INTEGER NOT NULL
                        )
                        """.trimIndent(),
                    )
                    MIGRATION_1_2.migrate(db)
                    val foodId = db.query("SELECT id FROM categories WHERE name = 'Ăn uống'").use {
                        it.moveToFirst()
                        it.getLong(0)
                    }
                    db.execSQL(
                        "INSERT INTO transactions VALUES (1, 'EXPENSE', 45000, ?, ?, 'Phở', 0)",
                        arrayOf<Any>(foodId, LocalDate.of(2026, 11, 20).toEpochDay()),
                    )
                    db.execSQL("INSERT INTO budgets VALUES (?, 2000000)", arrayOf<Any>(foodId))
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
            }).build()
        FrameworkSQLiteOpenHelperFactory().create(config).use { it.writableDatabase }
    }

    private fun assertDefaults(all: List<CategoryEntity>) {
        val defaults = all.filter { it.isDefault }
        assertEquals(2, defaults.size)
        assertEquals(setOf(EXPENSE, INCOME), defaults.map { it.type }.toSet())
        assertTrue(defaults.all { it.name == "Khác" && it.iconKey == "other" })
    }

    private suspend fun AppDatabase.byName(name: String): CategoryEntity =
        categoryDao().observeAll().first().single { it.name == name }

    private fun AppDatabase.budgetCount(categoryId: Long): Int =
        openHelper.writableDatabase
            .query("SELECT COUNT(*) FROM budgets WHERE categoryId = ?", arrayOf<Any>(categoryId))
            .use {
                it.moveToFirst()
                it.getInt(0)
            }

    // Trả về Unit để hàm @Test (= runBlocking { … }) luôn là void, JUnit 4 mới chạy.
    private inline fun AppDatabase.useDb(block: (AppDatabase) -> Unit) {
        try {
            block(this)
        } finally {
            close()
        }
    }

    private fun inMemoryDatabase() =
        Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).addCallback(DEFAULT_CATEGORIES).build()

    private fun expense(amount: Long, categoryId: Long) =
        TransactionEntity(
            type = EXPENSE, amount = amount, categoryId = categoryId,
            date = LocalDate.of(2026, 11, 20), note = "", createdAt = 0,
        )
}
