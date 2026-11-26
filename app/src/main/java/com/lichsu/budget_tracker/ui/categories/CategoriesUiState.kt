package com.lichsu.budget_tracker.ui.categories

import com.lichsu.budget_tracker.data.local.entity.categories.CategoryEntity
import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionType

data class CategoriesUiState(
    val type: TransactionType = TransactionType.EXPENSE,
    /** Danh mục của loại đang chọn; "Khác" luôn nằm cuối. */
    val categories: List<CategoryEntity> = emptyList(),
    val loaded: Boolean = false,
) {
    /** Tên "Khác" của loại đang chọn, để hộp thoại xóa nói rõ giao dịch sẽ chuyển đi đâu. */
    val defaultName: String? get() = categories.firstOrNull { it.isDefault }?.name
}
