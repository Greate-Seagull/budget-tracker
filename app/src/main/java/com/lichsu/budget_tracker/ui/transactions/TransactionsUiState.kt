package com.lichsu.budget_tracker.ui.transactions

import com.lichsu.budget_tracker.domain.model.AmountStats
import com.lichsu.budget_tracker.ui.categories.CategoryUi
import java.time.LocalDate
import java.time.YearMonth

data class TransactionsUiState(
    val selectedMonth: YearMonth,
    val today: LocalDate,
    val items: List<TransactionListItem> = emptyList(),
    val categories: List<CategoryUi> = emptyList(),
    val amounts: AmountStats = AmountStats.Empty,
    val isLoading: Boolean = true,
) {
    val isEmpty: Boolean get() = !isLoading && items.isEmpty()

    /** Tính một lần khi tạo state, lấy từ MonthItem nên không phải duyệt lại giao dịch. */
    val monthTotal: Long = items
        .filterIsInstance<TransactionListItem.MonthItem>()
        .firstOrNull { it.month == selectedMonth }
        ?.total ?: 0L
}
