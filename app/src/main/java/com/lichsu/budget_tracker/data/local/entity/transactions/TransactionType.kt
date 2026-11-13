package com.lichsu.budget_tracker.data.local.entity.transactions

import com.lichsu.budget_tracker.R

enum class TransactionType(val labelRes: Int) {
    EXPENSE(R.string.transaction_expense),
    INCOME(R.string.transaction_income)
}