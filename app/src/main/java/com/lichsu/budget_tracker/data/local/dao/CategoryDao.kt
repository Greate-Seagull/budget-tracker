package com.lichsu.budget_tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.lichsu.budget_tracker.data.local.entity.categories.CategoryEntity

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun findById(id: Long): CategoryEntity?
    @Insert
    suspend fun insert(entity: CategoryEntity)
    @Update
    suspend fun update(entity: CategoryEntity)
    @Delete
    suspend fun delete(entity: CategoryEntity)

}