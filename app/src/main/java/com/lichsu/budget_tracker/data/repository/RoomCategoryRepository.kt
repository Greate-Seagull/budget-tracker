package com.lichsu.budget_tracker.data.repository

import com.lichsu.budget_tracker.data.local.dao.CategoryDao
import com.lichsu.budget_tracker.data.local.entity.categories.CategoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RoomCategoryRepository @Inject constructor(
    private val dao: CategoryDao
) {
    suspend fun findById(id: Long): CategoryEntity? {
        return dao.findById(id)
    }

    suspend fun insert(entity: CategoryEntity) {
        return dao.insert(entity)
    }

    suspend fun update(entity: CategoryEntity) {
        return dao.update(entity)
    }

    suspend fun delete(entity: CategoryEntity) {
        return dao.delete(entity)
    }

    fun observeAll(): Flow<List<CategoryEntity>> {
        return dao.observeAll();
    }
}