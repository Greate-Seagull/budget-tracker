package com.lichsu.budget_tracker.ui.transactionform

import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionType
import com.lichsu.budget_tracker.domain.AmountError
import com.lichsu.budget_tracker.ui.transactionform.components.CategoryUi
import java.time.LocalDate

data class TransactionFormUiState(
    // Domain data
    val type: TransactionType = TransactionType.EXPENSE,
    val categories: List<CategoryUi> = emptyList(),
    /** Chỉ chứa chữ số ("1250000"); dấu chấm chỉ là phần hiển thị. */
    val amountText: String = "",
    val selectedCategoryId: Long? = null,
    val date: LocalDate = LocalDate.now(),
    val note: String = "",
    // UI data
    val isEditing: Boolean = false,
    val amountError: AmountError? = null,
    val categoryError: Boolean = false,
    val saveError: Boolean = false,
    val isSaving: Boolean = false,
)
