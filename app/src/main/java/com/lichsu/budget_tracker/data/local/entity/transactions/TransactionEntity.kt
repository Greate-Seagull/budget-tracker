package com.lichsu.budget_tracker.data.local.entity.transactions

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lichsu.budget_tracker.data.local.entity.categories.CategoryEntity
import java.time.LocalDate

@Entity(
    tableName = "transactions",
    foreignKeys = [ForeignKey(
        entity = CategoryEntity::class,
        parentColumns = ["id"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.SET_NULL,
    )],
    indices = [Index("categoryId")],
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: TransactionType,
    val amount: Long,
    val categoryId: Long,
    val date: LocalDate,
    val note: String,
    val createdAt: Long
) {
}