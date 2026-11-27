package com.lichsu.budget_tracker.ui.transactionform

import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionEntity

/** Dựng entity để lưu; giữ id và createdAt của bản gốc khi đang sửa. */
internal fun TransactionFormUiState.toEntity(
    original: TransactionEntity?,
    amount: Long,
    categoryId: Long,
) = TransactionEntity(
    id = original?.id ?: 0,
    type = type,
    amount = amount,
    categoryId = categoryId,
    date = date,
    note = note,
    createdAt = original?.createdAt ?: System.currentTimeMillis(),
)

/** Đổ dữ liệu của giao dịch đang sửa vào form. */
internal fun TransactionFormUiState.withTransaction(tx: TransactionEntity) = copy(
    type = tx.type,
    amountText = tx.amount.toString(),
    selectedCategoryId = tx.categoryId,
    date = tx.date,
    note = tx.note,
)
