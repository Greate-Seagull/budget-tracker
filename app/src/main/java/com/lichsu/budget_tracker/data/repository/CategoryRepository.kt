package com.lichsu.budget_tracker.data.repository

import androidx.room.withTransaction
import com.lichsu.budget_tracker.data.local.AppDatabase
import com.lichsu.budget_tracker.data.local.entity.categories.CategoryEntity
import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionType
import com.lichsu.budget_tracker.domain.CategoryNameError
import com.lichsu.budget_tracker.domain.CategoryNameResult
import com.lichsu.budget_tracker.domain.CategoryNameValidator
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Kết quả thêm, sửa, xóa danh mục; màn hình dựa vào đây để hiện đúng câu báo. */
sealed interface CategoryResult {
    data object Success : CategoryResult
    data class InvalidName(val error: CategoryNameError) : CategoryResult
    data object NotFound : CategoryResult
    data object DefaultCannotBeDeleted : CategoryResult
}

/** Nghiệp vụ danh mục (F03) của TV1. */
class CategoryRepository @Inject constructor(private val db: AppDatabase) {
    private val dao = db.categoryDao()

    fun observeByType(type: TransactionType): Flow<List<CategoryEntity>> =
        dao.observeAll().map { all -> all.filter { it.type == type } }

    /** Số giao dịch sẽ bị chuyển sang "Khác" nếu xóa danh mục này (cho hộp thoại xác nhận). */
    suspend fun countTransactions(categoryId: Long): Int = dao.countTransactions(categoryId)

    // Kiểm tên và ghi nằm chung một transaction, để hai lần lưu gần nhau không lọt tên trùng.
    suspend fun add(
        name: String,
        type: TransactionType,
        iconKey: String,
        colorHex: String,
    ): CategoryResult = db.withTransaction {
        val others = dao.getByType(type).map { it.name }
        when (val checked = CategoryNameValidator.validate(name, others)) {
            is CategoryNameResult.Invalid -> CategoryResult.InvalidName(checked.error)
            is CategoryNameResult.Valid -> {
                dao.insert(
                    CategoryEntity(name = checked.value, type = type, iconKey = iconKey, colorHex = colorHex),
                )
                CategoryResult.Success
            }
        }
    }

    /** Sửa tên, biểu tượng, màu. Không đổi loại và cờ isDefault (lấy từ bản đang lưu). */
    suspend fun update(
        id: Long,
        name: String,
        iconKey: String,
        colorHex: String,
    ): CategoryResult = db.withTransaction {
        val current = dao.findById(id) ?: return@withTransaction CategoryResult.NotFound
        val others = dao.getByType(current.type).filter { it.id != id }.map { it.name }
        when (val checked = CategoryNameValidator.validate(name, others)) {
            is CategoryNameResult.Invalid -> CategoryResult.InvalidName(checked.error)
            is CategoryNameResult.Valid -> {
                dao.update(current.copy(name = checked.value, iconKey = iconKey, colorHex = colorHex))
                CategoryResult.Success
            }
        }
    }

    /**
     * Xóa danh mục theo SPEC F03: "Khác" không xóa được; giao dịch chuyển sang "Khác" cùng loại;
     * hạn mức của danh mục bị xóa. Quy tắc lặp cũng chuyển sang "Khác" (đề xuất câu 3, chờ nhóm chốt),
     * nếu không khóa ngoại CASCADE sẽ xóa mất quy tắc.
     */
    suspend fun delete(id: Long): CategoryResult = db.withTransaction {
        val category = dao.findById(id) ?: return@withTransaction CategoryResult.NotFound
        if (category.isDefault) return@withTransaction CategoryResult.DefaultCannotBeDeleted
        // MIGRATION_2_3 và DEFAULT_CATEGORIES bảo đảm mỗi loại luôn có một "Khác".
        val fallback = checkNotNull(dao.findDefault(category.type)) { "Thiếu danh mục Khác cho ${category.type}" }

        dao.moveTransactions(fromId = category.id, toId = fallback.id)
        dao.moveRecurringRules(fromId = category.id, toId = fallback.id)
        dao.delete(category) // xóa hạn mức rồi xóa dòng danh mục
        CategoryResult.Success
    }
}
