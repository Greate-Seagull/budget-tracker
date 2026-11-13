package com.lichsu.budget_tracker.data.local

import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                type TEXT NOT NULL,
                colorHex TEXT NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            "CREATE TABLE budgets (categoryId INTEGER PRIMARY KEY NOT NULL, limitAmount INTEGER NOT NULL)",
        )
        db.execSQL(
            """
            CREATE TABLE recurring_rules (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                type TEXT NOT NULL,
                amount INTEGER NOT NULL,
                categoryId INTEGER NOT NULL,
                note TEXT NOT NULL,
                dayOfMonth INTEGER NOT NULL,
                lastGeneratedMonth TEXT,
                FOREIGN KEY(categoryId) REFERENCES categories(id) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX index_recurring_rules_categoryId ON recurring_rules(categoryId)")
        seedCategories(db)
    }
}

internal val DEFAULT_CATEGORIES = object : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) = seedCategories(db)
}

private fun seedCategories(db: SupportSQLiteDatabase) {
    val expenses = listOf(
        "Ăn uống" to "#008B8B", "Di chuyển" to "#5076C8", "Mua sắm" to "#A95691",
        "Nhà ở" to "#CA7139", "Giải trí" to "#7662B6", "Học tập" to "#448364",
        "Sức khỏe" to "#C85863", "Khác" to "#67718A",
    )
    val incomes = listOf(
        "Lương" to "#008B8B", "Thưởng" to "#7662B6", "Trợ cấp" to "#5076C8", "Khác" to "#67718A",
    )
    // V1 had no category table. Keep legacy category IDs distinct from seeded IDs.
    db.execSQL(
        """
        INSERT INTO sqlite_sequence(name, seq)
        SELECT 'categories', COALESCE(MAX(categoryId), 0) FROM transactions
        """.trimIndent(),
    )
    val insert = "INSERT INTO categories(name, type, colorHex) VALUES (?, ?, ?)"
    expenses.forEach { (name, color) -> db.execSQL(insert, arrayOf(name, "EXPENSE", color)) }
    incomes.forEach { (name, color) -> db.execSQL(insert, arrayOf(name, "INCOME", color)) }
}
