package com.lichsu.budget_tracker.ui.transactionform

import androidx.annotation.StringRes
import com.lichsu.budget_tracker.data.local.dao.BudgetUsage

sealed interface TransactionFormEvent {
    data class Saved(val budgetWarnings: List<BudgetUsage>) : TransactionFormEvent
    data class Error(@StringRes val messageRes: Int) : TransactionFormEvent
    data object Deleted : TransactionFormEvent
    data object NotFound : TransactionFormEvent
}
