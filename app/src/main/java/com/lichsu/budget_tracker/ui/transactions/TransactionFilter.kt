package com.lichsu.budget_tracker.ui.transactions

import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionType
import com.lichsu.budget_tracker.domain.model.DatePreset
import com.lichsu.budget_tracker.domain.model.DateRange
import com.lichsu.budget_tracker.domain.removeDiacritics
import java.time.LocalDate

data class TransactionFilter(
    val types: Set<TransactionType> = emptySet(),
    val categoryIds: Set<Long> = emptySet(),
    val dateRange: DateRange = DatePreset.THIS_YEAR.range(LocalDate.now()),
    val amountRange: ClosedFloatingPointRange<Float>? = null,
) {
    fun accepts(tx: TransactionUi): Boolean =
        acceptsType(tx) && acceptsCategory(tx) && acceptsDate(tx) && acceptsAmount(tx)

    private fun acceptsType(tx: TransactionUi) = types.isEmpty() || tx.type in types
    private fun acceptsCategory(tx: TransactionUi) = categoryIds.isEmpty() || tx.category.id in categoryIds
    private fun acceptsDate(tx: TransactionUi) = tx.date in dateRange.start..dateRange.end
    private fun acceptsAmount(tx: TransactionUi) = amountRange?.contains(tx.amount.toFloat()) ?: true
}

/** Chuẩn hoá chuỗi người dùng nhập để so khớp với searchNote / searchCategory. */
fun String.toSearchQuery(): String = trim().removeDiacritics().lowercase()

/** Lọc theo từ khoá (đã chuẩn hoá) rồi theo bộ lọc. */
fun List<TransactionUi>.filterBy(query: String, filter: TransactionFilter): List<TransactionUi> =
    asSequence()
        .filter { query.isEmpty() || it.matches(query) }
        .filter(filter::accepts)
        .toList()
