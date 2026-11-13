package com.lichsu.budget_tracker.domain

enum class BudgetStatus {
    NORMAL,
    WARNING,
    EXCEEDED,
}

object BudgetChecker {
    fun check(spent: Long, limit: Long): BudgetStatus {
        require(spent >= 0 && limit > 0)
        return when {
            spent >= limit -> BudgetStatus.EXCEEDED
            spent >= limit - limit / 5 -> BudgetStatus.WARNING
            else -> BudgetStatus.NORMAL
        }
    }
}
