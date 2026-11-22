package com.lichsu.budget_tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.lichsu.budget_tracker.data.local.entity.categories.CategoryEntity
import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    // TV2 (form giao dịch), TV3 (hạn mức, giao dịch lặp) đang gọi hai hàm này: giữ nguyên chữ ký.
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun findById(id: Long): CategoryEntity?

    @Query("SELECT * FROM categories ORDER BY id")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Insert
    suspend fun insert(entity: CategoryEntity): Long

    @Update
    suspend fun update(entity: CategoryEntity)

    @Query("DELETE FROM budgets WHERE categoryId = :categoryId")
    suspend fun deleteBudgetForCategory(categoryId: Long)

    @Delete
    suspend fun deleteCategoryRow(entity: CategoryEntity)

    /** Xóa hạn mức rồi xóa danh mục. Không chuyển giao dịch: dùng CategoryRepository.delete cho màn Danh mục. */
    @Transaction
    suspend fun delete(entity: CategoryEntity) {
        deleteBudgetForCategory(entity.id)
        deleteCategoryRow(entity)
    }

    // ---- TV1 (F03) ----

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY id")
    suspend fun getByType(type: TransactionType): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE type = :type AND isDefault = 1 ORDER BY id LIMIT 1")
    suspend fun findDefault(type: TransactionType): CategoryEntity?

    @Query("SELECT COUNT(*) FROM transactions WHERE categoryId = :categoryId")
    suspend fun countTransactions(categoryId: Long): Int

    @Query("UPDATE transactions SET categoryId = :toId WHERE categoryId = :fromId")
    suspend fun moveTransactions(fromId: Long, toId: Long): Int

    @Query("UPDATE recurring_rules SET categoryId = :toId WHERE categoryId = :fromId")
    suspend fun moveRecurringRules(fromId: Long, toId: Long): Int
}
