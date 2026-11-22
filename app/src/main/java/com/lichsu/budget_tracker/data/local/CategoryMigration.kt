package com.lichsu.budget_tracker.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Tên của danh mục mặc định trong mỗi loại (Chi, Thu). */
internal const val DEFAULT_CATEGORY_NAME = "Khác"

/** Biểu tượng cho 12 danh mục tạo sẵn: (loại, tên, iconKey). Khớp với seedCategories của TV3. */
internal val SEEDED_CATEGORY_ICONS = listOf(
    Triple("EXPENSE", "Ăn uống", "food"),
    Triple("EXPENSE", "Di chuyển", "transport"),
    Triple("EXPENSE", "Mua sắm", "shopping"),
    Triple("EXPENSE", "Nhà ở", "home"),
    Triple("EXPENSE", "Giải trí", "entertainment"),
    Triple("EXPENSE", "Học tập", "education"),
    Triple("EXPENSE", "Sức khỏe", "health"),
    Triple("EXPENSE", DEFAULT_CATEGORY_NAME, "other"),
    Triple("INCOME", "Lương", "salary"),
    Triple("INCOME", "Thưởng", "bonus"),
    Triple("INCOME", "Trợ cấp", "allowance"),
    Triple("INCOME", DEFAULT_CATEGORY_NAME, "other"),
)

/**
 * v2 → v3: thêm iconKey và isDefault cho bảng categories.
 * Chữ DEFAULT ở đây phải khớp @ColumnInfo(defaultValue) trong CategoryEntity,
 * nếu lệch Room sẽ báo "Migration didn't properly handle categories" khi mở DB.
 */
internal val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE categories ADD COLUMN iconKey TEXT NOT NULL DEFAULT 'other'")
        db.execSQL("ALTER TABLE categories ADD COLUMN isDefault INTEGER NOT NULL DEFAULT 0")
        backfillCategoryDefaults(db)
    }
}

/**
 * Điền iconKey cho danh mục tạo sẵn và đánh dấu đúng một "Khác" cho mỗi loại.
 * Chạy sau khi seed: trong MIGRATION_2_3 (máy đã cài bản cũ) và trong DEFAULT_CATEGORIES.onCreate (cài mới).
 * Chạy lại nhiều lần vẫn cho cùng kết quả.
 */
internal fun backfillCategoryDefaults(db: SupportSQLiteDatabase) {
    SEEDED_CATEGORY_ICONS.forEach { (type, name, iconKey) ->
        db.execSQL(
            "UPDATE categories SET iconKey = ? WHERE type = ? AND name = ?",
            arrayOf<Any>(iconKey, type, name),
        )
    }
    // Lỡ có hai "Khác" cùng loại thì chỉ cái cũ nhất (id nhỏ nhất) là mặc định.
    db.execSQL(
        """
        UPDATE categories SET isDefault = 1
        WHERE id IN (SELECT MIN(id) FROM categories WHERE name = ? GROUP BY type)
        """.trimIndent(),
        arrayOf<Any>(DEFAULT_CATEGORY_NAME),
    )
    // Loại nào chưa có "Khác" (dữ liệu cũ đã xóa mất) thì tạo lại, để xóa danh mục luôn có chỗ chuyển giao dịch.
    listOf("EXPENSE", "INCOME").forEach { type ->
        db.execSQL(
            """
            INSERT INTO categories(name, type, colorHex, iconKey, isDefault)
            SELECT ?, ?, '#67718A', 'other', 1
            WHERE NOT EXISTS (SELECT 1 FROM categories WHERE type = ? AND isDefault = 1)
            """.trimIndent(),
            arrayOf<Any>(DEFAULT_CATEGORY_NAME, type, type),
        )
    }
}
