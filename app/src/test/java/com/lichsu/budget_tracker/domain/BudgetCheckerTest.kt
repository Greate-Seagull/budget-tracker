package com.lichsu.budget_tracker.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class BudgetCheckerTest {
    @Test fun detectsWarningAndLimitAtExactBoundaries() {
        assertEquals(BudgetStatus.NORMAL, BudgetChecker.check(79, 100))
        assertEquals(BudgetStatus.WARNING, BudgetChecker.check(80, 100))
        assertEquals(BudgetStatus.WARNING, BudgetChecker.check(99, 100))
        assertEquals(BudgetStatus.EXCEEDED, BudgetChecker.check(100, 100))
        assertEquals(BudgetStatus.EXCEEDED, BudgetChecker.check(101, 100))
    }

    @Test fun usesTheFirstWholeDongAtOrAboveEightyPercent() {
        assertEquals(BudgetStatus.NORMAL, BudgetChecker.check(8, 11))
        assertEquals(BudgetStatus.WARNING, BudgetChecker.check(9, 11))
        assertEquals(BudgetStatus.WARNING, BudgetChecker.check(Long.MAX_VALUE - 1, Long.MAX_VALUE))
    }
}
