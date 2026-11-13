package com.lichsu.budget_tracker.ui.transactionform

import com.lichsu.budget_tracker.data.local.dao.BudgetUsage

sealed interface TransactionFormEvent {
    data class Error(val message: String) : TransactionFormEvent
    data class Saved(val budgetWarnings: List<BudgetUsage>) : TransactionFormEvent
    data object Deleted : TransactionFormEvent
    data object NotFound : TransactionFormEvent
}
