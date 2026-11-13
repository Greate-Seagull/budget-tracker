package com.lichsu.budget_tracker.ui.transactionform

sealed interface TransactionFormEvent {
    data class Error(val message: String) : TransactionFormEvent
    data object Saved : TransactionFormEvent
    data object Deleted : TransactionFormEvent
    data object NotFound : TransactionFormEvent
}
