package com.lichsu.budget_tracker.data.local.entity.recurring

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lichsu.budget_tracker.data.local.entity.categories.CategoryEntity
import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionType

@Entity(
    tableName = "recurring_rules",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("categoryId")],
)
data class RecurringRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: TransactionType,
    val amount: Long,
    val categoryId: Long,
    val note: String,
    val dayOfMonth: Int,
    val lastGeneratedMonth: String? = null,
)
