package com.lichsu.budget_tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.lichsu.budget_tracker.data.local.entity.categories.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun findById(id: Long): CategoryEntity?

    @Query("SELECT * FROM categories ORDER BY id")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Insert
    suspend fun insert(entity: CategoryEntity)

    @Update
    suspend fun update(entity: CategoryEntity)

    @Query("DELETE FROM budgets WHERE categoryId = :categoryId")
    suspend fun deleteBudgetForCategory(categoryId: Long)

    @Delete
    suspend fun deleteCategoryRow(entity: CategoryEntity)

    @Transaction
    suspend fun delete(entity: CategoryEntity) {
        deleteBudgetForCategory(entity.id)
        deleteCategoryRow(entity)
    }
}
