package com.lichsu.budget_tracker.data.local.entity.categories

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionType

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: TransactionType = TransactionType.EXPENSE,
    val colorHex: String = "#008B8B",
    // TV1 (F03). DEFAULT phải khớp từng chữ với MIGRATION_2_3 trong CategoryMigration.kt.
    @ColumnInfo(defaultValue = "'other'") val iconKey: String = "other",
    // true = "Khác" của loại này, không xóa được; giao dịch của danh mục bị xóa chuyển về đây.
    @ColumnInfo(defaultValue = "0") val isDefault: Boolean = false,
)
