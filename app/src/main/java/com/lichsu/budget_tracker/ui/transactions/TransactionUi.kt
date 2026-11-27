package com.lichsu.budget_tracker.ui.transactions

import com.lichsu.budget_tracker.data.local.entity.transactions.TransactionType
import com.lichsu.budget_tracker.data.local.relation.TransactionWithCategory
import com.lichsu.budget_tracker.domain.removeDiacritics
import com.lichsu.budget_tracker.ui.categories.CategoryUi
import com.lichsu.budget_tracker.ui.categories.toUi
import java.time.LocalDate

data class TransactionUi(
    val id: Long,
    val type: TransactionType,
    val date: LocalDate,
    val category: CategoryUi,
    val amount: Long,
    val signedAmount: Long,
    val searchNote: String,
    val searchCategory: String,
)

fun TransactionUi.matches(q: String): Boolean =
    searchCategory.contains(q, ignoreCase = true) ||
            amount.toString().contains(q) ||
            searchNote.contains(q, ignoreCase = true)

fun TransactionWithCategory.toUi(): TransactionUi = TransactionUi(
    id = transaction.id,
    type = transaction.type,
    date = transaction.date,
    category = category.toUi(),
    amount = transaction.amount,
    signedAmount = signedAmount(),
    searchNote = transaction.note.removeDiacritics(),
    searchCategory = category.name.removeDiacritics(),
)

private fun TransactionWithCategory.signedAmount(): Long =
    when (transaction.type) {
        TransactionType.INCOME -> transaction.amount
        TransactionType.EXPENSE -> -transaction.amount
    }
