package com.lichsu.budget_tracker.domain

import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Test

class RecurringProcessorTest {
    @Test fun backfillsEveryMonthAndClampsToTheLastDay() {
        val actual = RecurringProcessor.datesToGenerate(
            YearMonth.of(2024, 1), YearMonth.of(2024, 4), 31,
        ).toList()
        assertEquals(
            listOf(LocalDate.of(2024, 2, 29), LocalDate.of(2024, 3, 31), LocalDate.of(2024, 4, 30)),
            actual,
        )
    }

    @Test fun aNewRuleStartsInTheCurrentMonthAndReprocessingIsEmpty() {
        val month = YearMonth.of(2025, 2)
        assertEquals(listOf(LocalDate.of(2025, 2, 28)), RecurringProcessor.datesToGenerate(null, month, 30).toList())
        assertEquals(emptyList<LocalDate>(), RecurringProcessor.datesToGenerate(month, month, 30).toList())
    }
}
