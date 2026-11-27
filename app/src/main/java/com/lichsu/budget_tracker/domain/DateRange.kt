package com.lichsu.budget_tracker.domain.model

import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class DateRange(val start: LocalDate, val end: LocalDate)

enum class DatePreset {
    TODAY, YESTERDAY, LAST_7, LAST_30, LAST_90, THIS_MONTH, LAST_MONTH, THIS_YEAR;

    fun range(today: LocalDate): DateRange = when (this) {
        TODAY -> DateRange(today, today)
        YESTERDAY -> today.minusDays(1).let { DateRange(it, it) }
        LAST_7 -> DateRange(today.minusDays(6), today)
        LAST_30 -> DateRange(today.minusDays(29), today)
        LAST_90 -> DateRange(today.minusDays(89), today)
        THIS_MONTH -> DateRange(today.withDayOfMonth(1), today)
        LAST_MONTH -> {
            val first = today.withDayOfMonth(1).minusMonths(1)
            DateRange(first, first.with(TemporalAdjusters.lastDayOfMonth()))
        }
        THIS_YEAR -> DateRange(today.withDayOfYear(1), today)
    }
}
